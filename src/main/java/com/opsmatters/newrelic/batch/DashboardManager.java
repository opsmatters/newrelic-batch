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

import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import com.opsmatters.newrelic.api.model.insights.Dashboard;
import com.opsmatters.newrelic.batch.parsers.DashboardParser;
import com.opsmatters.newrelic.batch.renderers.DashboardRenderer;

/**
 * Manager of operations on dashboards.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class DashboardManager extends BaseManager
{
    private static final Logger logger = Logger.getLogger(DashboardManager.class.getName());

    /**
     * Constructor that takes an API key.
     * @param apiKey The API key used to authenticate the client
     */
    public DashboardManager(String apiKey)
    {
        this(apiKey, false);
    }

    /**
     * Constructor that takes an API key and a verbose flag.
     * @param apiKey The API key used to authenticate the client
     * @param verbose <CODE>true</CODE> if verbose logging is enabled
     */
    public DashboardManager(String apiKey, boolean verbose)
    {
        super(apiKey, verbose);
    }

    /**
     * Returns the dashboards.
     * @param detailed <CODE>true</CODE> if detailed info is required (including widgets) rather than a summary
     * @return The dashboards
     */
    public List<Dashboard> getDashboards(boolean detailed)
    {
        checkInitialize();
        if(!isInitialized())
            throw new IllegalStateException("client not initialized");

        // Get the dashboards
        if(verbose())
            logger.info("Getting the dashboards");
        Collection<Dashboard> dashboards = apiClient.dashboards().list();
        if(verbose())
            logger.info("Got "+dashboards.size()+" dashboards");

        List<Dashboard> ret = new ArrayList<Dashboard>();
        for(Dashboard dashboard : dashboards)
        {
            if(detailed)
                dashboard = apiClient.dashboards().show(dashboard.getId()).get();
            ret.add(dashboard);
        }
        return ret;
    }

    /**
     * Creates the given dashboards.
     * @param dashboards The dashboards to create
     * @return The created dashboards
     */
    public List<Dashboard> createDashboards(List<Dashboard> dashboards)
    {
        if(dashboards == null)
            throw new IllegalArgumentException("null dashboards");

        checkInitialize();
        if(!isInitialized())
            throw new IllegalStateException("client not initialized");

        // Create the dashboards
        List<Dashboard> ret = new ArrayList<Dashboard>();
        if(verbose())
            logger.info("Creating "+dashboards.size()+" dashboards");
        for(Dashboard dashboard : dashboards)
        {
            if(verbose())
                logger.info("Creating dashboard: "+dashboard.getTitle());
            dashboard = apiClient.dashboards().create(dashboard).get();
            logger.info("Created dashboard: "+dashboard.getId()+" - "+dashboard.getTitle());
            ret.add(dashboard);
        }

        return ret;
    }

    /**
     * Create the given dashboard.
     * @param dashboard The dashboard to create
     * @return The created dashboard
     */
    public Dashboard createDashboard(Dashboard dashboard)
    {
        checkInitialize();
        if(!isInitialized())
            throw new IllegalStateException("client not initialized");

        // Create the dashboard
        if(verbose())
            logger.info("Creating dashboard: "+dashboard.getTitle());
        dashboard = apiClient.dashboards().create(dashboard).get();
        logger.info("Created dashboard: "+dashboard.getId()+" - "+dashboard.getTitle());

        return dashboard;
    }

    /**
     * Delete the given dashboard.
     * @param dashboards The dashboards to delete
     * @return The deleted dashboards
     */
    public List<Dashboard> deleteDashboards(List<Dashboard> dashboards)
    {
        if(dashboards == null)
            throw new IllegalArgumentException("null dashboards");

        checkInitialize();
        if(!isInitialized())
            throw new IllegalStateException("client not initialized");

        // Delete the dashboards
        List<Dashboard> ret = new ArrayList<Dashboard>();
        for(Dashboard dashboard : dashboards)
        {
            deleteDashboards(dashboard.getTitle());
            ret.add(dashboard);
        }

        return ret;
    }

    /**
     * Delete the given dashboard.
     * @param dashboard The dashboard to delete
     * @return The deleted dashboard
     */
    public Dashboard deleteDashboard(Dashboard dashboard)
    {
        checkInitialize();
        if(!isInitialized())
            throw new IllegalStateException("client not initialized");

        // Delete the dashboard
        deleteDashboards(dashboard.getTitle());

        return dashboard;
    }

    /**
     * Delete the dashboards with the given title.
     * @param title The title of the dashboards
     */
    private void deleteDashboards(String title)
    {
        Collection<Dashboard> dashboards = apiClient.dashboards().list(title);
        for(Dashboard dashboard : dashboards)
        {
            if(verbose())
                logger.info("Deleting dashboard: "+dashboard.getId());
            apiClient.dashboards().delete(dashboard.getId());
            logger.info("Deleted dashboard: "+dashboard.getId()+" - "+dashboard.getTitle());
        }
    }

    /**
     * Reads dashboards from an import file with the given name.
     * Closes the reader after reading the file.
     * @param filename The name of the file to import
     * @param reader A reader for the file
     * @return The set of dashboards read from the import file
     */
    public List<Dashboard> readDashboards(String filename, Reader reader)
    {
        List<Dashboard> ret = null;

        try
        {
            if(verbose())
                logger.info("Loading dashboard file: "+filename);
            ret = DashboardParser.parseYaml(reader);
            logger.info("Read "+ret.size()+" dashboards");
        }
        finally
        {
            closeReader(reader);
        }

        return ret;
    }

    /**
     * Writes dashboards to an export file with the given name.
     * Closes the writer after writing the file.
     * @param dashboards The list of dashboards to be exported
     * @param filename The name of the file to export to
     * @param writer A writer for the file
     */
    public void writeDashboards(List<Dashboard> dashboards, String filename, Writer writer)
    {
        try
        {
            if(verbose())
                logger.info("Writing dashboard file: "+filename);
            DashboardRenderer.builder().withBanner(true).title(filename).build().renderYaml(dashboards, writer);
            logger.info("Wrote "+dashboards.size()+" dashboards");
        }
        finally
        {
            closeWriter(writer);
        }
    }
}