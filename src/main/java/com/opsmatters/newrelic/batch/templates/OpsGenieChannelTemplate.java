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

import com.opsmatters.newrelic.api.model.alerts.channels.OpsGenieChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.OpsGenieConfiguration;

/**
 * Template that defines the OpsGenie alert channel file format.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class OpsGenieChannelTemplate extends Template
{
    /**
     * The type of the template.  
     */
    public static final String TYPE = "opsgenie-channel";

    // The template columns
    public TemplateColumn NAME = new TemplateColumn(OpsGenieChannel.NAME, "Name", true);
    public TemplateColumn API_KEY = new TemplateColumn(OpsGenieConfiguration.API_KEY, "API Key", true);
    public TemplateColumn TEAMS = new TemplateColumn(OpsGenieConfiguration.TEAMS, "Teams", true);
    public TemplateColumn TAGS = new TemplateColumn(OpsGenieConfiguration.TAGS, "Tags", true);
    public TemplateColumn RECIPIENTS = new TemplateColumn(OpsGenieConfiguration.RECIPIENTS, "Recipients", true);

    /**
     * Default constructor.
     */
    public OpsGenieChannelTemplate()
    {
        addColumn(NAME);
        addColumn(Template.TYPE);
        addColumn(API_KEY);
        addColumn(TEAMS);
        addColumn(TAGS);
        addColumn(RECIPIENTS);
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