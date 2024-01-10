FROM openjdk:8-jdk-alpine as build
WORKDIR /workspace/app

ENV frontend.url=http\://localhost

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline


COPY src/main src/main
COPY templates templates
COPY es_settings.json .

RUN ./mvnw install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM openjdk:8-jdk-alpine as run
VOLUME /tmp
# RUN true, see
# https://github.com/moby/moby/issues/37965#issuecomment-488155862
ARG DEPENDENCY=/workspace/app/target/dependency
RUN apk --no-cache add curl
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
RUN true
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
RUN true
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
RUN true
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
RUN true
COPY --from=build /workspace/app/es_settings.json /
RUN true
COPY --from=build /workspace/app/templates /templates
RUN true
ENTRYPOINT ["java","-cp","app:app/lib/*","de.teamA.SWT.SwtApplication"]
