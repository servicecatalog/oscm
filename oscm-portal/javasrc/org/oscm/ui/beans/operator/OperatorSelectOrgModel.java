/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 17.03.2020
 *
 * <p>**************************************************************************
 */
package org.oscm.ui.beans.operator;

import java.io.Serializable;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import org.oscm.internal.vo.VOOperatorOrganization;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;

/**
 * Model for organization chooser.
 *
 * @author goebel
 */
@ManagedBean
@ViewScoped
public class OperatorSelectOrgModel extends BaseOperatorBean implements Serializable {
  private static final long serialVersionUID = 4738024067585332836L;

  private static final Log4jLogger logger = LoggerFactory.getLogger(OperatorSelectOrgModel.class);
  private String organizationRoleType;
  private Map<String, String> suggestedOrgs;

  VOOperatorOrganization operatorOrg;
  VOOperatorOrganization existingOrg;

  private String organizationId = null;

  private boolean initialized;

  public VOOperatorOrganization getOrganization() {
    return operatorOrg;
  }

  public void setOrganization(VOOperatorOrganization org) {
    operatorOrg = org;
  }

  public String getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  public void selectOrganizationAction() {}

  public String getOrganizationRoleType() {
    return organizationRoleType;
  }

  public void setOrganizationRoleType(String organizationRoleType) {
    this.organizationRoleType = organizationRoleType;
  }

  public Map<String, String> getSuggestedOrgs() {
    return suggestedOrgs;
  }

  public void setSuggestedOrgs(Map<String, String> map) {
    suggestedOrgs = map;
  }

  public void setExistingOrganization(VOOperatorOrganization org) {
    existingOrg = org;
  }

  public VOOperatorOrganization getExistingOrganization() {
    return existingOrg;
  }

  public boolean isInitialized() {
    return initialized;
  }

  public void setInitialized(boolean initialized) {
    this.initialized = initialized;
  }
}
