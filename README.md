# iu-code-ingestion-druid-extension

This is a project for useful Druid extensions in the Fahrbar project. 
The project is not to be distributed or for commercial use. It is in the current state for evaluation only.



Build as Dockerfile 

export DRUID_VERSION=$(mvn -q \
  -Dexec.executable=echo \
  -Dexec.args='${druid.version}' \
  --non-recursive \
  exec:exec)


--build-arg="DRUID_VERSION=$DRUID_VERSION"
export DRUID_VERSION=29.0.1

docker build --no-cache --build-arg="DRUID_VERSION=$DRUID_VERSION" -t iunera/druid:$DRUID_VERSION .