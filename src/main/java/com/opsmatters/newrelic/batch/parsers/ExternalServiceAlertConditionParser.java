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
import java.util.logging.Logger;
import com.opsmatters.core.documents.InputFileReader;
import com.opsmatters.newrelic.api.model.alerts.policies.AlertPolicy;
import com.opsmatters.newrelic.api.model.alerts.conditions.ExternalServiceAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.ApmExternalServiceAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.MobileExternalServiceAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.Term;
import com.opsmatters.newrelic.api.model.Entity;
import com.opsmatters.newrelic.batch.templates.FileTemplate;
import com.opsmatters.newrelic.batch.templates.TemplateFactory;
import com.opsmatters.newrelic.batch.templates.FileInstance;

/**
 * Parser that converts external service alert conditions from report lines.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class ExternalServiceAlertConditionParser extends MetricConditionParser<ExternalServiceAlertCondition>
{
    private static final Logger logger = Logger.getLogger(ExternalServiceAlertConditionParser.class.getName());

    /**
     * Private constructor.
     */
    private ExternalServiceAlertConditionParser()
    {
    }

    /**
     * Register this class with the given template.
     * @param template The template to register with this class
     */
    public static void registerTemplate(FileTemplate template)
    {
        TemplateFactory.registerTemplate(ExternalServiceAlertConditionParser.class, template);
    }

    /**
     * Reads the alert conditions from the given lines.
     * @param policies The set of alert policies for the conditions
     * @param entities The set of entities for the conditions
     * @param headers The headers of the file
     * @param lines The lines of the file
     * @return The alert conditions read from the lines
     */
    public static List<ExternalServiceAlertCondition> parse(List<AlertPolicy> policies, List<Entity> entities, String[] headers, List<String[]> lines)
    {
        return new ExternalServiceAlertConditionParser().get(policies, entities, headers, lines);
    }

    /**
     * Reads the alert conditions from the given reader.
     * @param policies The set of alert policies for the conditions
     * @param entities The set of entities for the conditions
     * @param reader The input stream reader used to read the lines
     * @return The alert conditions read from the lines
     * @throws IOException if there is a problem reading the input file or it does not exist
     */
    public static List<ExternalServiceAlertCondition> parse(List<AlertPolicy> policies, List<Entity> entities, InputFileReader reader) throws IOException
    {
        reader.parse();
        return parse(policies, entities, reader.getHeaders(), reader.getRows());
    }

    /**
     * Creates the alert conditions from the given lines.
     * @param policies The set of alert policies for the conditions
     * @param entities The set of entities for the conditions
     * @param headers The headers of the file
     * @param lines The input file lines
     * @return The alert conditions created from the lines
     */
    /**
     * Reads the alert condition from the given line.
     * @param file The file instance with the columns
     * @param line The input file line
     * @return The alert condition created
     */
    protected ExternalServiceAlertCondition create(FileInstance file, String[] line)
    {
        String name = file.getString(ExternalServiceAlertCondition.NAME, line);
        String type = file.getString(ExternalServiceAlertCondition.CONDITION_TYPE, line);
        if(type == null || type.length() == 0)
            throw new IllegalArgumentException("alert condition missing type: "+name);
        List<Term> terms = getTerms(file, line);

        ExternalServiceAlertCondition ret = null;
        switch(ExternalServiceAlertCondition.ConditionType.fromValue(type))
        {
            case APM:
                ret = getApmExternalServiceCondition(file, type, line, terms);
                break;
            case MOBILE:
                ret = getMobileExternalServiceCondition(file, type, line, terms);
                break;
        }

        if(ret == null)
            throw new IllegalArgumentException("Unknown external service alert condition type: "+type);
        if(ret.numTerms() == 0)
            throw new IllegalArgumentException("external service alert condition missing thresholds: "+name);

        return ret;
    }

    /**
     * Returns an alert condition of type "apm_external_service".
     */
    private ExternalServiceAlertCondition getApmExternalServiceCondition(FileInstance file, String type, String[] line, List<Term> terms)
    {
        // Check the metric is valid
        String metric = file.getString(ExternalServiceAlertCondition.METRIC, line);
        if(!ApmExternalServiceAlertCondition.Metric.contains(metric))
            throw new IllegalArgumentException("invalid metric for "+type+" alert condition: "+metric);

        return ApmExternalServiceAlertCondition.builder()
            .name(file.getString(ExternalServiceAlertCondition.NAME, line))
            .metric(metric)
            .externalServiceUrl(file.getString(ExternalServiceAlertCondition.EXTERNAL_SERVICE_URL, line))
            .terms(terms)
            .enabled(true)
            .build();
    }

    /**
     * Returns an alert condition of type "mobile_external_service".
     */
    private ExternalServiceAlertCondition getMobileExternalServiceCondition(FileInstance file, String type, String[] line, List<Term> terms)
    {
        // Check the metric is valid
        String metric = file.getString(ExternalServiceAlertCondition.METRIC, line);
        if(!MobileExternalServiceAlertCondition.Metric.contains(metric))
            throw new IllegalArgumentException("invalid metric for "+type+" alert condition: "+metric);

        return MobileExternalServiceAlertCondition.builder()
            .name(file.getString(ExternalServiceAlertCondition.NAME, line))
            .metric(metric)
            .externalServiceUrl(file.getString(ExternalServiceAlertCondition.EXTERNAL_SERVICE_URL, line))
            .terms(terms)
            .enabled(true)
            .build();
    }
}