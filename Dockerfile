FROM openjdk:17-jdk-slim

WORKDIR /app

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY pom.xml .
COPY src ./src

RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests && \
    apt-get remove -y maven && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/*

RUN curl -O https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip && \
    unzip newrelic-java.zip && \
    rm newrelic-java.zip

COPY src/main/resources/newrelic.yml ./newrelic/

EXPOSE 8080

ENV NEW_RELIC_LICENSE_KEY=""
ENV NEW_RELIC_APP_NAME="Java NewRelic Test Application"
ENV JAVA_OPTS="-javaagent:/app/newrelic/newrelic.jar"

CMD ["sh", "-c", "java $JAVA_OPTS -jar target/newrelic-test-1.0.0.jar"]