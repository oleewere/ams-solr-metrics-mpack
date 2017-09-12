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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jmx.support.MBeanServerConnectionFactoryBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.net.MalformedURLException;

@Configuration
public class SolrJmxConnectionConfig {

  private static final Logger logger = LogManager.getLogger(SolrJmxConnectionConfig.class);

  @Value("${infra.solr.jmx.url:}")
  private String serviceUrl;

  @Value("${infra.solr.metrics.thread.pool.size:2}")
  private Integer taskSchedulerThreadPoolSize;

  @Bean
  public MBeanServerConnectionFactoryBean clientConnector() throws MalformedURLException {
    if (StringUtils.isNotEmpty(serviceUrl)) {
      MBeanServerConnectionFactoryBean mBeanServerConnectionFactoryBean = new MBeanServerConnectionFactoryBean();
      mBeanServerConnectionFactoryBean.setServiceUrl(serviceUrl);
      mBeanServerConnectionFactoryBean.setConnectOnStartup(false);
      return mBeanServerConnectionFactoryBean;
    } else {
      logger.warn("JMX service url for Solr is missing. JMX export to AMS won't work.");
      return null;
    }
  }

  @Bean
  public  ThreadPoolTaskScheduler  taskScheduler(){
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setPoolSize(taskSchedulerThreadPoolSize);
    taskScheduler.setThreadNamePrefix("infra-solr-metrics-shipper-");
    return taskScheduler;
  }

}
