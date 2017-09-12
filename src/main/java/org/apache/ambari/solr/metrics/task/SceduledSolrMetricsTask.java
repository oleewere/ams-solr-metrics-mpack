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
package org.apache.ambari.solr.metrics.task;

import org.apache.ambari.solr.metrics.metrics.SolrJmxDataCollector;
import org.apache.ambari.solr.metrics.metrics.SolrMetricsData;
import org.apache.ambari.solr.metrics.metrics.SolrMetricsSink;
import org.apache.hadoop.metrics2.sink.timeline.TimelineMetric;
import org.apache.hadoop.metrics2.sink.timeline.TimelineMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class SceduledSolrMetricsTask {

  private static final Logger logger = LogManager.getLogger(SceduledSolrMetricsTask.class);

  @Autowired
  private SolrMetricsSink solrMetricsSink;

  @Autowired
  private SolrJmxDataCollector solrJmxDataCollector;

  @Value("${infra.solr.metrics.node.app.name:solr-host-app}")
  private String nodeAppName;

  @Value("${infra.solr.metrics.core.app.name:solr-core-app}")
  private String coreAppName;

  @Value("${infra.solr.metrics.node.push.rate:20000}")
  private String waitMillisecondsPerNode;

  @Value("${infra.solr.metrics.core.push.rate:30000}")
  private String waitMillisecondsPerCore;

  @Scheduled(fixedDelayString = "${infra.solr.metrics.node.push.rate}")
  public void reportNodeMetrics() throws Exception {
    logger.info("Start gather and push Solr node metrics");
    if (solrJmxDataCollector != null) {
      List<SolrMetricsData> nodeMetricData = solrJmxDataCollector.collectNodeJmxData();
      long currMs = System.currentTimeMillis();
      TimelineMetrics timelineMetrics = new TimelineMetrics();
      for (SolrMetricsData solrMetricsData : nodeMetricData) {
        TimelineMetric timelineMetric = new TimelineMetric();
        timelineMetric.setAppId(nodeAppName);
        timelineMetric.setMetricName(solrMetricsData.getMetricsName());
        timelineMetric.setType(solrMetricsData.getType());
        timelineMetric.setStartTime(currMs);
        timelineMetric.setHostName(solrMetricsSink.getHostname());
        TreeMap<Long, Double> values = new TreeMap<>();
        values.put(currMs, solrMetricsData.getValue());
        timelineMetric.setMetricValues(values);
        timelineMetrics.addOrMergeTimelineMetric(timelineMetric);
      }
      solrMetricsSink.emitMetrics(timelineMetrics);
    } else {
      logger.info("JMX client has not initialized yet.");
    }
    logger.info("Wait {} milliseconds until next execution (Solr node)...", waitMillisecondsPerNode);
  }

  @Scheduled(fixedDelayString = "${infra.solr.metrics.core.push.rate}")
  public void reportCoreMetrics() throws Exception {
    logger.info("Start gather and push Solr core metrics");
    if (solrJmxDataCollector != null) {
      Map<String, List<SolrMetricsData>> nodeMetricData = solrJmxDataCollector.collectCoreJmxData();
      long currMs = System.currentTimeMillis();
      if (!nodeMetricData.isEmpty()) {
        for(Map.Entry<String, List<SolrMetricsData>> entry : nodeMetricData.entrySet()) {
          TimelineMetrics timelineMetrics = new TimelineMetrics();
          for (SolrMetricsData solrMetricsData : entry.getValue()) {
            TimelineMetric timelineMetric = new TimelineMetric();
            timelineMetric.setAppId(coreAppName);
            // TODO: use core name inside metrics name
            //String formattedCore = entry.getKey().replaceAll("solr/", "solr_cores.") + ".";
            //timelineMetric.setMetricName(formattedCore + solrMetricsData.getMetricsName());
            timelineMetric.setMetricName(solrMetricsData.getMetricsName());
            timelineMetric.setType(solrMetricsData.getType());
            timelineMetric.setStartTime(currMs);
            timelineMetric.setHostName(solrMetricsSink.getHostname());
            TreeMap<Long, Double> values = new TreeMap<>();
            values.put(currMs, solrMetricsData.getValue());
            timelineMetric.setMetricValues(values);
            timelineMetrics.addOrMergeTimelineMetric(timelineMetric);
          }
          solrMetricsSink.emitMetrics(timelineMetrics);
        }
      }
    } else {
      logger.info("JMX client has not initialized yet.");
    }
    logger.info("Wait {} milliseconds until next execution (Solr cores)...", waitMillisecondsPerCore);
  }

  @PostConstruct
  public void init() {
    solrMetricsSink.init();
  }

}
