/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.json;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class JsonParameterOption {
    public JsonParameterOption() {
    }

    private String id;
    private String description;
    private double pricePerUser;
    private double pricePerSubscription;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        JsonParameterOption thisObj = (JsonParameterOption) obj;
        return new EqualsBuilder().append(id, thisObj.id)
                .append(description, thisObj.description).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                append(id).append(description).toHashCode();
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
