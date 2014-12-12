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

package org.apache.stratos.cloud.controller.iaases.mock.statistics.generator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.cloud.controller.iaases.mock.statistics.MockHealthStatistics;

/**
 * Update health statistics according to the given sample pattern, for each pattern there will be
 * one updater runnable created.
 */
public class MockHealthStatisticsUpdater implements Runnable {

    private static final Log log = LogFactory.getLog(MockHealthStatisticsUpdater.class);

    private MockHealthStatisticsPattern statisticsPattern;

    public MockHealthStatisticsUpdater(MockHealthStatisticsPattern statisticsPattern) {
        this.statisticsPattern = statisticsPattern;
    }

    @Override
    public void run() {
        try {
            int nextSample = statisticsPattern.getNextSample();
            if(nextSample != -1) {
                MockHealthStatistics.getInstance().addStatistics(statisticsPattern.getCartridgeType(),
                        statisticsPattern.getFactor(), nextSample);

                if (log.isInfoEnabled()) {
                    log.info(String.format("Mock statistics updated: [cartridge-type] %s [factor] %s [value] %d",
                            statisticsPattern.getCartridgeType(), statisticsPattern.getFactor().toString(), nextSample));
                }
            }
        } catch (Exception e) {
            log.error("Could not update mock statistics", e);
        }
    }
}
