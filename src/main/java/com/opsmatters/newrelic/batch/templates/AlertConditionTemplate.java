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
public class AlertConditionTemplate extends Template
{
    /**
     * The type of the template.  
     */
    public static final String TYPE = "alert-condition";

    // The template columns
    public TemplateColumn ALERT_POLICY = new TemplateColumn(AlertCondition.POLICY_NAME, "Alert Policy", true);
    public TemplateColumn NAME = new TemplateColumn(AlertCondition.NAME, "Name", true);
    public TemplateColumn CONDITION_TYPE = new TemplateColumn(AlertCondition.CONDITION_TYPE, "Condition Type", true);
    public TemplateColumn CONDITION_SCOPE = new TemplateColumn(AlertCondition.CONDITION_SCOPE, "Condition Scope", true);
    public TemplateColumn METRIC = new TemplateColumn(AlertCondition.METRIC, "Metric", true);
    public TemplateColumn OPERATOR = new TemplateColumn(Term.OPERATOR, "Operator", true);
    public TemplateColumn WARNING = new TemplateColumn(Term.WARNING_THRESHOLD, "Warning", true);
    public TemplateColumn CRITICAL = new TemplateColumn(Term.CRITICAL_THRESHOLD, "Critical", true);
    public TemplateColumn DURATION = new TemplateColumn(Term.DURATION, "Duration", true);
    public TemplateColumn TIME_FUNCTION = new TemplateColumn(Term.TIME_FUNCTION, "Time Function", false, 
        TimeFunction.ALL.value());
    public TemplateColumn VIOLATION_CLOSE_TIMER = new TemplateColumn(AlertCondition.VIOLATION_CLOSE_TIMER, "Violation Close Timer", false, 
        Integer.toString(AlertCondition.ViolationCloseTimerInterval.HOURS_24.value()));
    public TemplateColumn APPLICATION_FILTER = new TemplateColumn(AlertCondition.APPLICATION_FILTER, "Application Filter", true);

    /**
     * Default constructor.
     */
    public AlertConditionTemplate()
    {
        addColumn(ALERT_POLICY);
        addColumn(NAME);
        addColumn(Template.TYPE);
        addColumn(CONDITION_TYPE);
        addColumn(CONDITION_SCOPE);
        addColumn(METRIC);
        addColumn(OPERATOR);
        addColumn(WARNING);
        addColumn(CRITICAL);
        addColumn(DURATION);
        addColumn(TIME_FUNCTION);
        addColumn(VIOLATION_CLOSE_TIMER);
        addColumn(APPLICATION_FILTER);
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