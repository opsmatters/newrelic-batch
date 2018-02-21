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

import com.opsmatters.newrelic.api.model.alerts.channels.PagerDutyChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.PagerDutyConfiguration;

/**
 * Template that defines the PagerDuty alert channel file format.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class PagerDutyChannelTemplate extends FileTemplate
{
    /**
     * The type of the template.  
     */
    public static final String TYPE = "pagerduty-channel";

    // The template columns
    public TemplateColumn NAME = TemplateColumn.builder()
        .name(PagerDutyChannel.NAME)
        .header("Name")
        .build();
    public TemplateColumn SERVICE_KEY = TemplateColumn.builder()
        .name(PagerDutyConfiguration.SERVICE_KEY)
        .header("Service Key")
        .build();

    /**
     * Default constructor.
     */
    public PagerDutyChannelTemplate()
    {
        addColumn(NAME);
        addColumn(TEMPLATE_TYPE);
        addColumn(SERVICE_KEY);
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