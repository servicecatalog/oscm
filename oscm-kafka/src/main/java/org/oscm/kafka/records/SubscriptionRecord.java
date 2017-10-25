/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                 
 *  Creation Date: 03.07.2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.kafka.records;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.Subscription;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;

import com.google.gson.annotations.SerializedName;

/**
 * @author stavreva
 *
 */
public class SubscriptionRecord {

    private static final String TEMPLATE_PREFIX = "template.";
    private static final String TARGET = "target";
    private static final String NAMESPACE = "namespace";
    private static final String LABELS_PREFIX = "labels.";
    private static final String PARAMETERS_PREFIX = "parameters.";

    @SerializedName("version")
    int version;
    @SerializedName("id")
    UUID id;
    @SerializedName("timestamp")
    Date timestamp;
    @SerializedName("operation")
    Operation operation;
    @SerializedName("namespace")
    String namespace;
    @SerializedName("template")
    Map<String, String> template = new HashMap<>();
    @SerializedName("target")
    String target;
    @SerializedName("labels")
    Map<String, String> labels = new HashMap<>();
    @SerializedName("parameters")
    Map<String, Object> parameters = new HashMap<>();

    public SubscriptionRecord(Subscription subscription, Operation operation) {
        this.version = 0;
        this.id = subscription.getUuid();
        this.timestamp = new Date();
        this.operation = operation;
        ParameterSet paramSet = subscription.getParameterSet();
        List<Parameter> params = paramSet.getParameters();

        // this.operation = getOperation(subscription);
        //
        if (Operation.DELETE.equals(this.operation)) {
            return;
        }

        for (Parameter param : params) {
            ParameterDefinition paramDef = param.getParameterDefinition();
            String paramId = paramDef.getParameterId();
            if (paramId != null) {
                if (TARGET.equals(paramId)) {
                    this.target = param.getValue();
                } else if (NAMESPACE.equals(paramId)) {
                    this.namespace = param.getValue();
                } else if (paramId.contains(TEMPLATE_PREFIX)) {
                    paramId = paramId.substring(TEMPLATE_PREFIX.length());
                    template.put(paramId, param.getValue());
                } else if (paramId.contains(LABELS_PREFIX)) {
                    paramId = paramId.substring(LABELS_PREFIX.length());
                    labels.put(paramId, param.getValue());
                } else if (paramId.contains(PARAMETERS_PREFIX)) {
                    paramId = paramId.substring(PARAMETERS_PREFIX.length());
                    addParameter(paramId, param.getValue());
                }
            }
        }
    }

    Operation getOperation(Subscription subscription) {

        SubscriptionStatus status = subscription.getStatus();
        Operation operation = null;
        switch (status) {
        case DEACTIVATED:
        case EXPIRED:
        case SUSPENDED:
        case SUSPENDED_UPD:
            operation = Operation.DELETE;
            break;
        case PENDING:
        case PENDING_UPD:
            operation = Operation.UPDATE;
            break;
        // TODO
        case ACTIVE:
        case INVALID:
        default:
            break;
        }
        return operation;
    }

    @SuppressWarnings("unchecked")
    private void addParameter(String key, String value) {

        List<String> hierarchy = Arrays.asList(key.split("\\."));

        Map<String, Object> map = parameters;

        Iterator<String> it = hierarchy.iterator();

        while (it.hasNext()) {
            String level = it.next();

            if (it.hasNext()) {
                if (map.get(level) == null
                        || !(map.get(level) instanceof Map)) {
                    map.put(level, new HashMap<String, Object>());
                }

                map = (Map<String, Object>) map.get(level);
            } else {
                map.put(level, value);
            }
        }
    }
}
