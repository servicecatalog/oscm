/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 27.01.2010                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.beans.operator;

import java.io.Serializable;
import java.text.Collator;
import java.util.*;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.string.Strings;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.SelectOrganizationIncludeBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.model.Organization;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOOperatorOrganization;
import org.oscm.internal.vo.VOOrganization;

/**
 * Backing bean to select an organization to perform operator tasks on it.
 * 
 * @author pock
 */
@SessionScoped
@ManagedBean(name="operatorSelectOrgBean")
public class OperatorSelectOrgBean extends BaseOperatorBean implements Serializable {

    private static final long serialVersionUID = -7044849342962387357L;
    List<SelectItem> availableOrganizations;
    private Organization orgModel;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(OperatorSelectOrgBean.class);

    private VOOperatorOrganization organization;

    /*TODO: think more about that. It breaks architecture in view layer. Maybe model should be a bean and we should not get access to model through this bean but directly.*/
    @ManagedProperty(value="#{selectOrganizationIncludeBean}")
    private SelectOrganizationIncludeBean selectOrganizationIncludeBean;

    static final String APPLICATION_BEAN = "appBean";

    transient ApplicationBean appBean;

    public Organization getOrgModel() {
        if (orgModel == null) {
            // use disabled default if nothing is selected
            orgModel = new Organization(new VOOrganization());
        }
        return orgModel;
    }
    /**
     * Sort organization labels alphabetically in locale-sensitive order.
     */
    private class OrgComparator implements Comparator<Organization> {

        Collator collator = Collator.getInstance();

        @Override
        public int compare(Organization o1, Organization o2) {
            return collator.compare(o1.getOrganizationId(), o2.getOrganizationId());
        }
    }

    private class OrganizationComparator implements Comparator<VOOrganization> {

        Collator collator = Collator.getInstance();

        @Override
        public int compare(VOOrganization o1, VOOrganization o2) {
            return collator.compare(o1.getOrganizationId(), o2.getOrganizationId());
        }
    }

    public VOOperatorOrganization getOrganization() {
        return organization;
    }

    public void reloadOrganization() throws SaaSApplicationException {
        if (StringUtils.isNotBlank(getOrganizationId())) {
            organization = getOperatorService().getOrganization(getOrganizationId());
            if (organization != null && organization.getLocale() != null) {
                getApplicationBean()
                        .checkLocaleValidation(organization.getLocale());
            }
        }
    }

    public String getOrganizationId() {
        return getSelectOrganizationIncludeBean().getOrganizationId();
    }

    public void setOrganizationId(String organizationId) {
        if (organizationId != null) {
            getSelectOrganizationIncludeBean().setOrganizationId(organizationId.trim());
            selectOrganizationAction();
        }
    }

    public void selectOrganizationAction() {
        try {
            reloadOrganization();
        } catch (SaaSApplicationException exc) {
            ExceptionHandler.execute(exc);
            getRequest().setAttribute(Constants.REQ_ATTR_DIRTY,
                    Boolean.FALSE.toString());
        }
    }

    public List<SelectItem> getAvailableOrganizations() {
        if (availableOrganizations == null) {
            List<VOOrganization> organizations;
            try {
                List<OrganizationRoleType> roleTypes = new ArrayList<OrganizationRoleType>();
                System.out.println("Length of roleTypes list equals: " + roleTypes.size());
                String value = getRequest().getParameter(
                        Constants.REQ_PARAM_ORGANIZATION_ROLE_TYPE);
                System.out.println("Value of value equals: " + value);
                if (!isBlank(value)) {
                    try {
                        StringTokenizer st = new StringTokenizer(value, ",");
                        while (st.hasMoreElements()) {
                            roleTypes.add(OrganizationRoleType.valueOf(st
                                    .nextToken()));
                        }
                    } catch (NoSuchElementException e) {
                        logger.logError(
                                Log4jLogger.SYSTEM_LOG,
                                e,
                                LogMessageIdentifier.ERROR_CONVERT_ORGANIZATION_ROLE_TYPE_FAILED);
                    }
                }
                VOOrganization voOrg = new VOOrganization();
                String pattern = voOrg.getOrganizationId() + "%";

            System.out.println("Pattern value is: " + pattern);
            organizations = getOperatorService().getOrganizations(pattern, roleTypes);

            Collections.sort(organizations, new OrganizationComparator());

            List<SelectItem> result = new ArrayList<SelectItem>();
            // create the selection model based on the read data
            for (VOOrganization vOr : organizations) {
                result.add(new SelectItem(vOr.getOrganizationId(), getLabel(vOr)));
                System.out.println("Result should consist: " + vOr.getOrganizationId() + " and " + getLabel(vOr));
            }
            availableOrganizations = result;
            System.out.println("List have length: " + availableOrganizations.size());
            return availableOrganizations;
            } catch (SaaSApplicationException e) {
                ExceptionHandler.execute(e);
            } }
        return null;
    }

    String getLabel(VOOrganization vOr) {
        if (vOr == null) {
            return "";
        }
        if (vOr.getName() == null || Strings.isEmpty(vOr.getName())) {
            return vOr.getOrganizationId();
        }
        return String.format("%s (%s)", vOr.getName(), vOr.getOrganizationId());
    }

    public List<Organization> suggest(FacesContext context, UIComponent component, String organizationId) {
		organizationId = organizationId.replaceAll("\\p{C}", "");
		if (context == null || component == null) {
            return null;
        }
        Vo2ModelMapper<VOOrganization, Organization> mapper = new Vo2ModelMapper<VOOrganization, Organization>() {
            @Override
            public Organization createModel(final VOOrganization vo) {
                return new Organization(vo);
            }
        };
        try {
            List<OrganizationRoleType> roleTypes = new ArrayList<OrganizationRoleType>();
            String value = getRequest().getParameter(
                    Constants.REQ_PARAM_ORGANIZATION_ROLE_TYPE);
            if (!isBlank(value)) {
                try {
                    StringTokenizer st = new StringTokenizer(value, ",");
                    while (st.hasMoreElements()) {
                        roleTypes.add(OrganizationRoleType.valueOf(st
                                .nextToken()));
                    }
                } catch (NoSuchElementException e) {
                    logger.logError(
                            Log4jLogger.SYSTEM_LOG,
                            e,
                            LogMessageIdentifier.ERROR_CONVERT_ORGANIZATION_ROLE_TYPE_FAILED);
                }
            }
            String pattern = organizationId + "%";
            System.out.println("Pattern value in suggest method: " + pattern);
            List<Organization> organizations = mapper.map(
                    getOperatorService().getOrganizations(pattern, roleTypes));
            Collections.sort(organizations, new OrgComparator());
            System.out.println("List in suggest method have length: " + organizations.size());
            return organizations;
        } catch (SaaSApplicationException e) {
            ExceptionHandler.execute(e);
        }
        return null;
    }

    ApplicationBean getApplicationBean() {
        if (appBean == null) {
            appBean = ui.findBean(APPLICATION_BEAN);
        }
        return appBean;
    }

    public SelectOrganizationIncludeBean getSelectOrganizationIncludeBean() {
        return selectOrganizationIncludeBean;
    }

    public void setSelectOrganizationIncludeBean(SelectOrganizationIncludeBean selectOrganizationIncludeBean) {
        this.selectOrganizationIncludeBean = selectOrganizationIncludeBean;
    }
}
