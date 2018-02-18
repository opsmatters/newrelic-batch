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
 * Renderer that converts dashboards to YAML documents.
 * 
 * @author Gerald Curley (opsmatters)
 */
public class DashboardRenderer
{
    private static final Logger logger = Logger.getLogger(DashboardRenderer.class.getName());

    private DumperOptions options = new DumperOptions();
    private boolean banner = false;
    private String title;

    /**
     * Default constructor.
     */
    public DashboardRenderer()
    {
        // Set the default output options
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.AUTO);
    }

    /**
     * Sets the title of the banner.
     * @param title The title of the banner
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * Returns the title of the banner.
     * @return The title of the banner
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Set to <CODE>true</CODE> if the output should include a banner.
     * @param banner <CODE>true</CODE> if the output should include a banner
     */
    public void setBanner(boolean banner)
    {
        this.banner = banner;
    }

    /**
     * Returns <CODE>true</CODE> if the output should include a banner.
     * @return <CODE>true</CODE> if the output should include a banner
     */
    public boolean getBanner()
    {
        return banner;
    }

    /**
     * Returns the options for the output.
     * @return The options for the output
     */
    public DumperOptions getOptions()
    {
        return options;
    }

    /**
     * Sets the options for the output.
     * @param options The options for the output
     */
    public void setOptions(DumperOptions options)
    {
        this.options = options;
    }

    /**
     * Writes the given dashboards to a YAML string.
     * @param dashboards The dashboards to be serialized
     * @return The dashboards as a YAML string
     */
    public static String writeYaml(List<Dashboard> dashboards)
    {
        return new DashboardRenderer().renderYaml(dashboards);
    }

    /**
     * Writes the given dashboards to a YAML string.
     * @param dashboards The dashboards to be serialized
     * @return The dashboards as a YAML string
     */
    public String renderYaml(List<Dashboard> dashboards)
    {
        StringBuilder sb = new StringBuilder();

        // Write the banner
        if(banner)
            sb.append(getBanner(title));
        sb.append(new Yaml(options).dump(toDashboardMap(dashboards)));

        return sb.toString();
    }

    /**
     * Writes the given dashboards to a writer.
     * @param dashboards The dashboards to be serialized
     * @param writer The writer to use to serialize the dashboards
     */
    public static void toYaml(List<Dashboard> dashboards, Writer writer)
    {
        new DashboardRenderer().renderYaml(dashboards, writer);
    }

    /**
     * Writes the given dashboards to a writer.
     * @param dashboards The dashboards to be serialized
     * @param writer The writer to use to serialize the dashboards
     */
    public void renderYaml(List<Dashboard> dashboards, Writer writer)
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
     * Returns a banner for the YAML output.
     * @param title The title of the banner
     * @return The banner
     */
    public String getBanner(String title)
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
     * Converts the dashboards to a map.
     * @param dashboards The dashboards to be converted
     * @return The dashboards as a map
     */
    private Map<String,Object> toDashboardMap(List<Dashboard> dashboards)
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
    private Map<String,Object> toMap(Dashboard dashboard)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        putAs(ret, Dashboard.ICON, dashboard.getIcon());
        putAs(ret, Metadata.VERSION, dashboard.getMetadata() != null, dashboard.getMetadata().getVersion());
        putAs(ret, Dashboard.VISIBILITY, dashboard.getVisibility());
        putAs(ret, Dashboard.EDITABLE, dashboard.getEditable());
        putAs(ret, Dashboard.WIDGETS, dashboard.getWidgets() != null, toWidgetMap(dashboard.getWidgets()));
        putAs(ret, Dashboard.FILTER, dashboard.getFilter() != null, toMap(dashboard.getFilter()));
        return ret;
    }

    /**
     * Converts the filter to a map.
     * @param filter The filter to be converted
     * @return The filter as a map
     */
    private Map<String,Object> toMap(Filter filter)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        putAs(ret, Filter.EVENT_TYPES, filter.getEventTypes());
        putAs(ret, Filter.ATTRIBUTES, filter.getAttributes());
        return ret;
    }

    /**
     * Converts the widgets to a map.
     * @param widgets The widgets to be converted
     * @return The widgets as a map
     */
    private Map<String,Object> toWidgetMap(List<Widget> widgets)
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
    private Map<String,Object> toMap(Widget widget)
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
    private Map<String,Object> toMap(EventChart widget)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        addWidgetFields(ret, widget);
        if(widget.getData() != null)
            putAs(ret, Widget.DATA, widget.getData().size() > 0, toMap((EventsData)widget.getData().get(0)));
        return ret;
    }

    /**
     * Converts the chart widget to a map.
     * @param widget The chart widget to be converted
     * @return The chart widget as a map
     */
    private Map<String,Object> toMap(BreakdownMetricChart widget)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        addWidgetFields(ret, widget);
        if(widget.getData() != null)
            putAs(ret, Widget.DATA, widget.getData().size() > 0, toMap((MetricsData)widget.getData().get(0)));
        return ret;
    }

    /**
     * Converts the chart widget to a map.
     * @param widget The chart widget to be converted
     * @return The event widget as a map
     */
    private Map<String,Object> toMap(FacetChart widget)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        addWidgetFields(ret, widget);
        if(widget.getData() != null)
            putAs(ret, Widget.DATA, widget.getData().size() > 0, toMap((EventsData)widget.getData().get(0)));
        return ret;
    }

    /**
     * Converts the chart widget to a map.
     * @param widget The chart widget to be converted
     * @return The event widget as a map
     */
    private Map<String,Object> toMap(InventoryChart widget)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        addWidgetFields(ret, widget);
        if(widget.getData() != null)
            putAs(ret, Widget.DATA, widget.getData().size() > 0, toMap((InventoryData)widget.getData().get(0)));
        return ret;
    }

    /**
     * Converts the chart widget to a map.
     * @param widget The chart widget to be converted
     * @return The event widget as a map
     */
    private Map<String,Object> toMap(MetricLineChart widget)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        addWidgetFields(ret, widget);
        if(widget.getData() != null)
            putAs(ret, Widget.DATA, widget.getData().size() > 0, toMap((MetricsData)widget.getData().get(0)));
        return ret;
    }

    /**
     * Converts the chart widget to a map.
     * @param widget The chart widget to be converted
     * @return The event widget as a map
     */
    private Map<String,Object> toMap(ThresholdEventChart widget)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        addWidgetFields(ret, widget);
        if(widget.getData() != null)
            putAs(ret, Widget.DATA, widget.getData().size() > 0, toMap((EventsData)widget.getData().get(0)));
        return ret;
    }

    /**
     * Converts the chart widget to a map.
     * @param widget The chart widget to be converted
     * @return The event widget as a map
     */
    private Map<String,Object> toMap(TrafficLightChart widget)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        addWidgetFields(ret, widget);
        if(widget.getData() != null)
            putAs(ret, Widget.DATA, widget.getData().size() > 0, toMap((EventsData)widget.getData().get(0)));
        return ret;
    }

    /**
     * Converts the markdown widget to a map.
     * @param widget The markdown widget to be converted
     * @return The markdown widget as a map
     */
    private Map<String,Object> toMap(Markdown widget)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        addWidgetFields(ret, widget);
        if(widget.getData() != null)
            putAs(ret, Widget.DATA, widget.getData().size() > 0, toMap((MarkdownData)widget.getData().get(0)));
        return ret;
    }

    /**
     * Converts the given event data to a map.
     * @param data The event data to be converted
     * @return The event data as a map
     */
    private Map<String,Object> toMap(EventsData data)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        putAs(ret, EventsData.NRQL, data.getNrql() != null, data.getNrql());
        return ret;
    }

    /**
     * Converts the given metric data to a map.
     * @param data The metric data to be converted
     * @return The metric data as a map
     */
    private Map<String,Object> toMap(MetricsData data)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        putAs(ret, MetricsData.DURATION, data.getDuration() != null, data.getDuration());
        putAs(ret, MetricsData.END_TIME, data.getEndTime() != null, data.getEndTime());
        putAs(ret, MetricsData.ENTITY_IDS, data.getEntityIds() != null, data.getEntityIds());
        putAs(ret, MetricsData.METRICS, data.getMetrics() != null, toMetricList(data.getMetrics()));
        putAs(ret, MetricsData.ORDER_BY, data.getOrderBy() != null, data.getOrderBy());
        putAs(ret, MetricsData.LIMIT, data.getLimit() != null, data.getLimit());
        return ret;
    }

    /**
     * Converts the given inventory data to a map.
     * @param data The inventory data to be converted
     * @return The inventory data as a map
     */
    private Map<String,Object> toMap(InventoryData data)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        putAs(ret, InventoryData.SOURCES, data.getSources() != null, data.getSources());
        putAs(ret, InventoryData.FILTERS, data.getFilters() != null, data.getFilters());
        return ret;
    }

    /**
     * Converts the given markdown data to a map.
     * @param data The markdown data to be converted
     * @return The markdown data as a map
     */
    private Map<String,Object> toMap(MarkdownData data)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        putAs(ret, MarkdownData.SOURCE, data.getSource() != null, data.getSource());
        return ret;
    }

    /**
     * Adds the common widget fields to the given map.
     * @param map The map to write the fields to
     * @param widget The widget to be converted
     */
    private void addWidgetFields(Map<String,Object> map, Widget widget)
    {
        putAs(map, Widget.VISUALIZATION, widget.getVisualization());
        if(widget.getPresentation() != null)
            addPresentationFields(map, widget.getPresentation());
        putAs(map, Widget.LAYOUT, widget.getLayout() != null, toMap(widget.getLayout()));
        putAs(map, Widget.ACCOUNT_ID, widget.getAccountId());
    }

    /**
     * Adds the presentation fields to the given map.
     * @param map The map to write the fields to
     * @param presentation The presentation to be converted
     */
    private void addPresentationFields(Map<String,Object> map, Presentation presentation)
    {
        putAs(map, Presentation.NOTES, presentation.getNotes() != null, presentation.getNotes());
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
    private void addPresentationFields(Map<String,Object> map, DrilldownPresentation presentation)
    {
        putAs(map, DrilldownPresentation.DRILLDOWN_DASHBOARD_ID, presentation.getDrilldownDashboardId() != null, presentation.getDrilldownDashboardId());
    }

    /**
     * Adds the presentation fields to the given map.
     * @param map The map to write the fields to
     * @param presentation The presentation to be converted
     */
    private void addPresentationFields(Map<String,Object> map, ThresholdPresentation presentation)
    {
        putAs(map, ThresholdPresentation.THRESHOLD, presentation.getThreshold() != null, toMap(presentation.getThreshold()));
    }

    /**
     * Adds the presentation fields to the given map.
     * @param map The map to write the fields to
     * @param presentation The presentation to be converted
     */
    private void addPresentationFields(Map<String,Object> map, TrafficLightPresentation presentation)
    {
        List<TrafficLight> trafficLights = presentation.getTrafficLights();
        if(trafficLights != null)
        {
            for(TrafficLight trafficLight : trafficLights)
                putAs(map, TrafficLightPresentation.TRAFFIC_LIGHT, trafficLight != null, toMap(trafficLight));
        }
    }

    /**
     * Converts the threshold to a map.
     * @param threshold The threshold to be converted
     * @return The threshold as a map
     */
    private Map<String,Object> toMap(Threshold threshold)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        putAs(ret, Threshold.RED, threshold.getRed());
        putAs(ret, Threshold.YELLOW, threshold.getYellow());
        return ret;
    }

    /**
     * Converts the traffic light to a map.
     * @param trafficLight The traffic light to be converted
     * @return The traffic light as a map
     */
    private Map<String,Object> toMap(TrafficLight trafficLight)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        putAs(ret, TrafficLight.ID, trafficLight.getId());
        putAs(ret, TrafficLight.TITLE, trafficLight.getTitle());
        putAs(ret, TrafficLight.SUBTITLE, trafficLight.getSubtitle());
        putAs(ret, TrafficLight.STATES, trafficLight.getStates() != null, toStateList(trafficLight.getStates()));

        return ret;
    }

    /**
     * Converts the given state list to a list of maps.
     * @param state The state list to be converted
     * @return The state list as a list of maps
     */
    private List<Map<String,Object>> toStateList(List<TrafficLightState> states)
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
    private Map<String,Object> toMap(TrafficLightState state)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        putAs(ret, TrafficLightState.TYPE, state.getType() != null, state.getType());
        putAs(ret, TrafficLightState.MIN, state.getMin() != null, state.getMin());
        putAs(ret, TrafficLightState.MAX, state.getMax() != null, state.getMax());
        return ret;
    }

    /**
     * Converts the given metric list to a list of maps.
     * @param metrics The metric list to be converted
     * @return The metric list as a list of maps
     */
    private List<Map<String,Object>> toMetricList(List<Metric> metrics)
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
    private Map<String,Object> toMap(Metric metric)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        putAs(ret, Metric.NAME, metric.getName() != null, metric.getName());
        putAs(ret, Metric.UNITS, metric.getUnits() != null, metric.getUnits());
        putAs(ret, Metric.SCOPE, metric.getScope() != null, metric.getScope());
        putAs(ret, Metric.VALUES, metric.getValues() != null, metric.getValues());
        return ret;
    }

    /**
     * Converts the widget layout to a map.
     * @param layout The widget layout to be converted
     * @return The widget layout as a map
     */
    private Map<String,Object> toMap(Layout layout)
    {
        Map<String,Object> ret = new LinkedHashMap<String,Object>();
        putAs(ret, Layout.ROW, layout.getRow());
        putAs(ret, Layout.COLUMN, layout.getColumn());
        putAs(ret, Layout.WIDTH, layout.getWidth());
        putAs(ret, Layout.HEIGHT, layout.getHeight());
        return ret;
    }

    /**
     * Adds the given name and value to the given map.
     * @param map The map to write the field to
     * @param name The name of the field
     * @param put <CODE>true</CODE> if the field should be added
     * @param value The value of the field
     */
    private void putAs(Map<String,Object> map, String name, boolean put, Object value)
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
    private void putAs(Map<String,Object> map, String name, Object value)
    {
        putAs(map, name, value != null, value);
    }

    /**
     * Returns a builder for the renderer.
     * @return The builder instance.
     */
    public static Builder builder()
    {
        return new Builder();
    }

    /**
     * Builder to make renderer construction easier.
     */
    public static class Builder
    {
        private DashboardRenderer renderer = new DashboardRenderer();

        /**
         * Sets the title of the banner in the output.
         * @param title The title of the banner
         * @return This object
         */
        public Builder title(String title)
        {
            renderer.setTitle(title);
            return this;
        }

        /**
         * Set to <CODE>true</CODE> if the output should include a banner.
         * @param banner <CODE>true</CODE> if the output should include a banner
         * @return This object
         */
        public Builder withBanner(boolean banner)
        {
            renderer.setBanner(banner);
            return this;
        }

        /**
         * Sets the options for the output.
         * @param options The output options
         * @return This object
         */
        public Builder options(DumperOptions options)
        {
            renderer.setOptions(options);
            return this;
        }

        /**
         * Returns the configured renderer instance
         * @return The renderer instance
         */
        public DashboardRenderer build()
        {
            return renderer;
        }
    }
}