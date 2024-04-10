ARG DRUID_VERSION

FROM maven:3-openjdk-17 as maven

COPY . .

RUN mvn clean package 

FROM apache/druid:${DRUID_VERSION}

WORKDIR /opt/druid

USER druid

WORKDIR /opt/druid/extensions/

COPY --from=maven target/*.tar.gz extension.tar.gz
RUN tar xvfz extension.tar.gz && rm extension.tar.gz

WORKDIR /opt/druid

