/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.dao;

import java.math.BigDecimal;

/**
 * Internal class to represent customer event data. This class is used to create
 * the customer event report.
 * 
 * @author kulle
 */
public class CustomerEventData {

    private String actor;
    private String type;
    private String eventidentifier;
    private BigDecimal multiplier;
    private String productid;
    private long occurrencetime;
    private String firstname;
    private String lastname;
    private String subscriptionid;
    private BigDecimal subscriptiontkey; 
    private String eventdescription;
    private String locale;

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEventidentifier() {
        return eventidentifier;
    }

    public void setEventidentifier(String eventidentifier) {
        this.eventidentifier = eventidentifier;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

    public String getProductid() {
        return productid;
    }

    public void setProductid(String productid) {
        this.productid = productid;
    }

    public long getOccurrencetime() {
        return occurrencetime;
    }

    public void setOccurrencetime(long occurrencetime) {
        this.occurrencetime = occurrencetime;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getSubscriptionid() {
        return subscriptionid;
    }

    public void setSubscriptionid(String subscriptionid) {
        this.subscriptionid = subscriptionid;
    }

    public BigDecimal getSubscriptiontkey() {
		return subscriptiontkey;
	}

	public void setSubscriptiontkey(BigDecimal subscriptiontkey) {
		this.subscriptiontkey = subscriptiontkey;
	}

	public String getEventdescription() {
        return eventdescription;
    }

    public void setEventdescription(String eventdescription) {
        this.eventdescription = eventdescription;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * <code>locale</code>, <code>eventdescription</code> and
     * <code>multiplier</code> fields are ignored when computing the equals
     * state!
     */
    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true;

        if (that == null || !(that instanceof CustomerEventData)) {
            return false;
        }

        CustomerEventData compareTo = (CustomerEventData) that;
        if (!equalsString(getActor(), compareTo.getActor())) {
            return false;
        }

        if (!equalsString(getType(), compareTo.getType())) {
            return false;
        }

        if (!equalsString(getEventidentifier(), compareTo.getEventidentifier())) {
            return false;
        }

        if (!equalsString(getProductid(), compareTo.getProductid())) {
            return false;
        }

        if (!equalsLong(getOccurrencetime(), compareTo.getOccurrencetime())) {
            return false;
        }

        if (!equalsString(getFirstname(), compareTo.getFirstname())) {
            return false;
        }

        if (!equalsString(getLastname(), compareTo.getLastname())) {
            return false;
        }

        if (!equalsString(getSubscriptionid(), compareTo.getSubscriptionid())) {
            return false;
        }
        
        if (!equalsString(null == getSubscriptiontkey() ? null : getSubscriptiontkey().toString(), 
        		null == compareTo.getSubscriptiontkey() ? null : compareTo.getSubscriptiontkey().toString())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int multiplier = 25;

        int hash = 59;
        hash = multiplier * hash
                + (getActor() == null ? 0 : getActor().hashCode());
        hash += multiplier * hash
                + (getType() == null ? 0 : getType().hashCode());
        hash += multiplier
                * hash
                + (getEventidentifier() == null ? 0 : getEventidentifier()
                        .hashCode());
        hash += multiplier * hash
                + (getMultiplier() == null ? 0 : getMultiplier().hashCode());
        hash += multiplier * hash
                + (getProductid() == null ? 0 : getProductid().hashCode());
        hash += multiplier * hash
                + (Long.valueOf(getOccurrencetime())).hashCode();
        hash += multiplier * hash
                + (getFirstname() == null ? 0 : getFirstname().hashCode());
        hash += multiplier * hash
                + (getLastname() == null ? 0 : getLastname().hashCode());
        hash += multiplier
                * hash
                + (getSubscriptionid() == null ? 0 : getSubscriptionid()
                        .hashCode());
        hash += multiplier
                * hash
                + (getSubscriptiontkey() == null ? 0 : getSubscriptiontkey()
                        .hashCode());
        hash += multiplier
                * hash
                + (getEventdescription() == null ? 0 : getEventdescription()
                        .hashCode());
        hash += multiplier * hash
                + (getLocale() == null ? 0 : getLocale().hashCode());

        return hash;
    }

    private boolean equalsString(String a, String b) {
        if (a != null) {
            return a.equals(b);
        } else if (b != null) {
            return false;
        }
        return true;
    }

    private boolean equalsLong(long a, long b) {
        return a == b;
    }

}
