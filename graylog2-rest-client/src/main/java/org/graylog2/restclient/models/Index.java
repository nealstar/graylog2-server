/**
 * The MIT License
 * Copyright (c) 2012 TORCH GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.graylog2.restclient.models;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.graylog2.restclient.lib.ApiClient;
import org.graylog2.restclient.models.api.responses.system.indices.IndexRangeSummary;
import org.graylog2.restclient.models.api.responses.system.indices.IndexShardsResponse;
import org.graylog2.restclient.models.api.responses.system.indices.IndexSummaryResponse;
import org.graylog2.restclient.models.api.responses.system.indices.ShardDocumentsResponse;
import org.graylog2.restclient.models.api.responses.system.indices.ShardMeterResponse;
import org.graylog2.restclient.models.api.responses.system.indices.ShardRoutingResponse;
import org.graylog2.restroutes.generated.routes;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Index {
    private static final Logger LOG = LoggerFactory.getLogger(Index.class);

    public interface Factory {
        Index fromRangeResponse(IndexRangeSummary ir);
    }

    private final ApiClient api;
    private final Range range;
    private final String name;

    private Info indexInfo;

    @AssistedInject
    public Index(ApiClient api, @Assisted IndexRangeSummary ir) {
        this.api = api;

        this.range = new Range(ir);
        this.name = ir.index;
    }

    public Range getRange() {
        return range;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        try {
            return Integer.parseInt(getName().substring(getName().lastIndexOf("_") + 1));
        } catch (Exception e) {
            LOG.error("Could not get number of index [" + getName() + "].", e);
            return -1;
        }
    }

    public Info getInfo() {
        if (indexInfo == null) {
            loadIndexInfo();
        }

        return indexInfo;
    }

    private void loadIndexInfo() {
        try {
            this.indexInfo = new Info(api.path(routes.IndicesResource().single(getName()), IndexSummaryResponse.class).execute());
        } catch (Exception e) {
            LOG.error("Could not get index information for index [" + getName() + "]", e);
        }
    }

    public class Info {

        private final int openSearchContexts;
        private final long storeSizeBytes;
        private final long segments;

        private final boolean isReopened;

        private final ShardDocumentsResponse documents;
        private final ShardMeter primaryShards;
        private final ShardMeter allShards;
        private final List<ShardRoutingResponse> shardRouting;

        public Info(IndexSummaryResponse i) {
            this.primaryShards = new ShardMeter(i.primaryShards);
            this.allShards = new ShardMeter(i.allShards);

            IndexShardsResponse primaries = i.primaryShards;
            this.openSearchContexts = primaries.openSearchContexts;
            this.storeSizeBytes = primaries.storeSizeBytes;
            this.segments = primaries.segments;
            this.documents = primaries.documents;

            this.shardRouting = i.routing;

            this.isReopened = i.isReopened;
        }

        public int getOpenSearchContexts() {
            return openSearchContexts;
        }

        public long getStoreSizeBytes() {
            return storeSizeBytes;
        }

        public long getSegments() {
            return segments;
        }

        public List<ShardRoutingResponse> getShardRouting() {
            return shardRouting;
        }

        public ShardDocumentsResponse getDocuments() {
            return documents;
        }

        public ShardMeter getPrimaryShards() {
            return primaryShards;
        }

        public ShardMeter getAllShards() {
            return allShards;
        }

        public boolean isReopened() {
            return isReopened;
        }

        public class ShardMeter {

            private final ShardMeterResponse indexMeter;
            private final ShardMeterResponse flushMeter;
            private final ShardMeterResponse getMeter;
            private final ShardMeterResponse mergeMeter;
            private final ShardMeterResponse searchFetchMeter;
            private final ShardMeterResponse searchQueryMeter;
            private final ShardMeterResponse refreshMeter;

            public ShardMeter(IndexShardsResponse shards) {
                this.indexMeter = shards.index;
                this.flushMeter = shards.flush;
                this.getMeter = shards.get;
                this.mergeMeter = shards.merge;
                this.searchFetchMeter = shards.searchFetch;
                this.searchQueryMeter = shards.searchQuery;
                this.refreshMeter = shards.refresh;
            }

            public ShardMeterResponse getIndexMeter() {
                return indexMeter;
            }

            public ShardMeterResponse getFlushMeter() {
                return flushMeter;
            }

            public ShardMeterResponse getGetMeter() {
                return getMeter;
            }

            public ShardMeterResponse getMergeMeter() {
                return mergeMeter;
            }

            public ShardMeterResponse getSearchFetchMeter() {
                return searchFetchMeter;
            }

            public ShardMeterResponse getSearchQueryMeter() {
                return searchQueryMeter;
            }

            public ShardMeterResponse getRefreshMeter() {
                return refreshMeter;
            }

        }

    }

    public class Range {

        private final DateTime starts;
        private final boolean providesCalculationInfo;

        private long calculationTookMs = 0;
        private DateTime calculatedAt = null;

        public Range(IndexRangeSummary ir) {
            this.starts = new DateTime(ir.starts);

            if (ir.calculatedAt != null && !ir.calculatedAt.isEmpty() && ir.calculationTookMs >= 0) {
                this.providesCalculationInfo = true;
                this.calculationTookMs = ir.calculationTookMs;
                this.calculatedAt = new DateTime(ir.calculatedAt);
            } else {
                this.providesCalculationInfo = false;
            }
        }

        public DateTime getStarts() {
            return starts;
        }

        public boolean isProvidesCalculationInfo() {
            return providesCalculationInfo;
        }

        public long getCalculationTookMs() {
            return calculationTookMs;
        }

        public DateTime getCalculatedAt() {
            return calculatedAt;
        }
    }

}
