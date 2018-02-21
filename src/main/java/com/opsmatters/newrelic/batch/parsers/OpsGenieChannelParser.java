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

package com.opsmatters.newrelic.batch.parsers;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import com.opsmatters.core.documents.InputFileReader;
import com.opsmatters.newrelic.api.model.alerts.channels.OpsGenieChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.OpsGenieConfiguration;
import com.opsmatters.newrelic.batch.templates.FileTemplate;
import com.opsmatters.newrelic.batch.templates.TemplateFactory;
import com.opsmatters.newrelic.batch.templates.FileInstance;

/**
 * Parser that converts OpsGenie alert channels from report lines.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class OpsGenieChannelParser extends InputFileParser<OpsGenieChannel>
{
    private static final Logger logger = Logger.getLogger(OpsGenieChannelParser.class.getName());

    /**
     * Private constructor.
     */
    private OpsGenieChannelParser()
    {
    }

    /**
     * Register this class with the given template.
     * @param template The template to register with this class
     */
    public static void registerTemplate(FileTemplate template)
    {
        TemplateFactory.registerTemplate(OpsGenieChannelParser.class, template);
    }

    /**
     * Reads the alert channels from the given lines.
     * @param headers The headers of the file
     * @param lines The lines of the file
     * @return The alert channels read from the lines
     */
    public static List<OpsGenieChannel> parse(String[] headers, List<String[]> lines)
    {
        return new OpsGenieChannelParser().get(headers, lines);
    }

    /**
     * Reads the alert channels from the given reader.
     * @param reader The input stream reader used to read the lines
     * @return The alert channels read from the lines
     * @throws IOException if there is a problem reading the input file or it does not exist
     */
    public static List<OpsGenieChannel> parse(InputFileReader reader) throws IOException
    {
        reader.parse();
        return parse(reader.getHeaders(), reader.getRows());
    }

    /**
     * Reads the alert channel from the given line.
     * @param file The file instance with the columns
     * @param line The input file line
     * @return The alert channel created
     */
    protected OpsGenieChannel create(FileInstance file, String[] line)
    {
        return OpsGenieChannel.builder()
            .name(file.getString(OpsGenieChannel.NAME, line))
            .apiKey(file.getString(OpsGenieConfiguration.API_KEY, line))
            .teams(file.getString(OpsGenieConfiguration.TEAMS, line))
            .tags(file.getString(OpsGenieConfiguration.TAGS, line))
            .recipients(file.getString(OpsGenieConfiguration.RECIPIENTS, line))
            .build();
    }
}