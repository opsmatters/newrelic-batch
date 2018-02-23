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
import java.util.ArrayList;
import java.util.logging.Logger;
import org.junit.Test;
import junit.framework.Assert;
import com.opsmatters.newrelic.api.Constants;
import com.opsmatters.newrelic.api.model.alerts.policies.AlertPolicy;
import com.opsmatters.newrelic.api.model.alerts.channels.AlertChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.EmailChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.SlackChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.HipChatChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.CampfireChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.OpsGenieChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.PagerDutyChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.VictorOpsChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.xMattersChannel;
import com.opsmatters.newrelic.api.model.alerts.conditions.AlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.ExternalServiceAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.NrqlAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.InfraAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.InfraMetricAlertCondition;
import com.opsmatters.newrelic.api.model.Entity;
import com.opsmatters.newrelic.batch.parsers.AlertPolicyParser;
import com.opsmatters.newrelic.batch.parsers.EmailChannelParser;
import com.opsmatters.newrelic.batch.parsers.SlackChannelParser;
import com.opsmatters.newrelic.batch.parsers.HipChatChannelParser;
import com.opsmatters.newrelic.batch.parsers.CampfireChannelParser;
import com.opsmatters.newrelic.batch.parsers.OpsGenieChannelParser;
import com.opsmatters.newrelic.batch.parsers.PagerDutyChannelParser;
import com.opsmatters.newrelic.batch.parsers.VictorOpsChannelParser;
import com.opsmatters.newrelic.batch.parsers.xMattersChannelParser;
import com.opsmatters.newrelic.batch.parsers.AlertConditionParser;
import com.opsmatters.newrelic.batch.parsers.ExternalServiceAlertConditionParser;
import com.opsmatters.newrelic.batch.parsers.NrqlAlertConditionParser;
import com.opsmatters.newrelic.batch.parsers.InfraMetricAlertConditionParser;
import com.opsmatters.newrelic.batch.renderers.AlertPolicyRenderer;
import com.opsmatters.newrelic.batch.renderers.EmailChannelRenderer;
import com.opsmatters.newrelic.batch.renderers.SlackChannelRenderer;
import com.opsmatters.newrelic.batch.renderers.HipChatChannelRenderer;
import com.opsmatters.newrelic.batch.renderers.CampfireChannelRenderer;
import com.opsmatters.newrelic.batch.renderers.OpsGenieChannelRenderer;
import com.opsmatters.newrelic.batch.renderers.PagerDutyChannelRenderer;
import com.opsmatters.newrelic.batch.renderers.VictorOpsChannelRenderer;
import com.opsmatters.newrelic.batch.renderers.xMattersChannelRenderer;
import com.opsmatters.newrelic.batch.renderers.AlertConditionRenderer;
import com.opsmatters.newrelic.batch.renderers.ExternalServiceAlertConditionRenderer;
import com.opsmatters.newrelic.batch.renderers.NrqlAlertConditionRenderer;
import com.opsmatters.newrelic.batch.renderers.InfraMetricAlertConditionRenderer;
import com.opsmatters.newrelic.batch.model.AlertConfiguration;
import com.opsmatters.core.documents.InputFileReader;
import com.opsmatters.core.documents.OutputFileWriter;
import com.opsmatters.core.documents.Workbook;

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
    private static final String CAMPFIRE_CHANNEL_TAB = "campfire channels";
    private static final String OPSGENIE_CHANNEL_TAB = "opsgenie channels";
    private static final String PAGERDUTY_CHANNEL_TAB = "pagerduty channels";
    private static final String VICTOROPS_CHANNEL_TAB = "victorops channels";
    private static final String XMATTERS_CHANNEL_TAB = "xmatters channels";
    private static final String ALERT_CONDITION_TAB = "alert conditions";
    private static final String EXTERNAL_SERVICE_CONDITION_TAB = "external service conditions";
    private static final String NRQL_CONDITION_TAB = "nrql conditions";
    private static final String INFRA_METRIC_CONDITION_TAB = "infra metric conditions";

    @Test
    public void testNewRelicAlerts()
    {
        String testName = "NewRelicAlertTest";
        logger.info("Starting test: "+testName);

        AlertConfiguration config = new AlertConfiguration();
        AlertManager manager = new AlertManager(apiKey);

        // Read the alert channels
        readEmailChannels(config);
        readSlackChannels(config);
        readHipChatChannels(config);
        readCampfireChannels(config);
        readOpsGenieChannels(config);
        readPagerDutyChannels(config);
        readVictorOpsChannels(config);
        //readxMattersChannels(config);

        List<AlertChannel> channels = config.getAlertChannels();
        Assert.assertTrue(config.numAlertChannels() > 0);

        // Read the alert policies
        readAlertPolicies(config);

        List<AlertPolicy> policies = config.getAlertPolicies();
        Assert.assertTrue(config.numAlertPolicies() > 0);

        // Delete the existing alert channels
        List<AlertChannel> deletedChannels = manager.deleteAlertChannels(config.getAlertChannels());
        Assert.assertTrue(deletedChannels.size() == channels.size());

        // Delete the existing alert policies
        List<AlertPolicy> deletedPolicies = manager.deleteAlertPolicies(config.getAlertPolicies());
        Assert.assertTrue(deletedPolicies.size() == policies.size());

        // Create the new alert channels
        List<AlertChannel> createdChannels = manager.createAlertChannels(config.getAlertChannels());
        Assert.assertTrue(createdChannels.size() == channels.size());

        // Create the new alert policies
        List<AlertPolicy> createdPolicies = manager.createAlertPolicies(config.getAlertPolicies());
        Assert.assertTrue(createdPolicies.size() == policies.size());

        // Get the lists of policies and entities
        List<AlertPolicy> allPolicies = manager.getAlertPolicies();
        List<Entity> entities = new ArrayList<Entity>();
        entities.addAll(manager.getApplications());
        entities.addAll(manager.getServers());

        // Read the alert conditions
        readAlertConditions(allPolicies, entities, config);
        readExternalServiceAlertConditions(allPolicies, entities, config);
        readNrqlAlertConditions(allPolicies, config);
        readInfraMetricAlertConditions(allPolicies, config);

        // Get the alert conditions
        List<AlertCondition> conditions = config.getAlertConditions();
        Assert.assertTrue(config.numAlertConditions() > 0);
        List<ExternalServiceAlertCondition> externalServiceConditions = config.getExternalServiceAlertConditions();
        Assert.assertTrue(config.numExternalServiceAlertConditions() > 0);
        List<NrqlAlertCondition> nrqlConditions = config.getNrqlAlertConditions();
        Assert.assertTrue(config.numNrqlAlertConditions() > 0);
        List<InfraAlertCondition> infraConditions = config.getInfraAlertConditions();
        Assert.assertTrue(config.numInfraAlertConditions() > 0);

        // Create the new alert conditions
        List<AlertCondition> createdConditions = manager.createAlertConditions(config.getAlertConditions());
        Assert.assertTrue(createdConditions.size() == conditions.size());
        List<ExternalServiceAlertCondition> createdExternalServiceConditions = manager.createExternalServiceAlertConditions(config.getExternalServiceAlertConditions());
        Assert.assertTrue(createdExternalServiceConditions.size() == externalServiceConditions.size());
        List<NrqlAlertCondition> createdNrqlConditions = manager.createNrqlAlertConditions(config.getNrqlAlertConditions());
        Assert.assertTrue(createdNrqlConditions.size() == nrqlConditions.size());
        List<InfraAlertCondition> createdInfraConditions = manager.createInfraAlertConditions(config.getInfraAlertConditions());
        Assert.assertTrue(createdInfraConditions.size() == infraConditions.size());

        // Write the alert configuration
        writeEmailChannels(config);
        writeSlackChannels(config);
        writeHipChatChannels(config);
        writeCampfireChannels(config);
        writeOpsGenieChannels(config);
        writePagerDutyChannels(config);
        writeVictorOpsChannels(config);
        //writexMattersChannels(config);
        writeAlertPolicies(config);
        writeAlertConditions(allPolicies, config);
        writeExternalServiceAlertConditions(allPolicies, config);
        writeNrqlAlertConditions(allPolicies, config);
        writeInfraMetricAlertConditions(allPolicies, config);

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

        // Write the new channels to a new tab
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

        // Write the new channels to a new tab
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

        // Write the new channels to a new tab
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

    public void readCampfireChannels(AlertConfiguration config)
    {
        // Read the Campfire alert channel file
        logger.info("Loading Campfire alert channel file: "+INPUT_PATH+INPUT_FILENAME+"/"+CAMPFIRE_CHANNEL_TAB);
        InputStream is = null;
        try
        {
            is = new FileInputStream(INPUT_PATH+INPUT_FILENAME);
            InputFileReader reader = InputFileReader.builder()
                .name(INPUT_FILENAME)
                .worksheet(CAMPFIRE_CHANNEL_TAB)
                .withInputStream(is)
                .build();

            List<CampfireChannel> channels = CampfireChannelParser.parse(reader);
            logger.info("Read "+channels.size()+" Campfire alert channels");
            config.addAlertChannels(channels);
        }
        catch(FileNotFoundException e)
        {
            logger.severe("Unable to find Campfire alert channel file: "+e.getClass().getName()+": "+e.getMessage());
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

    public void writeCampfireChannels(AlertConfiguration config)
    {
        List<CampfireChannel> channels = config.getCampfireChannels();

        // Write the new channels to a new tab
        logger.info("Writing Campfire alert channel file: "+OUTPUT_PATH+OUTPUT_FILENAME+"/"+CAMPFIRE_CHANNEL_TAB);
        OutputStream os = null;
        OutputFileWriter writer = null;
        try
        {
            Workbook workbook = getOutputWorkbook();
            os = new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME);
            writer = OutputFileWriter.builder()
                .name(OUTPUT_FILENAME)
                .worksheet(CAMPFIRE_CHANNEL_TAB)
                .withOutputStream(os)
                .withWorkbook(workbook)
                .build();

            CampfireChannelRenderer.write(channels, writer);
            logger.info("Wrote "+channels.size()+" Campfire alert channels");
        }
        catch(IOException e)
        {
            logger.severe("Unable to write Campfire alert channel file: "+e.getClass().getName()+": "+e.getMessage());
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

    public void readOpsGenieChannels(AlertConfiguration config)
    {
        // Read the OpsGenie alert channel file
        logger.info("Loading OpsGenie alert channel file: "+INPUT_PATH+INPUT_FILENAME+"/"+OPSGENIE_CHANNEL_TAB);
        InputStream is = null;
        try
        {
            is = new FileInputStream(INPUT_PATH+INPUT_FILENAME);
            InputFileReader reader = InputFileReader.builder()
                .name(INPUT_FILENAME)
                .worksheet(OPSGENIE_CHANNEL_TAB)
                .withInputStream(is)
                .build();

            List<OpsGenieChannel> channels = OpsGenieChannelParser.parse(reader);
            logger.info("Read "+channels.size()+" OpsGenie alert channels");
            config.addAlertChannels(channels);
        }
        catch(FileNotFoundException e)
        {
            logger.severe("Unable to find OpsGenie alert channel file: "+e.getClass().getName()+": "+e.getMessage());
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

    public void writeOpsGenieChannels(AlertConfiguration config)
    {
        List<OpsGenieChannel> channels = config.getOpsGenieChannels();

        // Write the new channels to a new tab
        logger.info("Writing OpsGenie alert channel file: "+OUTPUT_PATH+OUTPUT_FILENAME+"/"+OPSGENIE_CHANNEL_TAB);
        OutputStream os = null;
        OutputFileWriter writer = null;
        try
        {
            Workbook workbook = getOutputWorkbook();
            os = new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME);
            writer = OutputFileWriter.builder()
                .name(OUTPUT_FILENAME)
                .worksheet(OPSGENIE_CHANNEL_TAB)
                .withOutputStream(os)
                .withWorkbook(workbook)
                .build();

            OpsGenieChannelRenderer.write(channels, writer);
            logger.info("Wrote "+channels.size()+" OpsGenie alert channels");
        }
        catch(IOException e)
        {
            logger.severe("Unable to write OpsGenie alert channel file: "+e.getClass().getName()+": "+e.getMessage());
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

    public void readPagerDutyChannels(AlertConfiguration config)
    {
        // Read the PagerDuty alert channel file
        logger.info("Loading PagerDuty alert channel file: "+INPUT_PATH+INPUT_FILENAME+"/"+PAGERDUTY_CHANNEL_TAB);
        InputStream is = null;
        try
        {
            is = new FileInputStream(INPUT_PATH+INPUT_FILENAME);
            InputFileReader reader = InputFileReader.builder()
                .name(INPUT_FILENAME)
                .worksheet(PAGERDUTY_CHANNEL_TAB)
                .withInputStream(is)
                .build();

            List<PagerDutyChannel> channels = PagerDutyChannelParser.parse(reader);
            logger.info("Read "+channels.size()+" PagerDuty alert channels");
            config.addAlertChannels(channels);
        }
        catch(FileNotFoundException e)
        {
            logger.severe("Unable to find PagerDuty alert channel file: "+e.getClass().getName()+": "+e.getMessage());
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

    public void writePagerDutyChannels(AlertConfiguration config)
    {
        List<PagerDutyChannel> channels = config.getPagerDutyChannels();

        // Write the new channels to a new tab
        logger.info("Writing PagerDuty alert channel file: "+OUTPUT_PATH+OUTPUT_FILENAME+"/"+PAGERDUTY_CHANNEL_TAB);
        OutputStream os = null;
        OutputFileWriter writer = null;
        try
        {
            Workbook workbook = getOutputWorkbook();
            os = new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME);
            writer = OutputFileWriter.builder()
                .name(OUTPUT_FILENAME)
                .worksheet(PAGERDUTY_CHANNEL_TAB)
                .withOutputStream(os)
                .withWorkbook(workbook)
                .build();

            PagerDutyChannelRenderer.write(channels, writer);
            logger.info("Wrote "+channels.size()+" PagerDuty alert channels");
        }
        catch(IOException e)
        {
            logger.severe("Unable to write PagerDuty alert channel file: "+e.getClass().getName()+": "+e.getMessage());
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

    public void readVictorOpsChannels(AlertConfiguration config)
    {
        // Read the VictorOps alert channel file
        logger.info("Loading VictorOps alert channel file: "+INPUT_PATH+INPUT_FILENAME+"/"+VICTOROPS_CHANNEL_TAB);
        InputStream is = null;
        try
        {
            is = new FileInputStream(INPUT_PATH+INPUT_FILENAME);
            InputFileReader reader = InputFileReader.builder()
                .name(INPUT_FILENAME)
                .worksheet(VICTOROPS_CHANNEL_TAB)
                .withInputStream(is)
                .build();

            List<VictorOpsChannel> channels = VictorOpsChannelParser.parse(reader);
            logger.info("Read "+channels.size()+" VictorOps alert channels");
            config.addAlertChannels(channels);
        }
        catch(FileNotFoundException e)
        {
            logger.severe("Unable to find VictorOps alert channel file: "+e.getClass().getName()+": "+e.getMessage());
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

    public void writeVictorOpsChannels(AlertConfiguration config)
    {
        List<VictorOpsChannel> channels = config.getVictorOpsChannels();

        // Write the new channels to a new tab
        logger.info("Writing VictorOps alert channel file: "+OUTPUT_PATH+OUTPUT_FILENAME+"/"+VICTOROPS_CHANNEL_TAB);
        OutputStream os = null;
        OutputFileWriter writer = null;
        try
        {
            Workbook workbook = getOutputWorkbook();
            os = new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME);
            writer = OutputFileWriter.builder()
                .name(OUTPUT_FILENAME)
                .worksheet(VICTOROPS_CHANNEL_TAB)
                .withOutputStream(os)
                .withWorkbook(workbook)
                .build();

            VictorOpsChannelRenderer.write(channels, writer);
            logger.info("Wrote "+channels.size()+" VictorOps alert channels");
        }
        catch(IOException e)
        {
            logger.severe("Unable to write VictorOps alert channel file: "+e.getClass().getName()+": "+e.getMessage());
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

    public void readxMattersChannels(AlertConfiguration config)
    {
        // Read the xMatters alert channel file
        logger.info("Loading xMatters alert channel file: "+INPUT_PATH+INPUT_FILENAME+"/"+XMATTERS_CHANNEL_TAB);
        InputStream is = null;
        try
        {
            is = new FileInputStream(INPUT_PATH+INPUT_FILENAME);
            InputFileReader reader = InputFileReader.builder()
                .name(INPUT_FILENAME)
                .worksheet(XMATTERS_CHANNEL_TAB)
                .withInputStream(is)
                .build();

            List<xMattersChannel> channels = xMattersChannelParser.parse(reader);
            logger.info("Read "+channels.size()+" xMatters alert channels");
            config.addAlertChannels(channels);
        }
        catch(FileNotFoundException e)
        {
            logger.severe("Unable to find xMatters alert channel file: "+e.getClass().getName()+": "+e.getMessage());
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

    public void writexMattersChannels(AlertConfiguration config)
    {
        List<xMattersChannel> channels = config.getxMattersChannels();

        // Write the new channels to a new tab
        logger.info("Writing xMatters alert channel file: "+OUTPUT_PATH+OUTPUT_FILENAME+"/"+XMATTERS_CHANNEL_TAB);
        OutputStream os = null;
        OutputFileWriter writer = null;
        try
        {
            Workbook workbook = getOutputWorkbook();
            os = new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME);
            writer = OutputFileWriter.builder()
                .name(OUTPUT_FILENAME)
                .worksheet(XMATTERS_CHANNEL_TAB)
                .withOutputStream(os)
                .withWorkbook(workbook)
                .build();

            xMattersChannelRenderer.write(channels, writer);
            logger.info("Wrote "+channels.size()+" xMatters alert channels");
        }
        catch(IOException e)
        {
            logger.severe("Unable to write xMatters alert channel file: "+e.getClass().getName()+": "+e.getMessage());
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

        // Write the new policies to a new tab
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

    public void readAlertConditions(List<AlertPolicy> policies, List<Entity> entities, AlertConfiguration config)
    {
        // Read the alert condition file
        logger.info("Loading alert condition file: "+INPUT_PATH+INPUT_FILENAME+"/"+ALERT_CONDITION_TAB);
        InputStream is = null;
        try
        {
            is = new FileInputStream(INPUT_PATH+INPUT_FILENAME);
            InputFileReader reader = InputFileReader.builder()
                .name(INPUT_FILENAME)
                .worksheet(ALERT_CONDITION_TAB)
                .withInputStream(is)
                .build();

            List<AlertCondition> conditions = AlertConditionParser.parse(policies, entities, reader);
            logger.info("Read "+conditions.size()+" alert conditions");
            config.setAlertConditions(conditions);
        }
        catch(FileNotFoundException e)
        {
            logger.severe("Unable to find alert condition file: "+e.getClass().getName()+": "+e.getMessage());
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

    public void writeAlertConditions(List<AlertPolicy> policies, AlertConfiguration config)
    {
        List<AlertCondition> conditions = config.getAlertConditions();

        // Write the new conditions to a new tab
        logger.info("Writing alert condition file: "+OUTPUT_PATH+OUTPUT_FILENAME+"/"+ALERT_CONDITION_TAB);
        OutputStream os = null;
        OutputFileWriter writer = null;
        try
        {
            Workbook workbook = getOutputWorkbook();
            os = new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME);
            writer = OutputFileWriter.builder()
                .name(OUTPUT_FILENAME)
                .worksheet(ALERT_CONDITION_TAB)
                .withOutputStream(os)
                .withWorkbook(workbook)
                .build();

            AlertConditionRenderer.write(policies, conditions, writer);
            logger.info("Wrote "+conditions.size()+" alert conditions");
        }
        catch(IOException e)
        {
            logger.severe("Unable to write alert condition file: "+e.getClass().getName()+": "+e.getMessage());
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

    public void readExternalServiceAlertConditions(List<AlertPolicy> policies, List<Entity> entities, AlertConfiguration config)
    {
        // Read the alert condition file
        logger.info("Loading external service alert condition file: "+INPUT_PATH+INPUT_FILENAME+"/"+EXTERNAL_SERVICE_CONDITION_TAB);
        InputStream is = null;
        try
        {
            is = new FileInputStream(INPUT_PATH+INPUT_FILENAME);
            InputFileReader reader = InputFileReader.builder()
                .name(INPUT_FILENAME)
                .worksheet(EXTERNAL_SERVICE_CONDITION_TAB)
                .withInputStream(is)
                .build();

            List<ExternalServiceAlertCondition> conditions = ExternalServiceAlertConditionParser.parse(policies, entities, reader);
            logger.info("Read "+conditions.size()+" external service alert conditions");
            config.setExternalServiceAlertConditions(conditions);
        }
        catch(FileNotFoundException e)
        {
            logger.severe("Unable to find external service alert condition file: "+e.getClass().getName()+": "+e.getMessage());
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

    public void writeExternalServiceAlertConditions(List<AlertPolicy> policies, AlertConfiguration config)
    {
        List<ExternalServiceAlertCondition> conditions = config.getExternalServiceAlertConditions();

        // Write the new conditions to a new tab
        logger.info("Writing external service alert condition file: "+OUTPUT_PATH+OUTPUT_FILENAME+"/"+EXTERNAL_SERVICE_CONDITION_TAB);
        OutputStream os = null;
        OutputFileWriter writer = null;
        try
        {
            Workbook workbook = getOutputWorkbook();
            os = new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME);
            writer = OutputFileWriter.builder()
                .name(OUTPUT_FILENAME)
                .worksheet(EXTERNAL_SERVICE_CONDITION_TAB)
                .withOutputStream(os)
                .withWorkbook(workbook)
                .build();

            ExternalServiceAlertConditionRenderer.write(policies, conditions, writer);
            logger.info("Wrote "+conditions.size()+" external service alert conditions");
        }
        catch(IOException e)
        {
            logger.severe("Unable to write external service alert condition file: "+e.getClass().getName()+": "+e.getMessage());
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

    public void readNrqlAlertConditions(List<AlertPolicy> policies, AlertConfiguration config)
    {
        // Read the alert condition file
        logger.info("Loading nrql alert condition file: "+INPUT_PATH+INPUT_FILENAME+"/"+NRQL_CONDITION_TAB);
        InputStream is = null;
        try
        {
            is = new FileInputStream(INPUT_PATH+INPUT_FILENAME);
            InputFileReader reader = InputFileReader.builder()
                .name(INPUT_FILENAME)
                .worksheet(NRQL_CONDITION_TAB)
                .withInputStream(is)
                .build();

            List<NrqlAlertCondition> conditions = NrqlAlertConditionParser.parse(policies, reader);
            logger.info("Read "+conditions.size()+" nrql alert conditions");
            config.setNrqlAlertConditions(conditions);
        }
        catch(FileNotFoundException e)
        {
            logger.severe("Unable to find nrql alert condition file: "+e.getClass().getName()+": "+e.getMessage());
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

    public void writeNrqlAlertConditions(List<AlertPolicy> policies, AlertConfiguration config)
    {
        List<NrqlAlertCondition> conditions = config.getNrqlAlertConditions();

        // Write the new conditions to a new tab
        logger.info("Writing nrql alert condition file: "+OUTPUT_PATH+OUTPUT_FILENAME+"/"+NRQL_CONDITION_TAB);
        OutputStream os = null;
        OutputFileWriter writer = null;
        try
        {
            Workbook workbook = getOutputWorkbook();
            os = new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME);
            writer = OutputFileWriter.builder()
                .name(OUTPUT_FILENAME)
                .worksheet(NRQL_CONDITION_TAB)
                .withOutputStream(os)
                .withWorkbook(workbook)
                .build();

            NrqlAlertConditionRenderer.write(policies, conditions, writer);
            logger.info("Wrote "+conditions.size()+" nrql alert conditions");
        }
        catch(IOException e)
        {
            logger.severe("Unable to write nrql alert condition file: "+e.getClass().getName()+": "+e.getMessage());
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

    public void readInfraMetricAlertConditions(List<AlertPolicy> policies, AlertConfiguration config)
    {
        // Read the alert condition file
        logger.info("Loading infra metric alert condition file: "+INPUT_PATH+INPUT_FILENAME+"/"+INFRA_METRIC_CONDITION_TAB);
        InputStream is = null;
        try
        {
            is = new FileInputStream(INPUT_PATH+INPUT_FILENAME);
            InputFileReader reader = InputFileReader.builder()
                .name(INPUT_FILENAME)
                .worksheet(INFRA_METRIC_CONDITION_TAB)
                .withInputStream(is)
                .build();

            List<InfraMetricAlertCondition> conditions = InfraMetricAlertConditionParser.parse(policies, reader);
            logger.info("Read "+conditions.size()+" infra metric alert conditions");
            config.addInfraAlertConditions(conditions);
        }
        catch(FileNotFoundException e)
        {
            logger.severe("Unable to find infra metric alert condition file: "+e.getClass().getName()+": "+e.getMessage());
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

    public void writeInfraMetricAlertConditions(List<AlertPolicy> policies, AlertConfiguration config)
    {
        List<InfraMetricAlertCondition> conditions = config.getInfraMetricAlertConditions();

        // Write the new conditions to a new tab
        logger.info("Writing infra metric alert condition file: "+OUTPUT_PATH+OUTPUT_FILENAME+"/"+INFRA_METRIC_CONDITION_TAB);
        OutputStream os = null;
        OutputFileWriter writer = null;
        try
        {
            Workbook workbook = getOutputWorkbook();
            os = new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME);
            writer = OutputFileWriter.builder()
                .name(OUTPUT_FILENAME)
                .worksheet(INFRA_METRIC_CONDITION_TAB)
                .withOutputStream(os)
                .withWorkbook(workbook)
                .build();

            InfraMetricAlertConditionRenderer.write(policies, conditions, writer);
            logger.info("Wrote "+conditions.size()+" infra metric alert conditions");
        }
        catch(IOException e)
        {
            logger.severe("Unable to write infra metric alert condition file: "+e.getClass().getName()+": "+e.getMessage());
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