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

package org.apache.stratos.manager.deploy.service.multitenant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.cloud.controller.pojo.CartridgeInfo;
import org.apache.stratos.cloud.controller.pojo.Properties;
import org.apache.stratos.manager.deploy.service.Service;
import org.apache.stratos.manager.exception.ADCException;
import org.apache.stratos.manager.exception.UnregisteredCartridgeException;

public class MultiTenantService extends Service {

    private static Log log = LogFactory.getLog(MultiTenantService.class);

    public MultiTenantService (String type, String autoscalingPolicyName, String deploymentPolicyName, int tenantId, CartridgeInfo cartridgeInfo,
    		String tenantRange) {
        super(type, autoscalingPolicyName, deploymentPolicyName, tenantId, cartridgeInfo, tenantRange);
    }

    @Override
    public void deploy(Properties properties) throws ADCException, UnregisteredCartridgeException {

        super.deploy(properties);

        //register the service
        //ApplicationManagementUtil.registerService(getType(), getClusterId(), CartridgeConstants.DEFAULT_SUBDOMAIN,
        //        getPayloadData().getCompletePayloadData(), getTenantRange(), getHostName(), getAutoscalingPolicyName(),
        //        getDeploymentPolicyName(), properties);

        register(getCartridgeInfo(), getCluster(), getPayloadData(), getAutoscalingPolicyName(), getDeploymentPolicyName(), properties);
    }
}
