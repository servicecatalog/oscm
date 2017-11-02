/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.classic.customizelandingpage;

import java.io.Serializable;

public class ServiceModel implements Serializable {

    private static final long serialVersionUID = -7650997709704934698L;

    String serviceKey;
    String pictureURI;

    String name;

    String description;

    String status;

    public ServiceModel(String serviceKey, String name) {
        super();
        this.serviceKey = serviceKey;
        this.name = name;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public String getPictureURI() {
        return pictureURI;
    }

    public void setPictureURI(String pictureURI) {
        this.pictureURI = pictureURI;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((pictureURI == null) ? 0 : pictureURI.hashCode());
        result = prime * result
                + ((serviceKey == null) ? 0 : serviceKey.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ServiceModel other = (ServiceModel) obj;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (pictureURI == null) {
            if (other.pictureURI != null)
                return false;
        } else if (!pictureURI.equals(other.pictureURI))
            return false;
        if (serviceKey == null) {
            if (other.serviceKey != null)
                return false;
        } else if (!serviceKey.equals(other.serviceKey))
            return false;
        if (status == null) {
            if (other.status != null)
                return false;
        } else if (!status.equals(other.status))
            return false;
        return true;
    }

}
