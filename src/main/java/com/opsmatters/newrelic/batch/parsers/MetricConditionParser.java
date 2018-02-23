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
import com.opsmatters.newrelic.api.model.alerts.conditions.MetricCondition;
import com.opsmatters.newrelic.api.model.Entity;
import com.opsmatters.newrelic.api.model.EntityList;
import com.opsmatters.newrelic.batch.templates.TemplateFactory;
import com.opsmatters.newrelic.batch.templates.FileInstance;

/**
 * Base class for all metric alert condition parsers.
 * 
 * @author Gerald Curley (opsmatters)
 */
public abstract class MetricConditionParser<T extends MetricCondition> extends TermsConditionParser<T>
{
    private static final Logger logger = Logger.getLogger(MetricConditionParser.class.getName());

    /**
     * Protected constructor.
     */
    protected MetricConditionParser()
    {
    }

    /**
     * Creates the alert conditions from the given lines.
     * @param policies The set of alert policies for the conditions
     * @param entities The set of entities for the conditions
     * @param headers The headers of the file
     * @param lines The input file lines
     * @return The alert conditions created from the lines
     */
    protected List<T> get(List<AlertPolicy> policies, List<Entity> entities, String[] headers, List<String[]> lines)
    {
        List<T> ret = new ArrayList<T>();
        FileInstance file = TemplateFactory.getTemplate(getClass()).getInstance(headers);
        AlertPolicyList policyList = new AlertPolicyList(policies);
        EntityList entityList = new EntityList(entities);
        logger.info("Processing "+file.getType()+" file: policies="+policies.size()
            +" entities="+entities.size()+" headers="+headers.length+" lines="+lines.size());

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
            setPolicyId(condition, policyList, file.getString(MetricCondition.POLICY_NAME, line));
            setEntities(condition, file.getString(MetricCondition.FILTER, line), 
                file.getString(MetricCondition.ENTITIES, line), entityList);
            ret.add(condition);
        }

        return ret;
    }

    /**
     * Sets the entities of the given condition.
     * @param condition The condition to be set
     * @param filter A wild-carded expression for the entities for the condition
     * @param entities A comma-separated list of entity ids for the condition
     * @param entityList The list of entities
     * @throws IllegalStateException if the policy is null or the id of the policy is null or empty
     */
    protected void setEntities(T condition, String filter, String entities, EntityList entityList)
    {
        if(filter != null)
            condition.setEntities(toIdList(entityList.list(filter)));
        else if(entities != null)
            condition.setEntities(toIdList(entities));
    }
}