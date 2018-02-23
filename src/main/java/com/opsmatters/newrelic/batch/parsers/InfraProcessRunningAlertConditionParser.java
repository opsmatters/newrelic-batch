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
import com.opsmatters.newrelic.api.model.alerts.conditions.InfraProcessRunningAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.AlertThreshold;
import com.opsmatters.newrelic.batch.templates.FileTemplate;
import com.opsmatters.newrelic.batch.templates.TemplateFactory;
import com.opsmatters.newrelic.batch.templates.FileInstance;

/**
 * Parser that converts infrastructure process running alert conditions from report lines.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class InfraProcessRunningAlertConditionParser extends InfraAlertConditionParser<InfraProcessRunningAlertCondition>
{
    private static final Logger logger = Logger.getLogger(InfraProcessRunningAlertConditionParser.class.getName());

    /**
     * Private constructor.
     */
    private InfraProcessRunningAlertConditionParser()
    {
    }

    /**
     * Register this class with the given template.
     * @param template The template to register with this class
     */
    public static void registerTemplate(FileTemplate template)
    {
        TemplateFactory.registerTemplate(InfraProcessRunningAlertConditionParser.class, template);
    }

    /**
     * Reads the alert conditions from the given lines.
     * @param policies The set of alert policies for the conditions
     * @param headers The headers of the file
     * @param lines The lines of the file
     * @return The alert conditions read from the lines
     */
    public static List<InfraProcessRunningAlertCondition> parse(List<AlertPolicy> policies, String[] headers, List<String[]> lines)
    {
        return new InfraProcessRunningAlertConditionParser().get(policies, headers, lines);
    }

    /**
     * Reads the alert conditions from the given reader.
     * @param policies The set of alert policies for the conditions
     * @param reader The input stream reader used to read the lines
     * @return The alert conditions read from the lines
     * @throws IOException if there is a problem reading the input file or it does not exist
     */
    public static List<InfraProcessRunningAlertCondition> parse(List<AlertPolicy> policies, InputFileReader reader) throws IOException
    {
        reader.parse();
        return parse(policies, reader.getHeaders(), reader.getRows());
    }

    /**
     * Creates the alert conditions from the given lines.
     * @param policies The set of alert policies for the conditions
     * @param headers The headers of the file
     * @param lines The input file lines
     * @return The alert conditions created from the lines
     */
    protected List<InfraProcessRunningAlertCondition> get(List<AlertPolicy> policies, String[] headers, List<String[]> lines)
    {
        List<InfraProcessRunningAlertCondition> ret = new ArrayList<InfraProcessRunningAlertCondition>();
        FileInstance file = TemplateFactory.getTemplate(getClass()).getInstance(headers);
        AlertPolicyList policyList = new AlertPolicyList(policies);
        logger.info("Processing "+file.getType()+" file: policies="+policies.size()
            +" headers="+headers.length+" lines="+lines.size());

        file.checkColumns();
        for(String[] line : lines)
        {
            // Check that the line matches the file type
            if(!file.matches(line))
            {
                logger.severe("found illegal line in "+file.getType()+" file: "+file.getType(line));
                continue;
            }

            InfraProcessRunningAlertCondition condition = create(file, line);
            setPolicyId(condition, policyList, file.getString(InfraProcessRunningAlertCondition.POLICY_NAME, line));
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
    protected InfraProcessRunningAlertCondition create(FileInstance file, String[] line)
    {
        String name = file.getString(InfraProcessRunningAlertCondition.NAME, line);
        Integer criticalThreshold = file.getInteger(InfraProcessRunningAlertCondition.CRITICAL_THRESHOLD, line);
        Integer duration = file.getInteger(AlertThreshold.DURATION, line);
        String whereClause = file.getString(InfraProcessRunningAlertCondition.WHERE_CLAUSE, line);
        String processWhereClause = file.getString(InfraProcessRunningAlertCondition.PROCESS_WHERE_CLAUSE, line);
        String comparison = file.getString(InfraProcessRunningAlertCondition.COMPARISON, line);

        InfraProcessRunningAlertCondition ret = InfraProcessRunningAlertCondition.builder()
            .name(file.getString(InfraProcessRunningAlertCondition.NAME, line))
            .criticalThreshold(getThreshold(criticalThreshold, duration))
            .comparison(comparison)
            .whereClause(whereClause)
            .processWhereClause(processWhereClause)
            .enabled(true)
            .build();

        if(ret.getCriticalThreshold() == null)
            throw new IllegalStateException("Infra alert condition missing critical threshold: "+name);

        return ret;
    }
}