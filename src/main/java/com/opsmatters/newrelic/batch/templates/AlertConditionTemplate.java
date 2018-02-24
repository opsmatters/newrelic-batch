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

import com.opsmatters.newrelic.api.model.alerts.conditions.AlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.Term;
import com.opsmatters.newrelic.api.model.alerts.conditions.TimeFunction;

/**
 * Template that defines the alert condition file format.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class AlertConditionTemplate extends FileTemplate
{
    /**
     * The type of the template.  
     */
    public static final String TYPE = "alert-condition";

    // The template columns
    public TemplateColumn ALERT_POLICY = TemplateColumn.builder()
        .name(AlertCondition.POLICY_NAME)
        .header("Alert Policy")
        .build();
    public TemplateColumn NAME = TemplateColumn.builder()
        .name(AlertCondition.NAME)
        .header("Name")
        .build();
    public TemplateColumn CONDITION_TYPE = TemplateColumn.builder()
        .name(AlertCondition.CONDITION_TYPE)
        .header("Condition Type")
        .build();
    public TemplateColumn CONDITION_SCOPE = TemplateColumn.builder()
        .name(AlertCondition.CONDITION_SCOPE)
        .header("Condition Scope")
        .build();
    public TemplateColumn METRIC = TemplateColumn.builder()
        .name(AlertCondition.METRIC)
        .header("Metric")
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
    public TemplateColumn VIOLATION_CLOSE_TIMER = TemplateColumn.builder()
        .name(AlertCondition.VIOLATION_CLOSE_TIMER)
        .header("Violation Close Timer")
        .mandatory(false)
        .defaultValue(Integer.toString(AlertCondition.ViolationCloseTimerInterval.HOURS_24.value()))
        .build();
    public TemplateColumn ENTITIES = TemplateColumn.builder()
        .name(AlertCondition.ENTITIES)
        .header("Entities")
        .mandatory(false)
        .build();

    /**
     * Default constructor.
     */
    public AlertConditionTemplate()
    {
        addColumn(ALERT_POLICY);
        addColumn(NAME);
        addColumn(TEMPLATE_TYPE);
        addColumn(CONDITION_TYPE);
        addColumn(CONDITION_SCOPE);
        addColumn(METRIC);
        addColumn(OPERATOR);
        addColumn(WARNING);
        addColumn(CRITICAL);
        addColumn(DURATION);
        addColumn(TIME_FUNCTION);
        addColumn(VIOLATION_CLOSE_TIMER);
        addColumn(ENTITIES);
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