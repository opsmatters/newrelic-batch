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

import com.opsmatters.newrelic.api.model.alerts.conditions.InfraMetricAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.AlertThreshold;
import com.opsmatters.newrelic.api.model.alerts.conditions.TimeFunction;

/**
 * Template that defines the Infrastructure metric alert condition file format.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class InfraMetricAlertConditionTemplate extends FileTemplate
{
    /**
     * The type of the template.  
     */
    public static final String TYPE = "infra-metric-alert-condition";

    // The template columns
    public TemplateColumn ALERT_POLICY = TemplateColumn.builder()
        .name(InfraMetricAlertCondition.POLICY_NAME)
        .header("Alert Policy")
        .build();
    public TemplateColumn NAME = TemplateColumn.builder()
        .name(InfraMetricAlertCondition.NAME)
        .header("Name")
        .build();
    public TemplateColumn EVENT_TYPE = TemplateColumn.builder()
        .name(InfraMetricAlertCondition.EVENT_TYPE)
        .header("Event Type")
        .build();
    public TemplateColumn SELECT_VALUE = TemplateColumn.builder()
        .name(InfraMetricAlertCondition.SELECT_VALUE)
        .header("Metric")
        .build();
    public TemplateColumn COMPARISON = TemplateColumn.builder()
        .name(InfraMetricAlertCondition.COMPARISON)
        .header("Comparison")
        .build();
    public TemplateColumn WARNING = TemplateColumn.builder()
        .name(InfraMetricAlertCondition.WARNING_THRESHOLD)
        .header("Warning")
        .build();
    public TemplateColumn CRITICAL = TemplateColumn.builder()
        .name(InfraMetricAlertCondition.CRITICAL_THRESHOLD)
        .header("Critical")
        .build();
    public TemplateColumn DURATION = TemplateColumn.builder()
        .name(AlertThreshold.DURATION)
        .header("Duration")
        .build();
    public TemplateColumn TIME_FUNCTION = TemplateColumn.builder()
        .name(AlertThreshold.TIME_FUNCTION)
        .header("Time Function")
        .mandatory(false)
        .defaultValue(TimeFunction.ALL.value())
        .build();
    public TemplateColumn WHERE_CLAUSE = TemplateColumn.builder()
        .name(InfraMetricAlertCondition.WHERE_CLAUSE)
        .header("Where Clause")
        .mandatory(false)
        .build();

    /**
     * Default constructor.
     */
    public InfraMetricAlertConditionTemplate()
    {
        addColumn(ALERT_POLICY);
        addColumn(NAME);
        addColumn(TEMPLATE_TYPE);
        addColumn(EVENT_TYPE);
        addColumn(SELECT_VALUE);
        addColumn(COMPARISON);
        addColumn(WARNING);
        addColumn(CRITICAL);
        addColumn(DURATION);
        addColumn(TIME_FUNCTION);
        addColumn(WHERE_CLAUSE);
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