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
import com.opsmatters.core.documents.InputFileReader;
import com.opsmatters.newrelic.api.model.alerts.policies.AlertPolicy;
import com.opsmatters.newrelic.api.model.alerts.policies.AlertPolicyList;
import com.opsmatters.newrelic.api.model.alerts.conditions.AlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.ApmAppAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.ApmKeyTransactionAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.ApmJvmAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.ServersAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.BrowserAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.MobileAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.Term;
import com.opsmatters.newrelic.api.model.alerts.Priority;
import com.opsmatters.newrelic.api.model.applications.Application;
import com.opsmatters.newrelic.api.model.applications.ApplicationList;
import com.opsmatters.newrelic.batch.templates.FileTemplate;
import com.opsmatters.newrelic.batch.templates.TemplateFactory;
import com.opsmatters.newrelic.batch.templates.FileInstance;

/**
 * Parser that converts alert conditions from report lines.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class AlertConditionParser extends InputFileParser<AlertCondition>
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
     * @param applications The set of applications for the conditions
     * @param headers The headers of the file
     * @param lines The lines of the file
     * @return The alert conditions read from the lines
     */
    public static List<AlertCondition> parse(List<AlertPolicy> policies, List<Application> applications, String[] headers, List<String[]> lines)
    {
        return new AlertConditionParser().get(policies, applications, headers, lines);
    }

    /**
     * Reads the alert conditions from the given reader.
     * @param policies The set of alert policies for the conditions
     * @param applications The set of applications for the conditions
     * @param reader The input stream reader used to read the lines
     * @return The alert conditions read from the lines
     * @throws IOException if there is a problem reading the input file or it does not exist
     */
    public static List<AlertCondition> parse(List<AlertPolicy> policies, List<Application> applications, InputFileReader reader) throws IOException
    {
        reader.parse();
        return parse(policies, applications, reader.getHeaders(), reader.getRows());
    }

    /**
     * Creates the alert conditions from the given lines.
     * @param policies The set of alert policies for the conditions
     * @param applications The set of applications for the conditions
     * @param headers The headers of the file
     * @param lines The input file lines
     * @return The alert conditions created from the lines
     */
    protected List<AlertCondition> get(List<AlertPolicy> policies, List<Application> applications, String[] headers, List<String[]> lines)
    {
        List<AlertCondition> ret = new ArrayList<AlertCondition>();
        FileInstance file = TemplateFactory.getTemplate(getClass()).getInstance(headers);
        AlertPolicyList policyList = new AlertPolicyList(policies);
        ApplicationList applicationList = new ApplicationList(applications);
        logger.info("Processing "+file.getType()+" file: policies="+policies.size()
            +" applications="+applications.size()+" headers="+headers.length+" lines="+lines.size());

        file.checkColumns();
        for(String[] line : lines)
        {
            // Check that the line matches the file type
            if(!file.matches(line))
            {
                logger.severe("found illegal line in "+file.getType()+" file: "+file.getType(line));
                continue;
            }

            AlertCondition condition = create(file, line);

            // Add policy id
            String policyName = file.getString(AlertCondition.POLICY_NAME, line);
            AlertPolicy policy = policyList.get(policyName);
            if(policy == null)
                throw new IllegalStateException("unable to find policy \""+policyName+"\" for alert condition: "+condition.getName());
            if(policy.getId() == null || policy.getId() == 0L)
                throw new IllegalStateException("missing policy_id: "+policy.getName());
            condition.setPolicyId(policy.getId());

            // Add entity ids
            String filter = file.getString(AlertCondition.FILTER, line);
            String entities = file.getString(AlertCondition.ENTITIES, line);
            if(filter != null)
                condition.setEntities(toIdList(applicationList.list(filter)));
            else if(entities != null)
                condition.setEntities(toIdList(entities));

            ret.add(condition);
        }

        return ret;
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
        String criticalThreshold = file.getString(Term.CRITICAL_THRESHOLD, line);
        String warningThreshold = file.getString(Term.WARNING_THRESHOLD, line);
        String duration = file.getString(Term.DURATION, line);
        String operator = file.getString(Term.OPERATOR, line);
        String timeFunction = file.getString(Term.TIME_FUNCTION, line);

        Term critical = null;
        if(criticalThreshold != null && criticalThreshold.length() > 0)
            critical = getTerm(criticalThreshold, duration, Priority.CRITICAL, operator, timeFunction);
        if(critical == null)
            throw new IllegalArgumentException("alert condition missing thresholds: "+name);

        Term warning = null;
        if(warningThreshold != null && warningThreshold.length() > 0)
            warning = getTerm(warningThreshold, duration, Priority.WARNING, operator, timeFunction);

        AlertCondition ret = null;
        switch(AlertCondition.ConditionType.fromValue(type))
        {
            case APM_APP:
                ret = getApmAppMetricCondition(file, type, line, critical, warning);
                break;
            case APM_KEY_TRANSACTION:
                ret = getApmKeyTransactionMetricCondition(file, type, line, critical, warning);
                break;
            case APM_JVM:
                ret = getApmJvmMetricCondition(file, type, line, critical, warning);
                break;
            case SERVERS:
                ret = getServersMetricCondition(file, type, line, critical, warning);
                break;
            case BROWSER:
                ret = getBrowserMetricCondition(file, type, line, critical, warning);
                break;
            case MOBILE:
                ret = getMobileMetricCondition(file, type, line, critical, warning);
                break;
        }

        return ret;
    }

    /**
     * Returns a term for the alert condition.
     */
    private Term getTerm(String threshold, String duration, Priority priority, String operator, String timeFunction)
    {
        return Term.builder()
            .threshold(threshold)
            .duration(duration)
            .priority(priority)
            .operator(operator)
            .timeFunction(timeFunction)
            .build();
    }

    /**
     * Returns an alert condition of type "apm_app_metric".
     */
    private AlertCondition getApmAppMetricCondition(FileInstance file, String type, String[] line, Term critical, Term warning)
    {
        // Check the metric is valid
        String metric = file.getString(AlertCondition.METRIC, line);
        if(!ApmAppAlertCondition.Metric.contains(metric))
            throw new IllegalArgumentException("invalid metric for "+type+" alert condition: "+metric);

        ApmAppAlertCondition.Builder builder = ApmAppAlertCondition.builder()
            .name(file.getString(AlertCondition.NAME, line))
            .metric(metric)
            .conditionScope(file.getString(AlertCondition.CONDITION_SCOPE, line))
            .violationCloseTimer(file.getInteger(AlertCondition.VIOLATION_CLOSE_TIMER, line))
            .enabled(true);

        if(critical != null)
            builder = builder.addTerm(critical);

        if(warning != null)
            builder = builder.addTerm(warning);

        return builder.build();
    }

    /**
     * Returns an alert condition of type "apm_kt_metric".
     */
    private AlertCondition getApmKeyTransactionMetricCondition(FileInstance file, String type, String[] line, Term critical, Term warning)
    {
        // Check the metric is valid
        String metric = file.getString(AlertCondition.METRIC, line);
        if(!ApmKeyTransactionAlertCondition.Metric.contains(metric))
            throw new IllegalArgumentException("invalid metric for "+type+" alert condition: "+metric);

        ApmKeyTransactionAlertCondition.Builder builder = ApmKeyTransactionAlertCondition.builder()
            .name(file.getString(AlertCondition.NAME, line))
            .metric(metric)
            .conditionScope(file.getString(AlertCondition.CONDITION_SCOPE, line))
            .violationCloseTimer(file.getInteger(AlertCondition.VIOLATION_CLOSE_TIMER, line))
            .enabled(true);

        if(critical != null)
            builder = builder.addTerm(critical);

        if(warning != null)
            builder = builder.addTerm(warning);

        return builder.build();
    }

    /**
     * Returns an alert condition of type "apm_jvm_metric".
     */
    private AlertCondition getApmJvmMetricCondition(FileInstance file, String type, String[] line, Term critical, Term warning)
    {
        // Check the metric is valid
        String metric = file.getString(AlertCondition.METRIC, line);
        if(!ApmJvmAlertCondition.Metric.contains(metric))
            throw new IllegalArgumentException("invalid metric for "+type+" alert condition: "+metric);

        ApmJvmAlertCondition.Builder builder = ApmJvmAlertCondition.builder()
            .name(file.getString(AlertCondition.NAME, line))
            .metric(metric)
            .conditionScope(file.getString(AlertCondition.CONDITION_SCOPE, line))
            .violationCloseTimer(file.getInteger(AlertCondition.VIOLATION_CLOSE_TIMER, line))
            .enabled(true);

        if(critical != null)
            builder = builder.addTerm(critical);

        if(warning != null)
            builder = builder.addTerm(warning);

        return builder.build();
    }

    /**
     * Returns an alert condition of type "servers_metric".
     */
    private AlertCondition getServersMetricCondition(FileInstance file, String type, String[] line, Term critical, Term warning)
    {
        // Check the metric is valid
        String metric = file.getString(AlertCondition.METRIC, line);
        if(!ServersAlertCondition.Metric.contains(metric))
            throw new IllegalArgumentException("invalid metric for "+type+" alert condition: "+metric);

        ServersAlertCondition.Builder builder = ServersAlertCondition.builder()
            .name(file.getString(AlertCondition.NAME, line))
            .metric(metric)
            .conditionScope(file.getString(AlertCondition.CONDITION_SCOPE, line))
            .violationCloseTimer(file.getInteger(AlertCondition.VIOLATION_CLOSE_TIMER, line))
            .enabled(true);

        if(critical != null)
            builder = builder.addTerm(critical);

        if(warning != null)
            builder = builder.addTerm(warning);

        return builder.build();
    }

    /**
     * Returns an alert condition of type "browser_metric".
     */
    private AlertCondition getBrowserMetricCondition(FileInstance file, String type, String[] line, Term critical, Term warning)
    {
        // Check the metric is valid
        String metric = file.getString(AlertCondition.METRIC, line);
        if(!BrowserAlertCondition.Metric.contains(metric))
            throw new IllegalArgumentException("invalid metric for "+type+" alert condition: "+metric);

        BrowserAlertCondition.Builder builder = BrowserAlertCondition.builder()
            .name(file.getString(AlertCondition.NAME, line))
            .metric(metric)
            .conditionScope(file.getString(AlertCondition.CONDITION_SCOPE, line))
            .violationCloseTimer(file.getInteger(AlertCondition.VIOLATION_CLOSE_TIMER, line))
            .enabled(true);

        if(critical != null)
            builder = builder.addTerm(critical);

        if(warning != null)
            builder = builder.addTerm(warning);

        return builder.build();
    }

    /**
     * Returns an alert condition of type "mobile_metric".
     */
    private AlertCondition getMobileMetricCondition(FileInstance file, String type, String[] line, Term critical, Term warning)
    {
        // Check the metric is valid
        String metric = file.getString(AlertCondition.METRIC, line);
        if(!MobileAlertCondition.Metric.contains(metric))
            throw new IllegalArgumentException("invalid metric for "+type+" alert condition: "+metric);

        MobileAlertCondition.Builder builder = MobileAlertCondition.builder()
            .name(file.getString(AlertCondition.NAME, line))
            .metric(metric)
            .conditionScope(file.getString(AlertCondition.CONDITION_SCOPE, line))
            .violationCloseTimer(file.getInteger(AlertCondition.VIOLATION_CLOSE_TIMER, line))
            .enabled(true);

        if(critical != null)
            builder = builder.addTerm(critical);

        if(warning != null)
            builder = builder.addTerm(warning);

        return builder.build();
    }
}