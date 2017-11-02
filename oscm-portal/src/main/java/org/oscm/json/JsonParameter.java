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

public class JsonParameter {
    public JsonParameter() {
    }

    private String id;
    private String valueType;
    @JsonInclude(Include.NON_EMPTY)
    private String minValue;
    @JsonInclude(Include.NON_EMPTY)
    private String maxValue;
    private boolean mandatory;
    private String description;
    private String value;
    private boolean readonly;
    private String modificationType;
    private boolean valueError;
    private double pricePerUser;
    private double pricePerSubscription;


    public boolean isValueError() {
        return valueError;
    }

    public void setValueError(boolean valueError) {
        this.valueError = valueError;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    @JsonInclude(Include.NON_EMPTY)
    private List<JsonParameterOption> options = new ArrayList<JsonParameterOption>();

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMinValue() {
        return minValue;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<JsonParameterOption> getOptions() {
        return options;
    }

    public void setOptions(List<JsonParameterOption> options) {
        this.options = options;
    }

    public String getModificationType() {
        return modificationType;
    }

    public void setModificationType(String modificationType) {
        this.modificationType = modificationType;
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
        JsonParameter thisObj = (JsonParameter) obj;
        return new EqualsBuilder().append(id, thisObj.id)
                .append(description, thisObj.description)
                .append(value, thisObj.value)
                .append(valueType, thisObj.valueType)
                .append(valueError, thisObj.valueError)
                .append(modificationType, thisObj.modificationType)
                .append(mandatory, thisObj.mandatory)
                .append(minValue, thisObj.minValue)
                .append(maxValue, thisObj.maxValue)
                .append(readonly, thisObj.readonly)
                .append(options, thisObj.options).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                . // two randomly chosen prime numbers
                append(id).append(description).append(value).append(valueType)
                .append(valueError).append(modificationType).append(mandatory)
                .append(minValue).append(maxValue).append(readonly)
                .append(options).toHashCode();
    }

	public double getPricePerUser() {
		return pricePerUser;
	}

	public void setPricePerUser(double pricePerUser) {
		this.pricePerUser = pricePerUser;
	}

	public double getPricePerSubscription() {
		return pricePerSubscription;
	}

	public void setPricePerSubscription(double pricePerSubscription) {
		this.pricePerSubscription = pricePerSubscription;
	}
}
