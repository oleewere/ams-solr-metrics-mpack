# AMS Solr Sink for Ambari Infra Solr

## Generate the mpack from source code
Download ams-solr-metrics-mpack repository from git then run:
```bash
cd ams-solr-metrics-mpack
./gradlew clean buildTar
```
Or optionally you can download the actual mpack from here:
```bash
curl -k -O -L https://github.com/oleewere/ams-solr-metrics-mpack/releases/download/1.0.0/ams-solr-metrics-mpack-0.1.0.tar.gz
```

## Install AMS Solr Sink mpack:

Stop Ambari Server:
```bash
ambari-server stop
```

Install Solr mpack:
```bash
ambari-server install-mpack --mpack=/my-path/ams-solr-metrics-mpack-0.1.0.tar.gz --verbose
```

Start Ambari Server
```bash
ambari-server start
```

## Build AMS Solr Sink RPM/DEB package
```bash
cd ams-solr-metrics-mpack
# rpm build:
# note: generated rpm file: build/distributions/ambari-infra-solr-metrics*.rpm,
./gradlew clean rpm
# deb build:
# note: generated deb file: build/distributions/mbari-infra-solr-metrics*.deb,
./gradlew clean deb

# Example of install:
yum install -y ambari-infra-solr-metrics-1.0.0-1.noarch.rpm
```
