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

package com.opsmatters.newrelic.batch.templates;

import com.opsmatters.newrelic.api.model.alerts.channels.VictorOpsChannel;
import com.opsmatters.newrelic.api.model.alerts.channels.VictorOpsConfiguration;

/**
 * Template that defines the VictorOps alert channel file format.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class VictorOpsChannelTemplate extends FileTemplate
{
    /**
     * The type of the template.  
     */
    public static final String TYPE = "victorops-channel";

    // The template columns
    public TemplateColumn NAME = TemplateColumn.builder()
        .name(VictorOpsChannel.NAME)
        .header("Name")
        .build();
    public TemplateColumn KEY = TemplateColumn.builder()
        .name(VictorOpsConfiguration.KEY)
        .header("Key")
        .build();
    public TemplateColumn ROUTE_KEY = TemplateColumn.builder()
        .name(VictorOpsConfiguration.ROUTE_KEY)
        .header("Route Key")
        .build();

    /**
     * Default constructor.
     */
    public VictorOpsChannelTemplate()
    {
        addColumn(NAME);
        addColumn(TEMPLATE_TYPE);
        addColumn(KEY);
        addColumn(ROUTE_KEY);
    }

    /**
     * Returns the type of the template.
     * @return The type of the template
     */
    public String getType()
    {
        return TYPE;
    }
}