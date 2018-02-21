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

/**
 * Class representing an instance of a file template.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class FileInstance
{
    private FileTemplate template;
    private List<String> headers = new ArrayList<String>();

    /**
     * Constructor that takes a set of headers.
     * Headers are converted to  lower case to support a case-insensitive lookup of the columns.
     * @param template The template for the instance
     * @param headers The headers of the input file
     */
    public FileInstance(FileTemplate template, String[] headers)
    {
        this.template = template;
        for(String header : headers)
            this.headers.add(header.toLowerCase());
    }

    /**
     * Returns the type of the template.
     * @return The type of the template
     */
    public String getType()
    {
        return template.getType();
    }

    /**
     * Checks the headers to see if there are any missing mandatory columns.
     * @throws IllegalStateException if there is a missing mandatory column
     */
    public void checkColumns()
    {
        for(TemplateColumn column : template.getColumns().values())
        {
            if(getIndex(column) == -1 && column.isMandatory())
                throw new IllegalStateException("missing mandatory column: "+column.getName());
        }
    }

    /**
     * Returns the index of the given column (ignoring the case of the headers).
     * @param column The column of the line
     * @return The index of the given column
     */
    protected int getIndex(TemplateColumn column)
    {
        return headers.indexOf(column.getHeader().toLowerCase());
    }

    /**
     * Returns the value of the "Type" column in the given line.
     * @param line The line of the file
     * @return The value of the "Type" column from the line
     */
    public String getType(String[] line)
    {
        int pos = getIndex(FileTemplate.TEMPLATE_TYPE);
        if(pos != -1 && pos < line.length)
            return line[pos];
        return null;
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
    public String getString(String name, String[] line)
    {
        String ret = null;
        TemplateColumn column = template.getColumn(name);
        if(column == null)
            throw new IllegalArgumentException("missing column: "+name);
        int pos = getIndex(column);
        if(pos != -1 && pos < line.length)
            ret = line[pos];
        if(ret == null)
            ret = column.getDefaultValue();
        return ret != null ? ret.trim() : null;
    }

    /**
     * Returns the boolean value of the given column in the given line.
     * @param name The name of the column
     * @param line The line of the file
     * @return The value of the column from the line
     */
    public Boolean getBoolean(String name, String[] line)
    {
        return Boolean.valueOf(getString(name, line));
    }

    /**
     * Returns the integer value of the given column in the given line.
     * @param name The name of the column
     * @param line The line of the file
     * @return The value of the column from the line
     */
    public Integer getInteger(String name, String[] line)
    {
        return Integer.valueOf(getString(name, line));
    }
}