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

package com.opsmatters.newrelic.batch.model;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import com.opsmatters.newrelic.api.model.insights.Dashboard;

/**
 * Represents a set of dashboards.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class Dashboards
{
    private static final Logger logger = Logger.getLogger(Dashboards.class.getName());

    private List<Dashboard> dashboards = new ArrayList<Dashboard>();

    /**
     * Default constructor.
     */
    public Dashboards()
    {
    }

    /**
     * Replaces the dashboards with the given set of dashboards.
     * @param dashboards The set of dashboards
     */
    public void setDashboards(List<Dashboard> dashboards)
    {
        this.dashboards.clear();
        this.dashboards.addAll(dashboards);
    }

    /**
     * Returns the set of dashboards.
     * @return The set of dashboards
     */
    public List<Dashboard> getDashboards()
    {
        return dashboards;
    }

    /**
     * Returns the number of dashboards.
     * @return The number of dashboards
     */
    public int numDashboards()
    {
        return dashboards.size();
    }
}
