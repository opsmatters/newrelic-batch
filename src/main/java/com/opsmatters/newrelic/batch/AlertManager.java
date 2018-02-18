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
import com.opsmatters.newrelic.api.model.alerts.policies.AlertPolicy;

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
     * Creates the given alert policies.
     * @param policies The alert policies to create
     * @return The created alert policies
     */
    public List<AlertPolicy> createAlertPolicies(List<AlertPolicy> policies)
    {
        if(policies == null)
            throw new IllegalArgumentException("null policies");

        checkInitialize();
        if(!isInitialized())
            throw new IllegalStateException("client not initialized");

        // Create the policies
        List<AlertPolicy> ret = new ArrayList<AlertPolicy>();
        logger.info("Creating "+policies+" alert policies");
        for(AlertPolicy policy : policies)
        {
            logger.info("Creating alert policy: "+policy.getName());
            policy = apiClient.alertPolicies().create(policy).get();
            logger.info("Created alert policy: "+policy.getId()+" -"+policy.getName());
            ret.add(policy);
        }

        return ret;
    }

    /**
     * Create the given alert policy.
     * @param policy The alert policy to create
     * @return The created alert policy
     */
    public AlertPolicy createPolicy(AlertPolicy policy)
    {
        checkInitialize();
        if(!isInitialized())
            throw new IllegalStateException("client not initialized");

        // Create the policy
        logger.info("Creating alert policy: "+policy.getName());
        policy = apiClient.alertPolicies().create(policy).get();
        logger.info("Created alert policy: "+policy.getId()+" -"+policy.getName());

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
        if(!isInitialized())
            throw new IllegalStateException("client not initialized");

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
        if(!isInitialized())
            throw new IllegalStateException("client not initialized");

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
            logger.info("Deleting existing alert policy: "+policy.getId());
            apiClient.alertPolicies().delete(policy.getId());
            logger.info("Deleted existing alert policy : "+policy.getId()+" - "+policy.getName());
        }
    }
}