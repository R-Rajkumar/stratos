/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.stratos.load.balancer.event.receivers;

import org.apache.stratos.load.balancer.common.domain.Cluster;
import org.apache.stratos.load.balancer.common.event.receivers.LoadBalancerCommonDomainMappingEventReceiver;
import org.apache.stratos.load.balancer.common.topology.TopologyProvider;
import org.apache.stratos.load.balancer.context.LoadBalancerContext;

/**
 * Load balancer domain mapping event receiver.
 */
public class LoadBalancerDomainMappingEventReceiver extends LoadBalancerCommonDomainMappingEventReceiver {

    public LoadBalancerDomainMappingEventReceiver(TopologyProvider topologyProvider) {
        super(topologyProvider);
    }

    @Override
    protected void addDomainMapping(Cluster cluster, String domainName, String contextPath) {
        super.addDomainMapping(cluster, domainName, contextPath);

        // Add domain mapping context path
        LoadBalancerContext.getInstance().addDomainMappingContextPath(domainName, contextPath);
    }

    @Override
    protected void removeDomainMapping(Cluster cluster, String domainName) {
        super.removeDomainMapping(cluster, domainName);

        // Remove domain mapping context path
        LoadBalancerContext.getInstance().removeDomainMappingContextPath(domainName);
    }
}
