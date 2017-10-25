/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.business.model.externalservices;

import java.util.ArrayList;
import java.util.List;

import org.oscm.converter.CharConverter;
import org.oscm.reportingservice.business.model.RDO;

/**
 * RDO for the technical service of type external. Holds no billing information
 * since the external services have neither subscriptions nor price models.
 * Everything is handled outside of BSS. Information here are address of
 * supplier and a list of the marketable services and their activation date.
 * 
 * @author afschar
 * 
 **/
public class RDOExternalSupplier extends RDO {

    private static final long serialVersionUID = 1269462373532220643L;

    // organization information
    private String name;
    private String address;
    private String country;
    private String phone;
    private String email;

    private List<RDOExternalService> services = new ArrayList<RDOExternalService>();

    public List<RDOExternalService> getExternalServices() {
        return services;
    }

    public void setExternalServices(List<RDOExternalService> value) {
        services = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = CharConverter.convertToSBC(name);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = CharConverter.convertToSBC(address);
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
