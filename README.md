# AMS Solr Sink for Ambari Infra Solr

## Generate the mpack from source code
Download ams-solr-metrics-mpack repository from git then run:
```bash
cd ams-solr-metrics-mpack
./gradlew clean buildTar
```
Or optionally you can download the actual mpack from here:
```bash
curl -k -O -L https://github.com/oleewere/ams-solr-metrics-mpack/releases/download/1.0.0/ams-solr-metrics-mpack-1.0.0.tar.gz
```

## Install AMS Solr Sink mpack:

Stop Ambari Server:
```bash
ambari-server stop
```

Install Solr mpack:
```bash
ambari-server install-mpack --mpack=/my-path/ams-solr-metrics-mpack-1.0.0.tar.gz --verbose
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


## Using with Ambari Infra service (Solr)

This external metrics shipper application uses JMX/RMI to gather the details of Solr instances (the local one, so the sinks are need to be installed on those hosts where Solr instances are located). 
By default Infra Solr JMX is enabled and uses 18886 as JMX port. (ambari configuration properties: `infra-solr-env/infra_solr_jmx_port` and `ENABLE_REMOTE_JMX_OPTS="true"` in `infra-solr-env/content`). To make this 2 component to work, there is one more thing needed: option 1 is to add `SOLR_OPTS="$SOLR_OPTS -Djava.rmi.server.hostname=localhost"` to `infra-solr-env/content` (recommended) or change `infra-solr-metrics-env/infra_solr_local_hostname` value to `{hostname}` (that gets the hostname value from the ambari-agent command.json)

## Import dashboards to grafana:

https://github.com/oleewere/ams-solr-metrics-mpack/releases/download/1.0.0/grafana-infra-solr-cores-aggregated.json
https://github.com/oleewere/ams-solr-metrics-mpack/releases/download/1.0.0/grafana-infra-solr-hosts.json



