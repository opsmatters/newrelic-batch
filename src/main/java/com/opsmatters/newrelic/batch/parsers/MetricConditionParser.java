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

import java.util.logging.Logger;
import com.opsmatters.newrelic.api.model.alerts.conditions.MetricCondition;
import com.opsmatters.newrelic.api.model.EntityList;

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