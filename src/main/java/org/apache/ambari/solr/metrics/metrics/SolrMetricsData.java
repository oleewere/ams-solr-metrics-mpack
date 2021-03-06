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

public class SolrMetricsData {
  private final String metricsName;
  private final Double value;
  private final String type;
  private final String core;
  private final boolean isPointInTime;

  public SolrMetricsData(String metricsName, Double value, boolean isPointInTime, String type, String core) {
    this.metricsName = metricsName;
    this.value = value;
    this.isPointInTime = isPointInTime;
    this.type = type;
    this.core = core;
  }

  public String getMetricsName() {
    return metricsName;
  }

  public boolean isPointInTime() {
    return isPointInTime;
  }

  public Double getValue() {
    return value;
  }

  public String getType() {
    return type;
  }

  public String getCore() {
    return core;
  }
}
