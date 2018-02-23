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

package com.opsmatters.newrelic.batch.parsers;

import com.opsmatters.newrelic.api.model.alerts.policies.AlertPolicy;
import com.opsmatters.newrelic.api.model.alerts.policies.AlertPolicyList;
import com.opsmatters.newrelic.api.model.alerts.conditions.BaseCondition;

/**
 * Base class for all alert condition parsers.
 * 
 * @author Gerald Curley (opsmatters)
 */
public abstract class BaseConditionParser<T extends BaseCondition> extends InputFileParser<T>
{
    /**
     * Protected constructor.
     */
    protected BaseConditionParser()
    {
    }

    /**
     * Sets the policy id of the given condition.
     * @param condition The condition to be set
     * @param policyList The list of alert policies
     * @param policyName The name of the alert policy for the condition
     * @throws IllegalStateException if the policy is null or the id of the policy is null or empty
     */
    protected void setPolicyId(T condition, AlertPolicyList policyList, String policyName)
    {
        AlertPolicy policy = policyList.get(policyName);
        if(policy == null)
            throw new IllegalStateException("unable to find policy \""+policyName+"\" for alert condition: "+condition.getName());
        if(policy.getId() == null || policy.getId() == 0L)
            throw new IllegalStateException("missing policy_id: "+policy.getName());
        condition.setPolicyId(policy.getId());
    }
}