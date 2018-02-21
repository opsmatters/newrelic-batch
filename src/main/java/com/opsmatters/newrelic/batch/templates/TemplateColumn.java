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
    private boolean mandatory = true;
    private String defaultValue;
    private boolean output = true;

    /**
     * Default constructor.
     */
    TemplateColumn()
    {
    }

    /**
     * Sets the name of the column.  
     * @param name The name of the column
     */
    public void setName(String name)
    {
        this.name = name;
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
     * Sets the header name of the column.  
     * @param header The header name of the column
     */
    public void setHeader(String header)
    {
        this.header = header;
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
     * Set to <CODE>true</CODE> if this column is mandatory in an input file.  
     * @param mandatory <CODE>true</CODE> if this column is mandatory in an input file
     */
    public void setMandatory(boolean mandatory)
    {
        this.mandatory = mandatory;
    }

    /**
     * Returns <CODE>true</CODE> if this column is mandatory in an input file.  
     * @return <CODE>true</CODE> if this column is mandatory in an input file
     */
    public boolean isMandatory()
    {
        return mandatory;
    }

    /**
     * Sets the default value of the column.  
     * @param defaultValue The default value of the column
     */
    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the default value of the column.  
     * @return The default value of the column
     */
    public String getDefaultValue()
    {
        return defaultValue;
    }

    /**
     * Set to <CODE>true</CODE> if this column should be written to an output file.  
     * @param output <CODE>true</CODE> if this column this column should be written to an output file
     */
    public void setOutput(boolean output)
    {
        this.output = output;
    }

    /**
     * Returns <CODE>true</CODE> if this column should be written to an output file.  
     * @return <CODE>true</CODE> if this column should be written to an output file
     */
    public boolean isOutput()
    {
        return output;
    }

    /**
     * Returns the name of the column.  
     * @return The name of the column
     */
    public String toString()
    {
        return getName();
    }

    /**
     * Returns a builder for the column.
     * @return The builder instance.
     */
    public static Builder builder()
    {
        return new Builder();
    }

    /**
     * Builder to make column construction easier.
     */
    public static class Builder
    {
        private TemplateColumn column = new TemplateColumn();

        /**
         * Sets the name of the column.
         * @param name The name of the column
         * @return This object
         */
        public Builder name(String name)
        {
            column.setName(name);
            return this;
        }

        /**
         * Sets the header name of the column.
         * @param header The header name of the column
         * @return This object
         */
        public Builder header(String header)
        {
            column.setHeader(header);
            return this;
        }

        /**
         * Set to <CODE>true</CODE> if this column is mandatory in an input file.  
         * @param mandatory <CODE>true</CODE> if this column is mandatory in an input file
         * @return This object
         */
        public Builder mandatory(boolean mandatory)
        {
            column.setMandatory(mandatory);
            return this;
        }

        /**
         * Sets the default value of the column.
         * @param defaultValue The default value of the column
         * @return This object
         */
        public Builder defaultValue(String defaultValue)
        {
            column.setDefaultValue(defaultValue);
            return this;
        }

        /**
         * Set to <CODE>true</CODE> if this column should be written to an output file.  
         * @param output <CODE>true</CODE> if this column this column should be written to an output file
         * @return This object
         */
        public Builder output(boolean output)
        {
            column.setOutput(output);
            return this;
        }

        /**
         * Returns the configured column instance
         * @return The column instance
         */
        public TemplateColumn build()
        {
            return column;
        }
    }
}