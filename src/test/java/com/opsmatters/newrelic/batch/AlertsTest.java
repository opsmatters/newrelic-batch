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
import com.opsmatters.newrelic.batch.parsers.AlertPolicyParser;
import com.opsmatters.newrelic.batch.renderers.AlertPolicyRenderer;
import com.opsmatters.newrelic.batch.model.AlertConfiguration;
import com.opsmatters.core.reports.InputFileReader;
import com.opsmatters.core.reports.OutputFileWriter;

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
    private static final String INPUT_POLICY_FILENAME = "test-alerts.xlsx";
    private static final String OUTPUT_PATH = "target/";
    private static final String OUTPUT_POLICY_FILENAME = "test-alerts-new.xlsx";

    @Test
    public void testNewRelicAlerts()
    {
        String testName = "NewRelicAlertTest";
        logger.info("Starting test: "+testName);

        AlertConfiguration config = new AlertConfiguration();

        // Read the alert policy file
        logger.info("Loading alert policy file: "+INPUT_PATH+INPUT_POLICY_FILENAME);
        InputStream is = null;
        try
        {
            is = new FileInputStream(INPUT_PATH+INPUT_POLICY_FILENAME);
            InputFileReader reader = InputFileReader.builder()
                .name(INPUT_POLICY_FILENAME)
                .worksheet("alert policies")
                .withInputStream(is)
                .build();
            config.setAlertPolicies(AlertPolicyParser.parse(reader));
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

        List<AlertPolicy> policies = config.getAlertPolicies();
        Assert.assertTrue(config.numAlertPolicies() > 0);

        AlertManager manager = new AlertManager(apiKey);

        // Delete the existing policies
        List<AlertPolicy> deleted = manager.deleteAlertPolicies(config.getAlertPolicies());
        Assert.assertTrue(deleted.size() == policies.size());

        // Create the new policies
        List<AlertPolicy> created = manager.createAlertPolicies(config.getAlertPolicies());
        Assert.assertTrue(created.size() == policies.size());

        // Write the new policies to a new filename
        logger.info("Writing alert policy file: "+OUTPUT_PATH+OUTPUT_POLICY_FILENAME);
        OutputStream os = null;
        try
        {
            os = new FileOutputStream(OUTPUT_PATH+OUTPUT_POLICY_FILENAME);
            OutputFileWriter writer = OutputFileWriter.builder()
                .name(OUTPUT_POLICY_FILENAME)
                .worksheet("alert policies")
                .withOutputStream(os)
                .build();
            AlertPolicyRenderer.write(policies, writer);
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
            }
            catch(IOException e)
            {
            }
        }

        logger.info("Completed test: "+testName);
    }
}