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

/**
 * Represents the factory to create templates.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class TemplateFactory
{
    private static final Logger logger = Logger.getLogger(TemplateFactory.class.getName());

    private static final Map<Class,Class> templates = new HashMap<Class,Class>();

    static
    {
        // Register each of the parsers and renderers with the appropriate template
        AlertPolicyParser.registerWith(AlertPolicyTemplate.class);
    }

    /**
     * Private constructor.
     */
    private TemplateFactory()
    {
    }

    /**
     * Register the given object with the given template class.
     * @param c The class to be registered with the template
     * @param templateClass The template class
     */
    public static void registerTemplate(Class c, Class templateClass)
    {
        templates.put(c, templateClass);
    }

    /**
     * Returns a new template of the given type.
     * @param c The class of the object for the template
     * @return The template created
     */
    public static Template getTemplate(Class c)
    {
        Class templateClass = templates.get(c);
        if(templateClass == null)
            throw new IllegalArgumentException("not a valid template type");

        Template ret = null;
        try
        {
            ret = (Template)templateClass.newInstance();
        }
        catch(InstantiationException e)
        {
            logger.severe("Unable to create template: "+e.getClass().getName()+": "+e.getMessage());
        }
        catch(IllegalAccessException e)
        {
            logger.severe("Unable to access template: "+e.getClass().getName()+": "+e.getMessage());
        }

        return ret;
    }  
}