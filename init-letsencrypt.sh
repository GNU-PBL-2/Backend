#!/bin/bash
set -e

if [ -z "$DOMAIN" ] || [ -z "$EMAIL" ]; then
  echo "ERROR: DOMAIN, EMAIL 환경변수가 필요합니다."
  exit 1
fi

CERT_PATH="./certbot/conf/live/$DOMAIN"

if [ -d "$CERT_PATH" ]; then
  echo "이미 인증서가 존재합니다: $CERT_PATH"
  exit 0
fi

echo "==== 임시 자체 서명 인증서 생성 ===="
mkdir -p "$CERT_PATH"
openssl req -x509 -nodes -newkey rsa:2048 -days 1 \
  -keyout "$CERT_PATH/privkey.pem" \
  -out "$CERT_PATH/fullchain.pem" \
  -subj "/CN=$DOMAIN"

echo "==== Nginx 시작 ===="
docker compose -f docker-compose.prod.yml up -d nginx

echo "==== Let's Encrypt 인증서 발급 ===="
rm -rf "$CERT_PATH"
docker compose -f docker-compose.prod.yml run --rm certbot certonly \
  --webroot --webroot-path=/var/www/certbot \
  --email "$EMAIL" \
  --agree-tos --no-eff-email \
  -d "$DOMAIN"

echo "==== Nginx 재시작 ===="
docker compose -f docker-compose.prod.yml exec nginx nginx -s reload

echo "==== 완료: https://$DOMAIN ===="
