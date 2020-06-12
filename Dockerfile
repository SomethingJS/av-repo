# Build container

FROM gradle:4.10.2-jdk11-slim AS build

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

# Run container

FROM openjdk:11-jre-slim AS runtime

WORKDIR /opt/av/

RUN adduser --disabled-password --gecos '' av; \
    chown av:av -R /opt/av; \
    chmod u+w /opt/av; \
    chmod 0755 -R /opt/av

USER av

COPY --from=build /home/gradle/src/av.jar /bin/

CMD ["java","-jar","/bin/av.jar","-env","--use-plugin-index"]
