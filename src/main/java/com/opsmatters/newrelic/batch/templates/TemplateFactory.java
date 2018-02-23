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

package com.opsmatters.newrelic.batch.templates;

import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import com.opsmatters.newrelic.batch.parsers.*;
import com.opsmatters.newrelic.batch.renderers.*;

/**
 * Represents the factory to create templates.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class TemplateFactory
{
    private static final Logger logger = Logger.getLogger(TemplateFactory.class.getName());

    // The templates
    private static final Map<Class,FileTemplate> templates = new HashMap<Class,FileTemplate>();
    private static final FileTemplate alertPolicyTemplate = new AlertPolicyTemplate();
    private static final FileTemplate emailChannelTemplate = new EmailChannelTemplate();
    private static final FileTemplate slackChannelTemplate = new SlackChannelTemplate();
    private static final FileTemplate hipchatChannelTemplate = new HipChatChannelTemplate();
    private static final FileTemplate campfireChannelTemplate = new CampfireChannelTemplate();
    private static final FileTemplate opsgenieChannelTemplate = new OpsGenieChannelTemplate();
    private static final FileTemplate pagerdutyChannelTemplate = new PagerDutyChannelTemplate();
    private static final FileTemplate userChannelTemplate = new UserChannelTemplate();
    private static final FileTemplate victoropsChannelTemplate = new VictorOpsChannelTemplate();
    private static final FileTemplate xmattersChannelTemplate = new xMattersChannelTemplate();
    private static final FileTemplate alertConditionTemplate = new AlertConditionTemplate();
    private static final FileTemplate externalServiceAlertConditionTemplate = new ExternalServiceAlertConditionTemplate();
    private static final FileTemplate nrqlAlertConditionTemplate = new NrqlAlertConditionTemplate();
    private static final FileTemplate infraMetricAlertConditionTemplate = new InfraMetricAlertConditionTemplate();
    private static final FileTemplate infraProcessRunningAlertConditionTemplate = new InfraProcessRunningAlertConditionTemplate();
    private static final FileTemplate infraHostNotReportingAlertConditionTemplate = new InfraHostNotReportingAlertConditionTemplate();

    static
    {
        // Register each of the parsers and renderers with the appropriate template
        AlertPolicyParser.registerTemplate(alertPolicyTemplate);
        AlertPolicyRenderer.registerTemplate(alertPolicyTemplate);
        EmailChannelParser.registerTemplate(emailChannelTemplate);
        EmailChannelRenderer.registerTemplate(emailChannelTemplate);
        SlackChannelParser.registerTemplate(slackChannelTemplate);
        SlackChannelRenderer.registerTemplate(slackChannelTemplate);
        HipChatChannelParser.registerTemplate(hipchatChannelTemplate);
        HipChatChannelRenderer.registerTemplate(hipchatChannelTemplate);
        CampfireChannelParser.registerTemplate(campfireChannelTemplate);
        CampfireChannelRenderer.registerTemplate(campfireChannelTemplate);
        OpsGenieChannelParser.registerTemplate(opsgenieChannelTemplate);
        OpsGenieChannelRenderer.registerTemplate(opsgenieChannelTemplate);
        PagerDutyChannelParser.registerTemplate(pagerdutyChannelTemplate);
        PagerDutyChannelRenderer.registerTemplate(pagerdutyChannelTemplate);
        UserChannelParser.registerTemplate(userChannelTemplate);
        UserChannelRenderer.registerTemplate(userChannelTemplate);
        VictorOpsChannelParser.registerTemplate(victoropsChannelTemplate);
        VictorOpsChannelRenderer.registerTemplate(victoropsChannelTemplate);
        xMattersChannelParser.registerTemplate(xmattersChannelTemplate);
        xMattersChannelRenderer.registerTemplate(xmattersChannelTemplate);
        AlertConditionParser.registerTemplate(alertConditionTemplate);
        AlertConditionRenderer.registerTemplate(alertConditionTemplate);
        ExternalServiceAlertConditionParser.registerTemplate(externalServiceAlertConditionTemplate);
        ExternalServiceAlertConditionRenderer.registerTemplate(externalServiceAlertConditionTemplate);
        NrqlAlertConditionParser.registerTemplate(nrqlAlertConditionTemplate);
        NrqlAlertConditionRenderer.registerTemplate(nrqlAlertConditionTemplate);
        InfraMetricAlertConditionParser.registerTemplate(infraMetricAlertConditionTemplate);
        InfraMetricAlertConditionRenderer.registerTemplate(infraMetricAlertConditionTemplate);
        InfraProcessRunningAlertConditionParser.registerTemplate(infraProcessRunningAlertConditionTemplate);
        InfraProcessRunningAlertConditionRenderer.registerTemplate(infraProcessRunningAlertConditionTemplate);
        InfraHostNotReportingAlertConditionParser.registerTemplate(infraHostNotReportingAlertConditionTemplate);
        InfraHostNotReportingAlertConditionRenderer.registerTemplate(infraHostNotReportingAlertConditionTemplate);
    }

    /**
     * Private constructor.
     */
    private TemplateFactory()
    {
    }

    /**
     * Register the given object with the given template.
     * @param c The class to be registered with the template
     * @param template The template to register the class with
     */
    public static void registerTemplate(Class c, FileTemplate template)
    {
        templates.put(c, template);
    }

    /**
     * Returns the template for the given class.
     * @param c The class of the object for the template
     * @return The template for the class
     */
    public static FileTemplate getTemplate(Class c)
    {
        FileTemplate ret = templates.get(c);
        if(ret == null)
            throw new IllegalArgumentException("not a valid template type");
        return ret;
    }  
}