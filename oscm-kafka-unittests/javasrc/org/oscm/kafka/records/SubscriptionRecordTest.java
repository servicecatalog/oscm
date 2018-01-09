/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 10.07.2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.kafka.records;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.kafka.serializer.DataSerializer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * @author stavreva
 *
 */
public class SubscriptionRecordTest {

    private final UUID ID = UUID.randomUUID();
    private final String TEMPLATE = "template";
    private final String LABELS = "labels";
    private final String PARAMETERS = "parameters";
    private final String PARAM_REPO_ID = "repository";
    private final String PARAM_REPO_VAL = "stable";
    private final String PARAM_CHART_ID = "name";
    private final String PARAM_CHART_VAL = "wordpress";
    private final String PARAM_VERSION_ID = "version";
    private final String PARAM_VERSION_VAL = "latest";
    private final String PARAM_RELEASE_ID = "release";
    private final String PARAM_RELEASE_VAL = "wordpress-release";
    private final String PARAM_NAMESPACE_ID = "namespace";
    private final String PARAM_NAMESPACE_VAL = "default";
    private final String PARAM_TARGET_ID = "target";
    private final String PARAM_TARGET_VAL = "http://target.com/rudder";

    private final String PARAM_VALUE_CPU_ID = "resources.requests.cpu";
    private final String PARAM_VALUE_CPU_VAL = "500m";
    private final String PARAM_VALUE_MEM_ID = "resources.requests.memory";
    private final String PARAM_VALUE_MEM_VAL = "500Mi";

    @Test
    public void toJson() {
        // given
        Subscription sub = getSubscriptionMessage();

        // when
        SubscriptionRecord subscription = new SubscriptionRecord(sub,
                Operation.UPDATE);
        DataSerializer serde = new DataSerializer(SubscriptionRecord.class);
        String json = new String(serde.serialize("", subscription),
                StandardCharsets.UTF_8);
        System.out.println(json);

        // then
        Gson gson = new Gson();
        JsonObject jsonObj = gson.fromJson(json, JsonObject.class);
        assertEquals(ID.toString(), jsonObj.get("id").getAsString());
        assertNotNull(jsonObj.get("timestamp").getAsString());
        assertEquals(PARAM_TARGET_VAL,
                jsonObj.get(PARAM_TARGET_ID).getAsString());
        assertEquals(PARAM_NAMESPACE_VAL,
                jsonObj.get(PARAM_NAMESPACE_ID).getAsString());
        assertEquals(PARAM_REPO_VAL, jsonObj.get(TEMPLATE).getAsJsonObject()
                .get(PARAM_REPO_ID).getAsString());
        assertEquals(PARAM_CHART_VAL, jsonObj.get(TEMPLATE).getAsJsonObject()
                .get(PARAM_CHART_ID).getAsString());
        assertEquals(PARAM_VERSION_VAL, jsonObj.get(TEMPLATE).getAsJsonObject()
                .get(PARAM_VERSION_ID).getAsString());
        assertEquals(PARAM_RELEASE_VAL, jsonObj.get(LABELS).getAsJsonObject()
                .get(PARAM_RELEASE_ID).getAsString());
        assertEquals(PARAM_VALUE_CPU_VAL, jsonObj.get(PARAMETERS)
                .getAsJsonObject().get(PARAM_VALUE_CPU_ID).getAsString());
        assertEquals(PARAM_VALUE_MEM_VAL, jsonObj.get(PARAMETERS)
                .getAsJsonObject().get(PARAM_VALUE_MEM_ID).getAsString());
    }

    private Subscription getSubscriptionMessage() {
        Subscription sub = new Subscription();
        sub.setStatus(SubscriptionStatus.PENDING);
        sub.setUuid(ID);

        List<Parameter> parameters = new ArrayList<>();
        ParameterDefinition paramDef = new ParameterDefinition();
        paramDef.setParameterId(TEMPLATE + "." + PARAM_REPO_ID);
        Parameter param = new Parameter();
        param.setParameterDefinition(paramDef);
        param.setValue(PARAM_REPO_VAL);
        parameters.add(param);

        paramDef = new ParameterDefinition();
        paramDef.setParameterId(TEMPLATE + "." + PARAM_CHART_ID);
        param = new Parameter();
        param.setParameterDefinition(paramDef);
        param.setValue(PARAM_CHART_VAL);
        parameters.add(param);

        paramDef = new ParameterDefinition();
        paramDef.setParameterId(TEMPLATE + "." + PARAM_VERSION_ID);
        param = new Parameter();
        param.setParameterDefinition(paramDef);
        param.setValue(PARAM_VERSION_VAL);
        parameters.add(param);

        paramDef = new ParameterDefinition();
        paramDef.setParameterId(LABELS + "." + PARAM_RELEASE_ID);
        param = new Parameter();
        param.setParameterDefinition(paramDef);
        param.setValue(PARAM_RELEASE_VAL);
        parameters.add(param);

        paramDef = new ParameterDefinition();
        paramDef.setParameterId(PARAM_NAMESPACE_ID);
        param = new Parameter();
        param.setParameterDefinition(paramDef);
        param.setValue(PARAM_NAMESPACE_VAL);
        parameters.add(param);

        paramDef = new ParameterDefinition();
        paramDef.setParameterId(PARAM_TARGET_ID);
        param = new Parameter();
        param.setParameterDefinition(paramDef);
        param.setValue(PARAM_TARGET_VAL);
        parameters.add(param);

        paramDef = new ParameterDefinition();
        paramDef.setParameterId(PARAMETERS + "." + PARAM_VALUE_CPU_ID);
        param = new Parameter();
        param.setParameterDefinition(paramDef);
        param.setValue(PARAM_VALUE_CPU_VAL);
        parameters.add(param);

        paramDef = new ParameterDefinition();
        paramDef.setParameterId(PARAMETERS + "." + PARAM_VALUE_MEM_ID);
        param = new Parameter();
        param.setParameterDefinition(paramDef);
        param.setValue(PARAM_VALUE_MEM_VAL);
        parameters.add(param);

        ParameterSet paramSet = new ParameterSet();
        paramSet.setParameters(parameters);

        Product prod = new Product();
        prod.setParameterSet(paramSet);

        sub.setProduct(prod);
        return sub;
    }
}
