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
import com.opsmatters.newrelic.api.model.alerts.IncidentPreference;

/**
 * Template that defines the alert policy file format.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class AlertPolicyTemplate extends Template
{
    /**
     * The type of the template.  
     */
    public static final String TYPE = "alert-policy";

    // The template columns
    public TemplateColumn NAME = new TemplateColumn(AlertPolicy.NAME, "Name", true);
    public TemplateColumn INCIDENT_PREFERENCE = new TemplateColumn(AlertPolicy.INCIDENT_PREFERENCE, "Incident Preference", false, 
        IncidentPreference.PER_POLICY.name());

    /**
     * Default constructor.
     */
    public AlertPolicyTemplate()
    {
        addColumn(NAME);
        addColumn(Template.TYPE);
        addColumn(INCIDENT_PREFERENCE);
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