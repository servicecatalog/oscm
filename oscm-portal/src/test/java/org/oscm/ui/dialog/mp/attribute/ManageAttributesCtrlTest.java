/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Nov 9, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.attribute;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;
import org.oscm.test.stubs.AccountServiceStub;
import org.oscm.test.stubs.MarketplaceServiceStub;
import org.oscm.ui.beans.OrganizationBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.beans.UserBean;
import org.oscm.ui.dialog.mp.attributes.ManageAttributesCtrl;
import org.oscm.ui.dialog.mp.attributes.ManageAttributesModel;
import org.oscm.ui.model.UdaRow;

/**
 * Unit test for ManageAttributesCtrl
 * 
 * @author miethaner
 */
public class ManageAttributesCtrlTest {

    private ManageAttributesCtrl ctrl;
    private ManageAttributesModel model;
    private SessionBean sessionBean;
    private UserBean userBean;

    private AccountService accountService;
    private MarketplaceService marketplaceService;

    @Before
    public void setup() throws Exception {
        ctrl = new ManageAttributesCtrl();
        model = new ManageAttributesModel();

        sessionBean = mock(SessionBean.class);
        when(sessionBean.getMarketplaceId()).thenReturn("marketplaceId");

        VOOrganization org = new VOOrganization();
        org.setOrganizationId("orgId");
        org.setKey(100L);
        userBean = mock(UserBean.class);
        OrganizationBean organizationBean = mock(OrganizationBean.class);
        when(userBean.getOrganizationBean()).thenReturn(organizationBean);
        when(organizationBean.getOrganization()).thenReturn(org);

        accountService = new AccountServiceStub() {

            @Override
            public void saveUdas(List<VOUda> udas) throws ValidationException,
                    ObjectNotFoundException, OperationNotPermittedException,
                    ConcurrentModificationException,
                    NonUniqueBusinessKeyException {
                // do nothing
            }

            @Override
            public List<VOUdaDefinition> getUdaDefinitionsForCustomer(
                    String supplierId) throws ObjectNotFoundException {

                List<VOUdaDefinition> list = new ArrayList<>();
                VOUdaDefinition def = new VOUdaDefinition();
                def.setUdaId("uda1");
                def.setLanguage("en");
                def.setEncrypted(false);
                def.setConfigurationType(
                        UdaConfigurationType.USER_OPTION_OPTIONAL);
                def.setTargetType("CUSTOMER");
                def.setDefaultValue("default");
                list.add(def);
                def = new VOUdaDefinition();
                def.setUdaId("uda2");
                def.setLanguage("en");
                def.setEncrypted(false);
                def.setConfigurationType(
                        UdaConfigurationType.USER_OPTION_OPTIONAL);
                def.setTargetType("CUSTOMER");
                def.setDefaultValue("default");
                list.add(def);

                return list;
            }

            @Override
            public List<VOUda> getUdasForCustomer(String targetType,
                    long targetObjectKey, String supplierId)
                    throws ValidationException,
                    OrganizationAuthoritiesException, ObjectNotFoundException,
                    OperationNotPermittedException {
                VOUdaDefinition def = new VOUdaDefinition();
                def.setUdaId("uda1");
                def.setLanguage("en");
                def.setEncrypted(false);
                def.setConfigurationType(
                        UdaConfigurationType.USER_OPTION_OPTIONAL);
                def.setTargetType("CUSTOMER");
                def.setDefaultValue("default");

                VOUda uda = new VOUda();
                uda.setUdaDefinition(def);
                uda.setUdaValue("value");

                return Arrays.asList(uda);
            }

        };

        marketplaceService = new MarketplaceServiceStub() {

            @Override
            public List<VOOrganization> getSuppliersForMarketplace(
                    String marketplaceId) throws ObjectNotFoundException,
                    OperationNotPermittedException {
                VOOrganization org = new VOOrganization();
                org.setOrganizationId("orgId");

                return Arrays.asList(org);
            }

        };

        ctrl.setModel(model);
        ctrl.setSessionBean(sessionBean);
        ctrl.setUserBean(userBean);
        ctrl.setAccountService(accountService);
        ctrl.setMarketplaceService(marketplaceService);
    }

    @Test
    public void testGetModel() {

        ctrl.construct();

        Map<String, UdaRow> map = ctrl.getModel().getAttributeMap();

        assertEquals("value", map.get("uda1").getUdaValue());
        assertEquals("default", map.get("uda2").getUdaValue());
    }

}
