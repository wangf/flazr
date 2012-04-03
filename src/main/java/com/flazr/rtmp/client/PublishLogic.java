/*
 * Flazr <http://flazr.com> Copyright (C) 2009  Peter Thomas.
 *
 * This file is part of Flazr.
 *
 * Flazr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flazr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flazr.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.flazr.rtmp.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flazr.rtmp.message.Command;
import com.flazr.rtmp.LoopedReader;
import com.flazr.rtmp.LimitedReader;
import com.flazr.rtmp.message.Control;
import com.flazr.rtmp.message.Metadata;
import com.flazr.rtmp.message.DataMessage;
import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.RtmpReader;
import com.flazr.rtmp.RtmpPublisher;

import org.jboss.netty.channel.MessageEvent;

public class PublishLogic implements ClientLogic {

    private static final Logger logger = LoggerFactory.getLogger(PublishLogic.class);

    private ClientOptions options;

    public PublishLogic(ClientOptions options) {
        this.options = options;
    }

    public void connected(final Connection conn) {
        conn.connectToScope(options.getAppName(), options.getTcUrl(), options.getParams(), options.getConnectArgs(),
            new ResultHandler() {
                public void handleResult(Object ignored) {
                    connectedToScope(conn);
                }
            });
    }

    public void closed(Connection conn) {
    }

    private void connectedToScope(final Connection conn) {
        conn.createStream(new ResultHandler() {
            public void handleResult(Object streamId) {
                int id = ((Double) streamId).intValue();
                readyToPublish(conn, id);
            }
        });
    }

    private void readyToPublish(final Connection conn, final int streamId) {
        RtmpReader reader = RtmpPublisher.getReader(options.getFileToPublish());
        if(options.getStart() > 0 || options.getLength() != -1) {
            reader = new LimitedReader(reader, options.getStart(), options.getLength());
        }
        if(options.getLoop() > 1) {
            reader = new LoopedReader(reader, options.getLoop());
        }
        conn.publish(streamId, options.getStreamName(), options.getPublishType(), options.getBuffer(), reader,
            new ResultHandler() {
                public void handleResult(Object ignored) {
                    logger.info("publish accepted successfully");
                }
            });
    }


    public Object command(Connection conn, Command command) {
        logger.warn("ignoring command from server: {}", command.getName());
        return null;
    }

    public void onMetaData(Connection conn, Metadata metadata) {
        logger.debug("ignoring metadata: {}", metadata);
    }


    public void onData(Connection conn, DataMessage message) {
        logger.debug("ignoring data: {}", message);
    }

}