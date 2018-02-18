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

package com.opsmatters.newrelic.batch.parsers;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import com.opsmatters.newrelic.batch.templates.TemplateFactory;
import com.opsmatters.newrelic.batch.templates.TemplateInstance;

/**
 * The base class for all input file parsers.
 * 
 * @author Gerald Curley (opsmatters)
 */
public abstract class InputFileParser<T> extends BaseParser
{
    private static final Logger logger = Logger.getLogger(InputFileParser.class.getName());

    /**
     * Creates the items from the given lines.
     * @param headers The headers of the file
     * @param lines The input file lines
     * @return The items created from the lines
     */
    protected List<T> get(String[] headers, List<String[]> lines)
    {
        List<T> ret = new ArrayList<T>();
        TemplateInstance template = TemplateFactory.getTemplate(getClass()).getInstance(headers);
        logger.info("Processing "+template.getType()+" file: headers="+headers.length+" lines="+lines.size());

        template.checkColumns();
        for(String[] line : lines)
        {
            // Check that the line matches the template type
            if(!template.matches(line))
            {
                logger.severe("found illegal line in "+template.getType()+" file: "+template.getType(line));
                continue;
            }

            ret.add(create(template, line));
        }

        return ret;
    }

    /**
     * Creates the item from the given line.
     * @param template The template with the columns
     * @param line The input file line
     * @return The item created
     */
    protected abstract T create(TemplateInstance template, String[] line);
}