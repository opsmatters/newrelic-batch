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

package com.opsmatters.newrelic.batch;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import com.opsmatters.newrelic.api.NewRelicApi;
import com.opsmatters.newrelic.api.NewRelicInfraApi;
import com.opsmatters.core.documents.InputFileReader;
import com.opsmatters.core.documents.OutputFileWriter;
import com.opsmatters.core.documents.Workbook;

/**
 * Base class for all manager classes.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class BaseManager
{
    private static final Logger logger = Logger.getLogger(BaseManager.class.getName());

    private String apiKey;
    protected NewRelicApi apiClient;
    protected NewRelicInfraApi infraApiClient;
    private boolean initialized = false;
    private boolean verbose = false;

    /**
     * Constructor that takes an API key.
     * @param apiKey The API key used to authenticate the client
     * @param verbose <CODE>true</CODE> if verbose logging is enabled
     */
    public BaseManager(String apiKey, boolean verbose)
    {
        this.apiKey = apiKey;
        this.verbose = verbose;
    }

    /**
     * Initialise the clients.
     */
    protected void checkInitialize()
    {
        if(!initialized)
            initialize();
        if(!initialized)
            throw new IllegalStateException("client not initialized");
    }

    /**
     * Called after setting configuration properties.
     */
    protected void initialize()
    {
        if(apiKey == null)
            throw new IllegalArgumentException("null API key");

        initialized = false;

        if(verbose)
            logger.info("Initialising the client");
        apiClient = NewRelicApi.builder().apiKey(apiKey).build();
        infraApiClient = NewRelicInfraApi.builder().apiKey(apiKey).build();
        if(verbose)
            logger.info("Initialised the clients");

        initialized = true;
    }

    /**
     * Returns <CODE>true</CODE> if the clients have been initialized.
     * @return <CODE>true</CODE> if the clients have been initialized
     */
    protected boolean isInitialized()
    {
        return initialized;
    }

    /**
     * Returns <CODE>true</CODE> if verbose logging is enabled.
     * @return <CODE>true</CODE> if verbose logging is enabled
     */
    protected boolean verbose()
    {
        return verbose;
    }

    /**
     * Returns the REST API client.
     * @return the REST API client 
     */
    public NewRelicApi getApiClient()
    {
        return apiClient;
    }

    /**
     * Returns the Infrastructure API client.
     * @return the Infrastructure API client 
     */
    public NewRelicInfraApi getInfraApiClient()
    {
        return infraApiClient;
    }

    /**
     * Returns an input file reader for the given file stream.
     * @param filename The name of the file to import
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to import
     * @param stream An input stream for the file
     * @return The input file reader
     */
    protected InputFileReader getReader(String filename, String worksheet, InputStream stream)
    {
         return InputFileReader.builder()
            .name(filename)
            .worksheet(worksheet)
            .withInputStream(stream)
            .build();
    }

    /**
     * Returns an output file writer for the given file stream.
     * @param filename The name of the file to export to
     * @param worksheet For XLS and XLSX files, the name of the worksheet in the file to export to
     * @param workbook For XLS and XLSX files, the workbook to append the worksheet to
     * @param stream An output stream for the file
     * @return The input file reader
     */
    protected OutputFileWriter getWriter(String filename, String worksheet, Workbook workbook, OutputStream stream)
    {
        return OutputFileWriter.builder()
            .name(filename)
            .worksheet(worksheet)
            .withOutputStream(stream)
            .withWorkbook(workbook)
            .build();
    }

    /**
     * Closes the given output writer.
     * @param writer The output writer to close
     */
    protected void closeWriter(OutputFileWriter writer)
    {
        if(writer != null)
            writer.close();
    }

    /**
     * Closes the given input stream.
     * @param stream The input stream to close
     */
    protected void closeStream(InputStream stream)
    {
        try
        {
            if(stream != null)
                stream.close();
        }
        catch(IOException e)
        {
        }
    }

    /**
     * Closes the given output stream.
     * @param stream The output stream to close
     */
    protected void closeStream(OutputStream stream)
    {
        try
        {
            if(stream != null)
                stream.close();
        }
        catch(IOException e)
        {
        }
    }

    /**
     * Closes the given reader.
     * @param reader The reader to close
     */
    protected void closeReader(Reader reader)
    {
        try
        {
            if(reader != null)
                reader.close();
        }
        catch(IOException e)
        {
        }
    }

    /**
     * Closes the given writer.
     * @param writer The writer to close
     */
    protected void closeWriter(Writer writer)
    {
        try
        {
            if(writer != null)
                writer.close();
        }
        catch(IOException e)
        {
        }
    }

    /**
     * Converts the given collection to a list.
     * @param <T> The type of the collection
     * @param collection The collection
     * @return The list
     */
    protected <T> List<T> toList(Collection<T> collection)
    {
        List<T> ret = new ArrayList<T>();
        ret.addAll(collection);
        return ret;
    }
}