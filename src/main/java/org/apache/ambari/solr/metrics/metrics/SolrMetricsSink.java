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

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.hadoop.metrics2.sink.timeline.AbstractTimelineMetricsSink;
import org.apache.hadoop.metrics2.sink.timeline.TimelineMetrics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

@Component
public class SolrMetricsSink extends AbstractTimelineMetricsSink {

  private static final Logger logger = LogManager.getLogger(SolrMetricsSink.class);

  @Value("#{'${infra.solr.metrics.ams.collector.hosts:}'.split(',')}")
  private Collection<String> collectorHosts;

  @Value("${infra.solr.metrics.ams.collector.protocol:http}")
  private String protocol;

  @Value("${infra.solr.metrics.ams.collector.port:6188}")
  private int port;

  @Value("${infra.solr.metrics.ams.hostname:}")
  private String hostName;

  @Value("${infra.solr.metrics.ams.ssl.keystore.path:}")
  private String sslKeystorePath;

  @Value("${infra.solr.metrics.ams.ssl.keystore.type:}")
  private String sslKeystoreType;

  @Value("${infra.solr.metrics.ams.ssl.keystore.password:}")
  private String sslKeystorePassword;

  @Override
  public void init() {
    if (StringUtils.isEmpty(hostName)) {
      try {
        hostName = InetAddress.getLocalHost().getHostName();
        //If not FQDN , call  DNS
        if ((hostName == null) || (!hostName.contains("."))) {
          hostName = InetAddress.getLocalHost().getCanonicalHostName();
        }
      } catch (UnknownHostException e) {
        logger.error("Could not identify hostname.");
        throw new RuntimeException("Could not identify hostname.", e);
      }
    }
    super.init();
    if (collectorHosts.isEmpty()) {
      logger.error("No Metric collector configured.");
    } else {
      if (protocol.contains("https")) {
        loadTruststore(sslKeystorePath, sslKeystoreType, sslKeystorePassword);
      }
    }
  }

  @Override
  protected String getCollectorUri(String host) {
    return constructTimelineMetricUri(this.protocol, host, getCollectorPort());
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
    return collectorHosts;
  }

  @Override
  public String getHostname() {
    return this.hostName;
  }

  @Override
  public boolean emitMetrics(TimelineMetrics timelineMetric) {
    return super.emitMetrics(timelineMetric);
  }

}
