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

import java.io.IOException;
import java.util.logging.Logger;
import org.junit.Test;
import junit.framework.Assert;
import com.opsmatters.newrelic.api.Constants;
import com.opsmatters.newrelic.batch.TextFile;
import com.opsmatters.newrelic.batch.parsers.DashboardParser;
import com.opsmatters.newrelic.batch.model.Dashboards;

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

    private static final String FILENAME = "target/test-classes/test-dashboards.yml";

    @Test
    public void testNewRelicDashboards()
    {
        String testName = "NewRelicDashboardTest";
        logger.info("Starting test: "+testName);

        // Load the test dashboards file
        logger.info("Loading dashboards file: "+FILENAME);
/* GERALD
        DashboardConfiguration config = new DashboardConfiguration();

        try
        {
            config.read(FILENAME);
        }
        catch(IOException e)
        {
            logger.severe("Error loading dashboards file: "+e.getClass().getName()+": "+e.getMessage());
        }
*/
        TextFile file = new TextFile(FILENAME);
        Dashboards dashboards = new Dashboards();
        try
        {
            if(file.read())
                DashboardParser.parseYaml(file.getContents(), dashboards);
        }
        catch(IOException e)
        {
            logger.severe("Error loading dashboards file: "+e.getClass().getName()+": "+e.getMessage());
        }

//GERALD
System.out.println("DashboardTest:1: dashboards="+dashboards.getDashboards());
        Assert.assertTrue(dashboards.getDashboards().size() > 0);

//GERALD: delete the dashboards before creating them again

//GERALD: import the dashboards to NR

//GERALD: write the dashboards to a new file name

        logger.info("Completed test: "+testName);
    }
}