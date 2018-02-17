/*
 * Copyright 2018 Gerald Curley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opsmatters.newrelic.batch.model;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import com.opsmatters.newrelic.api.model.alerts.policies.AlertPolicy;

/**
 * Represents a set of alert policies, conditions and channels.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class AlertConfiguration
{
    private static final Logger logger = Logger.getLogger(AlertConfiguration.class.getName());

    private List<AlertPolicy> policies = new ArrayList<AlertPolicy>();

    /**
     * Default constructor.
     */
    public AlertConfiguration()
    {
    }

    /**
     * Replaces the alert policies with the given set of policies.
     * @param policies The set of policies
     */
    public void setAlertPolicies(List<AlertPolicy> policies)
    {
        this.policies.clear();
        this.policies.addAll(policies);
    }

    /**
     * Returns the set of alert policies.
     * @return The set of policies
     */
    public List<AlertPolicy> getAlertPolicies()
    {
        return policies;
    }

    /**
     * Returns the number of alert policies.
     * @return The number of alert policies
     */
    public int numAlertPolicies()
    {
        return policies.size();
    }

    /**
     * Returns a string representation of the object.
     */
    @Override
    public String toString()
    {
        return "AlertConfiguration [policies="+policies.size()+"]";
    }
}
