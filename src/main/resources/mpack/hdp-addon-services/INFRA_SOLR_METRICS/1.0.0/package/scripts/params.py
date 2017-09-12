#!/usr/bin/env python

"""
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

"""
from ambari_commons.constants import AMBARI_SUDO_BINARY
from resource_management.libraries.functions.default import default
from resource_management.libraries.functions.format import format
from resource_management.libraries.functions.is_empty import is_empty
from resource_management.libraries.script.script import Script
import status_params

def get_port_from_url(address):
  if not is_empty(address):
    return address.split(':')[-1]
  else:
    return address

config = Script.get_config()
tmp_dir = Script.get_tmp_dir()

stack_version = default("/commandParams/version", None)
sudo = AMBARI_SUDO_BINARY
security_enabled = status_params.security_enabled

java64_home = config['hostLevelParams']['java_home']
java_exec = format("{java64_home}/bin/java")

user_group = config['configurations']['cluster-env']['user_group']
infra_solr_user = config['configurations']['infra-solr-env']['infra_solr_user']
infra_solr_jmx_port = default('configurations/infra-solr-env/infra_solr_jmx_port', '18886')
infra_solr_local_hostname = default('configurations/infra-solr-metrics-env/infra_solr_local_hostname', config['hostname'].lower())

infra_solr_metrics_conf_dir = "/etc/ambari-infra-solr-metrics/conf"
infra_solr_metrics_usr_dir = "/usr/lib/ambari-infra-solr-metrics"
infra_solr_metrics_log_dir = default('configurations/infra-solr-metrics-env/infra_solr_metrics_log_dir', '/var/log/ambari-infra-solr-metrics')
infra_solr_metrics_pid_dir = status_params.infra_solr_metrics_piddir
infra_solr_metrics_pidfile = status_params.infra_solr_metrics_pidfile

if security_enabled:
  _hostname_lowercase = config['hostname'].lower()
  infra_solr_jaas_file = '/etc/ambari-infra-solr/conf/infra_solr_jaas.conf'

metrics_http_policy = config['configurations']['ams-site']['timeline.metrics.service.http.policy']
metrics_collector_protocol = 'http'
if metrics_http_policy == 'HTTPS_ONLY':
  metrics_collector_protocol = 'https'

metrics_collector_hosts = ",".join(config['clusterHostInfo']['metrics_collector_hosts'])
metrics_collector_port = str(get_port_from_url(config['configurations']['ams-site']['timeline.metrics.service.webapp.address']))

infra_solr_metrics_properties = {}
infra_solr_metrics_properties = dict(infra_solr_metrics_properties.items() + dict(config['configurations']['infra-solr-metrics-properties']).items())
infra_solr_metrics_properties['infra.solr.jmx.url'] = format(infra_solr_metrics_properties['infra.solr.jmx.url'])
infra_solr_metrics_properties['infra.solr.metrics.ams.collector.hosts'] = format(infra_solr_metrics_properties['infra.solr.metrics.ams.collector.hosts'])
infra_solr_metrics_properties['infra.solr.metrics.ams.collector.port'] = format(infra_solr_metrics_properties['infra.solr.metrics.ams.collector.port'])
infra_solr_metrics_properties['infra.solr.metrics.ams.collector.protocol'] = format(infra_solr_metrics_properties['infra.solr.metrics.ams.collector.protocol'])

infra_solr_metrics_env_content = config['configurations']['infra-solr-metrics-env']['content']
infra_solr_metrics_log4j2_content = config['configurations']['infra-solr-metrics-log4j2']['content']

infra_solr_metrics_package_download_location = config['configuration']['infra-solr-metrics-env']['download_location']