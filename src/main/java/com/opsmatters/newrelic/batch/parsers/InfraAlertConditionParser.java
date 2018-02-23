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

import com.opsmatters.newrelic.api.model.alerts.conditions.InfraAlertCondition;
import com.opsmatters.newrelic.api.model.alerts.conditions.AlertThreshold;

/**
 * Base class for all infrastructure alert condition parsers.
 * 
 * @author Gerald Curley (opsmatters)
 */
public abstract class InfraAlertConditionParser<T extends InfraAlertCondition> extends BaseConditionParser<T>
{
    /**
     * Protected constructor.
     */
    protected InfraAlertConditionParser()
    {
    }

    /**
     * Returns a threshold for an alert condition.
     * <P>
     * Returns null if the value or duration is null.
     * </P>
     * @param value The value of the threshold
     * @param duration The duration of the threshold in minutes
     * @return The threshold created
     */
    protected AlertThreshold getThreshold(Integer value, Integer duration)
    {
        return getThreshold(value, duration, null);
    }

    /**
     * Returns a threshold for an alert condition.
     * <P>
     * Returns null if the value or duration is null.
     * </P>
     * @param value The value of the threshold
     * @param duration The duration of the threshold in minutes
     * @param timeFunction The time function of the threshold, either "any" or "all"
     * @return The threshold created
     */
    protected AlertThreshold getThreshold(Integer value, Integer duration, String timeFunction)
    {
        if(value == null || duration == null)
            return null;
        return AlertThreshold.builder()
            .value(value)
            .durationMinutes(duration)
            .timeFunction(timeFunction)
            .build();
    }

    /**
     * Returns a threshold for an alert condition.
     * <P>
     * Returns null if the duration is null.
     * </P>
     * @param duration The duration of the threshold in minutes
     * @return The threshold created
     */
    protected AlertThreshold getThreshold(Integer duration)
    {
        if(duration == null)
            return null;
        return AlertThreshold.builder()
            .durationMinutes(duration)
            .build();
    }
}