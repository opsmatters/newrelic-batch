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

package com.opsmatters.newrelic.batch.parsers;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import com.opsmatters.core.reports.InputFileReader;
import com.opsmatters.newrelic.api.model.alerts.policies.AlertPolicy;
import com.opsmatters.newrelic.batch.templates.Template;
import com.opsmatters.newrelic.batch.templates.TemplateFactory;

/**
 * Parser that converts alert policies from report rows.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class AlertPolicyParser
{
    private static final Logger logger = Logger.getLogger(AlertPolicyParser.class.getName());

    /**
     * Private constructor.
     */
    private AlertPolicyParser()
    {
    }

    /**
     * Register this class with the given template class.
     * @param templateClass The template class
     */
    public static void registerWith(Class templateClass)
    {
        TemplateFactory.registerTemplate(AlertPolicyParser.class, templateClass);
    }

    /**
     * Reads the alert policies from the given lines.
     * @param headers The headers of the file
     * @param lines The lines of the file
     * @return The alert policies read from the lines
     */
    public static List<AlertPolicy> parse(String[] headers, List<String[]> lines)
    {
        return new AlertPolicyParser().getAlertPolicies(headers, lines);
    }

    /**
     * Reads the alert policies from the given reader.
     * @param reader The input stream reader used to read the lines
     * @return The alert policies read from the lines
     * @throws IOException if there is a problem reading the input file or it does not exist
     */
    public static List<AlertPolicy> parse(InputFileReader reader) throws IOException
    {
        reader.parse();
        return parse(reader.getHeaders(), reader.getRows());
    }

    /**
     * Reads the alert policies from the given lines.
     * @param headers The headers of the file
     * @param lines The input file lines
     * @return The alert policies read from the lines
     */
    private List<AlertPolicy> getAlertPolicies(String[] headers, List<String[]> lines)
    {
        List<AlertPolicy> ret = new ArrayList<AlertPolicy>();
        Template template = TemplateFactory.getTemplate(getClass()).headers(headers);
        logger.info("Processing alert policies: headers="+headers.length+" lines="+lines.size());

        template.checkColumns();
        for(String[] line : lines)
        {
            // Check that the line matches the template type
            if(!template.matches(line))
            {
                logger.severe("found illegal line in "+template.getType()+" file: "+template.getType(line));
                continue;
            }

            AlertPolicy policy = AlertPolicy.builder()
                .name(template.getStringValue(AlertPolicy.NAME, line))
                .incidentPreference(template.getStringValue(AlertPolicy.INCIDENT_PREFERENCE, line))
                .build();
            ret.add(policy);
        }

        return ret;
    }
}