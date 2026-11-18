ARG DRUID_VERSION

FROM amazoncorretto:21-al2023-headless AS java-runtime
RUN cp -a "$(readlink -f /usr/lib/jvm/java-21-amazon-corretto)" /opt/corretto-21 && \
    rm -f /opt/corretto-21/lib/security/cacerts && \
    cp -L /etc/pki/java/cacerts /opt/corretto-21/lib/security/cacerts

FROM apache/druid:${DRUID_VERSION}

USER root
RUN rm -rf /usr/lib/jvm/*
COPY --from=java-runtime /opt/corretto-21 /usr/lib/jvm/corretto-21
RUN ln -sf /usr/lib/jvm/corretto-21/bin/java /usr/bin/java
ENV JAVA_HOME=/usr/lib/jvm/corretto-21
RUN java -version

USER druid
WORKDIR /opt/druid/extensions/

COPY --chown=druid:druid target/*.tar.gz extension.tar.gz
RUN tar xvfz extension.tar.gz && rm extension.tar.gz

WORKDIR /opt/druid
