# iu-code-ingestion-druid-extension

This project is an example how to add custom pre-ingestion parsers for Apache Druid.
It shows how simple scripts can be easily added in ingestion specs based on the example of simple Python 2.0 scripts that get compiled.
We hope the use of Python in Apache Druid ingestions will help to connect the Data Science community with the Big Data Community


## Usage in container

Deploy the `iunera/druid:30.0.0` image in your druid setting

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
  -c "com.iunera:iu-code-ingestion-druid-extension:30.0.0"
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
