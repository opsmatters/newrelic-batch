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
import com.opsmatters.newrelic.api.model.alerts.conditions.InfraMetricAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.AlertThreshold;
import com.opsmatters.newrelic.batch.templates.FileTemplate;
import com.opsmatters.newrelic.batch.templates.TemplateFactory;
import com.opsmatters.newrelic.batch.templates.FileInstance;

/**
 * Parser that converts infrastructure metric alert conditions from report lines.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class InfraMetricAlertConditionParser extends InfraAlertConditionParser<InfraMetricAlertCondition>
{
    private static final Logger logger = Logger.getLogger(InfraMetricAlertConditionParser.class.getName());

    /**
     * Private constructor.
     */
    private InfraMetricAlertConditionParser()
    {
    }

    /**
     * Register this class with the given template.
     * @param template The template to register with this class
     */
    public static void registerTemplate(FileTemplate template)
    {
        TemplateFactory.registerTemplate(InfraMetricAlertConditionParser.class, template);
    }

    /**
     * Reads the alert conditions from the given lines.
     * @param policies The set of alert policies for the conditions
     * @param headers The headers of the file
     * @param lines The lines of the file
     * @return The alert conditions read from the lines
     */
    public static List<InfraMetricAlertCondition> parse(List<AlertPolicy> policies, String[] headers, List<String[]> lines)
    {
        return new InfraMetricAlertConditionParser().get(policies, headers, lines);
    }

    /**
     * Reads the alert conditions from the given reader.
     * @param policies The set of alert policies for the conditions
     * @param reader The input stream reader used to read the lines
     * @return The alert conditions read from the lines
     * @throws IOException if there is a problem reading the input file or it does not exist
     */
    public static List<InfraMetricAlertCondition> parse(List<AlertPolicy> policies, InputFileReader reader) throws IOException
    {
        reader.parse();
        return parse(policies, reader.getHeaders(), reader.getRows());
    }

    /**
     * Reads the alert condition from the given line.
     * @param file The file instance with the columns
     * @param line The input file line
     * @return The alert condition created
     */
    protected InfraMetricAlertCondition create(FileInstance file, String[] line)
    {
        String name = file.getString(InfraMetricAlertCondition.NAME, line);
        Integer criticalThreshold = file.getInteger(InfraMetricAlertCondition.CRITICAL_THRESHOLD, line);
        Integer warningThreshold = file.getInteger(InfraMetricAlertCondition.WARNING_THRESHOLD, line);
        Integer duration = file.getInteger(AlertThreshold.DURATION, line);
        String timeFunction = file.getString(AlertThreshold.TIME_FUNCTION, line);
        String whereClause = file.getString(InfraMetricAlertCondition.WHERE_CLAUSE, line);
        String eventType = file.getString(InfraMetricAlertCondition.EVENT_TYPE, line);
        String comparison = file.getString(InfraMetricAlertCondition.COMPARISON, line);
        String selectValue = file.getString(InfraMetricAlertCondition.SELECT_VALUE, line);

        InfraMetricAlertCondition ret = InfraMetricAlertCondition.builder()
            .name(file.getString(InfraMetricAlertCondition.NAME, line))
            .criticalThreshold(getThreshold(criticalThreshold, duration, timeFunction))
            .warningThreshold(getThreshold(warningThreshold, duration, timeFunction))
            .eventType(eventType)
            .comparison(comparison)
            .selectValue(selectValue)
            .whereClause(whereClause)
            .enabled(true)
            .build();

        if(ret.getCriticalThreshold() == null)
            throw new IllegalStateException("Infra alert condition missing critical threshold: "+name);

        return ret;
    }
}