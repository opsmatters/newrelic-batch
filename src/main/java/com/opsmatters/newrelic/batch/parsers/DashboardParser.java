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

import java.io.Reader;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.logging.Logger;
import org.yaml.snakeyaml.Yaml;
import com.opsmatters.newrelic.api.model.insights.Dashboard;
import com.opsmatters.newrelic.api.model.insights.widgets.Widget;
import com.opsmatters.newrelic.api.model.insights.widgets.EventChart;
import com.opsmatters.newrelic.api.model.insights.widgets.BreakdownMetricChart;
import com.opsmatters.newrelic.api.model.insights.widgets.FacetChart;
import com.opsmatters.newrelic.api.model.insights.widgets.InventoryChart;
import com.opsmatters.newrelic.api.model.insights.widgets.Markdown;
import com.opsmatters.newrelic.api.model.insights.widgets.MetricLineChart;
import com.opsmatters.newrelic.api.model.insights.widgets.ThresholdEventChart;
import com.opsmatters.newrelic.api.model.insights.widgets.TrafficLightChart;
import com.opsmatters.newrelic.api.model.insights.widgets.MarkdownData;
import com.opsmatters.newrelic.api.model.insights.widgets.EventsData;
import com.opsmatters.newrelic.api.model.insights.widgets.MetricsData;
import com.opsmatters.newrelic.api.model.insights.widgets.InventoryData;
import com.opsmatters.newrelic.api.model.insights.widgets.Threshold;
import com.opsmatters.newrelic.api.model.insights.widgets.TrafficLight;
import com.opsmatters.newrelic.api.model.insights.widgets.TrafficLightState;
import com.opsmatters.newrelic.api.model.insights.widgets.Layout;
import com.opsmatters.newrelic.api.model.metrics.Metric;

/**
 * Parser that converts dashboards from YAML format.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class DashboardParser
{
    private static final Logger logger = Logger.getLogger(DashboardParser.class.getName());

//GERALD: move out
    // The field names
    public static final String TITLE = "title";
    public static final String SUBTITLE = "subtitle";
    public static final String NOTES = "notes";
    public static final String ICON = "icon";
    public static final String VERSION = "version";
    public static final String VISIBILITY = "visibility";
    public static final String EDITABLE = "editable";
    public static final String FILTER = "filter";
    public static final String EVENT_TYPES = "event_types";
    public static final String ATTRIBUTES = "attributes";
    public static final String WIDGETS = "widgets";
    public static final String VISUALIZATION = "visualization";
    public static final String ACCOUNT_ID = "account_id";
    public static final String DATA = "data";
    public static final String NRQL = "nrql";
    public static final String SOURCE = "source";
    public static final String SOURCES = "sources";
    public static final String DRILLDOWN_DASHBOARD_ID = "drilldown_dashboard_id";
    public static final String THRESHOLD = "threshold";
    public static final String DURATION = "duration";
    public static final String METRICS = "metrics";
    public static final String ENTITY_IDS = "entity_ids";
    public static final String END_TIME = "end_time";
    public static final String ORDER_BY = "order_by";
    public static final String LIMIT = "limit";
    public static final String FILTERS = "filters";
    public static final String ID = "id";
    public static final String RED = "red";
    public static final String YELLOW = "yellow";
    public static final String NAME = "name";
    public static final String UNITS = "units";
    public static final String SCOPE = "scope";
    public static final String VALUES = "values";
    public static final String TRAFFIC_LIGHT = "traffic_light";
    public static final String STATES = "states";
    public static final String TYPE = "type";
    public static final String MIN = "min";
    public static final String MAX = "max";
    public static final String LAYOUT = "layout";
    public static final String ROW = "row";
    public static final String COLUMN = "column";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";

    /**
     * Private constructor.
     */
    private DashboardParser()
    {
    }

    /**
     * Reads the dashboards configuration from the given string.
     * @param contents The contents of the file as a YAML string
     * @return The dashboards read from the YAML string
     */
    public static List<Dashboard> parseYaml(String contents)
    {
        return new DashboardParser().getDashboards(new Yaml().load(contents));
    }

    /**
     * Reads the dashboards configuration from the given reader.
     * @param reader The reader used to read the YAML string
     * @return The dashboards read from the YAML string
     */
    public static List<Dashboard> parseYaml(Reader reader)
    {
        return new DashboardParser().getDashboards(new Yaml().load(reader));
    }

    /**
     * Reads the dashboards configuration from the given object.
     * @param o The dashboard configuration as a map
     * @return The dashboards read from the map
     */
    private List<Dashboard> getDashboards(Object o)
    {
        List<Dashboard> ret = new ArrayList<Dashboard>();

        if(o instanceof Map)
        {
            Map<String,Object> map = (Map<String,Object>)o;
            for(Map.Entry<String,Object> entry : map.entrySet())
            {
                if(entry.getValue() instanceof Map)
                    ret.add(getDashboard(entry.getKey(), (Map<String,Object>)entry.getValue()));
                else
                    logger.severe("Not a YAML document");
            }
        }
        else
        {
            logger.severe("Not a YAML document");
        }

        return ret;
    }

    /**
     * Creates a dashboard configuration from the given map.
     * @param title The title of the dashboard
     * @param map The configuration properties
     * @return The dashboard
     */
    private Dashboard getDashboard(String title, Map<String,Object> map)
    {
        // Get the filter
        List<String> eventTypes = null;
        List<String> attributes = null;
        Map<String,Object> filter = getAs(map, FILTER, Map.class);
        if(filter != null)
        {
            eventTypes = getAs(filter, EVENT_TYPES, List.class);
            attributes = getAs(filter, ATTRIBUTES, List.class);
        }

        return Dashboard.builder()
            .title(title)
            .icon(getAs(map, ICON, String.class, false))
            .version(getAs(map, VERSION, Integer.class))
            .visibility(getAs(map, VISIBILITY, String.class))
            .editable(getAs(map, EDITABLE, String.class))
            .setFilter(eventTypes, attributes)
            .widgets(getWidgets(getAs(map, WIDGETS, Map.class)))
            .build();
    }

    /**
     * Reads a list of widgets from the given map.
     * @param map The map to read the widgets from
     * @return The widgets
     */
    private List<Widget> getWidgets(Map<String,Object> map)
    {
        List<Widget> ret = new ArrayList<Widget>();

        if(map != null)
        {
            for(Map.Entry<String,Object> entry : map.entrySet())
            {
                if(entry.getValue() instanceof Map)
                    ret.add(getWidget(entry.getKey(), (Map<String,Object>)entry.getValue()));
                else
                    logger.severe("Not a widget document");
            }
        }

        return ret;
    }

    /**
     * Creates a widget configuration from the given map.
     * @param title The title of the widget
     * @param map The configuration properties
     * @return The widget
     */
    private Widget getWidget(String title, Map<String,Object> map)
    {
        Widget ret = null;
        String visualization = getAs(map, VISUALIZATION, String.class);
        if(visualization != null)
        {
            if(EventChart.Visualization.contains(visualization))
                ret = getEventChart(visualization, title, map);
            else if(BreakdownMetricChart.Visualization.contains(visualization))
                ret = getBreakdownMetricChart(visualization, title, map);
            else if(FacetChart.Visualization.contains(visualization))
                ret = getFacetChart(visualization, title, map);
            else if(InventoryChart.Visualization.contains(visualization))
                ret = getInventoryChart(visualization, title, map);
            else if(Markdown.Visualization.contains(visualization))
                ret = getMarkdown(visualization, title, map);
            else if(MetricLineChart.Visualization.contains(visualization))
                ret = getMetricLineChart(visualization, title, map);
            else if(ThresholdEventChart.Visualization.contains(visualization))
                ret = getThresholdEventChart(visualization, title, map);
            else if(TrafficLightChart.Visualization.contains(visualization))
                ret = getTrafficLightChart(visualization, title, map);
        }

        return ret;
    }

    /**
     * Creates a markdown widget.
     * @param visualization The visualization type of the widget
     * @param title The title of the widget
     * @param map The configuration properties
     * @return The widget
     */
    private Widget getMarkdown(String visualization, String title, Map<String,Object> map)
    {
        Markdown.Builder builder = Markdown.builder()
            .visualization(visualization)
            .addData(getMarkdownData(getAs(map, DATA, Map.class)));
        return getWidget(builder, title, map).build();
    }

    /**
     * Creates an event chart widget.
     * @param visualization The visualization type of the widget
     * @param title The title of the widget
     * @param map The configuration properties
     * @return The widget
     */
    private Widget getEventChart(String visualization, String title, Map<String,Object> map)
    {
        EventChart.Builder builder = EventChart.builder()
            .visualization(visualization)
            .addData(getEventsData(getAs(map, DATA, Map.class)));
        return getWidget(builder, title, map).build();
    }

    /**
     * Creates a facet chart widget.
     * @param visualization The visualization type of the widget
     * @param title The title of the widget
     * @param map The configuration properties
     * @return The widget
     */
    private Widget getFacetChart(String visualization, String title, Map<String,Object> map)
    {
        FacetChart.Builder builder = FacetChart.builder()
            .visualization(visualization)
            .addData(getEventsData(getAs(map, DATA, Map.class)));

        Integer id = getAs(map, DRILLDOWN_DASHBOARD_ID, Integer.class);
        if(id != null)
            builder = builder.drilldownDashboardId(id);

        return getWidget(builder, title, map).build();
    }

    /**
     * Creates a threshold event chart widget.
     * @param visualization The visualization type of the widget
     * @param title The title of the widget
     * @param map The configuration properties
     * @return The widget
     */
    private Widget getThresholdEventChart(String visualization, String title, Map<String,Object> map)
    {
        ThresholdEventChart.Builder builder = ThresholdEventChart.builder()
            .visualization(visualization)
            .threshold(getThreshold(getAs(map, THRESHOLD, Map.class)))
            .addData(getEventsData(getAs(map, DATA, Map.class)));
        return getWidget(builder, title, map).build();
    }

    /**
     * Creates a breakdown metric chart widget.
     * @param visualization The visualization type of the widget
     * @param title The title of the widget
     * @param map The configuration properties
     * @return The widget
     */
    private Widget getBreakdownMetricChart(String visualization, String title, Map<String,Object> map)
    {
        BreakdownMetricChart.Builder builder = BreakdownMetricChart.builder()
            .visualization(visualization)
            .addData(getMetricsData(getAs(map, DATA, Map.class)));
        return getWidget(builder, title, map).build();
    }

    /**
     * Creates a metric line chart widget.
     * @param visualization The visualization type of the widget
     * @param title The title of the widget
     * @param map The configuration properties
     * @return The widget
     */
    private Widget getMetricLineChart(String visualization, String title, Map<String,Object> map)
    {
        MetricLineChart.Builder builder = MetricLineChart.builder()
            .visualization(visualization)
            .addData(getMetricsData(getAs(map, DATA, Map.class)));
        return getWidget(builder, title, map).build();
    }

    /**
     * Creates an inventory chart widget.
     * @param visualization The visualization type of the widget
     * @param title The title of the widget
     * @param map The configuration properties
     * @return The widget
     */
    private Widget getInventoryChart(String visualization, String title, Map<String,Object> map)
    {
        InventoryChart.Builder builder = InventoryChart.builder()
            .visualization(visualization)
            .addData(getInventoryData(getAs(map, DATA, Map.class)));
        return getWidget(builder, title, map).build();
    }

    /**
     * Creates a traffic light chart widget.
     * @param visualization The visualization type of the widget
     * @param title The title of the widget
     * @param map The configuration properties
     * @return The widget
     */
    private Widget getTrafficLightChart(String visualization, String title, Map<String,Object> map)
    {
        TrafficLightChart.Builder builder = TrafficLightChart.builder()
            .visualization(visualization)
            .addData(getEventsData(getAs(map, DATA, Map.class)))
            .addTrafficLight(getTrafficLight(getAs(map, TRAFFIC_LIGHT, Map.class)));
        return getWidget(builder, title, map).build();
    }

    /**
     * Creates a markdown data item.
     * @param map The configuration properties
     * @return The widget data
     */
    private MarkdownData getMarkdownData(Map<String,Object> map)
    {
        return MarkdownData.builder()
            .source(getAs(map, SOURCE, String.class))
            .build();
    }

    /**
     * Creates an event data item.
     * @param map The configuration properties
     * @return The widget data
     */
    private EventsData getEventsData(Map<String,Object> map)
    {
        return EventsData.builder()
            .nrql(getAs(map, NRQL, String.class))
            .build();
    }

    /**
     * Creates a metric data item.
     * @param map The configuration properties
     * @return The widget data
     */
    private MetricsData getMetricsData(Map<String,Object> map)
    {
        MetricsData.Builder builder = MetricsData.builder()
            .orderBy(getAs(map, ORDER_BY, String.class, false));

        Integer duration = getAs(map, DURATION, Integer.class, false);
        if(duration != null)
            builder = builder.duration(duration);

        List<Metric> metrics = null;
        List list = getAs(map, METRICS, List.class, false);
        if(list != null)
        {
            metrics = new ArrayList<Metric>();
            for(Object item : list)
                metrics.add(getMetric(coerceTo(METRICS, item, Map.class)));
        }

        if(metrics != null)
            builder = builder.metrics(metrics);

        List<Long> entityIds = getAs(map, ENTITY_IDS, List.class, false);
        if(entityIds != null)
            builder = builder.entityIds(entityIds);

        Integer limit = getAs(map, LIMIT, Integer.class, false);
        if(limit != null)
            builder = builder.limit(limit);

        return  builder.build();
    }

    /**
     * Creates an inventory data item.
     * @param map The configuration properties
     * @return The widget data
     */
    private InventoryData getInventoryData(Map<String,Object> map)
    {
        List<String> sources = getAs(map, SOURCES, List.class);
        if(sources == null)
            sources = new ArrayList<String>();

        Map<String,String> filters = getAs(map, FILTERS, Map.class, false);
        if(filters == null)
            filters = new LinkedHashMap<String,String>();

        return InventoryData.builder()
            .sources(sources)
            .filters(filters)
            .build();
    }

    /**
     * Creates a threshold data item.
     * @param map The configuration properties
     * @return The widget threshold
     */
    private Threshold getThreshold(Map<String,Object> map)
    {
        return Threshold.builder()
            .red(getAs(map, RED, Integer.class))
            .yellow(getAs(map, YELLOW, Integer.class))
            .build();
    }

    /**
     * Creates a metric item.
     * @param map The configuration properties
     * @return The metric
     */
    private Metric getMetric(Map<String,Object> map)
    {
        List<String> values = getAs(map, VALUES, List.class, false);
        Metric.Builder builder = Metric.builder()
            .name(getAs(map, NAME, String.class, false));
        if(values != null)
            builder = builder.values(values);
        return builder.build();
    }

    /**
     * Creates a traffic light.
     * @param map The configuration properties
     * @return The traffic light
     */
    private TrafficLight getTrafficLight(Map<String,Object> map)
    {
        return TrafficLight.builder()
            .id(getAs(map, ID, String.class))
            .title(getAs(map, TITLE, String.class, false))
            .subtitle(getAs(map, SUBTITLE, String.class, false))
            .states(getTrafficLightStates(getAs(map, STATES, List.class)))
            .build();
    }

    /**
     * Creates a set of traffic light states.
     * @param map The configuration properties
     * @return The traffic light states
     */
    private List<TrafficLightState> getTrafficLightStates(List states)
    {
        if(states == null)
            return null;

        List<TrafficLightState> ret = new ArrayList<TrafficLightState>();
        for(Object state : states)
        {
            if(state instanceof Map)
            {
                Map<String,Object> map = (Map<String,Object>)state;

                TrafficLightState.Builder builder = TrafficLightState.builder()
                    .type(getAs(map, TYPE, String.class));

                Integer min = getAs(map, MIN, Integer.class);
                if(min != null)
                    builder = builder.min(min);

                Integer max = getAs(map, MAX, Integer.class);
                if(max != null)
                    builder = builder.max(max);

                ret.add(builder.build());
            }
        }

        if(ret.size() == 0)
            throw new IllegalArgumentException("traffic light must contain at least one state");

        return ret;
    }

    /**
     * Adds common fields to the widget.
     * @param builder The widget builder
     * @param map The configuration properties
     * @return The widget
     */
    private Widget.Builder getWidget(Widget.Builder builder, String title, Map<String,Object> map)
    {
        builder = builder
            .title(title)
            .notes(getAs(map, NOTES, String.class, false));

        Integer accountId = getAs(map, ACCOUNT_ID, Integer.class);
        if(accountId != null)
            builder = builder.accountId(accountId);

        Object layout = map.get(LAYOUT);
        if(layout instanceof Map)
            builder = builder.layout(getLayout((Map<String,Object>)layout));
        else if(layout instanceof List)
            builder = builder.layout(getLayout((List<Integer>)layout));

        return builder;
    }

    /**
     * Returns a widget layout object.
     * @param map The layout properties as a map
     * @return The layout
     */
    private Layout getLayout(Map<String,Object> map)
    {
        Layout.Builder builder = Layout.builder();

        Integer row = getAs(map, ROW, Integer.class);
        if(row != null)
            builder = builder.row(row);

        Integer column = getAs(map, COLUMN, Integer.class);
        if(column != null)
            builder = builder.column(column);

        Integer width = getAs(map, WIDTH, Integer.class, false);
        if(width != null)
            builder = builder.width(width);

        Integer height = getAs(map, HEIGHT, Integer.class, false);
        if(height != null)
            builder = builder.height(height);

        return builder.build();
    }

    /**
     * Returns a widget layout object.
     * @param list The layout properties as a list
     * @return The layout
     */
    private Layout getLayout(List<Integer> list)
    {
        Layout.Builder builder = Layout.builder();
        if(list.size() >= 2)
            builder = builder.position(list.get(0), list.get(1));
        if(list.size() >= 4)
            builder = builder.size(list.get(2), list.get(3));
        return builder.build();
    }

    /**
     * Reads a value from the given map and coerces it to the given class.
     * @param map The map to read the value from
     * @param name The name of the property
     * @param target The target class of the returned value
     * @return The value of the property from the map
     */
    @SuppressWarnings("unchecked")
    private <E> E getAs(Map<String,Object> map, String name, Class<E> target) 
        throws IllegalArgumentException
    {
        return getAs(map, name, target, true);
    }

    /**
     * Reads a value from the given map and coerces it to the given class.
     * @param map The map to read the value from
     * @param name The name of the property
     * @param target The target class of the returned value
     * @param mandatory <CODE>true</CODE> if the field cannot be null
     * @return The value of the property from the map
     */
    @SuppressWarnings("unchecked")
    private <E> E getAs(Map<String,Object> map, String name, Class<E> target, boolean mandatory) 
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
     * @param name The name of the property
     * @param value The value to coerce
     * @param target The target class of the returned value
     * @return The value 
     */
    @SuppressWarnings("unchecked")
    private <E> E coerceTo(String name, Object value, Class<E> target) 
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