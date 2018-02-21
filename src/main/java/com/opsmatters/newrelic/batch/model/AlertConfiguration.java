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

package com.opsmatters.newrelic.batch.model;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import com.opsmatters.newrelic.api.model.alerts.policies.AlertPolicy;
import com.opsmatters.newrelic.api.model.alerts.channels.AlertChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.EmailChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.SlackChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.HipChatChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.CampfireChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.OpsGenieChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.PagerDutyChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.UserChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.VictorOpsChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.WebhookChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.xMattersChannel;
import com.opsmatters.newrelic.api.model.alerts.conditions.AlertCondition;

/**
 * Represents a set of alert policies, conditions and channels.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class AlertConfiguration
{
    private static final Logger logger = Logger.getLogger(AlertConfiguration.class.getName());

    private List<AlertPolicy> policies = new ArrayList<AlertPolicy>();
    private List<AlertChannel> channels = new ArrayList<AlertChannel>();
    private List<AlertCondition> conditions = new ArrayList<AlertCondition>();

    /**
     * Default constructor.
     */
    public AlertConfiguration()
    {
    }

    /**
     * Replaces the alert policies with the given set of policies.
     * @param policies The set of policies
     */
    public void setAlertPolicies(List<AlertPolicy> policies)
    {
        this.policies.clear();
        this.policies.addAll(policies);
    }

    /**
     * Adds the given alert policies to the current set of policies.
     * @param policies The policies to add
     */
    public void addAlertPolicies(List<AlertPolicy> policies)
    {
        this.policies.addAll(policies);
    }

    /**
     * Returns the set of alert policies.
     * @return The set of policies
     */
    public List<AlertPolicy> getAlertPolicies()
    {
        return policies;
    }

    /**
     * Returns the number of alert policies.
     * @return The number of alert policies
     */
    public int numAlertPolicies()
    {
        return policies.size();
    }

    /**
     * Replaces the alert channels with the given set of channels.
     * @param channels The set of channels
     */
    public void setAlertChannels(List<? extends AlertChannel> channels)
    {
        this.channels.clear();
        this.channels.addAll(channels);
    }

    /**
     * Adds the given alert channels to the current set of channels.
     * @param channels The channels to add
     */
    public void addAlertChannels(List<? extends AlertChannel> channels)
    {
        this.channels.addAll(channels);
    }

    /**
     * Returns the set of alert channels.
     * @return The set of channels
     */
    public List<AlertChannel> getAlertChannels()
    {
        return channels;
    }

    /**
     * Returns the set of email alert channels.
     * @return The set of email channels
     */
    public List<EmailChannel> getEmailChannels()
    {
        List<EmailChannel> channels = new ArrayList<EmailChannel>();
        for(AlertChannel channel : this.channels)
        {
            if(channel instanceof EmailChannel)
                channels.add((EmailChannel)channel);
        }
        return channels;
    }

    /**
     * Returns the set of Slack alert channels.
     * @return The set of Slack channels
     */
    public List<SlackChannel> getSlackChannels()
    {
        List<SlackChannel> channels = new ArrayList<SlackChannel>();
        for(AlertChannel channel : this.channels)
        {
            if(channel instanceof SlackChannel)
                channels.add((SlackChannel)channel);
        }
        return channels;
    }

    /**
     * Returns the set of HipChat alert channels.
     * @return The set of HipChat channels
     */
    public List<HipChatChannel> getHipChatChannels()
    {
        List<HipChatChannel> channels = new ArrayList<HipChatChannel>();
        for(AlertChannel channel : this.channels)
        {
            if(channel instanceof HipChatChannel)
                channels.add((HipChatChannel)channel);
        }
        return channels;
    }

    /**
     * Returns the set of Campfire alert channels.
     * @return The set of Campfire channels
     */
    public List<CampfireChannel> getCampfireChannels()
    {
        List<CampfireChannel> channels = new ArrayList<CampfireChannel>();
        for(AlertChannel channel : this.channels)
        {
            if(channel instanceof CampfireChannel)
                channels.add((CampfireChannel)channel);
        }
        return channels;
    }

    /**
     * Returns the set of OpsGenie alert channels.
     * @return The set of OpsGenie channels
     */
    public List<OpsGenieChannel> getOpsGenieChannels()
    {
        List<OpsGenieChannel> channels = new ArrayList<OpsGenieChannel>();
        for(AlertChannel channel : this.channels)
        {
            if(channel instanceof OpsGenieChannel)
                channels.add((OpsGenieChannel)channel);
        }
        return channels;
    }

    /**
     * Returns the set of PagerDuty alert channels.
     * @return The set of PagerDuty channels
     */
    public List<PagerDutyChannel> getPagerDutyChannels()
    {
        List<PagerDutyChannel> channels = new ArrayList<PagerDutyChannel>();
        for(AlertChannel channel : this.channels)
        {
            if(channel instanceof PagerDutyChannel)
                channels.add((PagerDutyChannel)channel);
        }
        return channels;
    }

    /**
     * Returns the set of user alert channels.
     * @return The set of user channels
     */
    public List<UserChannel> getUserChannels()
    {
        List<UserChannel> channels = new ArrayList<UserChannel>();
        for(AlertChannel channel : this.channels)
        {
            if(channel instanceof UserChannel)
                channels.add((UserChannel)channel);
        }
        return channels;
    }

    /**
     * Returns the set of VictorOps alert channels.
     * @return The set of VictorOps channels
     */
    public List<VictorOpsChannel> getVictorOpsChannels()
    {
        List<VictorOpsChannel> channels = new ArrayList<VictorOpsChannel>();
        for(AlertChannel channel : this.channels)
        {
            if(channel instanceof VictorOpsChannel)
                channels.add((VictorOpsChannel)channel);
        }
        return channels;
    }

    /**
     * Returns the set of webhook alert channels.
     * @return The set of webhook channels
     */
    public List<WebhookChannel> getWebhookChannels()
    {
        List<WebhookChannel> channels = new ArrayList<WebhookChannel>();
        for(AlertChannel channel : this.channels)
        {
            if(channel instanceof WebhookChannel)
                channels.add((WebhookChannel)channel);
        }
        return channels;
    }

    /**
     * Returns the set of xMatters alert channels.
     * @return The set of xMatters channels
     */
    public List<xMattersChannel> getxMattersChannels()
    {
        List<xMattersChannel> channels = new ArrayList<xMattersChannel>();
        for(AlertChannel channel : this.channels)
        {
            if(channel instanceof xMattersChannel)
                channels.add((xMattersChannel)channel);
        }
        return channels;
    }

    /**
     * Returns the number of alert channels.
     * @return The number of alert channels
     */
    public int numAlertChannels()
    {
        return channels.size();
    }

    /**
     * Replaces the alert conditions with the given set of conditions.
     * @param conditions The set of conditions
     */
    public void setAlertConditions(List<AlertCondition> conditions)
    {
        this.conditions.clear();
        this.conditions.addAll(conditions);
    }

    /**
     * Adds the given alert conditions to the current set of conditions.
     * @param conditions The conditions to add
     */
    public void addAlertConditions(List<AlertCondition> conditions)
    {
        this.conditions.addAll(conditions);
    }

    /**
     * Returns the set of alert conditions.
     * @return The set of conditions
     */
    public List<AlertCondition> getAlertConditions()
    {
        return conditions;
    }

    /**
     * Returns the number of alert conditions.
     * @return The number of alert conditions
     */
    public int numAlertConditions()
    {
        return conditions.size();
    }

    /**
     * Returns a string representation of the object.
     */
    @Override
    public String toString()
    {
        return "AlertConfiguration [policies="+policies.size()
            +", channels="+channels.size()
            +", conditions="+conditions.size()
            +"]";
    }
}
