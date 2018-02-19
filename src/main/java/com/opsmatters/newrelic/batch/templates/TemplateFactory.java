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
    private static final Map<Class,Template> templates = new HashMap<Class,Template>();
    private static final Template alertPolicyTemplate = new AlertPolicyTemplate();
    private static final Template emailChannelTemplate = new EmailChannelTemplate();
    private static final Template slackChannelTemplate = new SlackChannelTemplate();

    static
    {
        // Register each of the parsers and renderers with the appropriate template
        AlertPolicyParser.registerTemplate(alertPolicyTemplate);
        AlertPolicyRenderer.registerTemplate(alertPolicyTemplate);
        EmailChannelParser.registerTemplate(emailChannelTemplate);
        EmailChannelRenderer.registerTemplate(emailChannelTemplate);
        SlackChannelParser.registerTemplate(slackChannelTemplate);
        SlackChannelRenderer.registerTemplate(slackChannelTemplate);
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
    public static void registerTemplate(Class c, Template template)
    {
        templates.put(c, template);
    }

    /**
     * Returns the template for the given class.
     * @param c The class of the object for the template
     * @return The template for the class
     */
    public static Template getTemplate(Class c)
    {
        Template ret = templates.get(c);
        if(ret == null)
            throw new IllegalArgumentException("not a valid template type");
        return ret;
    }  
}