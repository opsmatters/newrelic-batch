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

package com.opsmatters.newrelic.batch.renderers;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import com.opsmatters.core.reports.OutputFileWriter;
import com.opsmatters.newrelic.batch.templates.Template;
import com.opsmatters.newrelic.batch.templates.TemplateFactory;

/**
 * The base class for all output file renderers.
 * 
 * @author Gerald Curley (opsmatters)
 */
public abstract class OutputFileRenderer<T>
{
    private static final Logger logger = Logger.getLogger(OutputFileRenderer.class.getName());

    /**
     * Writes the given items to a writer.
     * @param items The items to be serialized
     * @param writer The writer to use to serialize the items
     * @throws IOException if there was an error writing the items
     */
    public void render(List<T> items, OutputFileWriter writer) throws IOException
    {
        List<String[]> lines = new ArrayList<String[]>();
        Template template = TemplateFactory.getTemplate(getClass());
        String[] headers = template.getHeaders();
        lines.add(headers);
        for(T item : items)
            lines.add(serialize(template, item));

        logger.info("Rendering "+template.getType()+" file: headers="+headers.length+" lines="+lines.size());
        writer.write(lines);
    }

    /**
     * Serializes the item to a line.
     * <P>
     * Implemented by super-class.
     * </P>
     * @param template The template with the columns
     * @param item The item to be serialized
     * @return The line representing the item
     */
    protected String[] serialize(Template template, T item)
    {
        return null;
    }
}