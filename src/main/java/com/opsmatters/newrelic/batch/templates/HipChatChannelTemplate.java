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
public class HipChatChannelTemplate extends Template
{
    /**
     * The type of the template.  
     */
    public static final String TYPE = "hipchat-channel";

    // The template columns
    public TemplateColumn NAME = new TemplateColumn(HipChatChannel.NAME, "Name", true);
    public TemplateColumn AUTH_TOKEN = new TemplateColumn(HipChatConfiguration.AUTH_TOKEN, "Auth Token", true);
    public TemplateColumn ROOM_ID = new TemplateColumn(HipChatConfiguration.ROOM_ID, "Room ID", false);

    /**
     * Default constructor.
     */
    public HipChatChannelTemplate()
    {
        addColumn(NAME);
        addColumn(Template.TYPE);
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