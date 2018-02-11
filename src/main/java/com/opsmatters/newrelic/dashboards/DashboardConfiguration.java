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

import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import com.opsmatters.newrelic.api.model.insights.Dashboard;

/**
 * Represents the definition of the dashboards from a YAML file.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class DashboardConfiguration
{
    private static final Logger logger = Logger.getLogger(DashboardConfiguration.class.getName());

    private String filename;
    private boolean valid = true;
    private List<Dashboard> dashboards;

    /**
     * Default constructor.
     */
    public DashboardConfiguration()
    {
    }

    /**
     * Returns the name of this file.
     * @return The name of this file
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * Returns <CODE>true</CODE> if this file is valid.
     * @return <CODE>true</CODE> if this file is valid
     */
    public boolean isValid()
    {
        return valid;
    }

    /**
     * Returns the dashboards read from the file.
     * @return The dashboards read from the file
     */
    public List<Dashboard> getDashboards()
    {
        return dashboards;
    }

    /**
     * Reads the configuration from the given file.
     * @param filename The name of the YAML file
     * @return <CODE>true</CODE> if the file was processed successfully
     * @throws IOException The file could not be opened
     */
    public boolean read(String filename) throws IOException
    {
        return read(new File(filename));
    }

    /**
     * Reads the configuration from the given file.
     * @param file The YAML file
     * @return <CODE>true</CODE> if the file was processed successfully
     * @throws IOException The file could not be opened
     */
    public boolean read(File file) throws IOException
    {
        valid = false;
        this.filename = file.getName();
        FileReader reader = new FileReader(file);
        if(file.exists())
        {
            // Load the file contents
            String contents = getContents(reader, "\n");
            if(contents != null)
            {
                dashboards = DashboardParser.fromYaml(contents);
                valid = true;
            }
            else
            {
                logger.severe("Unable to find configuration in "+filename+" file: "+file.getAbsolutePath());
            }
        }
        else
        {
            logger.severe("Unable to find dashboards file: "+file.getAbsolutePath());
        }

        try
        {
            if(reader != null)
                reader.close();
        }
        catch(IOException e)
        {
        }

        return valid;
    }

    /**
     * Reads the configuration from the given stream.
     * @param filename The name of the YAML file
     * @param stream The input stream of the YAML file
     * @return <CODE>true</CODE> if the file was processed successfully
     * @throws IOException The file could not be opened
     */
    public boolean read(String filename, InputStream stream) throws IOException
    {
        valid = false;
        this.filename = filename;
        InputStreamReader reader = new InputStreamReader(stream);

        // Load the file contents
        String contents = getContents(reader, "\n");
        if(contents != null)
        {
            dashboards = DashboardParser.fromYaml(contents);
            valid = true;
        }
        else
        {
            logger.severe("Unable to find configuration in "+filename+" file");
        }

        try
        {
            if(reader != null)
                reader.close();
        }
        catch(IOException e)
        {
        }

        return valid;
    }

    /**
     * Read the contents of the configuration using the given reader.
     * @param reader The reader used to read the file stream
     * @param terminator The line terminator of the YAML file
     * @return The contents of the file as a string
     */
    private String getContents(Reader reader, String terminator) throws IOException
    {
        String line = null;
        StringBuffer buff = new StringBuffer();
        BufferedReader in = new BufferedReader(reader);
        while((line = in.readLine()) != null)
        {
            buff.append(line);
            if(terminator != null)
                buff.append(terminator);
        }
        reader.close();
        return buff.toString();
    }

}
