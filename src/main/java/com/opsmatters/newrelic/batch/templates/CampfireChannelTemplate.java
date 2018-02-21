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

import com.opsmatters.newrelic.api.model.alerts.channels.CampfireChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.CampfireConfiguration;

/**
 * Template that defines the Campfire alert channel file format.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class CampfireChannelTemplate extends FileTemplate
{
    /**
     * The type of the template.  
     */
    public static final String TYPE = "campfire-channel";

    // The template columns
    public TemplateColumn NAME = TemplateColumn.builder()
        .name(CampfireChannel.NAME)
        .header("Name")
        .build();
    public TemplateColumn SUBDOMAIN = TemplateColumn.builder()
        .name(CampfireConfiguration.SUBDOMAIN)
        .header("Subdomain")
        .build();
    public TemplateColumn TOKEN = TemplateColumn.builder()
        .name(CampfireConfiguration.TOKEN)
        .header("Token")
        .build();
    public TemplateColumn ROOM = TemplateColumn.builder()
        .name(CampfireConfiguration.ROOM)
        .header("Room")
        .build();

    /**
     * Default constructor.
     */
    public CampfireChannelTemplate()
    {
        addColumn(NAME);
        addColumn(TEMPLATE_TYPE);
        addColumn(SUBDOMAIN);
        addColumn(TOKEN);
        addColumn(ROOM);
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