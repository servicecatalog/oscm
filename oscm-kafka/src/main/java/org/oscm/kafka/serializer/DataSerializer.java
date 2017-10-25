/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                 
 *  Creation Date: Jul 27, 2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.kafka.serializer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * De-/Serializer class with gson for kafka.
 * 
 * @author miethaner
 */
public class DataSerializer
        implements Serializer<Object>, Deserializer<Object>, Serde<Object> {

    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final String FORMAT_DATE = "yyyy-MM-dd'T'HH:mm:ssXXX";

    private Class<?> clazz;
    private Gson gson;

    public DataSerializer(Class<?> clazz) {
        this.clazz = clazz;

        GsonBuilder builder = new GsonBuilder();
        builder.setDateFormat(FORMAT_DATE);
        this.gson = builder.create();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // ignore
    }

    @Override
    public byte[] serialize(String topic, Object data) {

        String json = gson.toJson(data);

        return json.getBytes(CHARSET);
    }

    @Override
    public Object deserialize(String topic, byte[] raw) {

        if (raw == null) {
            return null;
        }

        String json = new String(raw, CHARSET);

        return gson.fromJson(json, clazz);
    }

    @Override
    public void close() {
        // ignore
    }

    @Override
    public Deserializer<Object> deserializer() {
        return this;
    }

    @Override
    public Serializer<Object> serializer() {
        return this;
    }
}
