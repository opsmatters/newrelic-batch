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

import com.opsmatters.newrelic.api.model.alerts.policies.AlertPolicy;
import com.opsmatters.newrelic.api.model.alerts.policies.AlertPolicyChannel;
import com.opsmatters.newrelic.api.model.alerts.IncidentPreference;

/**
 * Template that defines the alert policy file format.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class AlertPolicyTemplate extends FileTemplate
{
    /**
     * The type of the template.  
     */
    public static final String TYPE = "alert-policy";

    // The template columns
    public TemplateColumn NAME = TemplateColumn.builder()
        .name(AlertPolicy.NAME)
        .header("Name")
        .build();
    public TemplateColumn INCIDENT_PREFERENCE = TemplateColumn.builder()
        .name(AlertPolicy.INCIDENT_PREFERENCE)
        .header("Incident Preference")
        .mandatory(false)
        .defaultValue(IncidentPreference.PER_POLICY.name())
        .build();
    public TemplateColumn CHANNELS = TemplateColumn.builder()
        .name(AlertPolicyChannel.CHANNELS)
        .header("Channels")
        .mandatory(false)
        .build();

    /**
     * Default constructor.
     */
    public AlertPolicyTemplate()
    {
        addColumn(NAME);
        addColumn(TEMPLATE_TYPE);
        addColumn(INCIDENT_PREFERENCE);
        addColumn(CHANNELS);
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