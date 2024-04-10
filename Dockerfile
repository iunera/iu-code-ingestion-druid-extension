FROM maven:3-openjdk-18 as maven

COPY . .

RUN mvn clean package 

FROM apache/druid:29.0.1

WORKDIR /opt/druid

USER druid

WORKDIR /opt/druid/extensions/

COPY --from=maven target/*.tar.gz extension.tar.gz
RUN tar xvfz extension.tar.gz && rm extension.tar.gz

WORKDIR /opt/druid

