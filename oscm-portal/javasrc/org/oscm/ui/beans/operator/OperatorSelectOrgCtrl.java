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

import java.io.IOException;
import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.lang3.StringUtils;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.vo.VOOperatorOrganization;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.model.Organization;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Controller to select an organization to perform operator tasks on it. */
@ManagedBean(name = "operatorSelectOrgCtrl")
@ViewScoped
public class OperatorSelectOrgCtrl extends BaseOperatorBean implements Serializable {

  private static final long serialVersionUID = -7044849342962387357L;

  private static final Log4jLogger logger = LoggerFactory.getLogger(OperatorSelectOrgCtrl.class);

  @ManagedProperty(value = "#{operatorSelectOrgModel}")
  private OperatorSelectOrgModel model;

  @PostConstruct
  public void init() {
    OperatorSelectOrgModel m = model;
    initSelectedOrganizationId();
    Map<String, String> orgs = getSuggestedOrgs();
    m.setSuggestedOrgs(orgs);
  }

  void initSelectedOrganizationId() {
    OperatorSelectOrgModel m = model;
    String id = (String) getRequest().getSession().getAttribute("SelectedOrgId");
    String selected = (id != null) ? id : getUserFromSession().getOrganizationId();

    selectOrganizationAction(selected);
  }

  public void setExistingOrganization(VOOperatorOrganization existingOrgInDB) {
    VOOperatorOrganization clone = deepCopy(existingOrgInDB);
    model.setExistingOrganization(clone);
  }

  private VOOperatorOrganization deepCopy(VOOperatorOrganization org) {

    try {
      ObjectMapper om = new ObjectMapper();
      String s = om.writeValueAsString(org);

      VOOperatorOrganization deepCopy = om.readValue(s, VOOperatorOrganization.class);
      return deepCopy;
    } catch (IOException e) {

      throw new SaaSSystemException(e);
    }
  }

  public VOOperatorOrganization getExistingOrganization() {
    return model.getExistingOrganization();
  }

  static final String APPLICATION_BEAN = "appBean";

  transient ApplicationBean appBean;

  /** Sort organization labels alphabetically in locale-sensitive order. */
  private class OrgComparator implements Comparator<Organization> {
    Collator collator = Collator.getInstance();

    @Override
    public int compare(Organization o1, Organization o2) {
      return collator.compare(o1.getNameWithOrganizationId(), o2.getNameWithOrganizationId());
    }
  }

  void reloadOrganization(String id) throws SaaSApplicationException {
    VOOperatorOrganization org = getOperatorService().getOrganization(id);

    if (org.getLocale() != null) {
      getApplicationBean().checkLocaleValidation(org.getLocale());
    }
    model.setOrganization(org);
    setExistingOrganization(org);
    model.setOrganizationId(id);
  }

  public String getOrganizationId() {
    return model.getOrganizationId();
  }

  public void setOrganizationId(String id) {
    if (id != null) {
      if (!id.equals(model.getOrganizationId())) {
        selectOrganizationAction(id);
      }
    }
  }

  public void selectOrganizationAction(ValueChangeEvent e) {
    String id = (String) e.getNewValue();
    String oldId = (String) e.getOldValue();
    if (StringUtils.isNotBlank(id) && !id.equals(oldId)) {
      model.setOrganizationId(id);
      selectOrganizationAction(id);
    }
  }

  public void selectOrganizationAction(String id) {
    try {
      reloadOrganization(id);
    } catch (SaaSApplicationException exc) {
      ExceptionHandler.execute(exc);
      getRequest().setAttribute(Constants.REQ_ATTR_DIRTY, Boolean.FALSE.toString());
    }
  }

  public String getOrganizationRoleType() {
    return model.getOrganizationRoleType();
  }

  public void setOrganizationRoleType(String organizationRoleType) {
    model.setOrganizationRoleType(organizationRoleType);
  }

  public VOOperatorOrganization getOrganization() {
    return model.getOrganization();
  }

  public void setOrganization(VOOperatorOrganization o) {
    model.setOrganization(o);
  }

  public Map<String, String> getSuggestedOrgs() {
    List<OrganizationRoleType> roleTypes = new ArrayList<OrganizationRoleType>();
    String value = getRequest().getParameter(Constants.REQ_PARAM_ORGANIZATION_ROLE_TYPE);
    if (!isBlank(value)) {
      Arrays.asList(value.split(",")).forEach(s -> roleTypes.add(OrganizationRoleType.valueOf(s)));
    }
    return getOperatorService().getOrganizationIdentifiers(roleTypes);
  }

  ApplicationBean getApplicationBean() {
    if (appBean == null) {
      appBean = ui.findBean(APPLICATION_BEAN);
    }
    return appBean;
  }

  public OperatorSelectOrgModel getModel() {
    return model;
  }

  public void setModel(OperatorSelectOrgModel m) {
    model = m;
  }
}
