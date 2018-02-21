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

import com.opsmatters.newrelic.api.model.alerts.channels.HipChatChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.HipChatConfiguration;

/**
 * Template that defines the HipChat alert channel file format.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class HipChatChannelTemplate extends FileTemplate
{
    /**
     * The type of the template.  
     */
    public static final String TYPE = "hipchat-channel";

    // The template columns
    public TemplateColumn NAME = TemplateColumn.builder()
        .name(HipChatChannel.NAME)
        .header("Name")
        .build();
    public TemplateColumn AUTH_TOKEN = TemplateColumn.builder()
        .name(HipChatConfiguration.AUTH_TOKEN)
        .header("Auth Token")
        .build();
    public TemplateColumn ROOM_ID = TemplateColumn.builder()
        .name(HipChatConfiguration.ROOM_ID)
        .header("Room ID")
        .build();

    /**
     * Default constructor.
     */
    public HipChatChannelTemplate()
    {
        addColumn(NAME);
        addColumn(TEMPLATE_TYPE);
        addColumn(AUTH_TOKEN);
        addColumn(ROOM_ID);
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