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

package com.opsmatters.newrelic.dashboards;

/**
 * Creates New Relic dashboards from a definition in a YAML file.  
 * 
 * @author Gerald Curley (opsmatters)
 */
public class NewRelicDashboards
{

    /**
     * Entry point that selects the command to execute.
     * @param args The argument list
     */
    public static void main(String[] args)
    {
        System.setProperty("java.util.logging.config.file","logging.properties");

    }
}