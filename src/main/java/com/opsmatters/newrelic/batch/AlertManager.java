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
import com.opsmatters.newrelic.api.model.alerts.channels.AlertChannel;
import com.opsmatters.newrelic.api.model.alerts.conditions.AlertCondition;

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
        logger.info("Creating "+policies.size()+" alert policies");
        for(AlertPolicy policy : policies)
        {
            logger.info("Creating alert policy: "+policy.getName());
            policy = apiClient.alertPolicies().create(policy).get();
            logger.info("Created alert policy: "+policy.getId()+" - "+policy.getName());
            ret.add(policy);
        }

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
        if(!isInitialized())
            throw new IllegalStateException("client not initialized");

        // Create the policy
        logger.info("Creating alert policy: "+policy.getName());
        policy = apiClient.alertPolicies().create(policy).get();
        logger.info("Created alert policy: "+policy.getId()+" - "+policy.getName());

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
            logger.info("Deleting alert policy: "+policy.getId());
            apiClient.alertPolicies().delete(policy.getId());
            logger.info("Deleted alert policy : "+policy.getId()+" - "+policy.getName());
        }
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
        if(!isInitialized())
            throw new IllegalStateException("client not initialized");

        // Create the channels
        List<AlertChannel> ret = new ArrayList<AlertChannel>();
        logger.info("Creating "+channels.size()+" alert channels");
        for(AlertChannel channel : channels)
        {
            logger.info("Creating alert channel: "+channel.getName());
            channel = apiClient.alertChannels().create(channel).get();
            logger.info("Created alert channel: "+channel.getId()+" - "+channel.getName());
            ret.add(channel);
        }

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
        if(!isInitialized())
            throw new IllegalStateException("client not initialized");

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
        if(!isInitialized())
            throw new IllegalStateException("client not initialized");

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
        if(!isInitialized())
            throw new IllegalStateException("client not initialized");

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
     * Creates the given alert conditions.
     * @param conditions The alert conditions to create
     * @return The created alert conditions
     */
    public List<AlertCondition> createAlertConditions(List<AlertCondition> conditions)
    {
        if(conditions == null)
            throw new IllegalArgumentException("null conditions");

        checkInitialize();
        if(!isInitialized())
            throw new IllegalStateException("client not initialized");

        // Create the conditions
        List<AlertCondition> ret = new ArrayList<AlertCondition>();
        logger.info("Creating "+conditions.size()+" alert conditions");
        for(AlertCondition condition : conditions)
        {
            logger.info("Creating alert condition: "+condition.getName());
            if(condition.getPolicyId() == null || condition.getPolicyId() == 0L)
                throw new IllegalArgumentException("condition has missing policy_id: "+condition.getName());
            condition = apiClient.alertConditions().create(condition.getPolicyId(), condition).get();
            logger.info("Created alert condition: "+condition.getId()+" - "+condition.getName());
            ret.add(condition);
        }

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
        if(!isInitialized())
            throw new IllegalStateException("client not initialized");

        // Create the condition
        logger.info("Creating alert condition: "+condition.getName());
        if(condition.getPolicyId() == null || condition.getPolicyId() == 0L)
            throw new IllegalArgumentException("condition has missing policyId: "+condition.getName());
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
        if(!isInitialized())
            throw new IllegalStateException("client not initialized");

        // Delete the conditions
        List<AlertCondition> ret = new ArrayList<AlertCondition>();
        for(AlertCondition condition : conditions)
        {
            if(condition.getPolicyId() == null || condition.getPolicyId() == 0L)
                throw new IllegalArgumentException("condition has missing policyId: "+condition.getName());
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
        if(!isInitialized())
            throw new IllegalStateException("client not initialized");

        // Delete the condition
        if(condition.getPolicyId() == null || condition.getPolicyId() == 0L)
            throw new IllegalArgumentException("condition has missing policyId: "+condition.getName());
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
}