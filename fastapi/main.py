import io
import os
from datetime import datetime
from pathlib import Path

import torch
from fastapi import FastAPI, File, HTTPException, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from PIL import Image

app = FastAPI()

_raw_origins = os.getenv("CORS_ALLOWED_ORIGINS", "http://localhost:3000")
ALLOWED_ORIGINS = [o.strip() for o in _raw_origins.split(",")]

app.add_middleware(
    CORSMiddleware,
    allow_origins=ALLOWED_ORIGINS,
    allow_methods=["*"],
    allow_headers=["*"],
)

# YOLOv5 커스텀 가중치 로드 (best.pt를 fastapi/ 폴더에 배치)
model = torch.hub.load(
    "ultralytics/yolov5:v7.0",  # master 브랜치는 ultralytics(YOLOv8) 의존성 추가됨 → v7.0 고정
    "custom",
    path="best.pt",
    force_reload=False,
    trust_repo=True,
)
model.conf = 0.5  # 신뢰도 임계값
model.eval()

# ── 디버그 설정 ────────────────────────────────────────────
# 환경변수 YOLO_DEBUG_SAVE_IMAGES=true 로 활성화
DEBUG_SAVE_IMAGES = os.getenv("YOLO_DEBUG_SAVE_IMAGES", "false").lower() == "true"
DEBUG_SAVE_DIR    = Path(os.getenv("YOLO_DEBUG_SAVE_DIR", "debug_images"))


def save_debug_image(image_bytes: bytes, prefix: str, content_type: str) -> None:
    """
    디버그 모드일 때 이미지를 로컬에 저장한다.

    저장 경로: debug_images/{prefix}_{timestamp}.{ext}
    예시: debug_images/fastapi_20260527_143052.jpg

    Spring에서 저장한 이미지(spring_*.jpg)와 비교해서
    파이프라인이 정상인지 육안으로 확인할 수 있다.
    """
    if not DEBUG_SAVE_IMAGES:
        return

    DEBUG_SAVE_DIR.mkdir(parents=True, exist_ok=True)

    ext_map = {"image/jpeg": "jpg", "image/png": "png", "image/webp": "webp"}
    ext = ext_map.get(content_type, "jpg")

    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S_%f")[:19]  # 밀리초 포함
    filename  = f"{prefix}_{timestamp}.{ext}"
    save_path = DEBUG_SAVE_DIR / filename

    save_path.write_bytes(image_bytes)
    size_kb = len(image_bytes) // 1024
    print(f"[YOLO DEBUG] 이미지 저장 완료 ─ {save_path.resolve()} ({size_kb}KB)")


@app.post("/detect-ingredients")
async def detect_ingredients(image: UploadFile = File(...)):
    if not image.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="이미지 파일만 업로드 가능합니다.")

    contents = await image.read()

    # ── 디버그: FastAPI가 수신한 이미지 저장 ───────────────
    # Spring이 저장한 spring_*.jpg 와 비교해서 전달 과정의 이상 유무를 확인
    save_debug_image(contents, prefix="fastapi", content_type=image.content_type)

    img = Image.open(io.BytesIO(contents)).convert("RGB")

    results = model(img)

    # 감지된 클래스명 목록 (중복 제거)
    detected = list({
        results.names[int(cls)]
        for cls in results.xyxy[0][:, 5].tolist()
    })

    return {"ingredients": detected}
