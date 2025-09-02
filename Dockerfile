# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# 의존성 캐시
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# 소스 빌드
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# New Relic 에이전트 다운로드
RUN apt-get update && apt-get install -y curl unzip && rm -rf /var/lib/apt/lists/* && \
    curl -fsSLO https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip && \
    unzip -q newrelic-java.zip && rm newrelic-java.zip

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# 보건 체크용 curl
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 앱 JAR 복사 (JAR 이름이 다르면 아래 파일명 수정)
COPY --from=builder /build/target/newrelic-test-1.0.0.jar /app/app.jar

# New Relic 에이전트 & (선택) 커스텀 설정 복사
COPY --from=builder /build/newrelic /app/newrelic/
# 프로젝트에 커스텀 yml이 있으면 덮어쓰기
COPY src/main/resources/newrelic.yml /app/newrelic/

# 비루트 사용자
RUN groupadd -r appuser && useradd -r -g appuser appuser && \
    chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

# 라이선스 키를 이미지에 bake-in (원하신 대로)
ENV NEW_RELIC_LICENSE_KEY="bb7a4dc5565cfa8d7771216bcebd99b8FFFFNRAL"
ENV NEW_RELIC_APP_NAME="Java NewRelic Test Application"
ENV JAVA_OPTS="-javaagent:/app/newrelic/newrelic.jar -Dnewrelic.config.app_name=${NEW_RELIC_APP_NAME} -Dnewrelic.config.license_key=${NEW_RELIC_LICENSE_KEY} -Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom"

# 헬스체크 (Actuator 포함 전제)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

CMD ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
