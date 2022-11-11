/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.proxy.remoting.common;

import java.nio.ByteBuffer;
import java.util.List;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.message.MessageDecoder;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.logging.InternalLogger;
import org.apache.rocketmq.logging.InternalLoggerFactory;

public class RemotingConverter {
    private static final InternalLogger log = InternalLoggerFactory.getLogger(LoggerName.PROXY_LOGGER_NAME);

    protected static final Object INSTANCE_CREATE_LOCK = new Object();
    protected static volatile RemotingConverter instance;

    public static RemotingConverter getInstance() {
        if (instance == null) {
            synchronized (INSTANCE_CREATE_LOCK) {
                if (instance == null) {
                    instance = new RemotingConverter();
                }
            }
        }
        return instance;
    }

    public byte[] convertMsgToBytes(List<MessageExt> msgList) {
        // set response body
        byte[][] msgBufferList = new byte[msgList.size()][];
        int bodyTotalSize = 0;
        for (int i = 0; i < msgList.size(); i++) {
            try {
                msgBufferList[i] = convertMsgToBytes(msgList.get(i));
                bodyTotalSize += msgBufferList[i].length;
            } catch (Exception e) {
                log.error("messageToByteBuffer UnsupportedEncodingException", e);
            }
        }

        ByteBuffer body = ByteBuffer.allocate(bodyTotalSize);
        for (byte[] bb : msgBufferList) {
            body.put(bb);
        }

        return body.array();
    }

    public byte[] convertMsgToBytes(final MessageExt msg) throws Exception {
        // change to 0 for recalculate storeSize
        msg.setStoreSize(0);
        if (msg.getTopic().length() > Byte.MAX_VALUE) {
            log.warn("Topic length is too long, topic: {}", msg.getTopic());
        }
        return MessageDecoder.encode(msg, false);
    }
}