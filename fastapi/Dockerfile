FROM ultralytics/yolov5:latest-cpu

WORKDIR /app

RUN pip install --no-cache-dir fastapi uvicorn python-multipart

COPY main.py .

EXPOSE 8000
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
