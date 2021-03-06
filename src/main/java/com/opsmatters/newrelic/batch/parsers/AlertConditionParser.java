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
import com.opsmatters.newrelic.api.model.alerts.conditions.AlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.ApmAppAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.ApmKeyTransactionAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.ApmJvmAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.ServersAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.BrowserAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.MobileAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.Term;
import com.opsmatters.newrelic.api.model.Entity;
import com.opsmatters.newrelic.batch.templates.FileTemplate;
import com.opsmatters.newrelic.batch.templates.TemplateFactory;
import com.opsmatters.newrelic.batch.templates.FileInstance;

/**
 * Parser that converts alert conditions from report lines.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class AlertConditionParser extends MetricConditionParser<AlertCondition>
{
    private static final Logger logger = Logger.getLogger(AlertConditionParser.class.getName());

    /**
     * Private constructor.
     */
    private AlertConditionParser()
    {
    }

    /**
     * Register this class with the given template.
     * @param template The template to register with this class
     */
    public static void registerTemplate(FileTemplate template)
    {
        TemplateFactory.registerTemplate(AlertConditionParser.class, template);
    }

    /**
     * Reads the alert conditions from the given lines.
     * @param policies The set of alert policies for the conditions
     * @param entities The set of entities for the conditions
     * @param headers The headers of the file
     * @param lines The lines of the file
     * @return The alert conditions read from the lines
     */
    public static List<AlertCondition> parse(List<AlertPolicy> policies, List<Entity> entities, String[] headers, List<String[]> lines)
    {
        return new AlertConditionParser().get(policies, entities, headers, lines);
    }

    /**
     * Reads the alert conditions from the given reader.
     * @param policies The set of alert policies for the conditions
     * @param entities The set of entities for the conditions
     * @param reader The input stream reader used to read the lines
     * @return The alert conditions read from the lines
     * @throws IOException if there is a problem reading the input file or it does not exist
     */
    public static List<AlertCondition> parse(List<AlertPolicy> policies, List<Entity> entities, InputFileReader reader) throws IOException
    {
        reader.parse();
        return parse(policies, entities, reader.getHeaders(), reader.getRows());
    }

    /**
     * Reads the alert condition from the given line.
     * @param file The file instance with the columns
     * @param line The input file line
     * @return The alert condition created
     */
    protected AlertCondition create(FileInstance file, String[] line)
    {
        String name = file.getString(AlertCondition.NAME, line);
        String type = file.getString(AlertCondition.CONDITION_TYPE, line);
        if(type == null || type.length() == 0)
            throw new IllegalArgumentException("alert condition missing type: "+name);
        List<Term> terms = getTerms(file, line);

        AlertCondition ret = null;
        switch(AlertCondition.ConditionType.fromValue(type))
        {
            case APM_APP:
                ret = getApmAppMetricCondition(file, type, line, terms);
                break;
            case APM_KEY_TRANSACTION:
                ret = getApmKeyTransactionMetricCondition(file, type, line, terms);
                break;
            case APM_JVM:
                ret = getApmJvmMetricCondition(file, type, line, terms);
                break;
            case SERVERS:
                ret = getServersMetricCondition(file, type, line, terms);
                break;
            case BROWSER:
                ret = getBrowserMetricCondition(file, type, line, terms);
                break;
            case MOBILE:
                ret = getMobileMetricCondition(file, type, line, terms);
                break;
        }

        if(ret == null)
            throw new IllegalArgumentException("Unknown alert condition type: "+type);
        if(ret.numTerms() == 0)
            throw new IllegalArgumentException("alert condition missing thresholds: "+name);

        return ret;
    }

    /**
     * Returns an alert condition of type "apm_app_metric".
     */
    private AlertCondition getApmAppMetricCondition(FileInstance file, String type, String[] line, List<Term> terms)
    {
        // Check the metric is valid
        String metric = file.getString(AlertCondition.METRIC, line);
        if(!ApmAppAlertCondition.Metric.contains(metric))
            throw new IllegalArgumentException("invalid metric for "+type+" alert condition: "+metric);

        return ApmAppAlertCondition.builder()
            .name(file.getString(AlertCondition.NAME, line))
            .metric(metric)
            .conditionScope(file.getString(AlertCondition.CONDITION_SCOPE, line))
            .violationCloseTimer(file.getInteger(AlertCondition.VIOLATION_CLOSE_TIMER, line))
            .terms(terms)
            .enabled(true)
            .build();
    }

    /**
     * Returns an alert condition of type "apm_kt_metric".
     */
    private AlertCondition getApmKeyTransactionMetricCondition(FileInstance file, String type, String[] line, List<Term> terms)
    {
        // Check the metric is valid
        String metric = file.getString(AlertCondition.METRIC, line);
        if(!ApmKeyTransactionAlertCondition.Metric.contains(metric))
            throw new IllegalArgumentException("invalid metric for "+type+" alert condition: "+metric);

        return ApmKeyTransactionAlertCondition.builder()
            .name(file.getString(AlertCondition.NAME, line))
            .metric(metric)
            .conditionScope(file.getString(AlertCondition.CONDITION_SCOPE, line))
            .violationCloseTimer(file.getInteger(AlertCondition.VIOLATION_CLOSE_TIMER, line))
            .terms(terms)
            .enabled(true)
            .build();
    }

    /**
     * Returns an alert condition of type "apm_jvm_metric".
     */
    private AlertCondition getApmJvmMetricCondition(FileInstance file, String type, String[] line, List<Term> terms)
    {
        // Check the metric is valid
        String metric = file.getString(AlertCondition.METRIC, line);
        if(!ApmJvmAlertCondition.Metric.contains(metric))
            throw new IllegalArgumentException("invalid metric for "+type+" alert condition: "+metric);

        return ApmJvmAlertCondition.builder()
            .name(file.getString(AlertCondition.NAME, line))
            .metric(metric)
            .conditionScope(file.getString(AlertCondition.CONDITION_SCOPE, line))
            .violationCloseTimer(file.getInteger(AlertCondition.VIOLATION_CLOSE_TIMER, line))
            .terms(terms)
            .enabled(true)
            .build();
    }

    /**
     * Returns an alert condition of type "servers_metric".
     */
    private AlertCondition getServersMetricCondition(FileInstance file, String type, String[] line, List<Term> terms)
    {
        // Check the metric is valid
        String metric = file.getString(AlertCondition.METRIC, line);
        if(!ServersAlertCondition.Metric.contains(metric))
            throw new IllegalArgumentException("invalid metric for "+type+" alert condition: "+metric);

        return ServersAlertCondition.builder()
            .name(file.getString(AlertCondition.NAME, line))
            .metric(metric)
            .conditionScope(file.getString(AlertCondition.CONDITION_SCOPE, line))
            .violationCloseTimer(file.getInteger(AlertCondition.VIOLATION_CLOSE_TIMER, line))
            .terms(terms)
            .enabled(true)
            .build();
    }

    /**
     * Returns an alert condition of type "browser_metric".
     */
    private AlertCondition getBrowserMetricCondition(FileInstance file, String type, String[] line, List<Term> terms)
    {
        // Check the metric is valid
        String metric = file.getString(AlertCondition.METRIC, line);
        if(!BrowserAlertCondition.Metric.contains(metric))
            throw new IllegalArgumentException("invalid metric for "+type+" alert condition: "+metric);

        return BrowserAlertCondition.builder()
            .name(file.getString(AlertCondition.NAME, line))
            .metric(metric)
            .conditionScope(file.getString(AlertCondition.CONDITION_SCOPE, line))
            .violationCloseTimer(file.getInteger(AlertCondition.VIOLATION_CLOSE_TIMER, line))
            .terms(terms)
            .enabled(true)
            .build();
    }

    /**
     * Returns an alert condition of type "mobile_metric".
     */
    private AlertCondition getMobileMetricCondition(FileInstance file, String type, String[] line, List<Term> terms)
    {
        // Check the metric is valid
        String metric = file.getString(AlertCondition.METRIC, line);
        if(!MobileAlertCondition.Metric.contains(metric))
            throw new IllegalArgumentException("invalid metric for "+type+" alert condition: "+metric);

        return MobileAlertCondition.builder()
            .name(file.getString(AlertCondition.NAME, line))
            .metric(metric)
            .conditionScope(file.getString(AlertCondition.CONDITION_SCOPE, line))
            .violationCloseTimer(file.getInteger(AlertCondition.VIOLATION_CLOSE_TIMER, line))
            .terms(terms)
            .enabled(true)
            .build();
    }
}