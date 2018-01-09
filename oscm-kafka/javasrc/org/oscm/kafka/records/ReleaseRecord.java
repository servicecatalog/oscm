/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 06.07.2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.kafka.records;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.google.gson.annotations.SerializedName;

/**
 * @author stavreva
 *
 */
public class ReleaseRecord {

    @SerializedName("version")
    private int version;
    @SerializedName("id")
    private UUID id;
    @SerializedName("timestamp")
    private Date timestamp;
    @SerializedName("status")
    private Status status;
    @SerializedName("operation")
    private Operation operation;
    @SerializedName("instance")
    private String instance;
    @SerializedName("namespace")
    private String namespace;
    @SerializedName("target")
    private String target;
    @SerializedName("parameters")
    private Map<String, Object> parameters;
    @SerializedName("services")
    private Map<String, String> services;
    @SerializedName("failure")
    private Map<String, String> failure;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Map<String, String> getServices() {
        return services;
    }

    public void setServices(Map<String, String> services) {
        this.services = services;
    }

    public void putService(String key, String value) {
        this.services.put(key, value);
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public void putParameter(String key, String value) {
        this.parameters.put(key, value);
    }

    public Map<String, String> getFailure() {
        return failure;
    }

    public void setFailure(Map<String, String> failure) {
        this.failure = failure;
    }

    public void putFailure(String key, String value) {
        this.failure.put(key, value);
    }

    public enum Status {
        @SerializedName(SerializedValues.OPTION_CREATNG)
        CREATING, //

        @SerializedName(SerializedValues.OPTION_UPDATING)
        UPDATING, //

        @SerializedName(SerializedValues.OPTION_DELETING)
        DELETING, //

        @SerializedName(SerializedValues.OPTION_PENDING)
        PENDING, //

        @SerializedName(SerializedValues.OPTION_DEPLOYED)
        DEPLOYED, //

        @SerializedName(SerializedValues.OPTION_DELETED)
        DELETED, //

        @SerializedName(SerializedValues.OPTION_FAILED)
        FAILED; //

        public static class SerializedValues {
            public static final String OPTION_CREATNG = "creating";
            public static final String OPTION_UPDATING = "updating";
            public static final String OPTION_DELETING = "deleting";
            public static final String OPTION_PENDING = "pending";
            public static final String OPTION_DEPLOYED = "deployed";
            public static final String OPTION_DELETED = "deleted";
            public static final String OPTION_FAILED = "failed";
        }
    }

}
