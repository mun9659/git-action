# base-image
FROM openjdk:17-jdk-alpine
# 변수설정 (빌드 파일의 경로)
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENV TZ=Asia/Seoul
ENTRYPOINT ["java", "-jar", "./app.jar"]