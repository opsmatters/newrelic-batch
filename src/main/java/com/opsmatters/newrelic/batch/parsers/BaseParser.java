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

import java.util.Map;

/**
 * The base class for all parsers.
 * 
 * @author Gerald Curley (opsmatters)
 */
public abstract class BaseParser
{
    /**
     * Reads a value from the given map and coerces it to the given class.
     * @param <E> The type of the class to coerce the value to
     * @param map The map to read the value from
     * @param name The name of the property
     * @param target The target class of the returned value
     * @return The value of the property from the map
     */
    @SuppressWarnings("unchecked")
    protected <E> E getAs(Map<String,Object> map, String name, Class<E> target) 
        throws IllegalArgumentException
    {
        return getAs(map, name, target, true);
    }

    /**
     * Reads a value from the given map and coerces it to the given class.
     * @param <E> The type of the class to coerce the value to
     * @param map The map to read the value from
     * @param name The name of the property
     * @param target The target class of the returned value
     * @param mandatory <CODE>true</CODE> if the field cannot be null
     * @return The value of the property from the map
     */
    @SuppressWarnings("unchecked")
    protected <E> E getAs(Map<String,Object> map, String name, Class<E> target, boolean mandatory) 
        throws IllegalArgumentException
    {
        E ret = null;

        Object value = map.get(name);
        if(value != null)
        {
            ret = coerceTo(name, value, target);
        }
        else if(mandatory)
        {
            throw new IllegalArgumentException(name+": expected "+target.getName()
                +" but was missing");
        }

        return ret;
    }

    /**
     * Coerce the value to the given class.
     * @param <E> The type of the class to coerce the value to
     * @param name The name of the property
     * @param value The value to coerce
     * @param target The target class of the returned value
     * @return The value 
     */
    @SuppressWarnings("unchecked")
    protected <E> E coerceTo(String name, Object value, Class<E> target) 
        throws IllegalArgumentException
    {
        E ret = null;

        if(target.isInstance(value))
            ret = (E)value;
        else if(value != null)
            throw new IllegalArgumentException(name+": expected "+target.getName()
                +" but was "+value.getClass().getName());

        return ret;
    }
}