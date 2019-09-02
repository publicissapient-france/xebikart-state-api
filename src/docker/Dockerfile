FROM openjdk:11-jre-slim

ADD xebikart-api.jar /project/xebikart-api.jar
ADD lib /project/lib

WORKDIR /project

EXPOSE 8080

ENTRYPOINT ["java", "--illegal-access=deny", "-jar", "xebikart-api.jar"]
