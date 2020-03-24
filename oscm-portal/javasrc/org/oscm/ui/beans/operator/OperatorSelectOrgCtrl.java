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

import com.fasterxml.jackson.databind.ObjectMapper;
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
import javax.faces.event.AjaxBehaviorEvent;
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

/**
 * Controller to select an organization for performing operator tasks.
 *
 * @author goebel
 */
@ManagedBean(name = "operatorSelectOrgCtrl")
@ViewScoped
public class OperatorSelectOrgCtrl extends BaseOperatorBean implements Serializable {

  private static final long serialVersionUID = -7044849342962387357L;

  private static final Log4jLogger logger = LoggerFactory.getLogger(OperatorSelectOrgCtrl.class);

  @ManagedProperty(value = "#{operatorSelectOrgModel}")
  private OperatorSelectOrgModel model;

  @PostConstruct
  public void init() {
    if (!model.isInitialized()) {
      OperatorSelectOrgModel m = model;
      initializeModel(m);
      model.setInitialized(true);
    }
  }

  void initializeModel(OperatorSelectOrgModel m) {
    String id = (String) getRequest().getSession().getAttribute("organizationId");
    String selected = (id != null) ? id : getUserFromSession().getOrganizationId();
    reloadOrganization(selected);
    Map<String, String> orgs = getSuggestedOrgs();
    m.setSuggestedOrgs(orgs);
  }

  public void setExistingOrganization(VOOperatorOrganization existingOrgInDB) {
    model.setExistingOrganization(deepCopy(existingOrgInDB, VOOperatorOrganization.class));
  }

  private <T> T deepCopy(Object obj, Class<T> clazz) {
    try {
      ObjectMapper om = new ObjectMapper();
      if (clazz.isAssignableFrom(obj.getClass())) {
        T t = clazz.cast(obj);
        final String s = om.writeValueAsString(obj);
        return clazz.cast(om.readValue(s, t.getClass()));
      }
      throw new AssertionError("Cannot cast " + obj.getClass().getName());
    } catch (IOException e) {
      throw new SaaSSystemException(e);
    }
  }

  public VOOperatorOrganization getExistingOrganization() {
    return model.getExistingOrganization();
  }

  static final String APPLICATION_BEAN = "appBean";

  transient ApplicationBean appBean;

  private class OrgComparator implements Comparator<Organization> {
    Collator collator = Collator.getInstance();

    @Override
    public int compare(Organization o1, Organization o2) {
      return collator.compare(o1.getNameWithOrganizationId(), o2.getNameWithOrganizationId());
    }
  }

  private void reloadOrganizationIntern(String id) throws SaaSApplicationException {
    VOOperatorOrganization org = getOperatorService().getOrganization(id);
    if (org.getLocale() != null) {
      getApplicationBean().checkLocaleValidation(org.getLocale());
    }
    model.setOrganization(org);
    setExistingOrganization(org);
    model.setOrganizationId(id);
    getRequest().getSession().setAttribute("organizationId", id);
  }

  public String getOrganizationId() {
    return model.getOrganizationId();
  }

  public void setOrganizationId(String id) {
    if (id != null) {
      if (!id.equals(model.getOrganizationId())) {
        reloadOrganization(id);
      }
    }
  }

  public void selectOrganizationAction(AjaxBehaviorEvent e) {
    reloadOrganization(model.getOrganizationId());
  }

  public void reloadOrganization(String id) {
    try {
      reloadOrganizationIntern(id);
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

  protected Map<String, String> getSuggestedOrgs() {
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
