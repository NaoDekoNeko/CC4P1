FROM python:3.11-slim
WORKDIR /app
COPY worker.py .
RUN pip install flask requests aiohttp
CMD ["python", "worker.py"]