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

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * The base class for all templates.
 * 
 * @author Gerald Curley (opsmatters)
 */
public abstract class Template
{
    public static final TemplateColumn TYPE = new TemplateColumn("type", "Type", true);

    private Map<String,TemplateColumn> columns = new LinkedHashMap<String,TemplateColumn>();

    /**
     * Default constructor.
     */
    public Template()
    {
    }

    /**
     * Returns the type of the template.
     * @return The type of the template
     */
    public abstract String getType();

    /**
     * Returns the template columns.
     * @return The template columns
     */
    public Map<String,TemplateColumn> getColumns()
    {
        return columns;
    }

    /**
     * Adds the given column to the columns for this template.
     * @param column The column to add
     */
    public void addColumn(TemplateColumn column)
    {
        if(columns.containsKey(column.getName())) // Error on duplicate column
            throw new IllegalStateException("column already exists: "+column.getName());
        columns.put(column.getName(), column);
    }

    /**
     * Returns the column in the template with the given name.
     * @param name The name of the column to return
     * @return The column for the given name
     */
    public TemplateColumn getColumn(String name)
    {
        return columns.get(name);
    }

    /**
     * Returns the template column headers.
     * @return The template column headers
     */
    public String[] getHeaders()
    {
        int i = 0;
        String[] ret = new String[columns.size()];
        for(TemplateColumn column : columns.values())
            ret[i++] = column.getHeader();
        return ret;
    }

    /**
     * Returns an instance of this template, with headers.
     * @param headers The headers of the input file
     * @return The template created
     */
    public TemplateInstance getInstance(String[] headers)
    {
        return new TemplateInstance(this, headers);
    }  
}