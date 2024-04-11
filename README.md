# iu-code-ingestion-druid-extension

This is a project for useful Druid extensions in the Fahrbar project. 
The project is not to be distributed or for commercial use. It is in the current state for evaluation only.


## Usage in container

Deploy the `iunera/druid:29.0.1` image in your druid setting

Add `"iu-code-ingestion-druid-extension"` into the `druid.extensions.loadList` in the Druid Configuration

e.g.
```
druid.extensions.loadList=["iu-code-ingestion-druid-extension", "druid-histogram", "druid-datasketches", "druid-lookups-cached-global", "postgresql-metadata-storage", "druid-multi-stage-query"]
```

## Load Extension from Maven Central into druid

```
java \
  -cp "lib/*" \
  -Ddruid.extensions.directory="extensions" \
  -Ddruid.extensions.hadoopDependenciesDir="hadoop-dependencies" \
  org.apache.druid.cli.Main tools pull-deps \
  --no-default-hadoop \
  -c "com.iunera:iu-code-ingestion-druid-extension:29.0.1"
```

## Build Dockerimage from Scratch

```
export DRUID_VERSION=$(mvn -q \
  -Dexec.executable=echo \
  -Dexec.args='${druid.version}' \
  --non-recursive \
  exec:exec)

docker build --no-cache --build-arg="DRUID_VERSION=$DRUID_VERSION" -t iunera/druid:$DRUID_VERSION .
```
