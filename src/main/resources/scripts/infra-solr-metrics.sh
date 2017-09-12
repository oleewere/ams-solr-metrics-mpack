#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

sdir="`dirname \"$0\"`"
source $sdir/infra-solr-metrics-env.sh

JVM="java"

if [ -x $JAVA_HOME/bin/java ]; then
  JVM=$JAVA_HOME/bin/java
fi

if [ -z "$LOGFILE" ]; then
  LOGFILE=infra-solr-metrics.out
fi

nohup $JVM $INFRA_SOLR_METRICS_OPTS -cp "/etc/ambari-infra-solr-metrics/conf:$sdir:$sdir/libs/ambari-solr-metrics-sink-1.0.0.jar" org.springframework.boot.loader.JarLauncher ${1+"$@"} > $LOGFILE 2>&1 & &