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
import com.opsmatters.newrelic.api.model.insights.Metadata;
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
 * Parser that converts dashboards from YAML format.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class DashboardParser extends BaseParser
{
    private static final Logger logger = Logger.getLogger(DashboardParser.class.getName());

    /**
     * Private constructor.
     */
    private DashboardParser()
    {
    }

    /**
     * Reads the dashboards from the given string.
     * @param contents The contents of the file as a YAML string
     * @return The dashboards read from the YAML string
     */
    public static List<Dashboard> parseYaml(String contents)
    {
        return new DashboardParser().getDashboards(new Yaml().load(contents));
    }

    /**
     * Reads the dashboards from the given reader.
     * @param reader The reader used to read the YAML string
     * @return The dashboards read from the YAML string
     */
    public static List<Dashboard> parseYaml(Reader reader)
    {
        return new DashboardParser().getDashboards(new Yaml().load(reader));
    }

    /**
     * Reads the dashboards from the given object.
     * @param o The dashboards as a map
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
     * Creates a dashboard from the given map.
     * @param title The title of the dashboard
     * @param map The configuration properties
     * @return The dashboard
     */
    private Dashboard getDashboard(String title, Map<String,Object> map)
    {
        // Get the filter
        List<String> eventTypes = null;
        List<String> attributes = null;
        Map<String,Object> filter = getAs(map,  Dashboard.FILTER, Map.class);
        if(filter != null)
        {
            eventTypes = getAs(filter, Filter.EVENT_TYPES, List.class);
            attributes = getAs(filter, Filter.ATTRIBUTES, List.class);
        }

        return Dashboard.builder()
            .title(title)
            .icon(getAs(map, Dashboard.ICON, String.class, false))
            .version(getAs(map, Metadata.VERSION, Integer.class))
            .visibility(getAs(map, Dashboard.VISIBILITY, String.class))
            .editable(getAs(map, Dashboard.EDITABLE, String.class))
            .setFilter(eventTypes, attributes)
            .widgets(getWidgets(getAs(map, Dashboard.WIDGETS, Map.class)))
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
     * Creates a widget from the given map.
     * @param title The title of the widget
     * @param map The configuration properties
     * @return The widget
     */
    private Widget getWidget(String title, Map<String,Object> map)
    {
        Widget ret = null;
        String visualization = getAs(map, Widget.VISUALIZATION, String.class);
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
            .addData(getMarkdownData(getAs(map, Widget.DATA, Map.class)));
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
            .addData(getEventsData(getAs(map, Widget.DATA, Map.class)));
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
            .addData(getEventsData(getAs(map, Widget.DATA, Map.class)));

        Integer id = getAs(map, DrilldownPresentation.DRILLDOWN_DASHBOARD_ID, Integer.class, false);
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
            .threshold(getThreshold(getAs(map, ThresholdPresentation.THRESHOLD, Map.class)))
            .addData(getEventsData(getAs(map, Widget.DATA, Map.class)));
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
            .addData(getMetricsData(getAs(map, Widget.DATA, Map.class)));
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
            .addData(getMetricsData(getAs(map, Widget.DATA, Map.class)));
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
            .addData(getInventoryData(getAs(map, Widget.DATA, Map.class)));
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
            .addData(getEventsData(getAs(map, Widget.DATA, Map.class)))
            .addTrafficLight(getTrafficLight(getAs(map, TrafficLightPresentation.TRAFFIC_LIGHT, Map.class)));
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
            .source(getAs(map, MarkdownData.SOURCE, String.class))
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
            .nrql(getAs(map, EventsData.NRQL, String.class))
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
            .orderBy(getAs(map, MetricsData.ORDER_BY, String.class, false));

        Integer duration = getAs(map, MetricsData.DURATION, Integer.class, false);
        if(duration != null)
            builder = builder.duration(duration);

        List<Metric> metrics = null;
        List list = getAs(map, MetricsData.METRICS, List.class, false);
        if(list != null)
        {
            metrics = new ArrayList<Metric>();
            for(Object item : list)
                metrics.add(getMetric(coerceTo(MetricsData.METRICS, item, Map.class)));
        }

        if(metrics != null)
            builder = builder.metrics(metrics);

        List<Long> entityIds = getAs(map, MetricsData.ENTITY_IDS, List.class, false);
        if(entityIds != null)
            builder = builder.entityIds(entityIds);

        Integer limit = getAs(map, MetricsData.LIMIT, Integer.class, false);
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
        List<String> sources = getAs(map, InventoryData.SOURCES, List.class);
        if(sources == null)
            sources = new ArrayList<String>();

        Map<String,String> filters = getAs(map, InventoryData.FILTERS, Map.class, false);
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
        Threshold.Builder builder = Threshold.builder();
        Integer red = getAs(map, Threshold.RED, Integer.class);
        Integer yellow = getAs(map, Threshold.YELLOW, Integer.class, false);
        if(red != null)
            builder = builder.red(red);
        if(yellow != null)
            builder = builder.yellow(yellow);
        return builder.build();
    }

    /**
     * Creates a metric item.
     * @param map The configuration properties
     * @return The metric
     */
    private Metric getMetric(Map<String,Object> map)
    {
        List<String> values = getAs(map, Metric.VALUES, List.class, false);
        Metric.Builder builder = Metric.builder()
            .name(getAs(map, Metric.NAME, String.class, false));
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
            .id(getAs(map, TrafficLight.ID, String.class))
            .title(getAs(map, TrafficLight.TITLE, String.class, false))
            .subtitle(getAs(map, TrafficLight.SUBTITLE, String.class, false))
            .states(getTrafficLightStates(getAs(map, TrafficLight.STATES, List.class)))
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
                    .type(getAs(map, TrafficLightState.TYPE, String.class));

                Integer min = getAs(map, TrafficLightState.MIN, Integer.class);
                if(min != null)
                    builder = builder.min(min);

                Integer max = getAs(map, TrafficLightState.MAX, Integer.class);
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
            .notes(getAs(map, Presentation.NOTES, String.class, false));

        Integer accountId = getAs(map, Widget.ACCOUNT_ID, Integer.class);
        if(accountId != null)
            builder = builder.accountId(accountId);

        Object layout = map.get(Widget.LAYOUT);
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

        Integer row = getAs(map, Layout.ROW, Integer.class);
        if(row != null)
            builder = builder.row(row);

        Integer column = getAs(map, Layout.COLUMN, Integer.class);
        if(column != null)
            builder = builder.column(column);

        Integer width = getAs(map, Layout.WIDTH, Integer.class, false);
        if(width != null)
            builder = builder.width(width);

        Integer height = getAs(map, Layout.HEIGHT, Integer.class, false);
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
}