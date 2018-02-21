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

import com.opsmatters.newrelic.api.model.alerts.channels.SlackChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.SlackConfiguration;

/**
 * Template that defines the Slack alert channel file format.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class SlackChannelTemplate extends FileTemplate
{
    /**
     * The type of the template.  
     */
    public static final String TYPE = "slack-channel";

    // The template columns
    public TemplateColumn NAME = TemplateColumn.builder()
        .name(SlackChannel.NAME)
        .header("Name")
        .build();
    public TemplateColumn URL = TemplateColumn.builder()
        .name(SlackConfiguration.URL)
        .header("URL")
        .build();
    public TemplateColumn CHANNEL = TemplateColumn.builder()
        .name(SlackConfiguration.CHANNEL)
        .header("Channel")
        .build();

    /**
     * Default constructor.
     */
    public SlackChannelTemplate()
    {
        addColumn(NAME);
        addColumn(TEMPLATE_TYPE);
        addColumn(URL);
        addColumn(CHANNEL);
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