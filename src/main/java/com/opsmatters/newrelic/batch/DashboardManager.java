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

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import com.opsmatters.newrelic.api.NewRelicApi;
import com.opsmatters.newrelic.api.model.insights.Dashboard;

/**
 * Manager of operations on dashboards.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class DashboardManager
{
    private static final Logger logger = Logger.getLogger(DashboardManager.class.getName());

    private String apiKey;
    private NewRelicApi apiClient;
    private boolean initialized = false;

    /**
     * Constructor that takes an API key.
     * @param apiKey The API key used to authenticate the client
     */
    public DashboardManager(String apiKey)
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
        logger.info("Creating "+dashboards+" dashboards");
        for(Dashboard dashboard : dashboards)
        {
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
            logger.info("Deleting dashboard: "+dashboard.getId());
            apiClient.dashboards().delete(dashboard.getId());
            logger.info("Deleted dashboard: "+dashboard.getId()+" - "+dashboard.getTitle());
        }
    }
}