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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import org.junit.Test;
import junit.framework.Assert;
import com.opsmatters.newrelic.api.Constants;
import com.opsmatters.newrelic.api.model.insights.Dashboard;
import com.opsmatters.newrelic.batch.model.DashboardConfiguration;

/**
 * The set of tests used for importing and exporting dashboards.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class DashboardTest
{
    private static final Logger logger = Logger.getLogger(DashboardTest.class.getName());

    // Get the properties
    private String apiKey = System.getProperty(Constants.API_KEY_PROPERTY);

    private static final String PATH = "target/test-classes/";
    private static final String INPUT_FILENAME = "test-dashboards.yml";
    private static final String OUTPUT_FILENAME = "test-dashboards-new.yml";

    @Test
    public void testNewRelicDashboards()
    {
        String testName = "NewRelicDashboardTest";
        logger.info("Starting test: "+testName);

        DashboardManager manager = new DashboardManager(apiKey, true);
        DashboardConfiguration config = new DashboardConfiguration();

        // Read the dashboard file
        try
        {
            config.setDashboards(manager.readDashboards(INPUT_FILENAME, new FileReader(PATH+INPUT_FILENAME)));
        }
        catch(FileNotFoundException e)
        {
            logger.severe("Unable to find dashboard file: "+e.getClass().getName()+": "+e.getMessage());
        }

        List<Dashboard> dashboards = config.getDashboards();
        Assert.assertTrue(config.numDashboards() > 0);

        // Delete the existing dashboards
        List<Dashboard> deleted = manager.deleteDashboards(config.getDashboards());
        Assert.assertTrue(deleted.size() == dashboards.size());

        // Create the new dashboards
        List<Dashboard> created = manager.createDashboards(config.getDashboards());
        Assert.assertTrue(created.size() == dashboards.size());

        try
        {
            // Write the new dashboards to a new filename
            manager.writeDashboards(dashboards, OUTPUT_FILENAME, new FileWriter(PATH+OUTPUT_FILENAME));
        }
        catch(IOException e)
        {
            logger.severe("Unable to write dashboard file: "+e.getClass().getName()+": "+e.getMessage());
        }

        logger.info("Completed test: "+testName);
    }
}