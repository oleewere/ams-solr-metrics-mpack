/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ambari.solr.metrics.metrics;

import org.apache.hadoop.metrics2.sink.timeline.AbstractTimelineMetricsSink;
import org.apache.hadoop.metrics2.sink.timeline.TimelineMetrics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class SolrMetricsSink extends AbstractTimelineMetricsSink {

  @Value("#{'${infra.solr.metrics.ams.collector.hosts:}'.split(',')}")
  private Collection<String> collectorHosts;

  @Value("${infra.solr.metrics.ams.collector.protocol:http}")
  private String protocol;

  @Value("${infra.solr.metrics.ams.collector.port:6188}")
  private int port;

  @Value("${infra.solr.metrics.ams.collector.path:/ws/v1/timeline/metrics}")
  private String collectorPath;

  @Value("${infra.solr.metrics.ams.hostname:localhost}")
  private String hostName;

  @Override
  public void init() {
    super.init();
  }

  @Override
  protected String getCollectorUri(String host) {
    return String.format("%s://%s:%s%s", protocol, host, getCollectorPort(), collectorPath);
  }

  @Override
  protected String getCollectorProtocol() {
    return protocol;
  }

  @Override
  protected String getCollectorPort() {
    return Integer.toString(port);
  }

  @Override
  protected int getTimeoutSeconds() {
    return 0;
  }

  @Override
  protected String getZookeeperQuorum() {
    return null;
  }

  @Override
  protected Collection<String> getConfiguredCollectorHosts() {
    return this.collectorHosts;
  }

  @Override
  protected String getHostname() {
    return this.hostName;
  }

  @Override
  public boolean emitMetrics(TimelineMetrics timelineMetric) {
    return super.emitMetrics(timelineMetric);
  }

}
