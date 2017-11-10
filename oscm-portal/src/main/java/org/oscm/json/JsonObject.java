/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.json;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "id", "valueType" })
public class JsonObject {

    public JsonObject() {
    }

    @JsonInclude(Include.NON_EMPTY)
    private MessageType messageType;

    @JsonInclude(Include.NON_EMPTY)
    private ResponseCode responseCode;

    @JsonInclude(Include.NON_EMPTY)
    private String locale;

    @JsonInclude(Include.NON_EMPTY)
    private List<JsonParameter> parameters = new ArrayList<JsonParameter>();

    @JsonInclude(Include.NON_EMPTY)
    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public List<JsonParameter> getParameters() {
        return parameters;
    }

    public JsonParameter getParameter(String parameterId) {
        JsonParameter parameter = null;
        for (JsonParameter par : parameters) {
            if (par.getId().equals(parameterId)) {
                parameter = par;
                break;
            }
        }
        return parameter;
    }

    public void setParameters(List<JsonParameter> parameters) {
        this.parameters = parameters;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public ResponseCode getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(ResponseCode responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        JsonObject thisObj = (JsonObject) obj;
        return new EqualsBuilder().append(locale, thisObj.locale)
                .append(parameters, thisObj.parameters)
                .append(messageType, thisObj.messageType)
                .append(responseCode, thisObj.responseCode).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                . // two randomly chosen prime numbers
                append(locale).append(parameters).append(messageType)
                .append(responseCode).toHashCode();
    }

}
