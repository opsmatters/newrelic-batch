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
import java.util.ArrayList;
import java.util.logging.Logger;
import com.opsmatters.core.documents.InputFileReader;
import com.opsmatters.newrelic.api.model.alerts.policies.AlertPolicy;
import com.opsmatters.newrelic.api.model.alerts.policies.AlertPolicyChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.AlertChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.AlertChannelList;
import com.opsmatters.newrelic.batch.templates.FileTemplate;
import com.opsmatters.newrelic.batch.templates.TemplateFactory;
import com.opsmatters.newrelic.batch.templates.FileInstance;

/**
 * Parser that converts alert policies from report lines.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class AlertPolicyParser extends InputFileParser<AlertPolicy>
{
    private static final Logger logger = Logger.getLogger(AlertPolicyParser.class.getName());

    /**
     * Private constructor.
     */
    private AlertPolicyParser()
    {
    }

    /**
     * Register this class with the given template.
     * @param template The template to register with this class
     */
    public static void registerTemplate(FileTemplate template)
    {
        TemplateFactory.registerTemplate(AlertPolicyParser.class, template);
    }

    /**
     * Reads the alert policies from the given lines.
     * @param channels The set of alert channels for the policies
     * @param headers The headers of the file
     * @param lines The lines of the file
     * @return The alert policies read from the lines
     */
    public static List<AlertPolicy> parse(List<AlertChannel> channels, String[] headers, List<String[]> lines)
    {
        return new AlertPolicyParser().get(channels, headers, lines);
    }

    /**
     * Reads the alert policies from the given reader.
     * @param channels The set of alert channels for the policies
     * @param reader The input stream reader used to read the lines
     * @return The alert policies read from the lines
     * @throws IOException if there is a problem reading the input file or it does not exist
     */
    public static List<AlertPolicy> parse(List<AlertChannel> channels, InputFileReader reader) throws IOException
    {
        reader.parse();
        return parse(channels, reader.getHeaders(), reader.getRows());
    }

    /**
     * Creates the alert policies from the given lines.
     * @param channels The set of alert channels for the policies
     * @param headers The headers of the file
     * @param lines The input file lines
     * @return The alert policies created from the lines
     */
    protected List<AlertPolicy> get(List<AlertChannel> channels, String[] headers, List<String[]> lines)
    {
        List<AlertPolicy> ret = new ArrayList<AlertPolicy>();
        FileInstance file = TemplateFactory.getTemplate(getClass()).getInstance(headers);
        AlertChannelList channelList = new AlertChannelList(channels);
        logger.fine("Processing "+file.getType()+" file: headers="+headers.length+" lines="+lines.size());

        file.checkColumns();
        for(String[] line : lines)
        {
            // Check that the line matches the file type
            if(!file.matches(line))
            {
                logger.severe("found illegal line in "+file.getType()+" file: "+file.getType(line));
                continue;
            }

            AlertPolicy policy = create(file, line);
            setChannelIds(policy, file.getString(AlertPolicyChannel.CHANNELS, line), channelList);
            ret.add(policy);
        }

        return ret;
    }

    /**
     * Reads the alert policy from the given line.
     * @param file The file instance with the columns
     * @param line The input file line
     * @return The alert policy created
     */
    protected AlertPolicy create(FileInstance file, String[] line)
    {
        return AlertPolicy.builder()
            .name(file.getString(AlertPolicy.NAME, line))
            .incidentPreference(file.getString(AlertPolicy.INCIDENT_PREFERENCE, line))
            .build();
    }

    /**
     * Sets the channel ids of the given policy.
     * @param policy The alert policy to be set
     * @param channelList The list of alert channels
     * @param channels The comma-separated list of alert channels for the policy
     */
    protected void setChannelIds(AlertPolicy policy, String channels, AlertChannelList channelList)
    {
        if(channels != null)
        {
            List<Long> channelIds = new ArrayList<Long>();
            String[] channelNames = channels.split(",");
            for(String channelName : channelNames)
            {
                AlertChannel channel = channelList.get(channelName.trim());
                if(channel != null)
                    channelIds.add(channel.getId());
            }
            policy.setChannelIds(channelIds);
        }
    }
}