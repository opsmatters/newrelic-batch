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

package com.opsmatters.newrelic.batch.renderers;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import com.opsmatters.core.documents.OutputFileWriter;
import com.opsmatters.newrelic.api.model.alerts.channels.UserChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.UserConfiguration;
import com.opsmatters.newrelic.batch.templates.FileTemplate;
import com.opsmatters.newrelic.batch.templates.TemplateFactory;

/**
 * Renderer that converts user alert channels to a text file.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class UserChannelRenderer extends OutputFileRenderer<UserChannel>
{
    private static final Logger logger = Logger.getLogger(UserChannelRenderer.class.getName());

    /**
     * Private constructor.
     */
    private UserChannelRenderer()
    {
    }

    /**
     * Register this class with the given template.
     * @param template The template to register with this class
     */
    public static void registerTemplate(FileTemplate template)
    {
        TemplateFactory.registerTemplate(UserChannelRenderer.class, template);
    }

    /**
     * Writes the given alert channels to a writer.
     * @param channels The alert channels to be serialized
     * @param writer The writer to use to serialize the alert channels
     * @throws IOException if there was an error writing the alert channels
     */
    public static void write(List<UserChannel> channels, OutputFileWriter writer) throws IOException
    {
        new UserChannelRenderer().render(channels, writer);
    }

    /**
     * Serializes the alert channel to a line.
     * @param template The template with the columns
     * @return The line representing the alert channel
     */
    protected String[] serialize(FileTemplate template, UserChannel channel)
    {
        List<String> line = new ArrayList<String>();
        line.add(channel.getName());
        line.add(template.getType());
        line.add(channel.getConfiguration().getUserId());
        return line.toArray(new String[]{});
    }
}