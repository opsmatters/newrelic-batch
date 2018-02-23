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

package com.opsmatters.newrelic.batch.templates;

import com.opsmatters.newrelic.api.model.alerts.conditions.NrqlAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.Term;
import com.opsmatters.newrelic.api.model.alerts.conditions.TimeFunction;
import com.opsmatters.newrelic.api.model.alerts.conditions.Nrql;

/**
 * Template that defines the NRQL alert condition file format.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class NrqlAlertConditionTemplate extends FileTemplate
{
    /**
     * The type of the template.  
     */
    public static final String TYPE = "nrql-alert-condition";

    // The template columns
    public TemplateColumn ALERT_POLICY = TemplateColumn.builder()
        .name(NrqlAlertCondition.POLICY_NAME)
        .header("Alert Policy")
        .build();
    public TemplateColumn NAME = TemplateColumn.builder()
        .name(NrqlAlertCondition.NAME)
        .header("Name")
        .build();
    public TemplateColumn QUERY = TemplateColumn.builder()
        .name(Nrql.QUERY)
        .header("Query")
        .build();
    public TemplateColumn VALUE_FUNCTION = TemplateColumn.builder()
        .name(NrqlAlertCondition.VALUE_FUNCTION)
        .header("Value Function")
        .mandatory(false)
        .defaultValue(NrqlAlertCondition.ValueFunction.SINGLE_VALUE.value())
        .build();
    public TemplateColumn SINCE_VALUE = TemplateColumn.builder()
        .name(Nrql.SINCE_VALUE)
        .header("Since Value")
        .mandatory(false)
        .defaultValue("3")
        .build();
    public TemplateColumn OPERATOR = TemplateColumn.builder()
        .name(Term.OPERATOR)
        .header("Operator")
        .build();
    public TemplateColumn WARNING = TemplateColumn.builder()
        .name(Term.WARNING_THRESHOLD)
        .header("Warning")
        .build();
    public TemplateColumn CRITICAL = TemplateColumn.builder()
        .name(Term.CRITICAL_THRESHOLD)
        .header("Critical")
        .build();
    public TemplateColumn DURATION = TemplateColumn.builder()
        .name(Term.DURATION)
        .header("Duration")
        .build();
    public TemplateColumn TIME_FUNCTION = TemplateColumn.builder()
        .name(Term.TIME_FUNCTION)
        .header("Time Function")
        .mandatory(false)
        .defaultValue(TimeFunction.ALL.value())
        .build();

    /**
     * Default constructor.
     */
    public NrqlAlertConditionTemplate()
    {
        addColumn(ALERT_POLICY);
        addColumn(NAME);
        addColumn(TEMPLATE_TYPE);
        addColumn(QUERY);
        addColumn(VALUE_FUNCTION);
        addColumn(SINCE_VALUE);
        addColumn(OPERATOR);
        addColumn(WARNING);
        addColumn(CRITICAL);
        addColumn(DURATION);
        addColumn(TIME_FUNCTION);
    }

    /**
     * Returns the type of the template.
     * @return The type of the template
     */
    public String getType()
    {
        return TYPE;
    }
}