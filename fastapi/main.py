import asyncio
import io
import json
import os
import re
import uuid
from datetime import datetime
from pathlib import Path

import functools

import torch
import yt_dlp

# PyTorch 2.6+: weights_only 기본값이 True로 변경됨
# hubconf.py 내부의 torch.load 호출에 weights_only=False를 강제 적용 (신뢰된 자체 모델 파일)
_original_torch_load = torch.load

@functools.wraps(_original_torch_load)
def _patched_torch_load(*args, **kwargs):
    kwargs.setdefault("weights_only", False)
    return _original_torch_load(*args, **kwargs)

torch.load = _patched_torch_load
from fastapi import BackgroundTasks, FastAPI, File, HTTPException, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from google import genai
from google.genai import types
from dotenv import load_dotenv
from PIL import Image
from pydantic import BaseModel

load_dotenv()

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

MAX_VIDEO_MINUTES = 10

# ── 비동기 작업 저장소 ─────────────────────────────────────
# {job_id: {"status": str, "result": dict|None, "error": str|None}}
jobs: dict = {}


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


# ── Gemini 레시피 분석 ─────────────────────────────────────

ALLOWED_CATEGORIES = "한식, 중식, 일식, 양식, 동남아식, 채식, 분식, 베이커리"
ALLOWED_TASTES = "매운맛, 단맛, 짠맛, 신맛, 고소한맛, 담백한맛, 쓴맛"
ALLOWED_INGREDIENTS = "떡, 어묵, 고추장, 설탕, 대파, 양배추, 계란, 김치, 돼지고기, 두부, 닭고기, 감자, 양파, 당근, 간장, 참기름, 마늘, 소금, 밀가루, 우유, 버터, 스파게티면, 베이컨, 크림, 김, 참치, 미역, 새우, 고추, 된장, 고춧가루, 국간장, 쌈장, 멸치, 다시마, 청양고추, 홍고추, 애호박, 호박, 콩나물, 숙주, 부추, 깻잎, 무, 배추, 시금치, 버섯, 표고버섯, 팽이버섯, 느타리버섯, 소고기, 차돌박이, 삼겹살, 목살, 갈비, 오징어, 문어, 조개, 바지락, 굴, 고등어, 연어, 올리브오일, 토마토소스, 토마토, 마요네즈, 케첩, 머스타드, 파슬리, 바질, 치즈, 파마산치즈, 모짜렐라치즈, 생크림, 화이트와인, 라면사리, 당면, 우동면, 칼국수면, 식빵, 밥, 떡국떡, 햄, 소시지, 어묵탕용 어묵"

PROMPT = f"""
당신은 제공된 유튜브 영상을 시청하고 레시피 정보를 추출하는 전문 요리사입니다.
영상의 시각 정보와 오디오 정보를 모두 활용하여 정확한 레시피를 생성하십시오.

### [명령어]
1. 입력된 유튜브 영상을 분석하여 레시피를 작성하십시오.
2. 재료명은 반드시 제공된 [허용 재료 목록]에서만 선택하십시오.
3. 영상에 나오지 않는 내용은 절대 지어내지 마십시오.
4. ingredients는 영상에 등장하는 모든 재료를 빠짐없이 포함하십시오.
5. steps는 영상의 모든 조리 단계를 순서대로 빠짐없이 포함하십시오.

### [허용 데이터 목록]
- 카테고리: {ALLOWED_CATEGORIES}
- 맛: {ALLOWED_TASTES}
- 허용 재료: {ALLOWED_INGREDIENTS}

### [JSON 추출 규칙 - DB 스키마 기반]
반드시 아래 구조의 순수 JSON만 반환하십시오:
{{
  "title": "요리 제목",
  "categoryName": "카테고리 (위 허용 목록 중 하나)",
  "tasteName": "맛 (위 허용 목록 중 하나)",
  "cookTimeMin": 조리시간(분, 정수),
  "description": "요리 간단 설명",
  "ingredients": [
    {{
      "name": "재료명 (허용 목록 중)",
      "amount": 수량(숫자),
      "unit": "단위(g/ml/개/큰술/작은술 등)",
      "isSubstitutable": true/false
    }},
    {{ "name": "재료2", "amount": 2, "unit": "큰술", "isSubstitutable": false }}
  ],
  "steps": [
    "1단계 조리 과정",
    "2단계 조리 과정",
    "3단계 조리 과정"
  ]
}}

※ ingredients와 steps는 반드시 2개 이상 포함하십시오. 단 하나만 반환하는 것은 오답입니다.
"""


def _normalize_youtube_url(url: str) -> str:
    """Gemini API 호환을 위해 YouTube URL에서 v= 이외의 파라미터 제거."""
    from urllib.parse import urlparse, parse_qs
    parsed = urlparse(url)
    host = parsed.netloc.lower()

    if "youtu.be" in host:
        video_id = parsed.path.strip("/")
        return f"https://youtu.be/{video_id}"
    if "/shorts/" in parsed.path:
        video_id = parsed.path.split("/shorts/", 1)[1].split("/")[0]
        return f"https://www.youtube.com/shorts/{video_id}"
    params = parse_qs(parsed.query)
    video_id = params.get("v", [None])[0]
    if video_id:
        return f"https://www.youtube.com/watch?v={video_id}"
    return url


def get_gemini_client() -> genai.Client:
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        raise ValueError("GEMINI_API_KEY가 .env에 없음")
    return genai.Client(api_key=api_key)


def _get_video_duration_seconds(url: str) -> int:
    """yt-dlp로 영상 길이(초)를 가져온다. 실패 시 0 반환."""
    ydl_opts = {"quiet": True, "no_warnings": True, "skip_download": True}
    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        info = ydl.extract_info(url, download=False)
        return info.get("duration", 0) or 0


async def _run_recipe_job(job_id: str, youtube_url: str) -> None:
    jobs[job_id]["status"] = "processing"
    normalized_url = _normalize_youtube_url(youtube_url)

    # 영상 길이 체크 (blocking I/O → executor로 오프로드)
    try:
        loop = asyncio.get_event_loop()
        duration = await loop.run_in_executor(None, _get_video_duration_seconds, normalized_url)
        if duration > MAX_VIDEO_MINUTES * 60:
            jobs[job_id]["status"] = "failed"
            jobs[job_id]["error"] = (
                f"영상이 너무 깁니다. {MAX_VIDEO_MINUTES}분 이하 영상만 분석 가능합니다. "
                f"(현재: {duration // 60}분 {duration % 60}초)"
            )
            return
    except Exception as e:
        # private 영상 등 길이 조회 불가 시 그냥 진행
        print(f"[analyze-recipe] 영상 길이 확인 실패 (계속 진행): {e}", flush=True)

    # Gemini 분석
    client = get_gemini_client()
    max_retries = 5
    retry_delay = 10

    for attempt in range(max_retries):
        try:
            response = client.models.generate_content(
                model="gemini-2.5-flash",
                contents=types.Content(
                    parts=[
                        types.Part(file_data=types.FileData(file_uri=normalized_url)),
                        types.Part(text=PROMPT),
                    ]
                ),
                config=types.GenerateContentConfig(
                    temperature=0.1,
                    response_mime_type="application/json",
                ),
            )

            if not response.text:
                raise ValueError("응답 텍스트가 비어있습니다.")

            raw = re.sub(r"^```json\s*|\s*```$", "", response.text.strip()).strip()
            jobs[job_id]["status"] = "completed"
            jobs[job_id]["result"] = json.loads(raw)
            return

        except Exception as e:
            error_msg = str(e)
            print(f"[analyze-recipe] attempt={attempt} error={error_msg}", flush=True)
            if ("503" in error_msg or "UNAVAILABLE" in error_msg) and attempt < max_retries - 1:
                wait = retry_delay * (2 ** attempt)  # 10s, 20s, 40s, 80s
                print(f"[analyze-recipe] {wait}초 후 재시도...", flush=True)
                await asyncio.sleep(wait)
                continue
            jobs[job_id]["status"] = "failed"
            jobs[job_id]["error"] = f"분석 실패: {error_msg}"
            return

    jobs[job_id]["status"] = "failed"
    jobs[job_id]["error"] = "최대 재시도 횟수 초과 (Gemini 서버 과부하)"


class YoutubeRequest(BaseModel):
    youtube_url: str


@app.post("/analyze-recipe")
async def analyze_recipe(req: YoutubeRequest, background_tasks: BackgroundTasks):
    job_id = str(uuid.uuid4())
    jobs[job_id] = {"status": "pending", "result": None, "error": None}
    background_tasks.add_task(_run_recipe_job, job_id, req.youtube_url)
    return {"job_id": job_id}


@app.get("/analyze-recipe/{job_id}")
async def get_recipe_status(job_id: str):
    if job_id not in jobs:
        raise HTTPException(status_code=404, detail="분석 작업을 찾을 수 없습니다.")
    return jobs[job_id]
