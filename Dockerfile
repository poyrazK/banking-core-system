FROM eclipse-temurin:17-jre
RUN groupadd -r cbs && useradd -r -g cbs cbs
USER cbs:cbs
COPY cbs-application/target/cbs-application-*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
