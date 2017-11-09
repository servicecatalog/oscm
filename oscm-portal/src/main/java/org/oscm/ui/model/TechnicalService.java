/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.vo.VOEventDefinition;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.internal.vo.VOTechnicalServiceOperation;

public class TechnicalService {

    private VOTechnicalService vo;

    private boolean selected = false;

    public TechnicalService(VOTechnicalService vo) {
        this.vo = vo;
    }

    public void setVo(VOTechnicalService vo) {
        this.vo = vo;
    }

    public VOTechnicalService getVo() {
        return vo;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean existAccessType() {
        return getAccessType() != null;
    }

    public boolean isAccessTypeDirect() {
        return getAccessType() == ServiceAccessType.DIRECT;
    }

    public boolean isAccessTypeLogin() {
        return getAccessType() == ServiceAccessType.LOGIN;
    }

    public boolean isAccessTypeSaml() {
        return getAccessType() == ServiceAccessType.USER;
    }

    public boolean isAccessTypeExternal() {
        return getAccessType() == ServiceAccessType.EXTERNAL;
    }

    /*
     * Delegate Methods
     */

    public String getTechnicalServiceId() {
        return vo.getTechnicalServiceId();
    }
    
    public String getBillingIdentifier() {
        return vo.getBillingIdentifier();
    }
    
    public String getTechnicalServiceBuildId() {
        return vo.getTechnicalServiceBuildId();
    }

    public String getTechnicalServiceDescription() {
        return vo.getTechnicalServiceDescription();
    }

    public String getAccessInfo() {
        return vo.getAccessInfo();
    }

    public ServiceAccessType getAccessType() {
        return vo.getAccessType();
    }

    public String getBaseUrl() {
        return vo.getBaseUrl();
    }

    public String getLoginPath() {
        return vo.getLoginPath();
    }

    public String getProvisioningUrl() {
        return vo.getProvisioningUrl();
    }

    public String getProvisioningVersion() {
        return vo.getProvisioningVersion();
    }
    
    public void setBillingIdentifier(String billingIdentifier) {
        vo.setBillingIdentifier(billingIdentifier);
    }
    
    public void setAccessInfo(String accessInfo) {
        vo.setAccessInfo(accessInfo);
    }

    public void setAccessType(ServiceAccessType accessType) {
        vo.setAccessType(accessType);
    }

    public void setBaseUrl(String baseUrl) {
        vo.setBaseUrl(baseUrl);
    }

    public void setLoginPath(String loginPath) {
        vo.setLoginPath(loginPath);
    }

    public void setProvisioningUrl(String provisioningUrl) {
        vo.setProvisioningUrl(provisioningUrl);
    }

    public void setProvisioningVersion(String provisioningVersion) {
        vo.setProvisioningVersion(provisioningVersion);
    }

    public void setTechnicalServiceBuildId(String technicalServiceBuildId) {
        vo.setTechnicalServiceBuildId(technicalServiceBuildId);
    }

    public void setTechnicalServiceDescription(
            String technicalServiceDescription) {
        if (technicalServiceDescription == null) {
            vo.setTechnicalServiceDescription("");
        } else {
            vo.setTechnicalServiceDescription(technicalServiceDescription);
        }
    }

    public void setTechnicalServiceId(String name) {
        vo.setTechnicalServiceId(name);
    }

    public List<VOEventDefinition> getEventDefinitions() {
        return vo.getEventDefinitions();
    }

    public long getKey() {
        return vo.getKey();
    }

    public String getLicense() {
        return vo.getLicense();
    }

    public List<VOParameterDefinition> getParameterDefinitions() {
        return vo.getParameterDefinitions();
    }

    public List<VORoleDefinition> getRoleDefinitions() {
        return vo.getRoleDefinitions();
    }

    public int getVersion() {
        return vo.getVersion();
    }

    public List<VOTechnicalServiceOperation> getTechnicalServiceOperations() {
        return vo.getTechnicalServiceOperations();
    }

    public void setTechnicalServiceOperations(
            List<VOTechnicalServiceOperation> technicalServiceOperations) {
        vo.setTechnicalServiceOperations(technicalServiceOperations);
    }

    public void setEventDefinitions(List<VOEventDefinition> eventDefinitions) {
        vo.setEventDefinitions(eventDefinitions);
    }

    public void setKey(long key) {
        vo.setKey(key);
    }

    public void setLicense(String license) {
        if (license == null) {
            vo.setLicense("");
        } else {
            vo.setLicense(license);
        }
    }

    public void setParameterDefinitions(List<VOParameterDefinition> parameters) {
        vo.setParameterDefinitions(parameters);
    }

    public void setRoleDefinitions(List<VORoleDefinition> roleDefinitions) {
        vo.setRoleDefinitions(roleDefinitions);
    }

    public List<String> getTags() {
        return vo.getTags();
    }

    public void setTags(List<String> tags) {
        vo.setTags(tags);
    }

    public String getTagsAsString() {
        // Returns tags as comma separated list
        StringBuffer b = new StringBuffer();
        Iterator<String> iter = vo.getTags().iterator();
        while (iter.hasNext()) {
            b.append(iter.next());
            if (iter.hasNext())
                b.append(", ");
        }
        return b.toString();
    }

    public void setTagsAsString(String tags) {
        // Convert comma separated list into list
        StringTokenizer tok = new StringTokenizer(tags, ",");
        List<String> list = new ArrayList<String>();
        while (tok.hasMoreElements()) {
            String tagValue = tok.nextElement().toString().trim();
            if (tagValue.length() > 0)
                list.add(tagValue.trim());
        }
        vo.setTags(list);
    }

}
