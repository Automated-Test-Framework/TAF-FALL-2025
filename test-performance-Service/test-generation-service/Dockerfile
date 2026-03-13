FROM python:3.12-slim

WORKDIR /app

COPY test-generation-service/requirements.txt ./requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

COPY test-generation-service/app ./app

EXPOSE 8090

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8090"]