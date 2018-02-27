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

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import com.opsmatters.newrelic.api.model.alerts.policies.AlertPolicy;
import com.opsmatters.newrelic.api.model.alerts.policies.AlertPolicyList;
import com.opsmatters.newrelic.api.model.alerts.conditions.BaseCondition;
import com.opsmatters.newrelic.batch.templates.TemplateFactory;
import com.opsmatters.newrelic.batch.templates.FileInstance;

/**
 * Base class for all alert condition parsers.
 * 
 * @author Gerald Curley (opsmatters)
 */
public abstract class BaseConditionParser<T extends BaseCondition> extends InputFileParser<T>
{
    private static final Logger logger = Logger.getLogger(BaseConditionParser.class.getName());

    /**
     * Protected constructor.
     */
    protected BaseConditionParser()
    {
    }

    /**
     * Creates the alert conditions from the given lines.
     * @param policies The set of alert policies for the conditions
     * @param headers The headers of the file
     * @param lines The input file lines
     * @return The alert conditions created from the lines
     */
    protected List<T> get(List<AlertPolicy> policies, String[] headers, List<String[]> lines)
    {
        List<T> ret = new ArrayList<T>();
        FileInstance file = TemplateFactory.getTemplate(getClass()).getInstance(headers);
        AlertPolicyList policyList = new AlertPolicyList(policies);
        logger.fine("Processing "+file.getType()+" file: policies="+policies.size()
            +" headers="+headers.length+" lines="+lines.size());

        file.checkColumns();
        for(String[] line : lines)
        {
            // Check that the line matches the file type
            if(!file.matches(line))
            {
                logger.severe("found illegal line in "+file.getType()+" file: "+file.getType(line));
                continue;
            }

            T condition = create(file, line);
            setPolicyId(condition, file.getString(BaseCondition.POLICY_NAME, line), policyList);
            ret.add(condition);
        }

        return ret;
    }

    /**
     * Sets the policy id of the given condition.
     * @param condition The condition to be set
     * @param policyName The name of the alert policy for the condition
     * @param policyList The list of alert policies
     * @throws IllegalStateException if the policy is null or the id of the policy is null or empty
     */
    protected void setPolicyId(T condition, String policyName, AlertPolicyList policyList)
    {
        AlertPolicy policy = policyList.get(policyName);
        if(policy == null)
            throw new IllegalStateException("unable to find policy \""+policyName+"\" for alert condition: "+condition.getName());
        if(policy.getId() == null || policy.getId() == 0L)
            throw new IllegalStateException("missing policy_id: "+policy.getName());
        condition.setPolicyId(policy.getId());
    }
}