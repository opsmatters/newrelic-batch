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

import com.opsmatters.newrelic.api.model.alerts.channels.xMattersChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.xMattersConfiguration;

/**
 * Template that defines the xMatters alert channel file format.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class xMattersChannelTemplate extends FileTemplate
{
    /**
     * The type of the template.  
     */
    public static final String TYPE = "xmatters-channel";

    // The template columns
    public TemplateColumn NAME = TemplateColumn.builder()
        .name(xMattersChannel.NAME)
        .header("Name")
        .build();
    public TemplateColumn URL = TemplateColumn.builder()
        .name(xMattersConfiguration.URL)
        .header("URL")
        .build();
    public TemplateColumn CHANNEL = TemplateColumn.builder()
        .name(xMattersConfiguration.CHANNEL)
        .header("Channel")
        .build();

    /**
     * Default constructor.
     */
    public xMattersChannelTemplate()
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