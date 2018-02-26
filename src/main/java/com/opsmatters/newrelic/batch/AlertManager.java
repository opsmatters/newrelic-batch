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

package com.opsmatters.newrelic.batch;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import com.opsmatters.core.documents.InputFileReader;
import com.opsmatters.core.documents.OutputFileWriter;
import com.opsmatters.core.documents.Workbook;
import com.opsmatters.newrelic.api.NewRelicApi;
import com.opsmatters.newrelic.api.NewRelicInfraApi;
import com.opsmatters.newrelic.api.model.Entity;
import com.opsmatters.newrelic.api.model.alerts.policies.AlertPolicy;
import com.opsmatters.newrelic.api.model.alerts.policies.AlertPolicyChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.AlertChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.EmailChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.SlackChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.HipChatChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.CampfireChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.OpsGenieChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.PagerDutyChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.VictorOpsChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.UserChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.xMattersChannel;
import com.opsmatters.newrelic.api.model.alerts.conditions.BaseCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.AlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.ExternalServiceAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.NrqlAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.InfraAlertCondition;
import com.opsmatters.newrelic.api.model.applications.Application;
import com.opsmatters.newrelic.api.model.servers.Server;
import com.opsmatters.newrelic.batch.parsers.EmailChannelParser;
import com.opsmatters.newrelic.batch.parsers.SlackChannelParser;
import com.opsmatters.newrelic.batch.parsers.HipChatChannelParser;
import com.opsmatters.newrelic.batch.parsers.CampfireChannelParser;
import com.opsmatters.newrelic.batch.parsers.OpsGenieChannelParser;
import com.opsmatters.newrelic.batch.parsers.PagerDutyChannelParser;
import com.opsmatters.newrelic.batch.parsers.VictorOpsChannelParser;
import com.opsmatters.newrelic.batch.parsers.UserChannelParser;
import com.opsmatters.newrelic.batch.parsers.xMattersChannelParser;
import com.opsmatters.newrelic.batch.parsers.AlertPolicyParser;
import com.opsmatters.newrelic.batch.parsers.AlertConditionParser;
import com.opsmatters.newrelic.batch.renderers.EmailChannelRenderer;
import com.opsmatters.newrelic.batch.renderers.SlackChannelRenderer;
import com.opsmatters.newrelic.batch.renderers.HipChatChannelRenderer;
import com.opsmatters.newrelic.batch.renderers.CampfireChannelRenderer;
import com.opsmatters.newrelic.batch.renderers.OpsGenieChannelRenderer;
import com.opsmatters.newrelic.batch.renderers.PagerDutyChannelRenderer;
import com.opsmatters.newrelic.batch.renderers.VictorOpsChannelRenderer;
import com.opsmatters.newrelic.batch.renderers.UserChannelRenderer;
import com.opsmatters.newrelic.batch.renderers.xMattersChannelRenderer;
import com.opsmatters.newrelic.batch.renderers.AlertPolicyRenderer;
import com.opsmatters.newrelic.batch.renderers.AlertConditionRenderer;

/**
 * Manager of operations on alert channels, policies and conditions.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class AlertManager
{
    private static final Logger logger = Logger.getLogger(AlertManager.class.getName());

    private String apiKey;
    private NewRelicApi apiClient;
    private NewRelicInfraApi infraApiClient;
    private boolean initialized = false;

    /**
     * Constructor that takes an API key.
     * @param apiKey The API key used to authenticate the client
     */
    public AlertManager(String apiKey)
    {
        this.apiKey = apiKey;
    }

    /**
     * Initialise the clients.
     */
    private void checkInitialize()
    {
        if(!initialized)
            initialize();
        if(!initialized)
            throw new IllegalStateException("client not initialized");
    }

    /**
     * Called after setting configuration properties.
     */
    public void initialize()
    {
        if(apiKey == null)
            throw new IllegalArgumentException("null API key");

        initialized = false;

        logger.info("Initialising the client");
        apiClient = NewRelicApi.builder().apiKey(apiKey).build();
        infraApiClient = NewRelicInfraApi.builder().apiKey(apiKey).build();
        logger.info("Initialised the clients");

        initialized = true;
    }

    /**
     * Returns <CODE>true</CODE> if the clients have been initialized.
     * @return <CODE>true</CODE> if the clients have been initialized
     */
    public boolean isInitialized()
    {
        return initialized;
    }

    /**
     * Returns the REST API client.
     * @return the REST API client 
     */
    public NewRelicApi getApiClient()
    {
        return apiClient;
    }

    /**
     * Returns the Infrastructure API client.
     * @return the Infrastructure API client 
     */
    public NewRelicInfraApi getInfraApiClient()
    {
        return infraApiClient;
    }

    /**
     * Returns an input file reader for the given file stream.
     * @param filename The name of the file to import
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An input stream for the file
     * @return The input file reader
     */
    private InputFileReader getReader(String filename, String worksheet, InputStream stream)
    {
         return InputFileReader.builder()
            .name(filename)
            .worksheet(worksheet)
            .withInputStream(stream)
            .build();
    }

    /**
     * Returns an output file writer for the given file stream.
     * @param filename The name of the file to export to
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to export to
     * @param workbook For XLS and XLSX files, the workbook to append the worksheet to
     * @param stream An output stream for the file
     * @return The input file reader
     */
    private OutputFileWriter getWriter(String filename, String worksheet, Workbook workbook, OutputStream stream)
    {
        return OutputFileWriter.builder()
            .name(filename)
            .worksheet(worksheet)
            .withOutputStream(stream)
            .withWorkbook(workbook)
            .build();
    }

    /**
     * Closes the given output writer.
     * @param writer The output writer to close
     */
    private void closeWriter(OutputFileWriter writer)
    {
        if(writer != null)
            writer.close();
    }

    /**
     * Closes the given input stream.
     * @param stream The input stream to close
     */
    private void closeStream(InputStream stream)
    {
        try
        {
            if(stream != null)
                stream.close();
        }
        catch(IOException e)
        {
        }
    }

    /**
     * Closes the given output stream.
     * @param stream The output stream to close
     */
    private void closeStream(OutputStream stream)
    {
        try
        {
            if(stream != null)
                stream.close();
        }
        catch(IOException e)
        {
        }
    }

    /**
     * Returns the alert policies.
     * @return The alert policies
     */
    public List<AlertPolicy> getAlertPolicies()
    {
        checkInitialize();

        // Get the alert policies
        logger.info("Getting the alert policies");
        Collection<AlertPolicy> policies = apiClient.alertPolicies().list();
        logger.info("Got "+policies.size()+" alert policies");
        return toList(policies);
    }

    /**
     * Creates the given alert policies.
     * @param policies The alert policies to create
     * @return The created alert policies
     */
    public List<AlertPolicy> createAlertPolicies(List<AlertPolicy> policies)
    {
        if(policies == null)
            throw new IllegalArgumentException("null policies");

        checkInitialize();

        // Create the policies
        List<AlertPolicy> ret = new ArrayList<AlertPolicy>();
        logger.info("Creating "+policies.size()+" alert policies");
        for(AlertPolicy policy : policies)
            ret.add(createAlertPolicy(policy));

        return ret;
    }

    /**
     * Create the given alert policy.
     * @param policy The alert policy to create
     * @return The created alert policy
     */
    public AlertPolicy createAlertPolicy(AlertPolicy policy)
    {
        checkInitialize();

        // Create the policy
        AlertPolicyChannel channels = policy.getChannels();
        logger.info("Creating alert policy: "+policy.getName());
        policy = apiClient.alertPolicies().create(policy).get();
        logger.info("Created alert policy: "+policy.getId()+" - "+policy.getName());

        // Add the channels for the policy
        if(channels != null)
        {
            for(Long channelId : channels.getChannelIds())
				{
                if(channelId != null)
                {
                    apiClient.alertPolicyChannels().update(policy.getId(), channelId);
                    logger.info("Added channel for alert policy: "+channelId);
                }
            }
        }

        return policy;
    }

    /**
     * Delete the given alert policies.
     * @param policies The alert policies to delete
     * @return The deleted alert policies
     */
    public List<AlertPolicy> deleteAlertPolicies(List<AlertPolicy> policies)
    {
        if(policies == null)
            throw new IllegalArgumentException("null policies");

        checkInitialize();

        // Delete the policies
        List<AlertPolicy> ret = new ArrayList<AlertPolicy>();
        for(AlertPolicy policy : policies)
        {
            deleteAlertPolicies(policy.getName());
            ret.add(policy);
        }

        return ret;
    }

    /**
     * Delete the given alert policy.
     * @param policy The alert policy to delete
     * @return The deleted alert policy
     */
    public AlertPolicy deleteAlertPolicy(AlertPolicy policy)
    {
        checkInitialize();

        // Delete the policy
        deleteAlertPolicies(policy.getName());

        return policy;
    }

    /**
     * Delete the alert policies with the given name.
     * @param name The name of the alert policies
     */
    private void deleteAlertPolicies(String name)
    {
        Collection<AlertPolicy> policies = apiClient.alertPolicies().list(name);
        for(AlertPolicy policy : policies)
        {
            logger.info("Deleting alert policy: "+policy.getId());
            apiClient.alertPolicies().delete(policy.getId());
            logger.info("Deleted alert policy : "+policy.getId()+" - "+policy.getName());
        }
    }

    /**
     * Reads alert policies from an import file with the given name.
     * Closes the stream after reading the file.
     * @param channels The list of channels for the alert policies
     * @param filename The name of the file to import
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An input stream for the file
     * @return The set of alert policies read from the import file
     * @throws IOException if there is an error reading the import file
     */
    public List<AlertPolicy> readAlertPolicies(List<AlertChannel> channels, String filename, String worksheet, 
        InputStream stream)
        throws IOException
    {
        List<AlertPolicy> ret = null;

        try
        {
            logger.info("Loading alert policy file: "+filename+(worksheet != null ? "/"+worksheet : ""));
            ret = AlertPolicyParser.parse(channels, getReader(filename, worksheet, stream));
            logger.info("Read "+ret.size()+" alert policies");
        }
        finally
        {
            closeStream(stream);
        }

        return ret;
    }

    /**
     * Writes alert policies to an export file with the given name.
     * Closes the stream after writing the file.
     * @param channels The list of channels for the alert policies
     * @param policies The list of alert policies to be exported
     * @param filename The name of the file to export to
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An output stream for the file
     * @param workbook For XLS and XLSX files, the workbook to append the worksheet to (or null to create a new workbook)
     * @throws IOException if there is an error reading the import file
     */
    public void writeAlertPolicies(List<AlertChannel> channels, List<AlertPolicy> policies, String filename, String worksheet, 
        OutputStream stream, Workbook workbook)
        throws IOException
    {
        OutputFileWriter writer = null;

        try
        {
            logger.info("Writing alert policy file: "+filename+(worksheet != null ? "/"+worksheet : ""));
            writer = getWriter(filename, worksheet, workbook, stream);
            AlertPolicyRenderer.write(channels, policies, writer);
            logger.info("Wrote "+policies.size()+" alert policies");
        }
        finally
        {
            closeStream(stream);
            closeWriter(writer);
        }
    }

    /**
     * Returns the alert channels.
     * @return The alert channels
     */
    public List<AlertChannel> getAlertChannels()
    {
        checkInitialize();

        // Get the alert channels
        logger.info("Getting the alert channels");
        Collection<AlertChannel> channels = apiClient.alertChannels().list();
        logger.info("Got "+channels.size()+" alert channels");
        return toList(channels);
    }

    /**
     * Creates the given alert channels.
     * @param channels The alert channels to create
     * @return The created alert channels
     */
    public List<AlertChannel> createAlertChannels(List<AlertChannel> channels)
    {
        if(channels == null)
            throw new IllegalArgumentException("null channels");

        checkInitialize();

        // Create the channels
        List<AlertChannel> ret = new ArrayList<AlertChannel>();
        logger.info("Creating "+channels.size()+" alert channels");
        for(AlertChannel channel : channels)
            ret.add(createAlertChannel(channel));

        return ret;
    }

    /**
     * Create the given alert channel.
     * @param channel The alert channel to create
     * @return The created alert channel
     */
    public AlertChannel createAlertChannel(AlertChannel channel)
    {
        checkInitialize();

        // Create the channel
        logger.info("Creating alert channel: "+channel.getName());
        channel = apiClient.alertChannels().create(channel).get();
        logger.info("Created alert channel: "+channel.getId()+" - "+channel.getName());

        return channel;
    }

    /**
     * Delete the given alert channels.
     * @param channels The alert channels to delete
     * @return The deleted alert channels
     */
    public List<AlertChannel> deleteAlertChannels(List<AlertChannel> channels)
    {
        if(channels == null)
            throw new IllegalArgumentException("null channels");

        checkInitialize();

        // Delete the channels
        List<AlertChannel> ret = new ArrayList<AlertChannel>();
        for(AlertChannel channel : channels)
        {
            deleteAlertChannels(channel.getName());
            ret.add(channel);
        }

        return ret;
    }

    /**
     * Delete the given alert channel.
     * @param channel The alert channel to delete
     * @return The deleted alert channel
     */
    public AlertChannel deleteAlertChannel(AlertChannel channel)
    {
        checkInitialize();

        // Delete the channel
        deleteAlertChannels(channel.getName());

        return channel;
    }

    /**
     * Delete the alert channels with the given name.
     * @param name The name of the alert channels
     */
    private void deleteAlertChannels(String name)
    {
        Collection<AlertChannel> channels = apiClient.alertChannels().list(name);
        for(AlertChannel channel : channels)
        {
            logger.info("Deleting alert channel: "+channel.getId());
            apiClient.alertChannels().delete(channel.getId());
            logger.info("Deleted alert channel : "+channel.getId()+" - "+channel.getName());
        }
    }

    /**
     * Reads email alert channels from an import file with the given name.
     * Closes the stream after reading the file.
     * @param filename The name of the file to import
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An input stream for the file
     * @return The set of email alert channels read from the import file
     * @throws IOException if there is an error reading the import file
     */
    public List<EmailChannel> readEmailChannels(String filename, String worksheet, InputStream stream)
        throws IOException
    {
        List<EmailChannel> ret = null;

        try
        {
            logger.info("Loading email alert channel file: "+filename+(worksheet != null ? "/"+worksheet : ""));
            ret = EmailChannelParser.parse(getReader(filename, worksheet, stream));
            logger.info("Read "+ret.size()+" email alert channels");
        }
        finally
        {
            closeStream(stream);
        }

        return ret;
    }

    /**
     * Writes email alert channels to an export file with the given name.
     * Closes the stream after writing the file.
     * @param channels The list of email alert channels to be exported
     * @param filename The name of the file to export to
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An output stream for the file
     * @param workbook For XLS and XLSX files, the workbook to append the worksheet to (or null to create a new workbook)
     * @throws IOException if there is an error reading the import file
     */
    public void writeEmailChannels(List<EmailChannel> channels, String filename, String worksheet, 
        OutputStream stream, Workbook workbook)
        throws IOException
    {
        OutputFileWriter writer = null;

        try
        {
            logger.info("Writing email alert channel file: "+filename+(worksheet != null ? "/"+worksheet : ""));
            writer = getWriter(filename, worksheet, workbook, stream);
            EmailChannelRenderer.write(channels, writer);
            logger.info("Wrote "+channels.size()+" email alert channels");
        }
        finally
        {
            closeStream(stream);
            closeWriter(writer);
        }
    }

    /**
     * Reads Slack alert channels from an import file with the given name.
     * Closes the stream after reading the file.
     * @param filename The name of the file to import
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An input stream for the file
     * @return The set of Slack alert channels read from the import file
     * @throws IOException if there is an error reading the import file
     */
    public List<SlackChannel> readSlackChannels(String filename, String worksheet, InputStream stream)
        throws IOException
    {
        List<SlackChannel> ret = null;

        try
        {
            logger.info("Loading Slack alert channel file: "+filename+(worksheet != null ? "/"+worksheet : ""));
            ret = SlackChannelParser.parse(getReader(filename, worksheet, stream));
            logger.info("Read "+ret.size()+" Slack alert channels");
        }
        finally
        {
            closeStream(stream);
        }

        return ret;
    }

    /**
     * Writes Slack alert channels to an export file with the given name.
     * Closes the stream after writing the file.
     * @param channels The list of Slack alert channels to be exported
     * @param filename The name of the file to export to
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An output stream for the file
     * @param workbook For XLS and XLSX files, the workbook to append the worksheet to (or null to create a new workbook)
     * @throws IOException if there is an error reading the import file
     */
    public void writeSlackChannels(List<SlackChannel> channels, String filename, String worksheet, 
        OutputStream stream, Workbook workbook)
        throws IOException
    {
        OutputFileWriter writer = null;

        try
        {
            logger.info("Writing Slack alert channel file: "+filename+(worksheet != null ? "/"+worksheet : ""));
            writer = getWriter(filename, worksheet, workbook, stream);
            SlackChannelRenderer.write(channels, writer);
            logger.info("Wrote "+channels.size()+" Slack alert channels");
        }
        finally
        {
            closeStream(stream);
            closeWriter(writer);
        }
    }

    /**
     * Reads HipChat alert channels from an import file with the given name.
     * Closes the stream after reading the file.
     * @param filename The name of the file to import
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An input stream for the file
     * @return The set of HipChat alert channels read from the import file
     * @throws IOException if there is an error reading the import file
     */
    public List<HipChatChannel> readHipChatChannels(String filename, String worksheet, InputStream stream)
        throws IOException
    {
        List<HipChatChannel> ret = null;

        try
        {
            logger.info("Loading HipChat alert channel file: "+filename+(worksheet != null ? "/"+worksheet : ""));
            ret = HipChatChannelParser.parse(getReader(filename, worksheet, stream));
            logger.info("Read "+ret.size()+" HipChat alert channels");
        }
        finally
        {
            closeStream(stream);
        }

        return ret;
    }

    /**
     * Writes HipChat alert channels to an export file with the given name.
     * Closes the stream after writing the file.
     * @param channels The list of HipChat alert channels to be exported
     * @param filename The name of the file to export to
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An output stream for the file
     * @param workbook For XLS and XLSX files, the workbook to append the worksheet to (or null to create a new workbook)
     * @throws IOException if there is an error reading the import file
     */
    public void writeHipChatChannels(List<HipChatChannel> channels, String filename, String worksheet, 
        OutputStream stream, Workbook workbook)
        throws IOException
    {
        OutputFileWriter writer = null;

        try
        {
            logger.info("Writing HipChat alert channel file: "+filename+(worksheet != null ? "/"+worksheet : ""));
            writer = getWriter(filename, worksheet, workbook, stream);
            HipChatChannelRenderer.write(channels, writer);
            logger.info("Wrote "+channels.size()+" HipChat alert channels");
        }
        finally
        {
            closeStream(stream);
            closeWriter(writer);
        }
    }

    /**
     * Reads Campfire alert channels from an import file with the given name.
     * Closes the stream after reading the file.
     * @param filename The name of the file to import
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An input stream for the file
     * @return The set of Campfire alert channels read from the import file
     * @throws IOException if there is an error reading the import file
     */
    public List<CampfireChannel> readCampfireChannels(String filename, String worksheet, InputStream stream)
        throws IOException
    {
        List<CampfireChannel> ret = null;

        try
        {
            logger.info("Loading Campfire alert channel file: "+filename+(worksheet != null ? "/"+worksheet : ""));
            ret = CampfireChannelParser.parse(getReader(filename, worksheet, stream));
            logger.info("Read "+ret.size()+" Campfire alert channels");
        }
        finally
        {
            closeStream(stream);
        }

        return ret;
    }

    /**
     * Writes Campfire alert channels to an export file with the given name.
     * Closes the stream after writing the file.
     * @param channels The list of Campfire alert channels to be exported
     * @param filename The name of the file to export to
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An output stream for the file
     * @param workbook For XLS and XLSX files, the workbook to append the worksheet to (or null to create a new workbook)
     * @throws IOException if there is an error reading the import file
     */
    public void writeCampfireChannels(List<CampfireChannel> channels, String filename, String worksheet, 
        OutputStream stream, Workbook workbook)
        throws IOException
    {
        OutputFileWriter writer = null;

        try
        {
            logger.info("Writing Campfire alert channel file: "+filename+(worksheet != null ? "/"+worksheet : ""));
            writer = getWriter(filename, worksheet, workbook, stream);
            CampfireChannelRenderer.write(channels, writer);
            logger.info("Wrote "+channels.size()+" Campfire alert channels");
        }
        finally
        {
            closeStream(stream);
            closeWriter(writer);
        }
    }

    /**
     * Reads OpsGenie alert channels from an import file with the given name.
     * Closes the stream after reading the file.
     * @param filename The name of the file to import
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An input stream for the file
     * @return The set of OpsGenie alert channels read from the import file
     * @throws IOException if there is an error reading the import file
     */
    public List<OpsGenieChannel> readOpsGenieChannels(String filename, String worksheet, InputStream stream)
        throws IOException
    {
        List<OpsGenieChannel> ret = null;

        try
        {
            logger.info("Loading OpsGenie alert channel file: "+filename+(worksheet != null ? "/"+worksheet : ""));
            ret = OpsGenieChannelParser.parse(getReader(filename, worksheet, stream));
            logger.info("Read "+ret.size()+" OpsGenie alert channels");
        }
        finally
        {
            closeStream(stream);
        }

        return ret;
    }

    /**
     * Writes OpsGenie alert channels to an export file with the given name.
     * Closes the stream after writing the file.
     * @param channels The list of OpsGenie alert channels to be exported
     * @param filename The name of the file to export to
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An output stream for the file
     * @param workbook For XLS and XLSX files, the workbook to append the worksheet to (or null to create a new workbook)
     * @throws IOException if there is an error reading the import file
     */
    public void writeOpsGenieChannels(List<OpsGenieChannel> channels, String filename, String worksheet, 
        OutputStream stream, Workbook workbook)
        throws IOException
    {
        OutputFileWriter writer = null;

        try
        {
            logger.info("Writing OpsGenie alert channel file: "+filename+(worksheet != null ? "/"+worksheet : ""));
            writer = getWriter(filename, worksheet, workbook, stream);
            OpsGenieChannelRenderer.write(channels, writer);
            logger.info("Wrote "+channels.size()+" OpsGenie alert channels");
        }
        finally
        {
            closeStream(stream);
            closeWriter(writer);
        }
    }

    /**
     * Reads PagerDuty alert channels from an import file with the given name.
     * Closes the stream after reading the file.
     * @param filename The name of the file to import
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An input stream for the file
     * @return The set of PagerDuty alert channels read from the import file
     * @throws IOException if there is an error reading the import file
     */
    public List<PagerDutyChannel> readPagerDutyChannels(String filename, String worksheet, InputStream stream)
        throws IOException
    {
        List<PagerDutyChannel> ret = null;

        try
        {
            logger.info("Loading PagerDuty alert channel file: "+filename+(worksheet != null ? "/"+worksheet : ""));
            ret = PagerDutyChannelParser.parse(getReader(filename, worksheet, stream));
            logger.info("Read "+ret.size()+" PagerDuty alert channels");
        }
        finally
        {
            closeStream(stream);
        }

        return ret;
    }

    /**
     * Writes PagerDuty alert channels to an export file with the given name.
     * Closes the stream after writing the file.
     * @param channels The list of PagerDuty alert channels to be exported
     * @param filename The name of the file to export to
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An output stream for the file
     * @param workbook For XLS and XLSX files, the workbook to append the worksheet to (or null to create a new workbook)
     * @throws IOException if there is an error reading the import file
     */
    public void writePagerDutyChannels(List<PagerDutyChannel> channels, String filename, String worksheet, 
        OutputStream stream, Workbook workbook)
        throws IOException
    {
        OutputFileWriter writer = null;

        try
        {
            logger.info("Writing PagerDuty alert channel file: "+filename+(worksheet != null ? "/"+worksheet : ""));
            writer = getWriter(filename, worksheet, workbook, stream);
            PagerDutyChannelRenderer.write(channels, writer);
            logger.info("Wrote "+channels.size()+" PagerDuty alert channels");
        }
        finally
        {
            closeStream(stream);
            closeWriter(writer);
        }
    }

    /**
     * Reads VictorOps alert channels from an import file with the given name.
     * Closes the stream after reading the file.
     * @param filename The name of the file to import
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An input stream for the file
     * @return The set of VictorOps alert channels read from the import file
     * @throws IOException if there is an error reading the import file
     */
    public List<VictorOpsChannel> readVictorOpsChannels(String filename, String worksheet, InputStream stream)
        throws IOException
    {
        List<VictorOpsChannel> ret = null;

        try
        {
            logger.info("Loading VictorOps alert channel file: "+filename+(worksheet != null ? "/"+worksheet : ""));
            ret = VictorOpsChannelParser.parse(getReader(filename, worksheet, stream));
            logger.info("Read "+ret.size()+" VictorOps alert channels");
        }
        finally
        {
            closeStream(stream);
        }

        return ret;
    }

    /**
     * Writes VictorOps alert channels to an export file with the given name.
     * Closes the stream after writing the file.
     * @param channels The list of VictorOps alert channels to be exported
     * @param filename The name of the file to export to
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An output stream for the file
     * @param workbook For XLS and XLSX files, the workbook to append the worksheet to (or null to create a new workbook)
     * @throws IOException if there is an error reading the import file
     */
    public void writeVictorOpsChannels(List<VictorOpsChannel> channels, String filename, String worksheet, 
        OutputStream stream, Workbook workbook)
        throws IOException
    {
        OutputFileWriter writer = null;

        try
        {
            logger.info("Writing VictorOps alert channel file: "+filename+(worksheet != null ? "/"+worksheet : ""));
            writer = getWriter(filename, worksheet, workbook, stream);
            VictorOpsChannelRenderer.write(channels, writer);
            logger.info("Wrote "+channels.size()+" VictorOps alert channels");
        }
        finally
        {
            closeStream(stream);
            closeWriter(writer);
        }
    }

    /**
     * Reads User alert channels from an import file with the given name.
     * Closes the stream after reading the file.
     * @param filename The name of the file to import
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An input stream for the file
     * @return The set of User alert channels read from the import file
     * @throws IOException if there is an error reading the import file
     */
    public List<UserChannel> readUserChannels(String filename, String worksheet, InputStream stream)
        throws IOException
    {
        List<UserChannel> ret = null;

        try
        {
            logger.info("Loading User alert channel file: "+filename+(worksheet != null ? "/"+worksheet : ""));
            ret = UserChannelParser.parse(getReader(filename, worksheet, stream));
            logger.info("Read "+ret.size()+" User alert channels");
        }
        finally
        {
            closeStream(stream);
        }

        return ret;
    }

    /**
     * Writes User alert channels to an export file with the given name.
     * Closes the stream after writing the file.
     * @param channels The list of User alert channels to be exported
     * @param filename The name of the file to export to
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An output stream for the file
     * @param workbook For XLS and XLSX files, the workbook to append the worksheet to (or null to create a new workbook)
     * @throws IOException if there is an error reading the import file
     */
    public void writeUserChannels(List<UserChannel> channels, String filename, String worksheet, 
        OutputStream stream, Workbook workbook)
        throws IOException
    {
        OutputFileWriter writer = null;

        try
        {
            logger.info("Writing User alert channel file: "+filename+(worksheet != null ? "/"+worksheet : ""));
            writer = getWriter(filename, worksheet, workbook, stream);
            UserChannelRenderer.write(channels, writer);
            logger.info("Wrote "+channels.size()+" User alert channels");
        }
        finally
        {
            closeStream(stream);
            closeWriter(writer);
        }
    }

    /**
     * Reads xMatters alert channels from an import file with the given name.
     * Closes the stream after reading the file.
     * @param filename The name of the file to import
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An input stream for the file
     * @return The set of xMatters alert channels read from the import file
     * @throws IOException if there is an error reading the import file
     */
    public List<xMattersChannel> readxMattersChannels(String filename, String worksheet, InputStream stream)
        throws IOException
    {
        List<xMattersChannel> ret = null;

        try
        {
            logger.info("Loading xMatters alert channel file: "+filename+(worksheet != null ? "/"+worksheet : ""));
            ret = xMattersChannelParser.parse(getReader(filename, worksheet, stream));
            logger.info("Read "+ret.size()+" xMatters alert channels");
        }
        finally
        {
            closeStream(stream);
        }

        return ret;
    }

    /**
     * Writes xMatters alert channels to an export file with the given name.
     * Closes the stream after writing the file.
     * @param channels The list of xMatters alert channels to be exported
     * @param filename The name of the file to export to
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An output stream for the file
     * @param workbook For XLS and XLSX files, the workbook to append the worksheet to (or null to create a new workbook)
     * @throws IOException if there is an error reading the import file
     */
    public void writexMattersChannels(List<xMattersChannel> channels, String filename, String worksheet, 
        OutputStream stream, Workbook workbook)
        throws IOException
    {
        OutputFileWriter writer = null;

        try
        {
            logger.info("Writing xMatters alert channel file: "+filename+(worksheet != null ? "/"+worksheet : ""));
            writer = getWriter(filename, worksheet, workbook, stream);
            xMattersChannelRenderer.write(channels, writer);
            logger.info("Wrote "+channels.size()+" xMatters alert channels");
        }
        finally
        {
            closeStream(stream);
            closeWriter(writer);
        }
    }

    /**
     * Throws an exception if the given alert condition does not have an id.
     * @param condition The alert condition to check
     * @throws IllegalArgumentException if the policy id of the condition is null or empty
     */
    private void checkPolicyId(BaseCondition condition)
    {
        if(condition.getPolicyId() == null || condition.getPolicyId() == 0L)
            throw new IllegalArgumentException("condition has missing policyId: "+condition.getName());
    }

    /**
     * Returns the alert conditions for the given policies.
     * @param policies The alert policies for the alert conditions
     * @return The alert conditions for the given policies
     */
    public List<AlertCondition> getAlertConditions(List<AlertPolicy> policies)
    {
        checkInitialize();

        List<AlertCondition> ret = new ArrayList<AlertCondition>();
        for(AlertPolicy policy : policies)
        {
            // Get the alert conditions
            logger.info("Getting the alert conditions for policy: "+policy.getId());
            Collection<AlertCondition> conditions = apiClient.alertConditions().list(policy.getId());
            logger.info("Got "+conditions.size()+" alert conditions for policy: "+policy.getId());

            // Set the policyId and add the condition to the list
            for(AlertCondition condition : conditions)
            {
                condition.setPolicyId(policy.getId());
                ret.add(condition);
            }
        }

        return ret;
    }

    /**
     * Creates the given alert conditions.
     * @param conditions The alert conditions to create
     * @return The created alert conditions
     */
    public List<AlertCondition> createAlertConditions(List<AlertCondition> conditions)
    {
        if(conditions == null)
            throw new IllegalArgumentException("null conditions");

        checkInitialize();

        // Create the conditions
        List<AlertCondition> ret = new ArrayList<AlertCondition>();
        logger.info("Creating "+conditions.size()+" alert conditions");
        for(AlertCondition condition : conditions)
            ret.add(createAlertCondition(condition));

        return ret;
    }

    /**
     * Create the given alert condition.
     * @param condition The alert condition to create
     * @return The created alert condition
     */
    public AlertCondition createAlertCondition(AlertCondition condition)
    {
        checkInitialize();

        // Create the condition
        logger.info("Creating alert condition: "+condition.getName());
        checkPolicyId(condition);
        condition = apiClient.alertConditions().create(condition.getPolicyId(), condition).get();
        logger.info("Created alert condition: "+condition.getId()+" - "+condition.getName());

        return condition;
    }

    /**
     * Delete the given alert conditions.
     * @param conditions The alert conditions to delete
     * @return The deleted alert conditions
     */
    public List<AlertCondition> deleteAlertConditions(List<AlertCondition> conditions)
    {
        if(conditions == null)
            throw new IllegalArgumentException("null conditions");

        checkInitialize();

        // Delete the conditions
        List<AlertCondition> ret = new ArrayList<AlertCondition>();
        for(AlertCondition condition : conditions)
        {
            checkPolicyId(condition);
            deleteAlertConditions(condition.getPolicyId(), condition.getName());
            ret.add(condition);
        }

        return ret;
    }

    /**
     * Delete the given alert condition.
     * @param condition The alert condition to delete
     * @return The deleted alert condition
     */
    public AlertCondition deleteAlertCondition(AlertCondition condition)
    {
        checkInitialize();

        // Delete the condition
        checkPolicyId(condition);
        deleteAlertConditions(condition.getPolicyId(), condition.getName());

        return condition;
    }

    /**
     * Delete the alert conditions with the given name.
     * @param policyId The id of the policy to delete the alert conditions from
     * @param name The name of the alert conditions
     */
    private void deleteAlertConditions(long policyId, String name)
    {
        Collection<AlertCondition> conditions = apiClient.alertConditions().list(policyId, name);
        for(AlertCondition condition : conditions)
        {
            logger.info("Deleting alert condition: "+condition.getId());
            apiClient.alertConditions().delete(condition.getId());
            logger.info("Deleted alert condition : "+condition.getId()+" - "+condition.getName());
        }
    }

    /**
     * Reads alert conditions from an import file with the given name.
     * Closes the stream after reading the file.
     * @param policies The list of policies for the alert conditions
     * @param entities The list of entities for the alert conditions
     * @param filename The name of the file to import
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An input stream for the file
     * @return The set of alert conditions read from the import file
     * @throws IOException if there is an error reading the import file
     */
    public List<AlertCondition> readAlertConditions(List<AlertPolicy> policies, List<Entity> entities, 
        String filename, String worksheet, InputStream stream)
        throws IOException
    {
        List<AlertCondition> ret = null;

        try
        {
            logger.info("Loading alert condition file: "+filename+(worksheet != null ? "/"+worksheet : ""));
            ret = AlertConditionParser.parse(policies, entities, getReader(filename, worksheet, stream));
            logger.info("Read "+ret.size()+" alert conditions");
        }
        finally
        {
            closeStream(stream);
        }

        return ret;
    }

    /**
     * Writes alert conditions to an export file with the given name.
     * Closes the stream after writing the file.
     * @param policies The list of policies for the alert conditions
     * @param entities The list of entities for the alert conditions
     * @param conditions The list of alert conditions to be exported
     * @param filename The name of the file to export to
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An output stream for the file
     * @param workbook For XLS and XLSX files, the workbook to append the worksheet to (or null to create a new workbook)
     * @throws IOException if there is an error reading the import file
     */
    public void writeAlertConditions(List<AlertPolicy> policies, List<Entity> entities, List<AlertCondition> conditions,
        String filename, String worksheet, OutputStream stream, Workbook workbook)
        throws IOException
    {
        OutputFileWriter writer = null;

        try
        {
            logger.info("Writing alert condition file: "+filename+(worksheet != null ? "/"+worksheet : ""));
            writer = getWriter(filename, worksheet, workbook, stream);
            AlertConditionRenderer.write(policies, entities, conditions, writer);
            logger.info("Wrote "+conditions.size()+" alert conditions");
        }
        finally
        {
            closeStream(stream);
            closeWriter(writer);
        }
    }

    /**
     * Creates the given external service alert conditions.
     * @param conditions The external service alert conditions to create
     * @return The created external service alert conditions
     */
    public List<ExternalServiceAlertCondition> createExternalServiceAlertConditions(List<ExternalServiceAlertCondition> conditions)
    {
        if(conditions == null)
            throw new IllegalArgumentException("null conditions");

        checkInitialize();

        // Create the conditions
        List<ExternalServiceAlertCondition> ret = new ArrayList<ExternalServiceAlertCondition>();
        logger.info("Creating "+conditions.size()+" external service alert conditions");
        for(ExternalServiceAlertCondition condition : conditions)
            ret.add(createExternalServiceAlertCondition(condition));

        return ret;
    }

    /**
     * Create the external service given alert condition.
     * @param condition The external service alert condition to create
     * @return The created external service alert condition
     */
    public ExternalServiceAlertCondition createExternalServiceAlertCondition(ExternalServiceAlertCondition condition)
    {
        checkInitialize();

        // Create the condition
        logger.info("Creating external service alert condition: "+condition.getName());
        checkPolicyId(condition);
        condition = apiClient.externalServiceAlertConditions().create(condition.getPolicyId(), condition).get();
        logger.info("Created external service alert condition: "+condition.getId()+" - "+condition.getName());

        return condition;
    }

    /**
     * Delete the given external service alert conditions.
     * @param conditions The external service alert conditions to delete
     * @return The deleted external service alert conditions
     */
    public List<ExternalServiceAlertCondition> deleteExternalServiceAlertConditions(List<ExternalServiceAlertCondition> conditions)
    {
        if(conditions == null)
            throw new IllegalArgumentException("null conditions");

        checkInitialize();

        // Delete the conditions
        List<ExternalServiceAlertCondition> ret = new ArrayList<ExternalServiceAlertCondition>();
        for(ExternalServiceAlertCondition condition : conditions)
        {
            checkPolicyId(condition);
            deleteExternalServiceAlertConditions(condition.getPolicyId(), condition.getName());
            ret.add(condition);
        }

        return ret;
    }

    /**
     * Delete the given external service alert condition.
     * @param condition The external service alert condition to delete
     * @return The deleted external service alert condition
     */
    public ExternalServiceAlertCondition deleteExternalServiceAlertCondition(ExternalServiceAlertCondition condition)
    {
        checkInitialize();

        // Delete the condition
        checkPolicyId(condition);
        deleteExternalServiceAlertConditions(condition.getPolicyId(), condition.getName());

        return condition;
    }

    /**
     * Delete the external service alert conditions with the given name.
     * @param policyId The id of the policy to delete the external service alert conditions from
     * @param name The name of the external service alert conditions
     */
    private void deleteExternalServiceAlertConditions(long policyId, String name)
    {
        Collection<ExternalServiceAlertCondition> conditions = apiClient.externalServiceAlertConditions().list(policyId, name);
        for(ExternalServiceAlertCondition condition : conditions)
        {
            logger.info("Deleting external service alert condition: "+condition.getId());
            apiClient.externalServiceAlertConditions().delete(condition.getId());
            logger.info("Deleted external service alert condition : "+condition.getId()+" - "+condition.getName());
        }
    }

    /**
     * Creates the given NRQL alert conditions.
     * @param conditions The NRQL alert conditions to create
     * @return The created NRQL alert conditions
     */
    public List<NrqlAlertCondition> createNrqlAlertConditions(List<NrqlAlertCondition> conditions)
    {
        if(conditions == null)
            throw new IllegalArgumentException("null conditions");

        checkInitialize();

        // Create the conditions
        List<NrqlAlertCondition> ret = new ArrayList<NrqlAlertCondition>();
        logger.info("Creating "+conditions.size()+" NRQL alert conditions");
        for(NrqlAlertCondition condition : conditions)
            ret.add(createNrqlAlertCondition(condition));

        return ret;
    }

    /**
     * Create the NRQL given alert condition.
     * @param condition The NRQL alert condition to create
     * @return The created NRQL alert condition
     */
    public NrqlAlertCondition createNrqlAlertCondition(NrqlAlertCondition condition)
    {
        checkInitialize();

        // Create the condition
        logger.info("Creating NRQL alert condition: "+condition.getName());
        checkPolicyId(condition);
        condition = apiClient.nrqlAlertConditions().create(condition.getPolicyId(), condition).get();
        logger.info("Created NRQL alert condition: "+condition.getId()+" - "+condition.getName());

        return condition;
    }

    /**
     * Delete the given NRQL alert conditions.
     * @param conditions The NRQL alert conditions to delete
     * @return The deleted NRQL alert conditions
     */
    public List<NrqlAlertCondition> deleteNrqlAlertConditions(List<NrqlAlertCondition> conditions)
    {
        if(conditions == null)
            throw new IllegalArgumentException("null conditions");

        checkInitialize();

        // Delete the conditions
        List<NrqlAlertCondition> ret = new ArrayList<NrqlAlertCondition>();
        for(NrqlAlertCondition condition : conditions)
        {
            checkPolicyId(condition);
            deleteNrqlAlertConditions(condition.getPolicyId(), condition.getName());
            ret.add(condition);
        }

        return ret;
    }

    /**
     * Delete the given NRQL alert condition.
     * @param condition The NRQL alert condition to delete
     * @return The deleted NRQL alert condition
     */
    public NrqlAlertCondition deleteNrqlAlertCondition(NrqlAlertCondition condition)
    {
        checkInitialize();

        // Delete the condition
        checkPolicyId(condition);
        deleteNrqlAlertConditions(condition.getPolicyId(), condition.getName());

        return condition;
    }

    /**
     * Delete the NRQL alert conditions with the given name.
     * @param policyId The id of the policy to delete the NRQL alert conditions from
     * @param name The name of the NRQL alert conditions
     */
    private void deleteNrqlAlertConditions(long policyId, String name)
    {
        Collection<NrqlAlertCondition> conditions = apiClient.nrqlAlertConditions().list(policyId, name);
        for(NrqlAlertCondition condition : conditions)
        {
            logger.info("Deleting NRQL alert condition: "+condition.getId());
            apiClient.nrqlAlertConditions().delete(condition.getId());
            logger.info("Deleted i alert condition : "+condition.getId()+" - "+condition.getName());
        }
    }

    /**
     * Creates the given infrastructure alert conditions.
     * @param conditions The infrastructure alert conditions to create
     * @return The created infrastructure alert conditions
     */
    public List<InfraAlertCondition> createInfraAlertConditions(List<InfraAlertCondition> conditions)
    {
        if(conditions == null)
            throw new IllegalArgumentException("null conditions");

        checkInitialize();

        // Create the conditions
        List<InfraAlertCondition> ret = new ArrayList<InfraAlertCondition>();
        logger.info("Creating "+conditions.size()+" infra alert conditions");
        for(InfraAlertCondition condition : conditions)
            ret.add(createInfraAlertCondition(condition));

        return ret;
    }

    /**
     * Create the infrastructure given alert condition.
     * @param condition The infrastructure alert condition to create
     * @return The created infrastructure alert condition
     */
    public InfraAlertCondition createInfraAlertCondition(InfraAlertCondition condition)
    {
        checkInitialize();

        // Create the condition
        logger.info("Creating infra alert condition: "+condition.getName());
        checkPolicyId(condition);
        condition = infraApiClient.infraAlertConditions().create(condition).get();
        logger.info("Created infra alert condition: "+condition.getId()+" - "+condition.getName());

        return condition;
    }

    /**
     * Delete the given infra alert conditions.
     * @param conditions The infra alert conditions to delete
     * @return The deleted infra alert conditions
     */
    public List<InfraAlertCondition> deleteInfraAlertConditions(List<InfraAlertCondition> conditions)
    {
        if(conditions == null)
            throw new IllegalArgumentException("null conditions");

        checkInitialize();

        // Delete the conditions
        List<InfraAlertCondition> ret = new ArrayList<InfraAlertCondition>();
        for(InfraAlertCondition condition : conditions)
        {
            checkPolicyId(condition);
            deleteInfraAlertConditions(condition.getPolicyId(), condition.getName());
            ret.add(condition);
        }

        return ret;
    }

    /**
     * Delete the given infrastructure alert condition.
     * @param condition The infrastructure alert condition to delete
     * @return The deleted infrastructure alert condition
     */
    public InfraAlertCondition deleteInfraAlertCondition(InfraAlertCondition condition)
    {
        checkInitialize();

        // Delete the condition
        checkPolicyId(condition);
        deleteInfraAlertConditions(condition.getPolicyId(), condition.getName());

        return condition;
    }

    /**
     * Delete the infrastructure alert conditions with the given name.
     * @param policyId The id of the policy to delete the infrastructure alert conditions from
     * @param name The name of the infrastructure alert conditions
     */
    private void deleteInfraAlertConditions(long policyId, String name)
    {
        Collection<InfraAlertCondition> conditions = infraApiClient.infraAlertConditions().list(policyId, name);
        for(InfraAlertCondition condition : conditions)
        {
            logger.info("Deleting infra alert condition: "+condition.getId());
            infraApiClient.infraAlertConditions().delete(condition.getId());
            logger.info("Deleted infra alert condition : "+condition.getId()+" - "+condition.getName());
        }
    }

    /**
     * Returns the applications.
     * @return The applications
     */
    public List<Application> getApplications()
    {
        checkInitialize();

        // Get the applications
        logger.info("Getting the applications");
        Collection<Application> applications = apiClient.applications().list();
        logger.info("Got "+applications.size()+" applications");
        return toList(applications);
   }

    /**
     * Returns the servers.
     * @return The servers
     */
    public List<Server> getServers()
    {
        checkInitialize();

        // Get the servers
        logger.info("Getting the servers");
        Collection<Server> servers = apiClient.servers().list();
        logger.info("Got "+servers.size()+" servers");
        return toList(servers);
    }

    /**
     * Converts the given collection to a list.
     * @param <T> The type of the collection
     * @param collection The collection
     * @return The list
     */
    public <T> List<T> toList(Collection<T> collection)
    {
        List<T> ret = new ArrayList<T>();
        ret.addAll(collection);
        return ret;
    }
}