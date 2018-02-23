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
import com.opsmatters.newrelic.api.model.alerts.conditions.InfraMetricAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.AlertThreshold;
import com.opsmatters.newrelic.batch.templates.FileTemplate;
import com.opsmatters.newrelic.batch.templates.TemplateFactory;

/**
 * Renderer that converts infrastructure alert conditions to a text file.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class InfraMetricAlertConditionRenderer extends BaseConditionRenderer<InfraMetricAlertCondition>
{
    private static final Logger logger = Logger.getLogger(InfraMetricAlertConditionRenderer.class.getName());

    /**
     * Private constructor.
     */
    private InfraMetricAlertConditionRenderer()
    {
    }

    /**
     * Register this class with the given template.
     * @param template The template to register with this class
     */
    public static void registerTemplate(FileTemplate template)
    {
        TemplateFactory.registerTemplate(InfraMetricAlertConditionRenderer.class, template);
    }

    /**
     * Writes the given alert conditions to a writer.
     * @param policies The set of alert policies for the conditions
     * @param conditions The alert conditions to be serialized
     * @param writer The writer to use to serialize the alert conditions
     * @throws IOException if there was an error writing the alert conditions
     */
    public static void write(List<AlertPolicy> policies, List<InfraMetricAlertCondition> conditions, OutputFileWriter writer) throws IOException
    {
        new InfraMetricAlertConditionRenderer().render(policies, conditions, writer);
    }

    /**
     * Serializes the alert condition to a line.
     * @param template The template with the columns
     * @param policy The alert policy for the condition
     * @param condition The alert condition to be serialized
     * @return The line representing the alert condition
     */
    protected String[] serialize(FileTemplate template, AlertPolicy policy, InfraMetricAlertCondition condition)
    {
        AlertThreshold critical = condition.getCriticalThreshold();
        if(critical == null)
            throw new IllegalStateException("infra alert condition missing critical threshold: "+condition.getName());
        AlertThreshold warning = condition.getWarningThreshold();

        List<String> line = new ArrayList<String>();
        line.add(policy.getName());
        line.add(condition.getName());
        line.add(template.getType());
        line.add(condition.getEventType());
        line.add(condition.getSelectValue());
        line.add(condition.getComparison());
        line.add(warning != null ? Integer.toString(warning.getValue()) : "");
        line.add(Integer.toString(critical.getValue()));
        line.add(Integer.toString(critical.getDurationMinutes()));
        line.add(critical.getTimeFunction());
        line.add(condition.getWhereClause());
        return line.toArray(new String[]{});
    }
}