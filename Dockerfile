ARG DRUID_VERSION

FROM apache/druid:${DRUID_VERSION}

USER druid
WORKDIR /opt/druid/extensions/

COPY target/*.tar.gz extension.tar.gz
RUN tar xvfz extension.tar.gz && rm extension.tar.gz

WORKDIR /opt/druid
