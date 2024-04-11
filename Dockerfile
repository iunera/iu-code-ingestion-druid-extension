ARG DRUID_VERSION

FROM maven:3-openjdk-17 as build
COPY . .
RUN mvn clean package 

FROM apache/druid:${DRUID_VERSION}
USER druid
WORKDIR /opt/druid/extensions/

COPY --from=build target/*.tar.gz extension.tar.gz
RUN tar xvfz extension.tar.gz && rm extension.tar.gz

WORKDIR /opt/druid
