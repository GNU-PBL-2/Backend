FROM ultralytics/yolov5:v7.3.0-cpu

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 빌드 시점에 YOLOv5 hub 캐시 생성 (컨테이너 시작 시 GitHub 다운로드 방지)
RUN python3 -c "\
import torch; \
torch.hub.load('ultralytics/yolov5:v7.0', 'yolov5s', pretrained=False, force_reload=False, trust_repo=True) \
" 2>/dev/null || true

COPY main.py .

EXPOSE 8000
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
