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

import java.util.List;
import java.util.ArrayList;
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

    private static Map<String,TemplateColumn> columns = new LinkedHashMap<String,TemplateColumn>();
    private List<String> headers = new ArrayList<String>();

    static
    {
        addColumn(TYPE);
    }

    /**
     * Returns the type of the template.
     * @return The type of the template
     */
    public abstract String getType();

    /**
     * Sets the headers for the template.
     * @param headers The headers of the input file
     * @return This object
     */
    public Template headers(String[] headers)
    {
        for(String header : headers)
            this.headers.add(header);
        return this;
    }

    /**
     * Adds the given column to the columns for this template.
     * @param column The column to add
     */
    public static void addColumn(TemplateColumn column)
    {
        columns.put(column.getName(), column);
    }

    /**
     * Returns the column in the template with the given name.
     * @param name The name of the column to return
     * @return The column for the given name
     */
    public static TemplateColumn getColumn(String name)
    {
        return columns.get(name);
    }

    /**
     * Checks the headers to see if there are any missing mandatory columns.
     * @throws IllegalStateException if there is a missing mandatory column
     */
    public void checkColumns()
    {
        if(columns.size() == 0)
            throw new IllegalStateException("columns missing");

        for(TemplateColumn column : columns.values())
        {
            if(getIndex(column) == -1 && column.isMandatory())
                throw new IllegalStateException("missing mandatory column: "+column.getName());
        }
    }

    /**
     * Returns the index of the given column.
     * @param column The column of the line
     * @return The index of the given column
     */
    protected int getIndex(TemplateColumn column)
    {
        return headers.indexOf(column.getHeader());
    }

    /**
     * Returns the value of the "Type" column in the given line.
     * @param line The line of the file
     * @return The value of the "Type" column from the line
     */
    public String getType(String[] line)
    {
        return line[getIndex(TYPE)];
    }

    /**
     * Returns <CODE>true</CODE> if the the "Type" column of the given line matches the type of the template.
     * @param line The line of the file
     * @return <CODE>true</CODE> if the the "Type" column of the given line matches the type of the template
     */
    public boolean matches(String[] line)
    {
        return getType().equals(getType(line));
    }

    /**
     * Returns the string value of the given column in the given line.
     * @param name The name of the column
     * @param line The line of the file
     * @return The value of the column from the line
     */
    public String getStringValue(String name, String[] line)
    {
        return line[getIndex(getColumn(name))];
    }
}