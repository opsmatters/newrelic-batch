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
import com.opsmatters.newrelic.api.model.Entity;
import com.opsmatters.newrelic.api.model.alerts.policies.AlertPolicy;
import com.opsmatters.newrelic.api.model.alerts.conditions.AlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.Term;
import com.opsmatters.newrelic.batch.templates.FileTemplate;
import com.opsmatters.newrelic.batch.templates.TemplateFactory;

/**
 * Renderer that converts alert conditions to a text file.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class AlertConditionRenderer extends MetricConditionRenderer<AlertCondition>
{
    private static final Logger logger = Logger.getLogger(AlertConditionRenderer.class.getName());

    /**
     * Private constructor.
     */
    private AlertConditionRenderer()
    {
    }

    /**
     * Register this class with the given template.
     * @param template The template to register with this class
     */
    public static void registerTemplate(FileTemplate template)
    {
        TemplateFactory.registerTemplate(AlertConditionRenderer.class, template);
    }

    /**
     * Writes the given alert conditions to a writer.
     * @param policies The set of alert policies for the conditions
     * @param conditions The alert conditions to be serialized
     * @param entities The set of entities for the condition
     * @param writer The writer to use to serialize the alert conditions
     * @throws IOException if there was an error writing the alert conditions
     */
    public static void write(List<AlertPolicy> policies, List<AlertCondition> conditions, List<Entity> entities, OutputFileWriter writer)
        throws IOException
    {
        new AlertConditionRenderer().render(policies, conditions, entities, writer);
    }

    /**
     * Serializes the alert condition to a line.
     * @param entities The set of entities for the condition
     * @param template The template with the columns
     * @param policy The alert policy for the condition
     * @param condition The alert condition to be serialized
     * @return The line representing the alert condition
     */
    protected String[] serialize(List<Entity> entities, FileTemplate template, AlertPolicy policy, AlertCondition condition)
    {
        List<Term> terms = condition.getTerms();
        if(terms.size() == 0)
            throw new IllegalStateException("alert condition missing terms: "+condition.getName());

        Term critical = terms.get(0);
        Term warning = terms.size() > 1 ? terms.get(1) : null;

        List<String> line = new ArrayList<String>();
        line.add(policy.getName());
        line.add(condition.getName());
        line.add(template.getType());
        line.add(condition.getType());
        line.add(condition.getConditionScope());
        line.add(condition.getMetric());
        line.add(critical.getOperator());
        line.add(warning != null ? warning.getThreshold() : "");
        line.add(critical.getThreshold());
        line.add(critical.getDuration());
        line.add(critical.getTimeFunction());
        line.add(Integer.toString(condition.getViolationCloseTimer()));
        line.add(fromItemList(entities));
        return line.toArray(new String[]{});
    }
}