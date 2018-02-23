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

import java.util.List;
import java.util.ArrayList;
import com.opsmatters.newrelic.api.model.alerts.conditions.Term;
import com.opsmatters.newrelic.api.model.alerts.conditions.TermsCondition;
import com.opsmatters.newrelic.api.model.alerts.Priority;
import com.opsmatters.newrelic.batch.templates.FileInstance;

/**
 * Base class for all terms alert condition parsers.
 * 
 * @author Gerald Curley (opsmatters)
 */
public abstract class TermsConditionParser<T extends TermsCondition> extends BaseConditionParser<T>
{
    /**
     * Protected constructor.
     */
    protected TermsConditionParser()
    {
    }

    /**
     * Reads the thresholds and term details from the given line and creates the required terms.
     * @param file The file instance with the columns
     * @param line The input file line
     * @return The terms created
     */
    protected List<Term> getTerms(FileInstance file, String[] line)
    {
        List<Term> ret = new ArrayList<Term>();

        String critical = file.getString(Term.CRITICAL_THRESHOLD, line);
        String warning = file.getString(Term.WARNING_THRESHOLD, line);
        String duration = file.getString(Term.DURATION, line);
        String operator = file.getString(Term.OPERATOR, line);
        String timeFunction = file.getString(Term.TIME_FUNCTION, line);

        if(critical != null && critical.length() > 0)
            ret.add(getTerm(critical, duration, Priority.CRITICAL, operator, timeFunction));
        if(warning != null && warning.length() > 0)
            ret.add(getTerm(warning, duration, Priority.WARNING, operator, timeFunction));

        return ret;
    }

    /**
     * Returns a term for an alert condition.
     * @param threshold The threshold of the term
     * @param duration The duration of the term in minutes
     * @param priority The priority of the term, either "warning" or "critical"
     * @param operator The operator of the term, either "above", "below" or "equal"
     * @param timeFunction The time function of the term, either "any" or "all"
     * @return The term created
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
}