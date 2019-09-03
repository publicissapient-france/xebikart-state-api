FROM maven:3.6.0-jdk-11
LABEL project=xebikart
LABEL maintainer=xebikart-team-dashboard

WORKDIR /workspace

COPY pom.xml .
COPY src    ./src

RUN mvn --batch-mode -DfinalName=xebikart-api.jar package
RUN ls -l target/
RUN ls -l target/lib

FROM openjdk:11-jre-slim

COPY --from=0 /workspace/target/xebikart-1.0-SNAPSHOT.jar /project/xebikart-api.jar
COPY --from=0 /workspace/target/lib /project/lib

WORKDIR /project

EXPOSE 80

ENTRYPOINT ["java", "--illegal-access=deny", "-jar", "xebikart-api.jar"]
