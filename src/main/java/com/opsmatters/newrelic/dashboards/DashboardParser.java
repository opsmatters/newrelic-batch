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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;
import org.yaml.snakeyaml.Yaml;
import com.opsmatters.newrelic.api.model.insights.Dashboard;
import com.opsmatters.newrelic.api.model.insights.widgets.Widget;
import com.opsmatters.newrelic.api.model.insights.widgets.Layout;
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
import com.opsmatters.newrelic.api.model.metrics.Metric;

/**
 * Dashboards parser that converts to/from YAML format.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class DashboardParser
{
    private static final Logger logger = Logger.getLogger(DashboardConfiguration.class.getName());

    /**
     * Private constructor.
     */
    private DashboardParser()
    {
    }

    /**
     * Reads the dashboards configuration from the given map.
     * @param contents The contents of the YAML file as a string
     * @return The dashboards read from the YAML string
     */
    public static List<Dashboard> fromYaml(String contents)
    {
        List<Dashboard> ret = new ArrayList<Dashboard>();

        Yaml yaml = new Yaml();
        Map<String,Object> map = (Map<String,Object>)yaml.load(contents);
        for(Map.Entry<String,Object> entry : map.entrySet())
        {
            ret.add(getDashboard(entry.getKey(), (Map<String,Object>)entry.getValue()));
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
        Map<String,Object> filter = (Map<String,Object>)map.get("filter");
        if(filter != null)
        {
            eventTypes = (List<String>)filter.get("event_types");
            attributes = (List<String>)filter.get("attributes");
        }

        return Dashboard.builder()
            .title(title)
            .icon((String)map.get("icon"))
            .version(getIntValue(map, "version", 1))
            .visibility((String)map.get("visibility"))
            .editable((String)map.get("editable"))
            .setFilter(eventTypes, attributes)
            .widgets(getWidgets((Map<String,Object>)map.get("widgets")))
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
                ret.add(getWidget(entry.getKey(), (Map<String,Object>)entry.getValue()));
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
        String visualization = (String)map.get("visualization");
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
            .addData(getMarkdownData((Map<String,Object>)map.get("data")));
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
            .addData(getEventsData((Map<String,Object>)map.get("data")));
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
            .addData(getEventsData((Map<String,Object>)map.get("data")));

        Object id = map.get("drilldown_dashboard_id");
        if(id instanceof Integer)
            builder = builder.drilldownDashboardId((Integer)id);

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
            .threshold(getThreshold((Map<String,Object>)map.get("threshold")))
            .addData(getEventsData((Map<String,Object>)map.get("data")));
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
            .addData(getMetricsData((Map<String,Object>)map.get("data")));
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
            .addData(getMetricsData((Map<String,Object>)map.get("data")));
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
            .addData(getInventoryData((Map<String,Object>)map.get("data")));
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
            .addData(getEventsData((Map<String,Object>)map.get("data")))
            .addTrafficLight(getTrafficLight((Map<String,Object>)map.get("traffic_light")));
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
            .source((String)map.get("source"))
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
            .nrql((String)map.get("nrql"))
            .build();
    }

    /**
     * Creates a metric data item.
     * @param map The configuration properties
     * @return The widget data
     */
    private static MetricsData getMetricsData(Map<String,Object> map)
    {
        Integer limit = null;
        Object l = map.get("limit");
        if(l instanceof Integer)
            limit = (Integer)l;

        Integer duration = null;
        Object d = map.get("duration");
        if(d instanceof Integer)
            duration = (Integer)d;

        List<Long> entityIds = null;
        Object e = map.get("entity_ids");
        if(e instanceof List)
            entityIds = (List<Long>)e;

        List<Metric> metrics = null;
        Object m = map.get("metrics");

        if(m instanceof List)
        {
            metrics = new ArrayList<Metric>();
            for(Map item : (List<Map>)m)
                metrics.add(getMetric(item));
        }

        MetricsData.Builder builder = MetricsData.builder()
            .orderBy((String)map.get("order_by"));
        if(duration != null)
            builder = builder.duration(duration);
        if(entityIds != null)
            builder = builder.entityIds(entityIds);
        if(metrics != null)
            builder = builder.metrics(metrics);
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
        List<String> sources = null;
        Object s = map.get("sources");
        if(s instanceof List)
            sources = (List<String>)s;

        Map<String,String> filters = null;
        Object f = map.get("filters");
        if(f instanceof Map)
            filters = (Map<String,String>)f;

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
            .red((Integer)map.get("red"))
            .yellow((Integer)map.get("yellow"))
            .build();
    }

    /**
     * Creates a metric item.
     * @param map The configuration properties
     * @return The metric
     */
    private static Metric getMetric(Map<String,Object> map)
    {
        List<String> values = null;
        Object v = map.get("values");
        if(v instanceof List)
            values = (List<String>)v;

        return Metric.builder()
            .name((String)map.get("name"))
            .values(values)
            .build();
    }

    /**
     * Creates a traffic light.
     * @param map The configuration properties
     * @return The traffic light
     */
    private static TrafficLight getTrafficLight(Map<String,Object> map)
    {
        List<TrafficLightState> states = null;
        Object s = map.get("states");
        if(s instanceof List)
            states = getTrafficLightStates((List)s);

        return TrafficLight.builder()
            .id((String)map.get("id"))
            .title((String)map.get("title"))
            .subtitle((String)map.get("subtitle"))
            .states(states)
            .build();
    }

    /**
     * Creates a set of traffic light states.
     * @param map The configuration properties
     * @return The traffic light states
     */
    private static List<TrafficLightState> getTrafficLightStates(List states)
    {
        List<TrafficLightState> ret = new ArrayList<TrafficLightState>();
        for(Object state : states)
        {
            if(state instanceof Map)
            {
                Map<String,Object> map = (Map<String,Object>)state;

                TrafficLightState.Builder builder = TrafficLightState.builder()
                    .type((String)map.get("type"));

                Integer min = (Integer)map.get("min");
                if(min != null)
                    builder = builder.min(min);

                Integer max = (Integer)map.get("max");
                if(max != null)
                    builder = builder.max(max);

                ret.add(builder.build());
            }
        }
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
            .notes((String)map.get("notes"));

        Integer accountId = (Integer)map.get("account_id");
        if(accountId != null)
            builder = builder.accountId(accountId);

        Object layout = map.get("layout");
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
        return Layout.builder()
            .row(getIntValue(map, "row", 0))
            .column(getIntValue(map, "column", 0))
            .width(getIntValue(map, "width", 1))
            .height(getIntValue(map, "height", 1))
            .build();
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
     * Reads an integer value from the given map.
     * @param map The map to read the value from
     * @param name The name of the property
     * @param deflt Default value to use if the value is not found
     * @return The integer value
     */
    private static int getIntValue(Map<String,Object> map, String name, int deflt)
    {
        int ret = -1;
        Object value = map.get(name);
        if(value != null)
        {
            ret = Integer.parseInt(value.toString());
        }
        else
        {
            ret = deflt;
        }
        return ret;
    }
}