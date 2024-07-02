FROM python:3.11-slim
WORKDIR /app
COPY client.py .
COPY templates /app/templates
RUN pip install quart aiohttp requests
CMD ["python", "client.py"]
