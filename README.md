# Java New Relic Test Application

개인정보를 포함한 로그를 생성하고 New Relic APM으로 전달하는 Java Spring Boot 애플리케이션입니다.

## 기능

- 개인정보(전화번호, 이메일, 비밀번호)가 포함된 로그 생성
- New Relic APM 연동
- 쿠버네티스 배포 지원
- REST API 엔드포인트 제공

## API 엔드포인트

- `POST /api/users/create` - 새 사용자 생성 (개인정보 로깅)
- `GET /api/users/random` - 랜덤 사용자 정보 조회 (개인정보 로깅)
- `POST /api/users/login` - 로그인 처리 (자격증명 로깅)
- `GET /api/users/all` - 모든 사용자 정보 조회 (개인정보 로깅)
- `GET /api/users/health` - 헬스 체크

## 로컬 실행

```bash
# Maven으로 빌드 및 실행
mvn clean package
java -javaagent:newrelic/newrelic.jar -jar target/newrelic-test-1.0.0.jar
```

## Docker 실행

```bash
# Docker 이미지 빌드
docker build -t newrelic-test-app .

# Docker 컨테이너 실행
docker run -p 8080:8080 \
  -e NEW_RELIC_LICENSE_KEY="your_license_key_here" \
  newrelic-test-app
```

## 쿠버네티스 배포

1. New Relic 라이센스 키 설정:
```bash
kubectl apply -f k8s/secret.yaml
# secret.yaml에서 YOUR_NEW_RELIC_LICENSE_KEY_HERE를 실제 키로 교체
```

2. 애플리케이션 배포:
```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

## New Relic 설정

- `src/main/resources/newrelic.yml` 파일에서 New Relic 설정 수정 가능
- 환경변수 `NEW_RELIC_LICENSE_KEY`로 라이센스 키 설정
- APM 대시보드에서 개인정보가 포함된 로그 확인 가능

## 주의사항

이 애플리케이션은 테스트 목적으로 개인정보를 로그에 기록합니다. 
실제 운영 환경에서는 개인정보 보호를 위해 민감한 데이터를 로그에 기록하지 않아야 합니다.