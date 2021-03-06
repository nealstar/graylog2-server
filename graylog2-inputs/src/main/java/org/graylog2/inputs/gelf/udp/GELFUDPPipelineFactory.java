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
package org.graylog2.inputs.gelf.udp;


import com.codahale.metrics.MetricRegistry;
import org.graylog2.inputs.gelf.GELFDispatcher;
import org.graylog2.inputs.gelf.gelf.GELFChunkManager;
import org.graylog2.inputs.network.PacketInformationDumper;
import org.graylog2.plugin.buffers.Buffer;
import org.graylog2.plugin.inputs.MessageInput;
import org.graylog2.plugin.inputs.util.ThroughputCounter;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

/**
 * @author Lennart Koopmann <lennart@socketfeed.com>
 */
public class GELFUDPPipelineFactory implements ChannelPipelineFactory {

    private final MetricRegistry metricRegistry;
    private final GELFChunkManager gelfChunkManager;
    private final Buffer processBuffer;
    private final MessageInput sourceInput;
    private final ThroughputCounter throughputCounter;

    public GELFUDPPipelineFactory(MetricRegistry metricRegistry,
                                  GELFChunkManager gelfChunkManager,
                                  Buffer processBuffer,
                                  MessageInput sourceInput,
                                  ThroughputCounter throughputCounter) {
        this.metricRegistry = metricRegistry;
        this.gelfChunkManager = gelfChunkManager;
        this.processBuffer = processBuffer;
        this.sourceInput = sourceInput;
        this.throughputCounter = throughputCounter;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline p = Channels.pipeline();
        p.addLast("packet-meta-dumper", new PacketInformationDumper(sourceInput));
        p.addLast("traffic-counter", throughputCounter);
        p.addLast("handler", new GELFDispatcher(metricRegistry, gelfChunkManager, processBuffer, sourceInput));

        return p;
    }
}
