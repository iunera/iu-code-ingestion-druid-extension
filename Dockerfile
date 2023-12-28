FROM maven:3-openjdk-18 as maven

# copy the main repo 
COPY . .

RUN mvn clean package 

FROM apache/druid:28.0.0

WORKDIR /opt/druid

USER druid


# # ADD *.jar /opt/druid/extensions

#     ~  tar xvfz /Users/chris/git/azuredevops/public-transport-project/public-transport-project/iu-publictransport/druid-example-extension/target/druid-example-extension-27.0.0-bin.tar.gz

WORKDIR /opt/druid/extensions/

COPY --from=maven target/*.tar.gz extension.tar.gz
RUN tar xvfz extension.tar.gz && rm extension.tar.gz

WORKDIR /opt/druid

# ENTRYPOINT []
