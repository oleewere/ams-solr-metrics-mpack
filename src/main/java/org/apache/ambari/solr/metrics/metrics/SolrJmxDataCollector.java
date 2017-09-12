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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SolrJmxDataCollector {

  @Autowired
  private MBeanServerConnection mbsc;

  public List<SolrMetricsData> collectNodeJmxData() throws Exception {
    List<SolrMetricsData> solrNodeMetricsList = new ArrayList<>();

    CompositeDataSupport heapMemoryUsage = (CompositeDataSupport) mbsc.getAttribute(new ObjectName("java.lang:type=Memory"), "HeapMemoryUsage");
    Long heapUsed = (Long) heapMemoryUsage.get("used");
    Double cpuProcessLoad = (Double) mbsc.getAttribute(new ObjectName("java.lang:type=OperatingSystem"), "ProcessCpuLoad");

    if (cpuProcessLoad == -1.0) {
      cpuProcessLoad = Double.NaN;
    } else {
      cpuProcessLoad = Double.parseDouble(String.format("%.3f", cpuProcessLoad));
    }
    CompositeDataSupport nonHeapMemoryUsage = (CompositeDataSupport) mbsc.getAttribute(new ObjectName("java.lang:type=Memory"), "NonHeapMemoryUsage");
    // TODO: add outher metrics as well
    solrNodeMetricsList.add(new SolrMetricsData("solr.admin.info.system.processCpuLoad", cpuProcessLoad,true, "Double"));
    solrNodeMetricsList.add(new SolrMetricsData("solr.admin.info.jvm.memory.used", heapUsed.doubleValue(),true, "Long"));

    return solrNodeMetricsList;
  }

  public Map<String, List<SolrMetricsData>> collectCoreJmxData() throws Exception {
    Map<String, List<SolrMetricsData>> metricsPerCore = new HashMap<>();
    String[] domains = mbsc.getDomains();
    String[] solrCores = filterCores(domains);
    if (solrCores.length > 0) {
      for (String solrCore : solrCores) {
        List<SolrMetricsData> solrCoreMetricsList = new ArrayList<>();
        ObjectName updateHandlerObjectName = new ObjectName(String.format("%s:type=updateHandler,id=org.apache.solr.update.DirectUpdateHandler2", solrCore));
        Long adds = (Long) mbsc.getAttribute(updateHandlerObjectName, "adds");
        Long deletesById = (Long) mbsc.getAttribute(updateHandlerObjectName, "deletesById");
        Long deletesByQuery = (Long) mbsc.getAttribute(updateHandlerObjectName, "deletesByQuery");
        Long docsPending = (Long) mbsc.getAttribute(updateHandlerObjectName, "docsPending");
        Long errors = (Long) mbsc.getAttribute(updateHandlerObjectName, "errors");
        Long transactionLogsTotalSize = (Long) mbsc.getAttribute(updateHandlerObjectName, "transaction_logs_total_size");
        Long transactionLogsTotalNumber = (Long) mbsc.getAttribute(updateHandlerObjectName, "transaction_logs_total_number");

        /* TODO: commits, autocommits, soft autocommits */
        solrCoreMetricsList.add(new SolrMetricsData("solr.admin.mbeans.updateHandler.adds", adds.doubleValue(), true, "Long"));
        solrCoreMetricsList.add(new SolrMetricsData("solr.admin.mbeans.updateHandler.deletesById", deletesById.doubleValue(), true, "Long"));
        solrCoreMetricsList.add(new SolrMetricsData("solr.admin.mbeans.updateHandler.deletesByQuery", deletesByQuery.doubleValue(), true, "Long"));
        solrCoreMetricsList.add(new SolrMetricsData("solr.admin.mbeans.updateHandler.docsPending", docsPending.doubleValue(), true, "Long"));
        solrCoreMetricsList.add(new SolrMetricsData("solr.admin.mbeans.updateHandler.errors", errors.doubleValue(), true, "Long"));
        // file in bytes
        solrCoreMetricsList.add(new SolrMetricsData("solr.admin.mbeans.updateHandler.transaction_logs_total_size", transactionLogsTotalSize.doubleValue(), true, "Long"));
        solrCoreMetricsList.add(new SolrMetricsData("solr.admin.mbeans.updateHandler.transaction_logs_total_number", transactionLogsTotalNumber.doubleValue(), true, "Long"));
        metricsPerCore.put(solrCore, solrCoreMetricsList);
      }
    }

    return metricsPerCore;
  }

  private String[] filterCores(String[] domains) {
    List<String> list = new ArrayList<>();
    if (domains != null && domains.length > 0) {
      for (String domain : domains) {
        if (domain.startsWith("solr/")) {
          list.add(domain);
        }
      }
    }
    return list.toArray(new String[0]);
  }
}
