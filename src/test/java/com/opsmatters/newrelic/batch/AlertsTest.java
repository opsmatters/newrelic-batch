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
import com.opsmatters.newrelic.api.model.alerts.conditions.AlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.ExternalServiceAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.NrqlAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.InfraAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.InfraMetricAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.InfraProcessRunningAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.InfraHostNotReportingAlertCondition;
import com.opsmatters.newrelic.api.model.Entity;
import com.opsmatters.newrelic.batch.parsers.AlertConditionParser;
import com.opsmatters.newrelic.batch.parsers.ExternalServiceAlertConditionParser;
import com.opsmatters.newrelic.batch.parsers.NrqlAlertConditionParser;
import com.opsmatters.newrelic.batch.parsers.InfraMetricAlertConditionParser;
import com.opsmatters.newrelic.batch.parsers.InfraProcessRunningAlertConditionParser;
import com.opsmatters.newrelic.batch.parsers.InfraHostNotReportingAlertConditionParser;
import com.opsmatters.newrelic.batch.renderers.AlertConditionRenderer;
import com.opsmatters.newrelic.batch.renderers.ExternalServiceAlertConditionRenderer;
import com.opsmatters.newrelic.batch.renderers.NrqlAlertConditionRenderer;
import com.opsmatters.newrelic.batch.renderers.InfraMetricAlertConditionRenderer;
import com.opsmatters.newrelic.batch.renderers.InfraProcessRunningAlertConditionRenderer;
import com.opsmatters.newrelic.batch.renderers.InfraHostNotReportingAlertConditionRenderer;
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
    private static final String INFRA_PROCESS_CONDITION_TAB = "infra process conditions";
    private static final String INFRA_HOST_CONDITION_TAB = "infra host conditions";

    @Test
    public void testNewRelicAlerts()
    {
        String testName = "NewRelicAlertTest";
        logger.info("Starting test: "+testName);

        AlertConfiguration config = new AlertConfiguration();
        AlertManager manager = new AlertManager(apiKey, true);

        try
        {
            // Read the alert channels
            config.addAlertChannels(manager.readEmailChannels(INPUT_FILENAME, EMAIL_CHANNEL_TAB, 
                new FileInputStream(INPUT_PATH+INPUT_FILENAME)));
            config.addAlertChannels(manager.readSlackChannels(INPUT_FILENAME, SLACK_CHANNEL_TAB, 
                new FileInputStream(INPUT_PATH+INPUT_FILENAME)));
            config.addAlertChannels(manager.readHipChatChannels(INPUT_FILENAME, HIPCHAT_CHANNEL_TAB, 
                new FileInputStream(INPUT_PATH+INPUT_FILENAME)));
            config.addAlertChannels(manager.readCampfireChannels(INPUT_FILENAME, CAMPFIRE_CHANNEL_TAB, 
                new FileInputStream(INPUT_PATH+INPUT_FILENAME)));
            config.addAlertChannels(manager.readOpsGenieChannels(INPUT_FILENAME, OPSGENIE_CHANNEL_TAB, 
                new FileInputStream(INPUT_PATH+INPUT_FILENAME)));
            config.addAlertChannels(manager.readPagerDutyChannels(INPUT_FILENAME, PAGERDUTY_CHANNEL_TAB, 
                new FileInputStream(INPUT_PATH+INPUT_FILENAME)));
            config.addAlertChannels(manager.readVictorOpsChannels(INPUT_FILENAME, VICTOROPS_CHANNEL_TAB, 
                new FileInputStream(INPUT_PATH+INPUT_FILENAME)));
            //config.addAlertChannels(manager.readxMattersChannels(INPUT_FILENAME, XMATTERS_CHANNEL_TAB, 
            //    new FileInputStream(INPUT_PATH+INPUT_FILENAME)));
        }
        catch(IOException e)
        {
            logger.severe("Unable to read alert channel file: "+e.getClass().getName()+": "+e.getMessage());
        }

        List<AlertChannel> channels = config.getAlertChannels();
        Assert.assertTrue(config.numAlertChannels() > 0);

        // Delete the existing alert channels
        List<AlertChannel> deletedChannels = manager.deleteAlertChannels(config.getAlertChannels());
        Assert.assertTrue(deletedChannels.size() == channels.size());

        // Create the new alert channels
        List<AlertChannel> createdChannels = manager.createAlertChannels(config.getAlertChannels());
        Assert.assertTrue(createdChannels.size() == channels.size());

        // Get the list of channels
        List<AlertChannel> allChannels = manager.getAlertChannels();

        try
        {
            // Read the alert policies
            config.setAlertPolicies(manager.readAlertPolicies(allChannels, INPUT_FILENAME, ALERT_POLICY_TAB, 
                new FileInputStream(INPUT_PATH+INPUT_FILENAME)));
        }
        catch(IOException e)
        {
            logger.severe("Unable to read alert policy file: "+e.getClass().getName()+": "+e.getMessage());
        }

        List<AlertPolicy> policies = config.getAlertPolicies();
        Assert.assertTrue(config.numAlertPolicies() > 0);

        // Delete the existing alert policies
        List<AlertPolicy> deletedPolicies = manager.deleteAlertPolicies(config.getAlertPolicies());
        Assert.assertTrue(deletedPolicies.size() == policies.size());

        // Create the new alert policies
        List<AlertPolicy> createdPolicies = manager.createAlertPolicies(config.getAlertPolicies());
        Assert.assertTrue(createdPolicies.size() == policies.size());

        // Get the list of channels again as it will have the updated links after creating the policies
        allChannels = manager.getAlertChannels();

        // Reset the list of policies so that we only use the (clean) returned objects
        config.setAlertPolicies(createdPolicies);

        // Get the lists of policies and entities
        List<AlertPolicy> allPolicies = manager.getAlertPolicies();
        List<Entity> entities = new ArrayList<Entity>();
        entities.addAll(manager.getApplications());
        entities.addAll(manager.getServers());

        try
        {
            // Read the alert conditions
            config.setAlertConditions(manager.readAlertConditions(allPolicies, entities, INPUT_FILENAME, ALERT_CONDITION_TAB, 
                new FileInputStream(INPUT_PATH+INPUT_FILENAME)));
            config.setExternalServiceAlertConditions(manager.readExternalServiceAlertConditions(allPolicies, entities, INPUT_FILENAME, EXTERNAL_SERVICE_CONDITION_TAB, 
                new FileInputStream(INPUT_PATH+INPUT_FILENAME)));
            config.setNrqlAlertConditions(manager.readNrqlAlertConditions(allPolicies, INPUT_FILENAME, NRQL_CONDITION_TAB, 
                new FileInputStream(INPUT_PATH+INPUT_FILENAME)));
            config.addInfraAlertConditions(manager.readInfraMetricAlertConditions(allPolicies, INPUT_FILENAME, INFRA_METRIC_CONDITION_TAB, 
                new FileInputStream(INPUT_PATH+INPUT_FILENAME)));
            config.addInfraAlertConditions(manager.readInfraProcessRunningAlertConditions(allPolicies, INPUT_FILENAME, INFRA_PROCESS_CONDITION_TAB, 
                new FileInputStream(INPUT_PATH+INPUT_FILENAME)));
            config.addInfraAlertConditions(manager.readInfraHostNotReportingAlertConditions(allPolicies, INPUT_FILENAME, INFRA_HOST_CONDITION_TAB, 
                new FileInputStream(INPUT_PATH+INPUT_FILENAME)));
        }
        catch(IOException e)
        {
            logger.severe("Unable to read alert condition file: "+e.getClass().getName()+": "+e.getMessage());
        }

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

        try
        {
            // Write the alert channels
            manager.writeEmailChannels(config.getEmailChannels(), 
                OUTPUT_FILENAME, EMAIL_CHANNEL_TAB, 
                new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME), null);

            Workbook workbook = getOutputWorkbook();
            manager.writeSlackChannels(config.getSlackChannels(), 
                OUTPUT_FILENAME, SLACK_CHANNEL_TAB, 
                new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME), workbook);

            workbook = getOutputWorkbook();
            manager.writeHipChatChannels(config.getHipChatChannels(), 
                OUTPUT_FILENAME, HIPCHAT_CHANNEL_TAB, 
                new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME), workbook);

            workbook = getOutputWorkbook();
            manager.writeCampfireChannels(config.getCampfireChannels(), 
                OUTPUT_FILENAME, CAMPFIRE_CHANNEL_TAB, 
                new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME), workbook);

            workbook = getOutputWorkbook();
            manager.writeOpsGenieChannels(config.getOpsGenieChannels(), 
                OUTPUT_FILENAME, OPSGENIE_CHANNEL_TAB, 
                new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME), workbook);

            workbook = getOutputWorkbook();
            manager.writePagerDutyChannels(config.getPagerDutyChannels(), 
                OUTPUT_FILENAME, PAGERDUTY_CHANNEL_TAB, 
                new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME), workbook);

            workbook = getOutputWorkbook();
            manager.writeVictorOpsChannels(config.getVictorOpsChannels(), 
                OUTPUT_FILENAME, VICTOROPS_CHANNEL_TAB, 
                new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME), workbook);

            //workbook = getOutputWorkbook();
            //manager.writexMattersChannels(config.getxMattersChannels(), 
            //    OUTPUT_FILENAME, XMATTERS_CHANNEL_TAB, 
            //    new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME), workbook);
        }
        catch(IOException e)
        {
            logger.severe("Unable to write alert channel file: "+e.getClass().getName()+": "+e.getMessage());
        }

        try
        {
            // Write the alert policies
            Workbook workbook = getOutputWorkbook();
            manager.writeAlertPolicies(allChannels, config.getAlertPolicies(), 
                OUTPUT_FILENAME, ALERT_POLICY_TAB, 
                new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME), workbook);
        }
        catch(IOException e)
        {
            logger.severe("Unable to write alert policy file: "+e.getClass().getName()+": "+e.getMessage());
        }

        try
        {
            // Write the alert conditions
            Workbook workbook = getOutputWorkbook();
            manager.writeAlertConditions(allPolicies, entities, config.getAlertConditions(), 
                OUTPUT_FILENAME, ALERT_CONDITION_TAB, 
                new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME), workbook);

            workbook = getOutputWorkbook();
            manager.writeExternalServiceAlertConditions(allPolicies, entities, config.getExternalServiceAlertConditions(), 
                OUTPUT_FILENAME, EXTERNAL_SERVICE_CONDITION_TAB, 
                new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME), workbook);

            workbook = getOutputWorkbook();
            manager.writeNrqlAlertConditions(allPolicies, config.getNrqlAlertConditions(), 
                OUTPUT_FILENAME, NRQL_CONDITION_TAB, 
                new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME), workbook);

            workbook = getOutputWorkbook();
            manager.writeInfraMetricAlertConditions(allPolicies, config.getInfraMetricAlertConditions(), 
                OUTPUT_FILENAME, INFRA_METRIC_CONDITION_TAB, 
                new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME), workbook);

            workbook = getOutputWorkbook();
            manager.writeInfraProcessRunningAlertConditions(allPolicies, config.getInfraProcessRunningAlertConditions(), 
                OUTPUT_FILENAME, INFRA_PROCESS_CONDITION_TAB, 
                new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME), workbook);

            workbook = getOutputWorkbook();
            manager.writeInfraHostNotReportingAlertConditions(allPolicies, config.getInfraHostNotReportingAlertConditions(), 
                OUTPUT_FILENAME, INFRA_HOST_CONDITION_TAB, 
                new FileOutputStream(OUTPUT_PATH+OUTPUT_FILENAME), workbook);
        }
        catch(IOException e)
        {
            logger.severe("Unable to write alert condition file: "+e.getClass().getName()+": "+e.getMessage());
        }

        logger.info("Completed test: "+testName);
    }

    public Workbook getOutputWorkbook() throws IOException
    {
        return Workbook.getWorkbook(new File(OUTPUT_PATH, OUTPUT_FILENAME));
    }
}