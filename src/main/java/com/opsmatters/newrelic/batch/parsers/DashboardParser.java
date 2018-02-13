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
import java.io.Writer;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.logging.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions;
import org.apache.commons.lang3.StringUtils;
import com.opsmatters.core.util.FormatUtilities;
import com.opsmatters.newrelic.api.model.insights.Dashboard;
import com.opsmatters.newrelic.api.model.insights.Filter;
import com.opsmatters.newrelic.api.model.insights.widgets.Widget;
import com.opsmatters.newrelic.api.model.insights.widgets.EventChart;
import com.opsmatters.newrelic.api.model.insights.widgets.BreakdownMetricChart;
import com.opsmatters.newrelic.api.model.insights.widgets.FacetChart;
import com.opsmatters.newrelic.api.model.insights.widgets.InventoryChart;
import com.opsmatters.newrelic.api.model.insights.widgets.Markdown;
import com.opsmatters.newrelic.api.model.insights.widgets.MetricLineChart;
import com.opsmatters.newrelic.api.model.insights.widgets.ThresholdEventChart;
import com.opsmatters.newrelic.api.model.insights.widgets.TrafficLightChart;
import com.opsmatters.newrelic.api.model.insights.widgets.WidgetData;
import com.opsmatters.newrelic.api.model.insights.widgets.MarkdownData;
import com.opsmatters.newrelic.api.model.insights.widgets.EventsData;
import com.opsmatters.newrelic.api.model.insights.widgets.MetricsData;
import com.opsmatters.newrelic.api.model.insights.widgets.InventoryData;
import com.opsmatters.newrelic.api.model.insights.widgets.Threshold;
import com.opsmatters.newrelic.api.model.insights.widgets.TrafficLight;
import com.opsmatters.newrelic.api.model.insights.widgets.TrafficLightState;
import com.opsmatters.newrelic.api.model.insights.widgets.Presentation;
import com.opsmatters.newrelic.api.model.insights.widgets.DrilldownPresentation;
import com.opsmatters.newrelic.api.model.insights.widgets.ThresholdPresentation;
import com.opsmatters.newrelic.api.model.insights.widgets.TrafficLightPresentation;
import com.opsmatters.newrelic.api.model.insights.widgets.Layout;
import com.opsmatters.newrelic.api.model.metrics.Metric;

/**
 * Dashboards parser that converts to/from YAML format.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class DashboardParser
{
    private static final Logger logger = Logger.getLogger(DashboardParser.class.getName());

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
    public static List<Dashboard> fromYaml(String contents)
    {
        return getDashboards(new Yaml().load(contents));
    }

    /**
     * Reads the dashboards configuration from the given reader.
     * @param reader The reader used to read the YAML string
     * @return The dashboards read from the YAML string
     */
    public static List<Dashboard> fromYaml(Reader reader)
    {
        return getDashboards(new Yaml().load(reader));
    }

    /**
     * Writes the dashboards configuration to a YAML string.
     * @param dashboards The dashboards to be serialized
     * @param options The dumper options used to format the YAML output
     * @param banner <CODE>true</CODE> if a banner should be included in the YAML output
     * @param title The title of the YAML output, included in the banner
     * @return The dashboards as a YAML string
     */
    public static String toYaml(List<Dashboard> dashboards, DumperOptions options, boolean banner, String title)
    {
        StringBuilder sb = new StringBuilder();

        // Write the banner
        if(banner)
            sb.append(getBanner(title));
        sb.append(new Yaml(options).dump(toDashboardMap(dashboards)));

        return sb.toString();
    }

    /**
     * Writes the dashboards configuration to a YAML string.
     * @param dashboards The dashboards to be serialized
     * @param banner <CODE>true</CODE> if a banner should be included in the YAML output
     * @param title The title of the YAML output, included in the banner
     * @return The dashboards as a YAML string
     */
    public static String toYaml(List<Dashboard> dashboards, boolean banner, String title)
    {
        return toYaml(dashboards, getOptions(), banner, title);
    }

    /**
     * Writes the dashboards configuration to a YAML string.
     * @param dashboards The dashboards to be serialized
     * @param title The title of the YAML output, included in the banner
     * @return The dashboards as a YAML string
     */
    public static String toYaml(List<Dashboard> dashboards, String title)
    {
        return toYaml(dashboards, true, title);
    }

    /**
     * Writes the dashboards configuration to a YAML string.
     * @param dashboards The dashboards to be serialized
     * @return The dashboards as a YAML string
     */
    public static String toYaml(List<Dashboard> dashboards)
    {
        return toYaml(dashboards, false, null);
    }

    /**
     * Writes the dashboards configuration to a writer.
     * @param dashboards The dashboards to be serialized
     * @param writer The writer to use to serialize the dashboards
     * @param options The dumper options used to format the YAML output
     * @param banner <CODE>true</CODE> if a banner should be included in the YAML output
     * @param title The title of the YAML output, included in the banner
     */
    public static void toYaml(List<Dashboard> dashboards, Writer writer, DumperOptions options, boolean banner, String title)
    {
        if(banner)
        {
            try
            {
                // Write the banner
                writer.write(getBanner(title));
            }
            catch(IOException e)
            {
            }
        }

        new Yaml(options).dump(toDashboardMap(dashboards), writer);
    }

    /**
     * Writes the dashboards configuration to a writer.
     * @param dashboards The dashboards to be serialized
     * @param writer The writer to use to serialize the dashboards
     * @param banner <CODE>true</CODE> if a banner should be included in the YAML output
     * @param title The title of the YAML output, included in the banner
     */
    public static void toYaml(List<Dashboard> dashboards, Writer writer, boolean banner, String title)
    {
        toYaml(dashboards, writer, getOptions(), banner, title);
    }

    /**
     * Writes the dashboards configuration to a writer.
     * @param dashboards The dashboards to be serialized
     * @param writer The writer to use to serialize the dashboards
     * @param title The title of the YAML output, included in the banner
     */
    public static void toYaml(List<Dashboard> dashboards, Writer writer, String title)
    {
        toYaml(dashboards, writer, true, title);
    }

    /**
     * Writes the dashboards configuration to a writer.
     * @param dashboards The dashboards to be serialized
     * @param writer The writer to use to serialize the dashboards
     */
    public static void toYaml(List<Dashboard> dashboards, Writer writer)
    {
        toYaml(dashboards, writer, false, null);
    }

    /**
     * Returns the default dumper options used to format the YAML output.
     * @return The default dumper options
     */
    public static DumperOptions getOptions()
    {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.AUTO);
        return options;
    }

    /**
     * Returns a banner for the YAML output.
     * @param title The title of the banner
     * @return The banner
     */
    public static String getBanner(String title)
    {
        String line = StringUtils.repeat("#", 80);
        String box = StringUtils.overlay(line, StringUtils.repeat(" ", line.length()-2), 1, line.length()-1);
        String comment = "Generated by opsmatters newrelic-batch "+FormatUtilities.getFormattedDateTime();

        StringBuilder sb = new StringBuilder();
        sb.append(line).append("\n");
        sb.append(box).append("\n");
        if(title != null)
            sb.append(StringUtils.overlay(box, title, 3, title.length()+3)).append("\n");
        sb.append(StringUtils.overlay(box, comment, 3, comment.length()+3)).append("\n");
        sb.append(box).append("\n");
        sb.append(line).append("\n\n");
        return sb.toString();
    }

    /**
     * Reads the dashboards configuration from the given object.
     * @param o The dashboard configuration as a map
     * @return The dashboards read from the map
     */
    private static List<Dashboard> getDashboards(Object o)
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
    private static Dashboard getDashboard(String title, Map<String,Object> map)
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
    private static List<Widget> getWidgets(Map<String,Object> map)
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
    private static Widget getWidget(String title, Map<String,Object> map)
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
    private static Widget getMarkdown(String visualization, String title, Map<String,Object> map)
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
    private static Widget getEventChart(String visualization, String title, Map<String,Object> map)
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
    private static Widget getFacetChart(String visualization, String title, Map<String,Object> map)
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
    private static Widget getThresholdEventChart(String visualization, String title, Map<String,Object> map)
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
    private static Widget getBreakdownMetricChart(String visualization, String title, Map<String,Object> map)
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
    private static Widget getMetricLineChart(String visualization, String title, Map<String,Object> map)
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
    private static Widget getInventoryChart(String visualization, String title, Map<String,Object> map)
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
    private static Widget getTrafficLightChart(String visualization, String title, Map<String,Object> map)
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
    private static MarkdownData getMarkdownData(Map<String,Object> map)
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
    private static EventsData getEventsData(Map<String,Object> map)
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
    private static MetricsData getMetricsData(Map<String,Object> map)
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
    private static InventoryData getInventoryData(Map<String,Object> map)
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
    private static Threshold getThreshold(Map<String,Object> map)
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
    private static Metric getMetric(Map<String,Object> map)
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
    private static TrafficLight getTrafficLight(Map<String,Object> map)
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
    private static List<TrafficLightState> getTrafficLightStates(List states)
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
    private static Widget.Builder getWidget(Widget.Builder builder, String title, Map<String,Object> map)
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
    private static Layout getLayout(Map<String,Object> map)
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
    private static Layout getLayout(List<Integer> list)
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
    private static <E> E getAs(Map<String,Object> map, String name, Class<E> target) 
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
    private static <E> E getAs(Map<String,Object> map, String name, Class<E> target, boolean mandatory) 
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
    private static <E> E coerceTo(String name, Object value, Class<E> target) 
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

    /**
     * Converts the dashboards to a map.
     * @param dashboards The dashboards to be converted
     * @return The dashboards as a map
     */
    private static Map<String,Object> toDashboardMap(List<Dashboard> dashboards)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        for(Dashboard dashboard : dashboards)
            putAs(ret, dashboard.getTitle(), toMap(dashboard));
        return ret;
    }

    /**
     * Converts the dashboard to a map.
     * @param dashboard The dashboard to be converted
     * @return The dashboard as a map
     */
    private static Map<String,Object> toMap(Dashboard dashboard)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        putAs(ret, ICON, dashboard.getIcon());
        putAs(ret, VERSION, dashboard.getMetadata() != null, dashboard.getMetadata().getVersion());
        putAs(ret, VISIBILITY, dashboard.getVisibility());
        putAs(ret, EDITABLE, dashboard.getEditable());
        putAs(ret, WIDGETS, dashboard.getWidgets() != null, toWidgetMap(dashboard.getWidgets()));
        putAs(ret, FILTER, dashboard.getFilter() != null, toMap(dashboard.getFilter()));
        return ret;
    }

    /**
     * Converts the filter to a map.
     * @param filter The filter to be converted
     * @return The filter as a map
     */
    private static Map<String,Object> toMap(Filter filter)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        putAs(ret, EVENT_TYPES, filter.getEventTypes());
        putAs(ret, ATTRIBUTES, filter.getAttributes());
        return ret;
    }

    /**
     * Converts the widgets to a map.
     * @param widgets The widgets to be converted
     * @return The widgets as a map
     */
    private static Map<String,Object> toWidgetMap(List<Widget> widgets)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        for(Widget widget : widgets)
            putAs(ret, widget.getPresentation().getTitle(), widget.getPresentation() != null, toMap(widget));
        return ret;
    }

    /**
     * Converts the widget to a map.
     * @param widget The widget to be converted
     * @return The widget as a map
     */
    private static Map<String,Object> toMap(Widget widget)
    {
        Map<String,Object> ret = null;
        if(widget instanceof EventChart)
            ret = toMap((EventChart)widget);
        else if(widget instanceof BreakdownMetricChart)
            ret = toMap((BreakdownMetricChart)widget);
        else if(widget instanceof FacetChart)
            ret = toMap((FacetChart)widget);
        else if(widget instanceof InventoryChart)
            ret = toMap((InventoryChart)widget);
        else if(widget instanceof Markdown)
            ret = toMap((Markdown)widget);
        else if(widget instanceof MetricLineChart)
            ret = toMap((MetricLineChart)widget);
        else if(widget instanceof ThresholdEventChart)
            ret = toMap((ThresholdEventChart)widget);
        else if(widget instanceof TrafficLightChart)
            ret = toMap((TrafficLightChart)widget);
        return ret;
    }

    /**
     * Converts the chart widget to a map.
     * @param widget The chart widget to be converted
     * @return The event widget as a map
     */
    private static Map<String,Object> toMap(EventChart widget)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        addWidgetFields(ret, widget);
        if(widget.getData() != null)
            putAs(ret, DATA, widget.getData().size() > 0, toMap((EventsData)widget.getData().get(0)));
        return ret;
    }

    /**
     * Converts the chart widget to a map.
     * @param widget The chart widget to be converted
     * @return The chart widget as a map
     */
    private static Map<String,Object> toMap(BreakdownMetricChart widget)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        addWidgetFields(ret, widget);
        if(widget.getData() != null)
            putAs(ret, DATA, widget.getData().size() > 0, toMap((MetricsData)widget.getData().get(0)));
        return ret;
    }

    /**
     * Converts the chart widget to a map.
     * @param widget The chart widget to be converted
     * @return The event widget as a map
     */
    private static Map<String,Object> toMap(FacetChart widget)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        addWidgetFields(ret, widget);
        if(widget.getData() != null)
            putAs(ret, DATA, widget.getData().size() > 0, toMap((EventsData)widget.getData().get(0)));
        return ret;
    }

    /**
     * Converts the chart widget to a map.
     * @param widget The chart widget to be converted
     * @return The event widget as a map
     */
    private static Map<String,Object> toMap(InventoryChart widget)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        addWidgetFields(ret, widget);
        if(widget.getData() != null)
            putAs(ret, DATA, widget.getData().size() > 0, toMap((InventoryData)widget.getData().get(0)));
        return ret;
    }

    /**
     * Converts the chart widget to a map.
     * @param widget The chart widget to be converted
     * @return The event widget as a map
     */
    private static Map<String,Object> toMap(MetricLineChart widget)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        addWidgetFields(ret, widget);
        if(widget.getData() != null)
            putAs(ret, DATA, widget.getData().size() > 0, toMap((MetricsData)widget.getData().get(0)));
        return ret;
    }

    /**
     * Converts the chart widget to a map.
     * @param widget The chart widget to be converted
     * @return The event widget as a map
     */
    private static Map<String,Object> toMap(ThresholdEventChart widget)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        addWidgetFields(ret, widget);
        if(widget.getData() != null)
            putAs(ret, DATA, widget.getData().size() > 0, toMap((EventsData)widget.getData().get(0)));
        return ret;
    }

    /**
     * Converts the chart widget to a map.
     * @param widget The chart widget to be converted
     * @return The event widget as a map
     */
    private static Map<String,Object> toMap(TrafficLightChart widget)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        addWidgetFields(ret, widget);
        if(widget.getData() != null)
            putAs(ret, DATA, widget.getData().size() > 0, toMap((EventsData)widget.getData().get(0)));
        return ret;
    }

    /**
     * Converts the markdown widget to a map.
     * @param widget The markdown widget to be converted
     * @return The markdown widget as a map
     */
    private static Map<String,Object> toMap(Markdown widget)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        addWidgetFields(ret, widget);
        if(widget.getData() != null)
            putAs(ret, DATA, widget.getData().size() > 0, toMap((MarkdownData)widget.getData().get(0)));
        return ret;
    }

    /**
     * Converts the given event data to a map.
     * @param data The event data to be converted
     * @return The event data as a map
     */
    private static Map<String,Object> toMap(EventsData data)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        putAs(ret, NRQL, data.getNrql() != null, data.getNrql());
        return ret;
    }

    /**
     * Converts the given metric data to a map.
     * @param data The metric data to be converted
     * @return The metric data as a map
     */
    private static Map<String,Object> toMap(MetricsData data)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        putAs(ret, DURATION, data.getDuration() != null, data.getDuration());
        putAs(ret, END_TIME, data.getEndTime() != null, data.getEndTime());
        putAs(ret, ENTITY_IDS, data.getEntityIds() != null, data.getEntityIds());
        putAs(ret, METRICS, data.getMetrics() != null, toMetricList(data.getMetrics()));
        putAs(ret, ORDER_BY, data.getOrderBy() != null, data.getOrderBy());
        putAs(ret, LIMIT, data.getLimit() != null, data.getLimit());
        return ret;
    }

    /**
     * Converts the given inventory data to a map.
     * @param data The inventory data to be converted
     * @return The inventory data as a map
     */
    private static Map<String,Object> toMap(InventoryData data)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        putAs(ret, SOURCES, data.getSources() != null, data.getSources());
        putAs(ret, FILTERS, data.getFilters() != null, data.getFilters());
        return ret;
    }

    /**
     * Converts the given markdown data to a map.
     * @param data The markdown data to be converted
     * @return The markdown data as a map
     */
    private static Map<String,Object> toMap(MarkdownData data)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        putAs(ret, SOURCE, data.getSource() != null, data.getSource());
        return ret;
    }

    /**
     * Adds the common widget fields to the given map.
     * @param map The map to write the fields to
     * @param widget The widget to be converted
     */
    private static void addWidgetFields(Map<String,Object> map, Widget widget)
    {
        putAs(map, VISUALIZATION, widget.getVisualization());
        if(widget.getPresentation() != null)
            addPresentationFields(map, widget.getPresentation());
        putAs(map, LAYOUT, widget.getLayout() != null, toMap(widget.getLayout()));
        putAs(map, ACCOUNT_ID, widget.getAccountId());
    }

    /**
     * Adds the presentation fields to the given map.
     * @param map The map to write the fields to
     * @param presentation The presentation to be converted
     */
    private static void addPresentationFields(Map<String,Object> map, Presentation presentation)
    {
        putAs(map, NOTES, presentation.getNotes() != null, presentation.getNotes());
        if(presentation instanceof DrilldownPresentation)
            addPresentationFields(map, (DrilldownPresentation)presentation);
        else if(presentation instanceof ThresholdPresentation)
            addPresentationFields(map, (ThresholdPresentation)presentation);
        else if(presentation instanceof TrafficLightPresentation)
            addPresentationFields(map, (TrafficLightPresentation)presentation);
    }

    /**
     * Adds the presentation fields to the given map.
     * @param map The map to write the fields to
     * @param presentation The presentation to be converted
     */
    private static void addPresentationFields(Map<String,Object> map, DrilldownPresentation presentation)
    {
        putAs(map, DRILLDOWN_DASHBOARD_ID, presentation.getDrilldownDashboardId() != null, presentation.getDrilldownDashboardId());
    }

    /**
     * Adds the presentation fields to the given map.
     * @param map The map to write the fields to
     * @param presentation The presentation to be converted
     */
    private static void addPresentationFields(Map<String,Object> map, ThresholdPresentation presentation)
    {
        putAs(map, THRESHOLD, presentation.getThreshold() != null, toMap(presentation.getThreshold()));
    }

    /**
     * Adds the presentation fields to the given map.
     * @param map The map to write the fields to
     * @param presentation The presentation to be converted
     */
    private static void addPresentationFields(Map<String,Object> map, TrafficLightPresentation presentation)
    {
        List<TrafficLight> trafficLights = presentation.getTrafficLights();
        if(trafficLights != null)
        {
            for(TrafficLight trafficLight : trafficLights)
                putAs(map, TRAFFIC_LIGHT, trafficLight != null, toMap(trafficLight));
        }
    }

    /**
     * Converts the threshold to a map.
     * @param threshold The threshold to be converted
     * @return The threshold as a map
     */
    private static Map<String,Object> toMap(Threshold threshold)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        putAs(ret, RED, threshold.getRed());
        putAs(ret, YELLOW, threshold.getYellow());
        return ret;
    }

    /**
     * Converts the traffic light to a map.
     * @param trafficLight The traffic light to be converted
     * @return The traffic light as a map
     */
    private static Map<String,Object> toMap(TrafficLight trafficLight)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        putAs(ret, ID, trafficLight.getId());
        putAs(ret, TITLE, trafficLight.getTitle());
        putAs(ret, SUBTITLE, trafficLight.getSubtitle());
        putAs(ret, STATES, trafficLight.getStates() != null, toStateList(trafficLight.getStates()));

        return ret;
    }

    /**
     * Converts the given state list to a list of maps.
     * @param state The state list to be converted
     * @return The state list as a list of maps
     */
    private static List<Map<String,Object>> toStateList(List<TrafficLightState> states)
    {
        List<Map<String,Object>> ret = new ArrayList<Map<String,Object>>();

        if(states != null)
        {
            for(TrafficLightState state : states)
                ret.add(toMap(state));
        }

        return ret;
    }

    /**
     * Converts the given state to a map.
     * @param metric The state to be converted
     * @return The state as a map
     */
    private static Map<String,Object> toMap(TrafficLightState state)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        putAs(ret, TYPE, state.getType() != null, state.getType());
        putAs(ret, MIN, state.getMin() != null, state.getMin());
        putAs(ret, MAX, state.getMax() != null, state.getMax());
        return ret;
    }

    /**
     * Converts the given metric list to a list of maps.
     * @param metrics The metric list to be converted
     * @return The metric list as a list of maps
     */
    private static List<Map<String,Object>> toMetricList(List<Metric> metrics)
    {
        List<Map<String,Object>> ret = new ArrayList<Map<String,Object>>();

        if(metrics != null)
        {
            for(Metric metric : metrics)
                ret.add(toMap(metric));
        }

        return ret;
    }

    /**
     * Converts the given metric to a map.
     * @param metric The metric to be converted
     * @return The metric as a map
     */
    private static Map<String,Object> toMap(Metric metric)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        putAs(ret, NAME, metric.getName() != null, metric.getName());
        putAs(ret, UNITS, metric.getUnits() != null, metric.getUnits());
        putAs(ret, SCOPE, metric.getScope() != null, metric.getScope());
        putAs(ret, VALUES, metric.getValues() != null, metric.getValues());
        return ret;
    }

    /**
     * Converts the widget layout to a map.
     * @param layout The widget layout to be converted
     * @return The widget layout as a map
     */
    private static Map<String,Object> toMap(Layout layout)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        putAs(ret, ROW, layout.getRow());
        putAs(ret, COLUMN, layout.getColumn());
        putAs(ret, WIDTH, layout.getWidth());
        putAs(ret, HEIGHT, layout.getHeight());
        return ret;
    }

    /**
     * Adds the given name and value to the given map.
     * @param map The map to write the field to
     * @param name The name of the field
     * @param put <CODE>true</CODE> if the field should be added
     * @param value The value of the field
     */
    private static void putAs(Map<String,Object> map, String name, boolean put, Object value)
    {
        if(put)
            map.put(name, value);
    }

    /**
     * Adds the given name and value to the given map.
     * @param map The map to write the field to
     * @param name The name of the field
     * @param value The value of the field
     */
    private static void putAs(Map<String,Object> map, String name, Object value)
    {
        putAs(map, name, value != null, value);
    }
}