/**
 * This file is part of Graylog2.
 *
 * Graylog2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog2.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog2.dashboards.widgets;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;
import org.graylog2.indexer.IndexHelper;
import org.graylog2.indexer.results.TermsResult;
import org.graylog2.indexer.searches.Searches;
import org.graylog2.indexer.searches.timeranges.TimeRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class QuickvaluesWidget extends DashboardWidget {

    private static final Logger LOG = LoggerFactory.getLogger(QuickvaluesWidget.class);

    private final String query;
    private final TimeRange timeRange;
    private final String streamId;

    private final String field;
    private final Searches searches;

    public QuickvaluesWidget(MetricRegistry metricRegistry, Searches searches, String id, String description, int cacheTime, Map<String, Object> config, String query, TimeRange timeRange, String creatorUserId) throws InvalidWidgetConfigurationException {
        super(metricRegistry, Type.QUICKVALUES, id, description, cacheTime, config, creatorUserId);
        this.searches = searches;

        if (!checkConfig(config)) {
            throw new InvalidWidgetConfigurationException("Missing or invalid widget configuration. Provided config was: " + config.toString());
        }

        this.query = query;
        this.timeRange = timeRange;

        this.field = (String) config.get("field");

        if (config.containsKey("stream_id")) {
            this.streamId = (String) config.get("stream_id");
        } else {
            this.streamId = null;
        }
    }

    public String getQuery() {
        return query;
    }

    public TimeRange getTimeRange() {
        return timeRange;
    }

    @Override
    public Map<String, Object> getPersistedConfig() {
        return new HashMap<String, Object>() {{
            put("query", query);
            put("timerange", timeRange.getPersistedConfig());
            put("stream_id", streamId);

            put("field", field);
        }};
    }

    @Override
    protected ComputationResult compute() {
        String filter = null;
        if (streamId != null && !streamId.isEmpty()) {
            filter = "streams:" + streamId;
        }

        try {
            TermsResult terms = searches.terms(field, 50, query, filter, timeRange);

            Map<String, Object> result = Maps.newHashMap();
            result.put("terms", terms.getTerms());
            result.put("total", terms.getTotal());
            result.put("other", terms.getOther());
            result.put("missing", terms.getMissing());

            return new ComputationResult(result, terms.took().millis());
        } catch (IndexHelper.InvalidRangeFormatException e) {
            String msg = "Could not calculate [" + this.getClass().getCanonicalName() + "] widget <" + getId() + ">. Invalid time range.";
            LOG.error(msg, e);
            throw new RuntimeException(msg);
        }
    }

    private boolean checkConfig(Map<String, Object> config) {
        return config.containsKey("field");
    }

}
