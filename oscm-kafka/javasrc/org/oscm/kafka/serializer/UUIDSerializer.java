/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jun 1, 2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.kafka.serializer;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

/**
 * De-/Serializer class with UUIDs for kafka.
 * 
 * @author miethaner
 */
public class UUIDSerializer
        implements Serializer<UUID>, Deserializer<UUID>, Serde<UUID> {

    private static final int UUID_BYTES = 16;

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // ignore
    }

    @Override
    public UUID deserialize(String topic, byte[] data) {

        if (data == null) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        long high = buffer.getLong();
        long low = buffer.getLong();

        return new UUID(high, low);
    }

    @Override
    public byte[] serialize(String topic, UUID data) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[UUID_BYTES]);
        buffer.putLong(data.getMostSignificantBits());
        buffer.putLong(data.getLeastSignificantBits());

        return buffer.array();
    }

    @Override
    public void close() {
        // ignore
    }

    @Override
    public Deserializer<UUID> deserializer() {
        return this;
    }

    @Override
    public Serializer<UUID> serializer() {
        return this;
    }
}
