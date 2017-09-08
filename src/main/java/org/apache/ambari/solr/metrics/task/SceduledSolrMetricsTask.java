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

import org.apache.ambari.solr.metrics.metrics.SolrMetricsSink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SceduledSolrMetricsTask {

  private static final Logger logger = LogManager.getLogger(SceduledSolrMetricsTask.class);

  @Autowired
  private SolrMetricsSink solrMetricsSink;

  @Value("${infra.solr.metrics.push.rate}")
  private String waitMilliseconds;

  @Scheduled(fixedDelayString = "${infra.solr.metrics.push.rate}")
  public void reportCurrentTime() {
    logger.info("Start gather and push Solr metrics");
    solrMetricsSink.emitMetrics(null);
    logger.info("Wait {} milliseconds until next execution...", waitMilliseconds);
  }

  @PostConstruct
  public void init() {
    solrMetricsSink.init();
  }



}
