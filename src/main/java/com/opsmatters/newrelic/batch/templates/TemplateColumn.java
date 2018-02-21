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

/**
 * Represents a column in a template.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class TemplateColumn
{
    private String name;
    private String header;
    private boolean mandatory = false;
    private String deflt;

    /**
     * Constructor that takes a set of column attributes.
     * @param name The name of the column
     * @param header The header name of the column
     * @param mandatory <CODE>true</CODE> if this column is mandatory
     */
    TemplateColumn(String name, String header, boolean mandatory)
    {
        this(name, header, mandatory, null);
    }

    /**
     * Constructor that takes a set of column attributes.
     * @param name The name of the column
     * @param header The header name of the column
     * @param mandatory <CODE>true</CODE> if this column is mandatory
     * @param deflt The default value for the column if it is missing
     */
    TemplateColumn(String name, String header, boolean mandatory, String deflt)
    {
        this.name = name;
        this.header = header;
        this.mandatory = mandatory;
        this.deflt = deflt;
    }

    /**
     * Returns the name of the column.  
     * @return The name of the column
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the header name of the column.  
     * @return The header name of the column
     */
    public String getHeader()
    {
        return header;
    }

    /**
     * Returns <CODE>true</CODE> if this column is mandatory.  
     * @return <CODE>true</CODE> if this column is mandatory
     */
    public boolean isMandatory()
    {
        return mandatory;
    }

    /**
     * Returns the default value of the column.  
     * @return The default value of the column
     */
    public String getDefault()
    {
        return deflt;
    }

    /**
     * Returns the name of the column.  
     * @return The name of the column
     */
    public String toString()
    {
        return getName();
    }
}