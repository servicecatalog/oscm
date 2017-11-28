/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 5, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ImageResource;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.SupportedCountry;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.IncompatibleRolesException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOImageResource;
import org.oscm.internal.vo.VOOperatorOrganization;

public class OperatorServiceBeanPartnerTest {

    OperatorServiceBean os;
    DataService ds;
    AccountServiceLocal as;

    Organization organization;
    SupportedCountry sc;

    @Before
    public void setup() throws Exception {
        ds = mock(DataService.class);
        os = spy(new OperatorServiceBean());
        os.dm = ds;
        as = mock(AccountServiceLocal.class);
        os.accMgmt = as;

        organization = new Organization();
        organization.setKey(1L);
        sc = new SupportedCountry();
        sc.setKey(1L);

        doReturn(organization).doReturn(sc).when(ds)
                .getReferenceByBusinessKey(any(Organization.class));
        doNothing().when(as).processImage(any(ImageResource.class), anyLong());
        when(
                as.addOrganizationToRole(anyString(),
                        any(OrganizationRoleType.class))).thenReturn(
                organization);
        doReturn(null).when(os).createLocalizerFacade();
    }

    @Test
    public void updateOrganizationIntern_broker_emailNotSet() throws Exception {
        // given
        VOImageResource image = new VOImageResource();
        VOOperatorOrganization reseller = givenSellerOrganization(OrganizationRoleType.BROKER);
        reseller.setEmail("");

        // when
        try {
            os.updateOrganizationIntern(reseller, image);
            fail();
        } catch (ValidationException e) {

            // then
            assertEquals(e.getReason(), ReasonEnum.REQUIRED);
            assertEquals(e.getMember(), "email");
        }
    }

    @Test
    public void updateOrganizationIntern_broker_phoneNotSet() throws Exception {
        // given
        VOImageResource image = new VOImageResource();
        VOOperatorOrganization reseller = givenSellerOrganization(OrganizationRoleType.BROKER);
        reseller.setPhone("");

        // when
        try {
            os.updateOrganizationIntern(reseller, image);
            fail();
        } catch (ValidationException e) {

            // then
            assertEquals(e.getReason(), ReasonEnum.REQUIRED);
            assertEquals(e.getMember(), "phone");
        }
    }

    @Test
    public void updateOrganizationIntern_brokerr_urlNotSet() throws Exception {
        // given
        VOImageResource image = new VOImageResource();
        VOOperatorOrganization reseller = givenSellerOrganization(OrganizationRoleType.BROKER);
        reseller.setUrl("");

        // when
        try {
            os.updateOrganizationIntern(reseller, image);
            fail();
        } catch (ValidationException e) {

            // then
            assertEquals(e.getReason(), ReasonEnum.REQUIRED);
            assertEquals(e.getMember(), "url");
        }
    }

    @Test
    public void updateOrganizationIntern_broker_nameNotSet() throws Exception {
        // given
        VOImageResource image = new VOImageResource();
        VOOperatorOrganization reseller = givenSellerOrganization(OrganizationRoleType.BROKER);
        reseller.setName("");

        // when
        try {
            os.updateOrganizationIntern(reseller, image);
            fail();
        } catch (ValidationException e) {

            // then
            assertEquals(e.getReason(), ReasonEnum.REQUIRED);
            assertEquals(e.getMember(), "name");
        }
    }

    @Test
    public void updateOrganizationIntern_broker_addressNotSet()
            throws Exception {
        // given
        VOImageResource image = new VOImageResource();
        VOOperatorOrganization reseller = givenSellerOrganization(OrganizationRoleType.BROKER);
        reseller.setAddress("");

        // when
        try {
            os.updateOrganizationIntern(reseller, image);
            fail();
        } catch (ValidationException e) {

            // then
            assertEquals(e.getReason(), ReasonEnum.REQUIRED);
            assertEquals(e.getMember(), "address");
        }
    }

    @Test
    public void updateOrganizationIntern_broker_localeNotSet() throws Exception {
        // given
        VOImageResource image = new VOImageResource();
        VOOperatorOrganization reseller = givenSellerOrganization(OrganizationRoleType.BROKER);
        reseller.setLocale("");

        // when
        try {
            os.updateOrganizationIntern(reseller, image);
            fail();
        } catch (ValidationException e) {

            // then
            assertEquals(e.getReason(), ReasonEnum.REQUIRED);
            assertEquals(e.getMember(), "locale");
        }
    }

    @Test
    public void updateOrganizationIntern_reseller_emailNotSet()
            throws Exception {
        // given
        VOImageResource image = new VOImageResource();
        VOOperatorOrganization reseller = givenSellerOrganization(OrganizationRoleType.RESELLER);
        reseller.setEmail("");

        // when
        try {
            os.updateOrganizationIntern(reseller, image);
            fail();
        } catch (ValidationException e) {

            // then
            assertEquals(e.getReason(), ReasonEnum.REQUIRED);
            assertEquals(e.getMember(), "email");
        }
    }

    @Test
    public void updateOrganizationIntern_reseller_phoneNotSet()
            throws Exception {
        // given
        VOImageResource image = new VOImageResource();
        VOOperatorOrganization reseller = givenSellerOrganization(OrganizationRoleType.RESELLER);
        reseller.setPhone("");

        // when
        try {
            os.updateOrganizationIntern(reseller, image);
            fail();
        } catch (ValidationException e) {

            // then
            assertEquals(e.getReason(), ReasonEnum.REQUIRED);
            assertEquals(e.getMember(), "phone");
        }
    }

    @Test
    public void updateOrganizationIntern_reseller_urlNotSet() throws Exception {
        // given
        VOImageResource image = new VOImageResource();
        VOOperatorOrganization reseller = givenSellerOrganization(OrganizationRoleType.RESELLER);
        reseller.setUrl("");

        // when
        try {
            os.updateOrganizationIntern(reseller, image);
            fail();
        } catch (ValidationException e) {

            // then
            assertEquals(e.getReason(), ReasonEnum.REQUIRED);
            assertEquals(e.getMember(), "url");
        }
    }

    @Test
    public void updateOrganizationIntern_reseller_nameNotSet() throws Exception {
        // given
        VOImageResource image = new VOImageResource();
        VOOperatorOrganization reseller = givenSellerOrganization(OrganizationRoleType.RESELLER);
        reseller.setName("");

        // when
        try {
            os.updateOrganizationIntern(reseller, image);
            fail();
        } catch (ValidationException e) {

            // then
            assertEquals(e.getReason(), ReasonEnum.REQUIRED);
            assertEquals(e.getMember(), "name");
        }
    }

    @Test
    public void updateOrganizationIntern_reseller_addressNotSet()
            throws Exception {
        // given
        VOImageResource image = new VOImageResource();
        VOOperatorOrganization reseller = givenSellerOrganization(OrganizationRoleType.RESELLER);
        reseller.setAddress("");

        // when
        try {
            os.updateOrganizationIntern(reseller, image);
            fail();
        } catch (ValidationException e) {

            // then
            assertEquals(e.getReason(), ReasonEnum.REQUIRED);
            assertEquals(e.getMember(), "address");
        }
    }

    @Test
    public void updateOrganizationIntern_reseller_localeNotSet()
            throws Exception {
        // given
        VOImageResource image = new VOImageResource();
        VOOperatorOrganization reseller = givenSellerOrganization(OrganizationRoleType.RESELLER);
        reseller.setLocale("");

        // when
        try {
            os.updateOrganizationIntern(reseller, image);
            fail();
        } catch (ValidationException e) {

            // then
            assertEquals(e.getReason(), ReasonEnum.REQUIRED);
            assertEquals(e.getMember(), "locale");
        }
    }

    private VOOperatorOrganization givenSellerOrganization(
            OrganizationRoleType roleType) {
        VOOperatorOrganization reseller = new VOOperatorOrganization();
        reseller.setKey(1L);
        reseller.setVersion(0);
        reseller.setDistinguishedName(null);

        reseller.setEmail("email@email.de");
        reseller.setPhone("phone");
        reseller.setUrl("http://www.fujitsu.com");
        reseller.setName("name");
        reseller.setAddress("address");
        reseller.setLocale("en");

        reseller.setOrganizationRoles(Arrays.asList(roleType));
        return reseller;
    }

    @Test(expected = IncompatibleRolesException.class)
    public void updateOrganizationIntern_reseller_addBrokerRole()
            throws Exception {
        // given
        VOImageResource image = new VOImageResource();
        VOOperatorOrganization voReseller = givenSellerOrganization(OrganizationRoleType.RESELLER);
        voReseller.setOrganizationRoles(Arrays.asList(
                OrganizationRoleType.BROKER, OrganizationRoleType.RESELLER));

        // when
        os.updateOrganizationIntern(voReseller, image);
        fail();
    }

    @Test(expected = IncompatibleRolesException.class)
    public void updateOrganizationIntern_reseller_addSupplierRole()
            throws Exception {
        // given
        VOImageResource image = new VOImageResource();
        VOOperatorOrganization voReseller = givenSellerOrganization(OrganizationRoleType.RESELLER);
        voReseller.setOperatorRevenueShare(BigDecimal.valueOf(15));
        voReseller.setOrganizationRoles(Arrays.asList(
                OrganizationRoleType.SUPPLIER, OrganizationRoleType.RESELLER));

        os.updateOrganizationIntern(voReseller, image);
        fail();
    }

    @Test(expected = IncompatibleRolesException.class)
    public void updateOrganizationIntern_broker_addSupplierRole()
            throws Exception {
        // given
        VOImageResource image = new VOImageResource();
        VOOperatorOrganization voBroker = givenSellerOrganization(OrganizationRoleType.BROKER);
        voBroker.setOperatorRevenueShare(BigDecimal.valueOf(15));
        voBroker.setOrganizationRoles(Arrays.asList(
                OrganizationRoleType.SUPPLIER, OrganizationRoleType.BROKER));

        os.updateOrganizationIntern(voBroker, image);
        fail();
    }

    @Test
    public void updateOrganizationIntern_reseller() throws Exception {
        // given
        VOImageResource image = new VOImageResource();
        VOOperatorOrganization voReseller = givenSellerOrganization(OrganizationRoleType.RESELLER);

        // when
        VOOperatorOrganization reseller = os.updateOrganizationIntern(
                voReseller, image);

        // then
        verify(as, times(1)).processImage(any(ImageResource.class), anyLong());
        assertEquals(voReseller.getEmail(), reseller.getEmail());
        assertEquals(voReseller.getPhone(), reseller.getPhone());
        assertEquals(voReseller.getUrl(), reseller.getUrl());
        assertEquals(voReseller.getName(), reseller.getName());
        assertEquals(voReseller.getAddress(), reseller.getAddress());
        assertEquals(voReseller.getLocale(), reseller.getLocale());

        assertEquals(voReseller.getEmail(), organization.getEmail());
        assertEquals(voReseller.getPhone(), organization.getPhone());
        assertEquals(voReseller.getUrl(), organization.getUrl());
        assertEquals(voReseller.getName(), organization.getName());
        assertEquals(voReseller.getAddress(), organization.getAddress());
        assertEquals(voReseller.getLocale(), organization.getLocale());
    }

    @Test
    public void updateOrganizationIntern_broker() throws Exception {
        // given
        VOImageResource image = new VOImageResource();
        VOOperatorOrganization voBroker = givenSellerOrganization(OrganizationRoleType.BROKER);

        // when
        VOOperatorOrganization broker = os.updateOrganizationIntern(voBroker,
                image);

        // then
        verify(as, times(1)).processImage(any(ImageResource.class), anyLong());
        assertEquals(voBroker.getEmail(), broker.getEmail());
        assertEquals(voBroker.getPhone(), broker.getPhone());
        assertEquals(voBroker.getUrl(), broker.getUrl());
        assertEquals(voBroker.getName(), broker.getName());
        assertEquals(voBroker.getAddress(), broker.getAddress());
        assertEquals(voBroker.getLocale(), broker.getLocale());

        assertEquals(voBroker.getEmail(), organization.getEmail());
        assertEquals(voBroker.getPhone(), organization.getPhone());
        assertEquals(voBroker.getUrl(), organization.getUrl());
        assertEquals(voBroker.getName(), organization.getName());
        assertEquals(voBroker.getAddress(), organization.getAddress());
        assertEquals(voBroker.getLocale(), organization.getLocale());
    }
}
