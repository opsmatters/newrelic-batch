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

import com.opsmatters.newrelic.api.model.alerts.conditions.InfraProcessRunningAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.AlertThreshold;

/**
 * Template that defines the Infrastructure process running alert condition file format.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class InfraProcessRunningAlertConditionTemplate extends FileTemplate
{
    /**
     * The type of the template.  
     */
    public static final String TYPE = "infra-process-alert-condition";

    // The template columns
    public TemplateColumn ALERT_POLICY = TemplateColumn.builder()
        .name(InfraProcessRunningAlertCondition.POLICY_NAME)
        .header("Alert Policy")
        .build();
    public TemplateColumn NAME = TemplateColumn.builder()
        .name(InfraProcessRunningAlertCondition.NAME)
        .header("Name")
        .build();
    public TemplateColumn COMPARISON = TemplateColumn.builder()
        .name(InfraProcessRunningAlertCondition.COMPARISON)
        .header("Comparison")
        .build();
    public TemplateColumn CRITICAL = TemplateColumn.builder()
        .name(InfraProcessRunningAlertCondition.CRITICAL_THRESHOLD)
        .header("Critical")
        .build();
    public TemplateColumn DURATION = TemplateColumn.builder()
        .name(AlertThreshold.DURATION)
        .header("Duration")
        .build();
    public TemplateColumn PROCESS_WHERE_CLAUSE = TemplateColumn.builder()
        .name(InfraProcessRunningAlertCondition.PROCESS_WHERE_CLAUSE)
        .header("Process Where Clause")
        .mandatory(false)
        .build();
    public TemplateColumn WHERE_CLAUSE = TemplateColumn.builder()
        .name(InfraProcessRunningAlertCondition.WHERE_CLAUSE)
        .header("Where Clause")
        .mandatory(false)
        .build();

    /**
     * Default constructor.
     */
    public InfraProcessRunningAlertConditionTemplate()
    {
        addColumn(ALERT_POLICY);
        addColumn(NAME);
        addColumn(TEMPLATE_TYPE);
        addColumn(COMPARISON);
        addColumn(CRITICAL);
        addColumn(DURATION);
        addColumn(PROCESS_WHERE_CLAUSE);
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