/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                 
 *  Creation Date: 10.07.2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.kafka.records;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.oscm.kafka.serializer.DataSerializer;

/**
 * @author stavreva
 *
 */
public class ReleaseRecordTest {

    // TODO define constants for keys in ReleaseRecord and use it here
    private final String ID = "8fd1c107-4f72-4196-96e2-b6c4373b8108";
    private final String TIMESTAMP = "2017-06-26T09:11:39+02:00";
    private final String OP = Operation.SerializedValues.OPTION_UPDATE;
    private final String STATUS = ReleaseRecord.Status.SerializedValues.OPTION_PENDING;
    private final String INSTANCE = "8fd1c107-4f72-4196-96e2-b6c4373b8108";
    private final String TARGET = "http://tarteg.com:8080";
    private final String NAMESPACE = "default";
    private final String ENDPOINT_KEY = "endpoint";
    private final String ENDPOINT_VALUE = "127.0.0.1:5000";
    private final String PARAM_KEY = "param1";
    private final String PARAM_VALUE = "xxxx";
    private final String FAILURE_KEY = "failure1";
    private final String FAILURE_VALUE = "yyyy";

    @Test
    public void fromJson() {

        // given
        String json = "{\"version\": 100000, \"id\": \"" + ID
                + "\", \"timestamp\": \"" + TIMESTAMP + "\", \"operation\": \""
                + OP + "\", \"status\": \"" + STATUS + "\", \"instance\": \""
                + INSTANCE + "\", \"target\": \"" + TARGET
                + "\", \"namespace\": \"" + NAMESPACE + "\", \"services\": {\""
                + ENDPOINT_KEY + "\": \"" + ENDPOINT_VALUE
                + "\"},  \"parameters\": {\"" + PARAM_KEY + "\": \""
                + PARAM_VALUE + "\"}, \"failure\": {\"" + FAILURE_KEY + "\": \""
                + FAILURE_VALUE + "\"} }";

        System.out.println(json);

        DataSerializer serde = new DataSerializer(ReleaseRecord.class);
        // when
        ReleaseRecord release = (ReleaseRecord) serde.deserialize("",
                json.getBytes(StandardCharsets.UTF_8));

        // then
        assertEquals(ID, release.getId().toString());
        // assertEquals(TIMESTAMP,
        // release.getTimestamp().compareTo(anotherDate));
        assertEquals(Operation.UPDATE, release.getOperation());
        assertEquals(ReleaseRecord.Status.PENDING, release.getStatus());
        assertEquals(INSTANCE, release.getInstance().toString());
        assertEquals(TARGET, release.getTarget().toString());
        assertEquals(NAMESPACE, release.getNamespace().toString());
        assertEquals(ENDPOINT_VALUE, release.getServices().get(ENDPOINT_KEY));
        assertEquals(PARAM_VALUE, release.getParameters().get(PARAM_KEY));
        assertEquals(FAILURE_VALUE, release.getFailure().get(FAILURE_KEY));
    }
}
