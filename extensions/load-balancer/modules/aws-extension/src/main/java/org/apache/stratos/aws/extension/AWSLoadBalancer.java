/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.stratos.aws.extension;

import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.load.balancer.common.domain.Cluster;
import org.apache.stratos.load.balancer.common.domain.Member;
import org.apache.stratos.load.balancer.common.domain.Service;
import org.apache.stratos.load.balancer.common.domain.Topology;
import org.apache.stratos.load.balancer.extension.api.LoadBalancer;
import org.apache.stratos.load.balancer.extension.api.exception.LoadBalancerExtensionException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AWSLoadBalancer implements LoadBalancer {

	private static final Log log = LogFactory.getLog(AWSLoadBalancer.class);

	// A map <clusterId, load balancer info> to store load balancer information
	// against the cluster id
	private static ConcurrentHashMap<String, LoadBalancerInfo> clusterIdToLoadBalancerMap = new ConcurrentHashMap<String, LoadBalancerInfo>();

	// Object used to invoke methods related to AWS API
	private AWSHelper awsHelper;

	public AWSLoadBalancer() throws LoadBalancerExtensionException {
		awsHelper = new AWSHelper();
	}

	/*
	 * configure method iterates over topology and configures the AWS load
	 * balancers needed. Configuration may involve creating a new load balancer
	 * for a cluster, updating existing load balancers or deleting unwanted load
	 * balancers.
	 */
	public boolean configure(Topology topology)
			throws LoadBalancerExtensionException {

		log.info("AWS load balancer extension is being reconfigured.");

		HashSet<String> activeClusters = new HashSet<String>();

		for (Service service : topology.getServices()) {
			for (Cluster cluster : service.getClusters()) {
				// Check if a load balancer is created for this cluster
				if (clusterIdToLoadBalancerMap.containsKey(cluster
						.getClusterId())) {
					// A load balancer is already present for this cluster
					// Get the load balancer and update it.

					if (log.isDebugEnabled()) {
						log.debug("Load balancer for cluster "
								+ cluster.getClusterId()
								+ " is already present.");
					}

					LoadBalancerInfo loadBalancerInfo = clusterIdToLoadBalancerMap
							.get(cluster.getClusterId());

					String loadBalancerName = loadBalancerInfo.getName();
					String region = loadBalancerInfo.getRegion();

					// Get all the instances attached
					// Attach newly added instances to load balancer

					// attachedInstances list is useful in finding out what
					// all new instances which
					// should be attached to this load balancer.
					List<Instance> attachedInstances = awsHelper
							.getAttachedInstances(loadBalancerName, region);

					// clusterMembers stores all the members of a cluster.
					Collection<Member> clusterMembers = cluster.getMembers();

					if (clusterMembers.size() > 0) {
						activeClusters.add(cluster.getClusterId());

						List<Instance> instancesToAddToLoadBalancer = new ArrayList<Instance>();

						for (Member member : clusterMembers) {
							// if instance id of member is not in
							// attachedInstances
							// add this to instancesToAddToLoadBalancer
							Instance instance = new Instance(
									awsHelper.getAWSInstanceName(member
											.getInstanceId()));

							if (attachedInstances == null
									|| !attachedInstances.contains(instance)) {
								instancesToAddToLoadBalancer.add(instance);

								if (log.isDebugEnabled()) {
									log.debug("Instance "
											+ awsHelper
													.getAWSInstanceName(member
															.getInstanceId())
											+ " needs to be registered to load balancer "
											+ loadBalancerName);
								}

							}
						}

						if (instancesToAddToLoadBalancer.size() > 0)
							awsHelper.registerInstancesToLoadBalancer(
									loadBalancerName,
									instancesToAddToLoadBalancer, region);
					}

				} else {
					// Create a new load balancer for this cluster
					Collection<Member> clusterMembers = cluster.getMembers();

					if (clusterMembers.size() > 0) {
						// a unique load balancer name with user-defined
						// prefix and a sequence number.
						String loadBalancerName = awsHelper
								.generateLoadBalancerName();

						String region = awsHelper.getAWSRegion(clusterMembers
								.iterator().next().getInstanceId());

						// list of AWS listeners obtained using port
						// mappings of one of the members of the cluster.
						List<Listener> listenersForThisCluster = awsHelper
								.getRequiredListeners(clusterMembers.iterator()
										.next());

						// DNS name of load balancer which was created.
						// This is used in the domain mapping of this
						// cluster.
						String loadBalancerDNSName = awsHelper
								.createLoadBalancer(loadBalancerName,
										listenersForThisCluster, region);

						// Add the inbound rule the security group of the load
						// balancer
						// For each listener, add a new rule with load
						// balancer port as allowed protocol in the security
						// group.
						for (Listener listener : listenersForThisCluster) {
							int port = listener.getLoadBalancerPort();

							for (String protocol : awsHelper
									.getAllowedProtocolsForLBSecurityGroup()) {
								awsHelper
										.addInboundRuleToSecurityGroup(
												awsHelper.getSecurityGroupId(
														awsHelper
																.getLbSecurityGroupName(),
														region), region,
												protocol, port);
							}
						}

						log.info("Load balancer '" + loadBalancerDNSName
								+ "' created for cluster '"
								+ cluster.getClusterId());

						// Register instances in the cluster to load balancer
						List<Instance> instances = new ArrayList<Instance>();

						for (Member member : clusterMembers) {
							String instanceId = member.getInstanceId();

							if (log.isDebugEnabled()) {
								log.debug("Instance "
										+ awsHelper
												.getAWSInstanceName(instanceId)
										+ " needs to be registered to load balancer "
										+ loadBalancerName);
							}

							Instance instance = new Instance();
							instance.setInstanceId(awsHelper
									.getAWSInstanceName(instanceId));

							instances.add(instance);
						}

						awsHelper.registerInstancesToLoadBalancer(
								loadBalancerName, instances, region);

						LoadBalancerInfo loadBalancerInfo = new LoadBalancerInfo(
								loadBalancerName, region);

						clusterIdToLoadBalancerMap.put(cluster.getClusterId(),
								loadBalancerInfo);
						activeClusters.add(cluster.getClusterId());
					}
				}
			}
		}

		// Find out clusters which were present earlier but are not now.
		List<String> clustersToRemoveFromMap = new ArrayList<String>();

		for (String clusterId : clusterIdToLoadBalancerMap.keySet()) {
			if (!activeClusters.contains(clusterId)) {
				clustersToRemoveFromMap.add(clusterId);

				if (log.isDebugEnabled()) {
					log.debug("Load balancer for cluster " + clusterId
							+ " needs to be removed.");
				}

			}
		}

		// Delete load balancers associated with these clusters.
		for (String clusterId : clustersToRemoveFromMap) {
			// Remove load balancer for this cluster.
			awsHelper.deleteLoadBalancer(
					clusterIdToLoadBalancerMap.get(clusterId).getName(),
					clusterIdToLoadBalancerMap.get(clusterId).getRegion());
			clusterIdToLoadBalancerMap.remove(clusterId);
		}

		activeClusters.clear();
		log.info("AWS load balancer extension was reconfigured as per the topology.");
		return true;
	}

	/*
	 * start method is called after extension if configured first time. Does
	 * nothing but logs the message.
	 */
	public void start() throws LoadBalancerExtensionException {

		log.info("AWS load balancer extension started.");
	}

	/*
	 * reload method is called every time after extension if configured. Does
	 * nothing but logs the message.
	 */
	public void reload() throws LoadBalancerExtensionException {
		// Check what is appropriate to do here.
		log.info("AWS load balancer extension reloaded.");
	}

	/*
	 * stop method deletes load balancers for all clusters in the topology.
	 */
	public void stop() throws LoadBalancerExtensionException {
		// Remove all load balancers
		for (LoadBalancerInfo loadBalancerInfo : clusterIdToLoadBalancerMap
				.values()) {
			// Remove load balancer
			awsHelper.deleteLoadBalancer(loadBalancerInfo.getName(),
					loadBalancerInfo.getRegion());
		}
	}

	public static ConcurrentHashMap<String, LoadBalancerInfo> getClusterIdToLoadBalancerMap() {
		return clusterIdToLoadBalancerMap;
	}
}

/**
 * Used to store load balancer name and the region in which it is created. This
 * helps in finding region while calling API methods to modify/delete a load
 * balancer.
 */
class LoadBalancerInfo {
	private String name;
	private String region;

	public LoadBalancerInfo(String name, String region) {
		this.name = name;
		this.region = region;
	}

	public String getName() {
		return name;
	}

	public String getRegion() {
		return region;
	}
}
