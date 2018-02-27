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

package com.opsmatters.newrelic.batch.renderers;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import com.opsmatters.core.documents.OutputFileWriter;
import com.opsmatters.newrelic.api.model.alerts.policies.AlertPolicy;
import com.opsmatters.newrelic.api.model.alerts.policies.AlertPolicyList;
import com.opsmatters.newrelic.api.model.alerts.conditions.BaseCondition;
import com.opsmatters.newrelic.batch.templates.FileTemplate;
import com.opsmatters.newrelic.batch.templates.TemplateFactory;

/**
 * Base class for all alert condition renderers.
 * 
 * @author Gerald Curley (opsmatters)
 */
public abstract class BaseConditionRenderer<T extends BaseCondition> extends OutputFileRenderer<T>
{
    private static final Logger logger = Logger.getLogger(BaseConditionRenderer.class.getName());

    /**
     * Protected constructor.
     */
    protected BaseConditionRenderer()
    {
    }

    /**
     * Writes the given alert conditions to a writer.
     * @param policies The set of alert policies for the conditions
     * @param conditions The alert conditions to be serialized
     * @param writer The writer to use to serialize the alert conditions
     * @throws IOException if there was an error writing the alert conditions
     */
    public void render(List<AlertPolicy> policies, List<T> conditions, OutputFileWriter writer) throws IOException
    {
        List<String[]> lines = new ArrayList<String[]>();
        FileTemplate template = TemplateFactory.getTemplate(getClass());
        AlertPolicyList policyList = new AlertPolicyList(policies);
        String[] headers = template.getOutputHeaders();

        lines.add(headers);
        for(T condition : conditions)
        {
            AlertPolicy policy = policyList.get(condition.getPolicyId());
            if(policy == null)
                throw new IllegalStateException("unable to find policy \""+condition.getPolicyId()+"\" for alert condition: "+condition.getName());
            if(policy.getId() == null || policy.getId() == 0L)
                throw new IllegalStateException("missing policy_id: "+policy.getName());
            lines.add(serialize(template, policy, condition));
        }

        logger.fine("Rendering "+template.getType()+" file: headers="+headers.length+" lines="+lines.size());
        writer.write(lines);
    }

    /**
     * Serializes the alert condition to a line.
     * <P>
     * Implemented by super-class.
     * </P>
     * @param template The template with the columns
     * @param policy The alert policy for the condition
     * @param condition The alert condition to be serialized
     * @return The line representing the alert condition
     */
    protected String[] serialize(FileTemplate template, AlertPolicy policy, T condition)
    {
        return null;
    }
}