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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SolrJmxDataCollector {

  private static final Logger logger = LogManager.getLogger(SolrJmxDataCollector.class);

  @Autowired
  private MBeanServerConnection mbsc;

  @Value("${infra.solr.metrics.filter.cache.class:org.apache.solr.search.FastLRUCache}")
  private String filterCacheClass;

  @Value("${infra.solr.metrics.per_seq_filter.cache.class:org.apache.solr.search.LRUCache}")
  private String perSeqFilterCacheClass;

  @Value("${infra.solr.metrics.query_result.cache.class:org.apache.solr.search.LRUCache}")
  private String queryResultCacheCacheClass;

  @Value("${infra.solr.metrics.document.cache.class:org.apache.solr.search.LRUCache}")
  private String documentCacheClass;

  @Value("${infra.solr.metrics.field_value.cache.class:org.apache.solr.search.FastLRUCache}")
  private String fieldValueCacheClass;

  public List<SolrMetricsData> collectNodeJmxData() throws Exception {
    List<SolrMetricsData> solrNodeMetricsList = new ArrayList<>();

    CompositeDataSupport heapMemoryUsage = (CompositeDataSupport) mbsc.getAttribute(new ObjectName("java.lang:type=Memory"), "HeapMemoryUsage");
    Long heapUsed = (Long) heapMemoryUsage.get("used");
    Long heapMax = (Long) heapMemoryUsage.get("max");

    Integer threadCount = (Integer) mbsc.getAttribute(new ObjectName("java.lang:type=Threading"), "ThreadCount");
    CompositeDataSupport nonHeapMemoryUsage = (CompositeDataSupport) mbsc.getAttribute(new ObjectName("java.lang:type=Memory"), "NonHeapMemoryUsage");
    Long nonHeapUsed = (Long) nonHeapMemoryUsage.get("used");
    Long nonHeapMax = (Long) nonHeapMemoryUsage.get("max");

    Long g1OldGenCollCount = (Long) getOrSkipJMXObjectAttribute("java.lang:name=G1 Old Generation,type=GarbageCollector", "CollectionCount");
    Long g1OldGenCollTime = (Long)getOrSkipJMXObjectAttribute("java.lang:name=G1 Old Generation,type=GarbageCollector", "CollectionTime");
    Long g1YoungGenCollCount = (Long) getOrSkipJMXObjectAttribute("java.lang:name=G1 Young Generation,type=GarbageCollector", "CollectionCount");
    Long g1YoungGenCollTime = (Long)getOrSkipJMXObjectAttribute("java.lang:name=G1 Young Generation,type=GarbageCollector", "CollectionTime");
    Long g1CMSCollCount = (Long) getOrSkipJMXObjectAttribute("java.lang:name=ConcurrentMarkSweep,type=GarbageCollector", "CollectionCount");
    Long g1CMSCollTime = (Long) getOrSkipJMXObjectAttribute("java.lang:name=ConcurrentMarkSweep,type=GarbageCollector", "CollectionTime");
    Long g1ParNewCollCount = (Long) getOrSkipJMXObjectAttribute("java.lang:name=ParNew,type=GarbageCollector", "CollectionCount");
    Long g1ParNewCollTime = (Long) getOrSkipJMXObjectAttribute("java.lang:name=ParNew,type=GarbageCollector", "CollectionTime");

    Double cpuProcessLoad = (Double) mbsc.getAttribute(new ObjectName("java.lang:type=OperatingSystem"), "ProcessCpuLoad");
    if (cpuProcessLoad == -1.0) {
      cpuProcessLoad = Double.NaN;
    } else {
      cpuProcessLoad = Double.parseDouble(String.format("%.3f", cpuProcessLoad));
    }

    addToMetricListIfNotNUll("solr.admin.info.gc.g1oldgen.count", g1OldGenCollCount, solrNodeMetricsList);
    addToMetricListIfNotNUll("solr.admin.info.gc.g1oldgen.time", g1OldGenCollTime, solrNodeMetricsList);
    addToMetricListIfNotNUll("solr.admin.info.gc.g1younggen.count", g1YoungGenCollCount, solrNodeMetricsList);
    addToMetricListIfNotNUll("solr.admin.info.gc.g1younggen.time", g1YoungGenCollTime, solrNodeMetricsList);
    addToMetricListIfNotNUll("solr.admin.info.gc.cms.count", g1CMSCollCount, solrNodeMetricsList);
    addToMetricListIfNotNUll("solr.admin.info.gc.cms.time", g1CMSCollTime, solrNodeMetricsList);
    addToMetricListIfNotNUll("solr.admin.info.gc.parnew.count", g1ParNewCollCount, solrNodeMetricsList);
    addToMetricListIfNotNUll("solr.admin.info.gc.parnew.time", g1ParNewCollTime, solrNodeMetricsList);

    solrNodeMetricsList.add(new SolrMetricsData("solr.admin.info.system.processCpuLoad", cpuProcessLoad,true, "Double", null));
    solrNodeMetricsList.add(new SolrMetricsData("solr.admin.info.jvm.memory.used", heapUsed.doubleValue(),true, "Long", null));
    solrNodeMetricsList.add(new SolrMetricsData("solr.admin.info.jvm.memory.max", heapMax.doubleValue(),true, "Long", null));
    solrNodeMetricsList.add(new SolrMetricsData("solr.admin.info.jvm.non-heap.used", nonHeapUsed.doubleValue(),true, "Long", null));
    solrNodeMetricsList.add(new SolrMetricsData("solr.admin.info.jvm.non-heap.max", nonHeapMax.doubleValue(),true, "Long", null));
    solrNodeMetricsList.add(new SolrMetricsData("solr.admin.info.jvm.thread.count", threadCount.doubleValue(),true, "Long", null));

    return solrNodeMetricsList;
  }

  public List<SolrMetricsData> collectAggregatedCoreJmxData() throws Exception {
    List<SolrMetricsData> solrCoreMetricsData = getSolrCoreMetricsData();
    final Map<String, SolrMetricsData> metricNameAndData = new HashMap<>();
    for (SolrMetricsData solrMetricsData : solrCoreMetricsData) {
      if (metricNameAndData.containsKey(solrMetricsData.getMetricsName())) {
        SolrMetricsData actualMetric = metricNameAndData.get(solrMetricsData.getMetricsName());
        SolrMetricsData newMetric = new SolrMetricsData(
          actualMetric.getMetricsName(),actualMetric.getValue() + solrMetricsData.getValue(),
          actualMetric.isPointInTime(), actualMetric.getType(), null);
        metricNameAndData.put(actualMetric.getMetricsName(), newMetric);

      } else {
        metricNameAndData.put(solrMetricsData.getMetricsName(),
          new SolrMetricsData(solrMetricsData.getMetricsName(), solrMetricsData.getValue(),
            solrMetricsData.isPointInTime(), solrMetricsData.getType(), null));
      }
    }
    final List<SolrMetricsData> result;
    if (metricNameAndData.isEmpty()) {
      result = new ArrayList<>();
    } else {
      result = new ArrayList<>(metricNameAndData.values());
    }
    return result;
  }

  public Map<String, List<SolrMetricsData>> collectCoreJmxData() throws Exception {
    final Map<String, List<SolrMetricsData>> metricsPerCore = new HashMap<>();
    final List<SolrMetricsData> solrCoreMetricsList = getSolrCoreMetricsData();
    for (SolrMetricsData solrMetricsData : solrCoreMetricsList) {
      if (metricsPerCore.containsKey(solrMetricsData.getCore())) {
        List<SolrMetricsData> existingList = metricsPerCore.get(solrMetricsData.getCore());
        existingList.add(solrMetricsData);
      } else {
        List<SolrMetricsData> newList = new ArrayList<>();
        newList.add(solrMetricsData);
        metricsPerCore.put(solrMetricsData.getCore(), newList);
      }
    }
    return metricsPerCore;
  }

  private List<SolrMetricsData> getSolrCoreMetricsData() throws Exception {
    String[] domains = mbsc.getDomains();
    String[] solrCores = filterCores(domains);
    List<SolrMetricsData> solrCoreMetricsList = new ArrayList<>();
    if (solrCores.length > 0) {
      for (String solrCore : solrCores) {
        ObjectName updateHandlerObjectName = new ObjectName(String.format("%s:type=updateHandler,id=org.apache.solr.update.DirectUpdateHandler2", solrCore));
        Long adds = (Long) mbsc.getAttribute(updateHandlerObjectName, "adds");
        Long deletesById = (Long) mbsc.getAttribute(updateHandlerObjectName, "deletesById");
        Long deletesByQuery = (Long) mbsc.getAttribute(updateHandlerObjectName, "deletesByQuery");
        Long docsPending = (Long) mbsc.getAttribute(updateHandlerObjectName, "docsPending");
        Long errors = (Long) mbsc.getAttribute(updateHandlerObjectName, "errors");
        Long transactionLogsTotalSize = (Long) mbsc.getAttribute(updateHandlerObjectName, "transaction_logs_total_size");
        Long transactionLogsTotalNumber = (Long) mbsc.getAttribute(updateHandlerObjectName, "transaction_logs_total_number");

        ObjectName searcherObjectName = new ObjectName(String.format("%s:type=searcher,id=org.apache.solr.search.SolrIndexSearcher", solrCore));
        Integer searcherNumDocs = (Integer) mbsc.getAttribute(searcherObjectName,"numDocs");
        Integer searcherMaxDoc = (Integer) mbsc.getAttribute(searcherObjectName,"maxDoc");
        Integer searcherDeletedDocs = (Integer) mbsc.getAttribute(searcherObjectName,"deletedDocs");
        Long searcherWarmupTime = (Long) mbsc.getAttribute(searcherObjectName,"warmupTime");

        solrCoreMetricsList.add(new SolrMetricsData("solr.admin.mbeans.searcher.numDocs", searcherNumDocs.doubleValue(), true, "Long", solrCore));
        solrCoreMetricsList.add(new SolrMetricsData("solr.admin.mbeans.searcher.maxDoc", searcherMaxDoc.doubleValue(), true, "Long", solrCore));
        solrCoreMetricsList.add(new SolrMetricsData("solr.admin.mbeans.searcher.deletedDocs", searcherDeletedDocs.doubleValue(), true, "Long", solrCore));
        solrCoreMetricsList.add(new SolrMetricsData("solr.admin.mbeans.searcher.warmupTime", searcherWarmupTime.doubleValue(), true, "Long", solrCore));

        addFilterClassMetrics(filterCacheClass, "filterCache", solrCore, solrCoreMetricsList, Long.class);
        addFilterClassMetrics(perSeqFilterCacheClass, "perSegFilter", solrCore, solrCoreMetricsList, Integer.class);
        addFilterClassMetrics(queryResultCacheCacheClass, "queryResultCache", solrCore, solrCoreMetricsList, Integer.class);
        addFilterClassMetrics(fieldValueCacheClass, "fieldValueCache", solrCore, solrCoreMetricsList, Long.class);
        addFilterClassMetrics(documentCacheClass, "documentCache", solrCore, solrCoreMetricsList, Long.class);

        addQueryMetrics("select", "org.apache.solr.handler.component.SearchHandler", "/select", solrCore, solrCoreMetricsList);
        addQueryMetrics("update", "org.apache.solr.handler.UpdateRequestHandler", "/update", solrCore, solrCoreMetricsList);
        addQueryMetrics("query", "org.apache.solr.handler.component.SearchHandler", "/query", solrCore, solrCoreMetricsList);
        addQueryMetrics("get", "org.apache.solr.handler.RealTimeGetHandler", "/get", solrCore, solrCoreMetricsList);
        addQueryMetrics("luke", "org.apache.solr.handler.admin.LukeRequestHandler", "/admin/luke", solrCore, solrCoreMetricsList);

        /* TODO: commits, autocommits, soft autocommits */
        solrCoreMetricsList.add(new SolrMetricsData("solr.admin.mbeans.updateHandler.adds", adds.doubleValue(), true, "Long", solrCore));
        solrCoreMetricsList.add(new SolrMetricsData("solr.admin.mbeans.updateHandler.deletesById", deletesById.doubleValue(), true, "Long", solrCore));
        solrCoreMetricsList.add(new SolrMetricsData("solr.admin.mbeans.updateHandler.deletesByQuery", deletesByQuery.doubleValue(), true, "Long", solrCore));
        solrCoreMetricsList.add(new SolrMetricsData("solr.admin.mbeans.updateHandler.docsPending", docsPending.doubleValue(), true, "Long", solrCore));
        solrCoreMetricsList.add(new SolrMetricsData("solr.admin.mbeans.updateHandler.errors", errors.doubleValue(), true, "Long", solrCore));
        // file in bytes
        solrCoreMetricsList.add(new SolrMetricsData("solr.admin.mbeans.updateHandler.transaction_logs_total_size",
          transactionLogsTotalSize.doubleValue(), true, "Long", solrCore));
        solrCoreMetricsList.add(new SolrMetricsData("solr.admin.mbeans.updateHandler.transaction_logs_total_number",
          transactionLogsTotalNumber.doubleValue(), true, "Long", solrCore));
      }
    }
    return solrCoreMetricsList;
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

  private void addToMetricListIfNotNUll(String metricName, Long data, List<SolrMetricsData> solrNodeMetricsList) {
    if (data != null) {
      solrNodeMetricsList.add(new SolrMetricsData(metricName, data.doubleValue(), true, "Long", null));
    }
  }

  private Object getOrSkipJMXObjectAttribute(String objectName, String attributeName) throws Exception {
    try {
      return mbsc.getAttribute(new ObjectName(objectName), attributeName);
    } catch (AttributeNotFoundException | InstanceNotFoundException e) {
      // skip
      logger.debug("Cannot load: {}", e.getMessage());
    }
    return null;
  }

  private <T extends Number> void addFilterClassMetrics(String filterClassName, String type, String solrCore, List<SolrMetricsData> solrCoreMetricsList, Class<T> clazz)
    throws Exception {
    try {
      ObjectName filterCacheObjectName = new ObjectName(String.format("%s:type=%s,id=%s", solrCore, type, filterClassName));
      Float cacheHitRatio = (Float) mbsc.getAttribute(filterCacheObjectName, "hitratio");
      T cacheSize = (T) mbsc.getAttribute(filterCacheObjectName, "size");
      Long cacheWarmupTime = (Long) mbsc.getAttribute(filterCacheObjectName, "warmupTime");

      solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.cache.%s.hitratio", type), cacheHitRatio.doubleValue(), true, "Double", solrCore));
      solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.cache.%s.size", type), cacheSize.doubleValue(), true, "Long", solrCore));
      solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.cache.%s.warmupTime", type), cacheWarmupTime.doubleValue(), true, "Long", solrCore));
    } catch (Exception e) {
      // skip
      logger.error("{} - {}", type, e.getMessage());
    }
  }

  private void addQueryMetrics(String queryName, String queryClass, String type, String solrCore, List<SolrMetricsData> solrCoreMetricsList)
  throws Exception {
    ObjectName queryHandlerObjectName = new ObjectName(String.format("%s:type=%s,id=%s", solrCore, type, queryClass));
    Long requests = (Long) mbsc.getAttribute(queryHandlerObjectName, "requests");
    Double avgRequestsPerSec = (Double) mbsc.getAttribute(queryHandlerObjectName, "avgRequestsPerSecond");
    Double avgTimePerRequest = (Double) mbsc.getAttribute(queryHandlerObjectName,"avgTimePerRequest");
    Double medianRequestTime = (Double) mbsc.getAttribute(queryHandlerObjectName,"medianRequestTime");

    solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.queryHandler.%s.requests", queryName), requests.doubleValue(), true, "Long", solrCore));
    solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.queryHandler.%s.avgRequestsPerSec", queryName), avgRequestsPerSec, true, "Double", solrCore));
    solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.queryHandler.%s.avgTimePerRequest", queryName), avgTimePerRequest, true, "Double", solrCore));
    solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.queryHandler.%s.medianRequestTime", queryName), medianRequestTime, true, "Double", solrCore));
  }
}
