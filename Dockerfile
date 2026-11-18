ARG DRUID_VERSION=38.0.0

# Replace the unpatched Temurin 21.0.10 JRE bundled inside upstream apache/druid:38.0.0
# to remediate CVE-2026-114938 ("Java4Shell" - OpenJDK Base64 AVX-512 VBMI heap buffer overflow,
# fixed in Java >= 21.0.13.1).
# Notes:
# 1. Upstream apache/druid:38.0.0 is published as a single-arch linux/amd64 Debian Distroless image,
#    so we pin --platform=linux/amd64 here to match its rootfs and glibc/shared libraries.
# 2. On Amazon Linux 2023 RPMs, lib/security/cacerts is a symlink to /etc/pki/java/cacerts (which
#    does not exist in Druid's Distroless image), so we replace that symlink with the actual CA bundle.
# 3. This JVM override can be removed once a future upstream apache/druid release ships Java >= 21.0.13.1.
FROM --platform=linux/amd64 amazoncorretto:21-al2023-headless AS java-runtime
RUN cp -a "$(readlink -f /usr/lib/jvm/java-21-amazon-corretto)" /opt/corretto-21 && \
    rm -f /opt/corretto-21/lib/security/cacerts && \
    cp -L /etc/pki/java/cacerts /opt/corretto-21/lib/security/cacerts

FROM apache/druid:${DRUID_VERSION}

# Swap out the vulnerable bundled JVM and verify the patched runtime starts cleanly
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
