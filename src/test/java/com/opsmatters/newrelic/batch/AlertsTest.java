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

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import org.junit.Test;
import junit.framework.Assert;
import com.opsmatters.newrelic.api.Constants;
import com.opsmatters.newrelic.api.model.alerts.policies.AlertPolicy;
import com.opsmatters.newrelic.api.model.alerts.channels.AlertChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.EmailChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.SlackChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.HipChatChannel;
import com.opsmatters.newrelic.batch.parsers.AlertPolicyParser;
import com.opsmatters.newrelic.batch.parsers.EmailChannelParser;
import com.opsmatters.newrelic.batch.parsers.SlackChannelParser;
import com.opsmatters.newrelic.batch.parsers.HipChatChannelParser;
import com.opsmatters.newrelic.batch.renderers.AlertPolicyRenderer;
import com.opsmatters.newrelic.batch.renderers.EmailChannelRenderer;
import com.opsmatters.newrelic.batch.renderers.SlackChannelRenderer;
import com.opsmatters.newrelic.batch.renderers.HipChatChannelRenderer;
import com.opsmatters.newrelic.batch.model.AlertConfiguration;
import com.opsmatters.core.reports.InputFileReader;
import com.opsmatters.core.reports.OutputFileWriter;
import com.opsmatters.core.reports.Workbook;

/**
 * The set of tests used for importing and exporting alert channels, policies and conditions.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class AlertsTest
{
    private static final Logger logger = Logger.getLogger(AlertsTest.class.getName());

    // Get the properties
    private String apiKey = System.getProperty(Constants.API_KEY_PROPERTY);

    private static final String INPUT_PATH = "target/test-classes/";
    private static final String INPUT_FILENAME = "test-alerts.xlsx";
    private static final String OUTPUT_PATH = "target/";
    private static final String OUTPUT_FILENAME = "test-alerts-new.xlsx";
    private static final String ALERT_POLICY_TAB = "alert policies";
    private static final String EMAIL_CHANNEL_TAB = "email channels";
    private static final String SLACK_CHANNEL_TAB = "slack channels";
    private static final String HIPCHAT_CHANNEL_TAB = "hipchat channels";

    @Test
    public void testNewRelicAlerts()
    {
        String testName = "NewRelicAlertTest";
        logger.info("Starting test: "+testName);

        AlertConfiguration config = new AlertConfiguration();

        // Read the alert configuration
        readEmailChannels(config);
        readSlackChannels(config);
        readHipChatChannels(config);
        readAlertPolicies(config);

        List<AlertChannel> channels = config.getAlertChannels();
        List<AlertPolicy> policies = config.getAlertPolicies();
        Assert.assertTrue(config.numAlertChannels() > 0);
        Assert.assertTrue(config.numAlertPolicies() > 0);

        AlertManager manager = new AlertManager(apiKey);

        // Delete the existing alert configuration
        List<AlertChannel> deletedChannels = manager.deleteAlertChannels(config.getAlertChannels());
        List<AlertPolicy> deletedPolicies = manager.deleteAlertPolicies(config.getAlertPolicies());
        Assert.assertTrue(deletedChannels.size() == channels.size());
        Assert.assertTrue(deletedPolicies.size() == policies.size());

        // Create the new alert configuration
        List<AlertChannel> createdChannels = manager.createAlertChannels(config.getAlertChannels());
        List<AlertPolicy> createdPolicies = manager.createAlertPolicies(config.getAlertPolicies());
        Assert.assertTrue(createdChannels.size() == channels.size());
        Assert.assertTrue(createdPolicies.size() == policies.size());

        // Write the alert configuration
        writeEmailChannels(config);
        writeSlackChannels(config);
        writeHipChatChannels(config);
        writeAlertPolicies(config);

        logger.info("Completed test: "+testName);
    }

    public Workbook getOutputWorkbook() throws IOException
    {
        return Workbook.getWorkbook(new File(OUTPUT_PATH, OUTPUT_FILENAME));
    }

    public void readEmailChannels(AlertConfiguration config)
    {
        // Read the email alert channel file
        logger.info("Loading email alert channel file: "+INPUT_PATH+INPUT_FILENAME+"/"+EMAIL_CHANNEL_TAB);
        InputStream is = null;
        try
        {
            is = new FileInputStream(INPUT_PATH+INPUT_FILENAME);
            InputFileReader reader = InputFileReader.builder()
                .name(INPUT_FILENAME)
                .worksheet(EMAIL_CHANNEL_TAB)
                .withInputStream(is)
                .build();

            List<EmailChannel> channels = EmailChannelParser.parse(reader);
            logger.info("Read "+channels.size()+" email alert channels");
            config.addAlertChannels(channels);
        }
        catch(FileNotFoundException e)
        {
            logger.severe("Unable to find email alert channel file: "+e.getClass().getName()+": "+e.getMessage());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(is != null)
                    is.close();
            }
            catch(IOException e)
            {
            }
        }
    }

    public void writeEmailChannels(AlertConfiguration config)
    {
        List<EmailChannel> channels = config.getEmailChannels();

        // Write the new channels to a new filename
        logger.info("Writing email alert channel file: "+OUTPUT_PATH+OUTPUT_FILENAME+"/"+EMAIL_CHANNEL_TAB);
        OutputStream os = null;
        OutputFileWriter writer = null;
        try
        {
            os = new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME);
            writer = OutputFileWriter.builder()
                .name(OUTPUT_FILENAME)
                .worksheet(EMAIL_CHANNEL_TAB)
                .withOutputStream(os)
                .build();

            EmailChannelRenderer.write(channels, writer);
            logger.info("Wrote "+channels.size()+" email alert channels");
        }
        catch(IOException e)
        {
            logger.severe("Unable to write email alert channel file: "+e.getClass().getName()+": "+e.getMessage());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(os != null)
                    os.close();
                if(writer != null)
                    writer.close();
            }
            catch(IOException e)
            {
            }
        }
    }

    public void readSlackChannels(AlertConfiguration config)
    {
        // Read the Slack alert channel file
        logger.info("Loading Slack alert channel file: "+INPUT_PATH+INPUT_FILENAME+"/"+SLACK_CHANNEL_TAB);
        InputStream is = null;
        try
        {
            is = new FileInputStream(INPUT_PATH+INPUT_FILENAME);
            InputFileReader reader = InputFileReader.builder()
                .name(INPUT_FILENAME)
                .worksheet(SLACK_CHANNEL_TAB)
                .withInputStream(is)
                .build();

            List<SlackChannel> channels = SlackChannelParser.parse(reader);
            logger.info("Read "+channels.size()+" Slack alert channels");
            config.addAlertChannels(channels);
        }
        catch(FileNotFoundException e)
        {
            logger.severe("Unable to find Slack alert channel file: "+e.getClass().getName()+": "+e.getMessage());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(is != null)
                    is.close();
            }
            catch(IOException e)
            {
            }
        }
    }

    public void writeSlackChannels(AlertConfiguration config)
    {
        List<SlackChannel> channels = config.getSlackChannels();

        // Write the new channels to a new filename
        logger.info("Writing Slack alert channel file: "+OUTPUT_PATH+OUTPUT_FILENAME+"/"+SLACK_CHANNEL_TAB);
        OutputStream os = null;
        OutputFileWriter writer = null;
        try
        {
            Workbook workbook = getOutputWorkbook();
            os = new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME);
            writer = OutputFileWriter.builder()
                .name(OUTPUT_FILENAME)
                .worksheet(SLACK_CHANNEL_TAB)
                .withOutputStream(os)
                .withWorkbook(workbook)
                .build();

            SlackChannelRenderer.write(channels, writer);
            logger.info("Wrote "+channels.size()+" Slack alert channels");
        }
        catch(IOException e)
        {
            logger.severe("Unable to write Slack alert channel file: "+e.getClass().getName()+": "+e.getMessage());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(os != null)
                    os.close();
                if(writer != null)
                    writer.close();
            }
            catch(IOException e)
            {
            }
        }
    }

    public void readHipChatChannels(AlertConfiguration config)
    {
        // Read the HipChat alert channel file
        logger.info("Loading HipChat alert channel file: "+INPUT_PATH+INPUT_FILENAME+"/"+HIPCHAT_CHANNEL_TAB);
        InputStream is = null;
        try
        {
            is = new FileInputStream(INPUT_PATH+INPUT_FILENAME);
            InputFileReader reader = InputFileReader.builder()
                .name(INPUT_FILENAME)
                .worksheet(HIPCHAT_CHANNEL_TAB)
                .withInputStream(is)
                .build();

            List<HipChatChannel> channels = HipChatChannelParser.parse(reader);
            logger.info("Read "+channels.size()+" HipChat alert channels");
            config.addAlertChannels(channels);
        }
        catch(FileNotFoundException e)
        {
            logger.severe("Unable to find HipChat alert channel file: "+e.getClass().getName()+": "+e.getMessage());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(is != null)
                    is.close();
            }
            catch(IOException e)
            {
            }
        }
    }

    public void writeHipChatChannels(AlertConfiguration config)
    {
        List<HipChatChannel> channels = config.getHipChatChannels();

        // Write the new channels to a new filename
        logger.info("Writing HipChat alert channel file: "+OUTPUT_PATH+OUTPUT_FILENAME+"/"+HIPCHAT_CHANNEL_TAB);
        OutputStream os = null;
        OutputFileWriter writer = null;
        try
        {
            Workbook workbook = getOutputWorkbook();
            os = new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME);
            writer = OutputFileWriter.builder()
                .name(OUTPUT_FILENAME)
                .worksheet(HIPCHAT_CHANNEL_TAB)
                .withOutputStream(os)
                .withWorkbook(workbook)
                .build();

            HipChatChannelRenderer.write(channels, writer);
            logger.info("Wrote "+channels.size()+" HipChat alert channels");
        }
        catch(IOException e)
        {
            logger.severe("Unable to write HipChat alert channel file: "+e.getClass().getName()+": "+e.getMessage());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(os != null)
                    os.close();
                if(writer != null)
                    writer.close();
            }
            catch(IOException e)
            {
            }
        }
    }

    public void readAlertPolicies(AlertConfiguration config)
    {
        // Read the alert policy file
        logger.info("Loading alert policy file: "+INPUT_PATH+INPUT_FILENAME+"/"+ALERT_POLICY_TAB);
        InputStream is = null;
        try
        {
            is = new FileInputStream(INPUT_PATH+INPUT_FILENAME);
            InputFileReader reader = InputFileReader.builder()
                .name(INPUT_FILENAME)
                .worksheet(ALERT_POLICY_TAB)
                .withInputStream(is)
                .build();

            List<AlertPolicy> policies = AlertPolicyParser.parse(reader);
            logger.info("Read "+policies.size()+" alert policies");
            config.setAlertPolicies(policies);
        }
        catch(FileNotFoundException e)
        {
            logger.severe("Unable to find alert policy file: "+e.getClass().getName()+": "+e.getMessage());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(is != null)
                    is.close();
            }
            catch(IOException e)
            {
            }
        }
    }

    public void writeAlertPolicies(AlertConfiguration config)
    {
        List<AlertPolicy> policies = config.getAlertPolicies();

        // Write the new policies to a new filename
        logger.info("Writing alert policy file: "+OUTPUT_PATH+OUTPUT_FILENAME+"/"+ALERT_POLICY_TAB);
        OutputStream os = null;
        OutputFileWriter writer = null;
        try
        {
            Workbook workbook = getOutputWorkbook();
            os = new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME);
            writer = OutputFileWriter.builder()
                .name(OUTPUT_FILENAME)
                .worksheet(ALERT_POLICY_TAB)
                .withOutputStream(os)
                .withWorkbook(workbook)
                .build();

            AlertPolicyRenderer.write(policies, writer);
            logger.info("Wrote "+policies.size()+" alert policies");
        }
        catch(IOException e)
        {
            logger.severe("Unable to write alert policy file: "+e.getClass().getName()+": "+e.getMessage());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(os != null)
                    os.close();
                if(writer != null)
                    writer.close();
            }
            catch(IOException e)
            {
            }
        }
    }
}