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

from resource_management.core.resources.system import Execute, File, Directory
from resource_management.core.source import InlineTemplate
from resource_management.libraries.script.script import Script
from resource_management.libraries.functions.format import format
from resource_management.libraries.functions.check_process_status import check_process_status
from resource_management.libraries.functions.get_user_call_output import get_user_call_output
from resource_management.libraries.functions.show_logs import show_logs
from resource_management.libraries.resources.properties_file import PropertiesFile

class InfraSolrMetrics(Script):
  def install(self, env):
    import params
    env.set_params(params)
    Execute('yum remove -y -t ambari-infra-solr-metrics') # remove package first -> error tolerant
    Execute('curl -k -L -o /tmp/ams-solr-metrics-1.0.0.rpm ' + params.infra_solr_metrics_package_download_location)
    Execute('yum install -y /tmp/ams-solr-metrics-1.0.0.rpm')

  def configure(self, env):
    import params
    env.set_params(params)

    Directory([params.infra_solr_metrics_conf_dir, params.infra_solr_metrics_usr_dir, params.infra_solr_metrics_pid_dir,
               params.infra_solr_metrics_log_dir],
              mode=0755,
              cd_access='a',
              owner=params.infra_solr_user,
              group=params.user_group,
              create_parents=True,
              recursive_ownership=True
              )

    PropertiesFile(format("{infra_solr_metrics_conf_dir}/infra-solr-metrics.properties"),
                   properties=params.infra_solr_metrics_properties,
                   mode=0644,
                   owner=params.infra_solr_user,
                   group=params.user_group
                   )

    File(format("{infra_solr_metrics_conf_dir}/log4j2.xml"),
         content=InlineTemplate(params.infra_solr_metrics_log4j2_content),
         owner=params.infra_solr_user,
         group=params.user_group
         )

    File(format("{infra_solr_metrics_usr_dir}/infra-solr-metrics-env.sh"),
       content=InlineTemplate(params.infra_solr_metrics_env_content),
       mode=0755,
       owner=params.infra_solr_user,
       group=params.user_group
       )

  def start(self, env):
    import params
    env.set_params(params)
    self.configure(env)

    Execute(
      format('{infra_solr_metrics_usr_dir}/infra-solr-metrics.sh'),
      user=params.infra_solr_user
    )

  def stop(self, env, upgrade_type=None):
    import params
    env.set_params(params)
    self.kill_process(params.infra_solr_metrics_pidfile, params.infra_solr_user, params.infra_solr_metrics_log_dir)

  def status(self, env):
    import status_params
    env.set_params(status_params)

    check_process_status(status_params.infra_solr_metrics_pidfile)

  def kill_process(self, pid_file, user, log_dir):
    import params
    """
    Kill the process by pid file, then check the process is running or not. If the process is still running after the kill
    command, it will try to kill with -9 option (hard kill)
    """
    pid = get_user_call_output(format("cat {pid_file}"), user=user, is_checked_call=False)[1]
    process_id_exists_command = format("ls {pid_file} >/dev/null 2>&1 && ps -p {pid} >/dev/null 2>&1")

    kill_cmd = format("{sudo} kill {pid}")
    Execute(kill_cmd,
            not_if=format("! ({process_id_exists_command})"))
    wait_time = 5

    hard_kill_cmd = format("{sudo} kill -9 {pid}")
    Execute(hard_kill_cmd,
            not_if=format("! ({process_id_exists_command}) || ( sleep {wait_time} && ! ({process_id_exists_command}) )"),
            ignore_failures=True)
    try:
      Execute(format("! ({process_id_exists_command})"),
              tries=20,
              try_sleep=3,
              )
    except:
      show_logs(log_dir, user)
      raise

    File(pid_file,
         action="delete"
         )

if __name__ == "__main__":
  InfraSolrMetrics().execute()