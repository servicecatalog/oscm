/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.converter.api;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import org.junit.AfterClass;
import org.junit.Test;

import org.oscm.converter.common.VOAssert;
import org.oscm.converter.common.VOInitializer;
import org.oscm.dataservice.local.DataService;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOTriggerProcessParameter;
import org.oscm.vo.VOServiceOperationParameter;

public class VOConverterApiTest {

    static VOAssert voAssert = new VOAssert(null);

    @AfterClass
    public static void after() {
        for (String s : voAssert.getWarning()) {
            System.out.println(s);
        }
    }

    @Test
    public void LdapProperties_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.LdapProperties) null));
    }

    @Test
    public void LdapProperties_convertToUp() throws Exception {
        org.oscm.vo.LdapProperties oldVO = new org.oscm.vo.LdapProperties();
        oldVO.setProperty("key", "value");
        org.oscm.internal.vo.LdapProperties newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void LdapProperties_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.LdapProperties) null));
    }

    @Test
    public void LdapProperties_convertToApi() throws Exception {
        org.oscm.internal.vo.LdapProperties oldVO = new org.oscm.internal.vo.LdapProperties();
        oldVO.setProperty("key", "value");
        org.oscm.vo.LdapProperties newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void ListCriteria_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.ListCriteria) null));
    }

    @Test
    public void ListCriteria_convertToUp() throws Exception {
        org.oscm.vo.ListCriteria oldVO = new org.oscm.vo.ListCriteria();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.ListCriteria newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void ListCriteria_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.ListCriteria) null));
    }

    @Test
    public void ListCriteria_convertToApi() throws Exception {
        org.oscm.internal.vo.ListCriteria oldVO = new org.oscm.internal.vo.ListCriteria();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.ListCriteria newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void Setting_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.Setting) null));
    }

    @Test
    public void Setting_convertToUp() throws Exception {
        org.oscm.vo.Setting oldVO = new org.oscm.vo.Setting();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.Setting newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void Setting_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.Setting) null));
    }

    @Test
    public void Setting_convertToApi() throws Exception {
        org.oscm.internal.vo.Setting oldVO = new org.oscm.internal.vo.Setting();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.Setting newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOBillingContact_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOBillingContact) null));
    }

    @Test
    public void VOBillingContact_convertToUp() throws Exception {
        org.oscm.vo.VOBillingContact oldVO = new org.oscm.vo.VOBillingContact();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOBillingContact newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOBillingContact_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOBillingContact) null));
    }

    @Test
    public void VOBillingContact_convertToApi() throws Exception {
        org.oscm.internal.vo.VOBillingContact oldVO = new org.oscm.internal.vo.VOBillingContact();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOBillingContact newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOCatalogEntry_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOCatalogEntry) null));
    }

    @Test
    public void VOCatalogEntry_convertToUp() throws Exception {
        org.oscm.vo.VOCatalogEntry oldVO = new org.oscm.vo.VOCatalogEntry();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOCatalogEntry newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOCatalogEntry_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOCatalogEntry) null));
    }

    @Test
    public void VOCatalogEntry_convertToApi() throws Exception {
        org.oscm.internal.vo.VOCatalogEntry oldVO = new org.oscm.internal.vo.VOCatalogEntry();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOCatalogEntry newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOCategory_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOCategory) null));
    }

    @Test
    public void VOCategory_convertToUp() throws Exception {
        org.oscm.vo.VOCategory oldVO = new org.oscm.vo.VOCategory();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOCategory newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOCategory_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOCategory) null));
    }

    @Test
    public void VOCategory_convertToApi() throws Exception {
        org.oscm.internal.vo.VOCategory oldVO = new org.oscm.internal.vo.VOCategory();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOCategory newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOCompatibleService_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOCompatibleService) null));
    }

    @Test
    public void VOCompatibleService_convertToUp() throws Exception {
        org.oscm.vo.VOCompatibleService oldVO = new org.oscm.vo.VOCompatibleService();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOCompatibleService newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOCompatibleService_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOCompatibleService) null));
    }

    @Test
    public void VOCompatibleService_convertToApi() throws Exception {
        org.oscm.internal.vo.VOCompatibleService oldVO = new org.oscm.internal.vo.VOCompatibleService();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOCompatibleService newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOCountryVatRate_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOCountryVatRate) null));
    }

    @Test
    public void VOCountryVatRate_convertToUp() throws Exception {
        org.oscm.vo.VOCountryVatRate oldVO = new org.oscm.vo.VOCountryVatRate();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOCountryVatRate newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOCountryVatRate_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOCountryVatRate) null));
    }

    @Test
    public void VOCountryVatRate_convertToApi() throws Exception {
        org.oscm.internal.vo.VOCountryVatRate oldVO = new org.oscm.internal.vo.VOCountryVatRate();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOCountryVatRate newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOCustomerService_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOCustomerService) null));
    }

    @Test
    public void VOCustomerService_convertToUp() throws Exception {
        org.oscm.vo.VOCustomerService oldVO = new org.oscm.vo.VOCustomerService();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOCustomerService newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOCustomerService_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOCustomerService) null));
    }

    @Test
    public void VOCustomerService_convertToApi() throws Exception {
        org.oscm.internal.vo.VOCustomerService oldVO = new org.oscm.internal.vo.VOCustomerService();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOCustomerService newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VODiscount_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VODiscount) null));
    }

    @Test
    public void VODiscount_convertToUp() throws Exception {
        org.oscm.vo.VODiscount oldVO = new org.oscm.vo.VODiscount();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VODiscount newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VODiscount_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VODiscount) null));
    }

    @Test
    public void VODiscount_convertToApi() throws Exception {
        org.oscm.internal.vo.VODiscount oldVO = new org.oscm.internal.vo.VODiscount();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VODiscount newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOEventDefinition_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOEventDefinition) null));
    }

    @Test
    public void VOEventDefinition_convertToUp() throws Exception {
        org.oscm.vo.VOEventDefinition oldVO = new org.oscm.vo.VOEventDefinition();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOEventDefinition newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOEventDefinition_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOEventDefinition) null));
    }

    @Test
    public void VOEventDefinition_convertToApi() throws Exception {
        org.oscm.internal.vo.VOEventDefinition oldVO = new org.oscm.internal.vo.VOEventDefinition();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOEventDefinition newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOGatheredEvent_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOGatheredEvent) null));
    }

    @Test
    public void VOGatheredEvent_convertToUp() throws Exception {
        org.oscm.vo.VOGatheredEvent oldVO = new org.oscm.vo.VOGatheredEvent();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOGatheredEvent newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOGatheredEvent_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOGatheredEvent) null));
    }

    @Test
    public void VOGatheredEvent_convertToApi() throws Exception {
        org.oscm.internal.vo.VOGatheredEvent oldVO = new org.oscm.internal.vo.VOGatheredEvent();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOGatheredEvent newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOImageResource_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOImageResource) null));
    }

    @Test
    public void VOImageResource_convertToUp() throws Exception {
        org.oscm.vo.VOImageResource oldVO = new org.oscm.vo.VOImageResource();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOImageResource newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOImageResource_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOImageResource) null));
    }

    @Test
    public void VOImageResource_convertToApi() throws Exception {
        org.oscm.internal.vo.VOImageResource oldVO = new org.oscm.internal.vo.VOImageResource();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOImageResource newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOInstanceInfo_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOInstanceInfo) null));
    }

    @Test
    public void VOInstanceInfo_convertToUp() throws Exception {
        org.oscm.vo.VOInstanceInfo oldVO = new org.oscm.vo.VOInstanceInfo();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOInstanceInfo newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOInstanceInfo_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOInstanceInfo) null));
    }

    @Test
    public void VOInstanceInfo_convertToApi() throws Exception {
        org.oscm.internal.vo.VOInstanceInfo oldVO = new org.oscm.internal.vo.VOInstanceInfo();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOInstanceInfo newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOLocalizedText_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOLocalizedText) null));
    }

    @Test
    public void VOLocalizedText_convertToUp() throws Exception {
        org.oscm.vo.VOLocalizedText oldVO = new org.oscm.vo.VOLocalizedText();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOLocalizedText newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOLocalizedText_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOLocalizedText) null));
    }

    @Test
    public void VOLocalizedText_convertToApi() throws Exception {
        org.oscm.internal.vo.VOLocalizedText oldVO = new org.oscm.internal.vo.VOLocalizedText();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOLocalizedText newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOMarketplace_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOMarketplace) null));
    }

    @Test
    public void VOMarketplace_convertToUp() throws Exception {
        org.oscm.vo.VOMarketplace oldVO = new org.oscm.vo.VOMarketplace();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOMarketplace newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOMarketplace_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOMarketplace) null));
    }

    @Test
    public void VOMarketplace_convertToApi() throws Exception {
        org.oscm.internal.vo.VOMarketplace oldVO = new org.oscm.internal.vo.VOMarketplace();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOMarketplace newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOOrganization_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOOrganization) null));
    }

    @Test
    public void VOOrganization_convertToUp() throws Exception {
        org.oscm.vo.VOOrganization oldVO = new org.oscm.vo.VOOrganization();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOOrganization newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOOrganization_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOOrganization) null));
    }

    @Test
    public void VOOrganization_convertToApi() throws Exception {
        org.oscm.internal.vo.VOOrganization oldVO = new org.oscm.internal.vo.VOOrganization();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOOrganization newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOOrganizationPaymentConfiguration_convertToUp_NullCheck()
            throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOOrganizationPaymentConfiguration) null));
    }

    @Test
    public void VOOrganizationPaymentConfiguration_convertToUp()
            throws Exception {
        org.oscm.vo.VOOrganizationPaymentConfiguration oldVO = new org.oscm.vo.VOOrganizationPaymentConfiguration();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOOrganizationPaymentConfiguration newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOOrganizationPaymentConfiguration_convertToApi_NullCheck()
            throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOOrganizationPaymentConfiguration) null));
    }

    @Test
    public void VOOrganizationPaymentConfiguration_convertToApi()
            throws Exception {
        org.oscm.internal.vo.VOOrganizationPaymentConfiguration oldVO = new org.oscm.internal.vo.VOOrganizationPaymentConfiguration();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOOrganizationPaymentConfiguration newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOOrganizationVatRate_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOOrganizationVatRate) null));
    }

    @Test
    public void VOOrganizationVatRate_convertToUp() throws Exception {
        org.oscm.vo.VOOrganizationVatRate oldVO = new org.oscm.vo.VOOrganizationVatRate();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOOrganizationVatRate newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOOrganizationVatRate_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOOrganizationVatRate) null));
    }

    @Test
    public void VOOrganizationVatRate_convertToApi() throws Exception {
        org.oscm.internal.vo.VOOrganizationVatRate oldVO = new org.oscm.internal.vo.VOOrganizationVatRate();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOOrganizationVatRate newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOParameter_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOParameter) null));
    }

    @Test
    public void VOParameter_convertToUp() throws Exception {
        org.oscm.vo.VOParameter oldVO = new org.oscm.vo.VOParameter();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOParameter newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOParameter_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOParameter) null));
    }

    @Test
    public void VOParameter_convertToApi() throws Exception {
        org.oscm.internal.vo.VOParameter oldVO = new org.oscm.internal.vo.VOParameter();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOParameter newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOParameterDefinition_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOParameterDefinition) null));
    }

    @Test
    public void VOParameterDefinition_convertToUp() throws Exception {
        org.oscm.vo.VOParameterDefinition oldVO = new org.oscm.vo.VOParameterDefinition();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOParameterDefinition newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOParameterDefinition_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOParameterDefinition) null));
    }

    @Test
    public void VOParameterDefinition_convertToApi() throws Exception {
        org.oscm.internal.vo.VOParameterDefinition oldVO = new org.oscm.internal.vo.VOParameterDefinition();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOParameterDefinition newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOParameterOption_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOParameterOption) null));
    }

    @Test
    public void VOParameterOption_convertToUp() throws Exception {
        org.oscm.vo.VOParameterOption oldVO = new org.oscm.vo.VOParameterOption();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOParameterOption newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOParameterOption_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOParameterOption) null));
    }

    @Test
    public void VOParameterOption_convertToApi() throws Exception {
        org.oscm.internal.vo.VOParameterOption oldVO = new org.oscm.internal.vo.VOParameterOption();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOParameterOption newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOPaymentInfo_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOPaymentInfo) null));
    }

    @Test
    public void VOPaymentInfo_convertToUp() throws Exception {
        org.oscm.vo.VOPaymentInfo oldVO = new org.oscm.vo.VOPaymentInfo();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOPaymentInfo newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOPaymentInfo_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOPaymentInfo) null));
    }

    @Test
    public void VOPaymentInfo_convertToApi() throws Exception {
        org.oscm.internal.vo.VOPaymentInfo oldVO = new org.oscm.internal.vo.VOPaymentInfo();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOPaymentInfo newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOPaymentType_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOPaymentType) null));
    }

    @Test
    public void VOPaymentType_convertToUp() throws Exception {
        org.oscm.vo.VOPaymentType oldVO = new org.oscm.vo.VOPaymentType();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOPaymentType newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOPaymentType_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOPaymentType) null));
    }

    @Test
    public void VOPaymentType_convertToApi() throws Exception {
        org.oscm.internal.vo.VOPaymentType oldVO = new org.oscm.internal.vo.VOPaymentType();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOPaymentType newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOPricedEvent_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOPricedEvent) null));
    }

    @Test
    public void VOPricedEvent_convertToUp() throws Exception {
        org.oscm.vo.VOPricedEvent oldVO = new org.oscm.vo.VOPricedEvent();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOPricedEvent newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOPricedEvent_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOPricedEvent) null));
    }

    @Test
    public void VOPricedEvent_convertToApi() throws Exception {
        org.oscm.internal.vo.VOPricedEvent oldVO = new org.oscm.internal.vo.VOPricedEvent();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOPricedEvent newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOPricedOption_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOPricedOption) null));
    }

    @Test
    public void VOPricedOption_convertToUp() throws Exception {
        org.oscm.vo.VOPricedOption oldVO = new org.oscm.vo.VOPricedOption();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOPricedOption newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOPricedOption_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOPricedOption) null));
    }

    @Test
    public void VOPricedOption_convertToApi() throws Exception {
        org.oscm.internal.vo.VOPricedOption oldVO = new org.oscm.internal.vo.VOPricedOption();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOPricedOption newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOPricedParameter_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOPricedParameter) null));
    }

    @Test
    public void VOPricedParameter_convertToUp() throws Exception {
        org.oscm.vo.VOPricedParameter oldVO = new org.oscm.vo.VOPricedParameter();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOPricedParameter newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOPricedParameter_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOPricedParameter) null));
    }

    @Test
    public void VOPricedParameter_convertToApi() throws Exception {
        org.oscm.internal.vo.VOPricedParameter oldVO = new org.oscm.internal.vo.VOPricedParameter();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOPricedParameter newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOPricedRole_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOPricedRole) null));
    }

    @Test
    public void VOPricedRole_convertToUp() throws Exception {
        org.oscm.vo.VOPricedRole oldVO = new org.oscm.vo.VOPricedRole();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOPricedRole newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOPricedRole_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOPricedRole) null));
    }

    @Test
    public void VOPricedRole_convertToApi() throws Exception {
        org.oscm.internal.vo.VOPricedRole oldVO = new org.oscm.internal.vo.VOPricedRole();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOPricedRole newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOPriceModel_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOPriceModel) null));
    }

    @Test
    public void VOPriceModel_convertToUp() throws Exception {
        org.oscm.vo.VOPriceModel oldVO = new org.oscm.vo.VOPriceModel();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOPriceModel newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOPriceModel_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOPriceModel) null));
    }

    @Test
    public void VOPriceModel_convertToApi() throws Exception {
        org.oscm.internal.vo.VOPriceModel oldVO = new org.oscm.internal.vo.VOPriceModel();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOPriceModel newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOPriceModelLocalization_convertToUp_NullCheck()
            throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOPriceModelLocalization) null));
    }

    @Test
    public void VOPriceModelLocalization_convertToUp() throws Exception {
        org.oscm.vo.VOPriceModelLocalization oldVO = new org.oscm.vo.VOPriceModelLocalization();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOPriceModelLocalization newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOPriceModelLocalization_convertToApi_NullCheck()
            throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOPriceModelLocalization) null));
    }

    @Test
    public void VOPriceModelLocalization_convertToApi() throws Exception {
        org.oscm.internal.vo.VOPriceModelLocalization oldVO = new org.oscm.internal.vo.VOPriceModelLocalization();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOPriceModelLocalization newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOReport_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOReport) null));
    }

    @Test
    public void VOReport_convertToUp() throws Exception {
        org.oscm.vo.VOReport oldVO = new org.oscm.vo.VOReport();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOReport newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOReport_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOReport) null));
    }

    @Test
    public void VOReport_convertToApi() throws Exception {
        org.oscm.internal.vo.VOReport oldVO = new org.oscm.internal.vo.VOReport();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOReport newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VORoleDefinition_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VORoleDefinition) null));
    }

    @Test
    public void VORoleDefinition_convertToUp() throws Exception {
        org.oscm.vo.VORoleDefinition oldVO = new org.oscm.vo.VORoleDefinition();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VORoleDefinition newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VORoleDefinition_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VORoleDefinition) null));
    }

    @Test
    public void VORoleDefinition_convertToApi() throws Exception {
        org.oscm.internal.vo.VORoleDefinition oldVO = new org.oscm.internal.vo.VORoleDefinition();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VORoleDefinition newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOService_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOService) null));
    }

    @Test
    public void VOService_convertToUp() throws Exception {
        org.oscm.vo.VOService oldVO = new org.oscm.vo.VOService();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOService newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOService_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOService) null));
    }

    @Test
    public void VOService_convertToApi() throws Exception {
        org.oscm.internal.vo.VOService oldVO = new org.oscm.internal.vo.VOService();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOService newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOServiceActivation_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOServiceActivation) null));
    }

    @Test
    public void VOServiceActivation_convertToUp() throws Exception {
        org.oscm.vo.VOServiceActivation oldVO = new org.oscm.vo.VOServiceActivation();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOServiceActivation newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOServiceActivation_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOServiceActivation) null));
    }

    @Test
    public void VOServiceActivation_convertToApi() throws Exception {
        org.oscm.internal.vo.VOServiceActivation oldVO = new org.oscm.internal.vo.VOServiceActivation();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOServiceActivation newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOServiceDetails_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOServiceDetails) null));
    }

    @Test
    public void VOServiceDetails_convertToUp() throws Exception {
        org.oscm.vo.VOServiceDetails oldVO = new org.oscm.vo.VOServiceDetails();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOServiceDetails newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOServiceDetails_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOServiceDetails) null));
    }

    @Test
    public void VOServiceDetails_convertToApi() throws Exception {
        org.oscm.internal.vo.VOServiceDetails oldVO = new org.oscm.internal.vo.VOServiceDetails();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOServiceDetails newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOServiceEntry_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOServiceEntry) null));
    }

    @Test
    public void VOServiceEntry_convertToUp() throws Exception {
        org.oscm.vo.VOServiceEntry oldVO = new org.oscm.vo.VOServiceEntry();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOServiceEntry newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOServiceEntry_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOServiceEntry) null));
    }

    @Test
    public void VOServiceEntry_convertToApi() throws Exception {
        org.oscm.internal.vo.VOServiceEntry oldVO = new org.oscm.internal.vo.VOServiceEntry();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOServiceEntry newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOServiceFeedback_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOServiceFeedback) null));
    }

    @Test
    public void VOServiceFeedback_convertToUp() throws Exception {
        org.oscm.vo.VOServiceFeedback oldVO = new org.oscm.vo.VOServiceFeedback();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOServiceFeedback newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOServiceFeedback_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOServiceFeedback) null));
    }

    @Test
    public void VOServiceFeedback_convertToApi() throws Exception {
        org.oscm.internal.vo.VOServiceFeedback oldVO = new org.oscm.internal.vo.VOServiceFeedback();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOServiceFeedback newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOServiceListResult_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOServiceListResult) null));
    }

    @Test
    public void VOServiceListResult_convertToUp() throws Exception {
        org.oscm.vo.VOServiceListResult oldVO = new org.oscm.vo.VOServiceListResult();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOServiceListResult newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOServiceListResult_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOServiceListResult) null));
    }

    @Test
    public void VOServiceListResult_convertToApi() throws Exception {
        org.oscm.internal.vo.VOServiceListResult oldVO = new org.oscm.internal.vo.VOServiceListResult();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOServiceListResult newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOServiceLocalization_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOServiceLocalization) null));
    }

    @Test
    public void VOServiceLocalization_convertToUp() throws Exception {
        org.oscm.vo.VOServiceLocalization oldVO = new org.oscm.vo.VOServiceLocalization();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOServiceLocalization newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOServiceLocalization_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOServiceLocalization) null));
    }

    @Test
    public void VOServiceLocalization_convertToApi() throws Exception {
        org.oscm.internal.vo.VOServiceLocalization oldVO = new org.oscm.internal.vo.VOServiceLocalization();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOServiceLocalization newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOServicePaymentConfiguration_convertToUp_NullCheck()
            throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOServicePaymentConfiguration) null));
    }

    @Test
    public void VOServicePaymentConfiguration_convertToUp() throws Exception {
        org.oscm.vo.VOServicePaymentConfiguration oldVO = new org.oscm.vo.VOServicePaymentConfiguration();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOServicePaymentConfiguration newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOServicePaymentConfiguration_convertToApi_NullCheck()
            throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOServicePaymentConfiguration) null));
    }

    @Test
    public void VOServicePaymentConfiguration_convertToApi() throws Exception {
        org.oscm.internal.vo.VOServicePaymentConfiguration oldVO = new org.oscm.internal.vo.VOServicePaymentConfiguration();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOServicePaymentConfiguration newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOServiceReview_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOServiceReview) null));
    }

    @Test
    public void VOServiceReview_convertToUp() throws Exception {
        org.oscm.vo.VOServiceReview oldVO = new org.oscm.vo.VOServiceReview();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOServiceReview newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOServiceReview_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOServiceReview) null));
    }

    @Test
    public void VOServiceReview_convertToApi() throws Exception {
        org.oscm.internal.vo.VOServiceReview oldVO = new org.oscm.internal.vo.VOServiceReview();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOServiceReview newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOSteppedPrice_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOSteppedPrice) null));
    }

    @Test
    public void VOSteppedPrice_convertToUp() throws Exception {
        org.oscm.vo.VOSteppedPrice oldVO = new org.oscm.vo.VOSteppedPrice();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOSteppedPrice newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOSteppedPrice_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOSteppedPrice) null));
    }

    @Test
    public void VOSteppedPrice_convertToApi() throws Exception {
        org.oscm.internal.vo.VOSteppedPrice oldVO = new org.oscm.internal.vo.VOSteppedPrice();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOSteppedPrice newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOSubscription_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOSubscription) null));
    }

    @Test
    public void VOSubscription_convertToUp() throws Exception {
        org.oscm.vo.VOSubscription oldVO = new org.oscm.vo.VOSubscription();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOSubscription newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOSubscription_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOSubscription) null));
    }

    @Test
    public void VOSubscription_convertToApi() throws Exception {
        org.oscm.internal.vo.VOSubscription oldVO = new org.oscm.internal.vo.VOSubscription();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOSubscription newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOSubscriptionDetails_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOSubscriptionDetails) null));
    }

    @Test
    public void VOSubscriptionDetails_convertToUp() throws Exception {
        org.oscm.vo.VOSubscriptionDetails oldVO = new org.oscm.vo.VOSubscriptionDetails();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOSubscriptionDetails newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOSubscriptionDetails_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOSubscriptionDetails) null));
    }

    @Test
    public void VOSubscriptionDetails_convertToApi() throws Exception {
        org.oscm.internal.vo.VOSubscriptionDetails oldVO = new org.oscm.internal.vo.VOSubscriptionDetails();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOSubscriptionDetails newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOSubscriptionIdAndOrganizations_convertToUp_NullCheck()
            throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOSubscriptionIdAndOrganizations) null));
    }

    @Test
    public void VOSubscriptionIdAndOrganizations_convertToUp() throws Exception {
        org.oscm.vo.VOSubscriptionIdAndOrganizations oldVO = new org.oscm.vo.VOSubscriptionIdAndOrganizations();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOSubscriptionIdAndOrganizations newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOSubscriptionIdAndOrganizations_convertToApi_NullCheck()
            throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOSubscriptionIdAndOrganizations) null));
    }

    @Test
    public void VOSubscriptionIdAndOrganizations_convertToApi()
            throws Exception {
        org.oscm.internal.vo.VOSubscriptionIdAndOrganizations oldVO = new org.oscm.internal.vo.VOSubscriptionIdAndOrganizations();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOSubscriptionIdAndOrganizations newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOTag_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOTag) null));
    }

    @Test
    public void VOTag_convertToUp() throws Exception {
        org.oscm.vo.VOTag oldVO = new org.oscm.vo.VOTag();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOTag newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOTag_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOTag) null));
    }

    @Test
    public void VOTag_convertToApi() throws Exception {
        org.oscm.internal.vo.VOTag oldVO = new org.oscm.internal.vo.VOTag();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOTag newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOTechnicalService_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOTechnicalService) null));
    }

    @Test
    public void VOTechnicalService_convertToUp() throws Exception {
        org.oscm.vo.VOTechnicalService oldVO = new org.oscm.vo.VOTechnicalService();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOTechnicalService newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOTechnicalService_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOTechnicalService) null));
    }

    @Test
    public void VOTechnicalService_convertToApi() throws Exception {
        org.oscm.internal.vo.VOTechnicalService oldVO = new org.oscm.internal.vo.VOTechnicalService();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOTechnicalService newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOTechnicalServiceOperation_convertToUp_NullCheck()
            throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOTechnicalServiceOperation) null));
    }

    @Test
    public void VOTechnicalServiceOperation_convertToUp() throws Exception {
        org.oscm.vo.VOTechnicalServiceOperation oldVO = new org.oscm.vo.VOTechnicalServiceOperation();
        VOInitializer.initialize(oldVO);

        VOServiceOperationParameter sop = new VOServiceOperationParameter();
        VOInitializer.initialize(sop, 0);
        oldVO.getOperationParameters().add(sop);

        sop = new VOServiceOperationParameter();
        VOInitializer.initialize(sop, 1);
        oldVO.getOperationParameters().add(sop);

        org.oscm.internal.vo.VOTechnicalServiceOperation newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOServiceOperationParameter_convertToUp_NullCheck()
            throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOServiceOperationParameter) null));
    }

    @Test
    public void VOServiceOperationParameter_convertToUp() throws Exception {
        org.oscm.vo.VOServiceOperationParameter oldVO = new org.oscm.vo.VOServiceOperationParameter();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOServiceOperationParameter newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOTechnicalServiceOperation_convertToApi_NullCheck()
            throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOTechnicalServiceOperation) null));
    }

    @Test
    public void VOTechnicalServiceOperation_convertToApi() throws Exception {
        org.oscm.internal.vo.VOTechnicalServiceOperation oldVO = new org.oscm.internal.vo.VOTechnicalServiceOperation();
        VOInitializer.initialize(oldVO);

        org.oscm.internal.vo.VOServiceOperationParameter sop = new org.oscm.internal.vo.VOServiceOperationParameter();
        VOInitializer.initialize(sop, 0);
        oldVO.getOperationParameters().add(sop);

        sop = new org.oscm.internal.vo.VOServiceOperationParameter();
        VOInitializer.initialize(sop, 1);
        oldVO.getOperationParameters().add(sop);

        org.oscm.vo.VOTechnicalServiceOperation newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOServiceOperationParameter_convertToApi_NullCheck()
            throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOServiceOperationParameter) null));
    }

    @Test
    public void VOServiceOperationParameter_convertToApi() throws Exception {
        org.oscm.internal.vo.VOServiceOperationParameter oldVO = new org.oscm.internal.vo.VOServiceOperationParameter();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOServiceOperationParameter newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOTriggerDefinition_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOTriggerDefinition) null));
    }

    @Test
    public void VOTriggerDefinition_convertToUp() throws Exception {
        org.oscm.vo.VOTriggerDefinition oldVO = new org.oscm.vo.VOTriggerDefinition();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOTriggerDefinition newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOTriggerDefinition_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOTriggerDefinition) null));
    }

    @Test
    public void VOTriggerDefinition_convertToApi() throws Exception {
        org.oscm.internal.vo.VOTriggerDefinition oldVO = new org.oscm.internal.vo.VOTriggerDefinition();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOTriggerDefinition newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOTriggerProcess_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOTriggerProcess) null));
    }

    @Test
    public void VOTriggerProcess_convertToUp() throws Exception {
        org.oscm.vo.VOTriggerProcess oldVO = new org.oscm.vo.VOTriggerProcess();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOTriggerProcess newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOTriggerProcess_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOTriggerProcess) null));
    }

    @Test
    public void VOTriggerProcess_convertToApi() throws Exception {
        org.oscm.internal.vo.VOTriggerProcess oldVO = new org.oscm.internal.vo.VOTriggerProcess();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOTriggerProcess newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOUda_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOUda) null));
    }

    @Test
    public void VOUda_convertToUp() throws Exception {
        org.oscm.vo.VOUda oldVO = new org.oscm.vo.VOUda();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOUda newVO = org.oscm.converter.api.VOConverter
                .reflectiveConvert(oldVO, mock(DataService.class));
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOUda_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOUda) null));
    }

    @Test
    public void VOUda_convertToApi() throws Exception {
        org.oscm.internal.vo.VOUda oldVO = new org.oscm.internal.vo.VOUda();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOUda newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOUdaDefinition_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOUdaDefinition) null));
    }

    @Test
    public void VOUdaDefinition_convertToUp() throws Exception {
        org.oscm.vo.VOUdaDefinition oldVO = new org.oscm.vo.VOUdaDefinition();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOUdaDefinition newVO = org.oscm.converter.api.VOConverter
                .reflectiveConvert(oldVO, mock(DataService.class));
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOUdaDefinition_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOUdaDefinition) null));
    }

    @Test
    public void VOUdaDefinition_convertToApi() throws Exception {
        org.oscm.internal.vo.VOUdaDefinition oldVO = new org.oscm.internal.vo.VOUdaDefinition();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOUdaDefinition newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOUsageLicense_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOUsageLicense) null));
    }

    @Test
    public void VOUsageLicense_convertToUp() throws Exception {
        org.oscm.vo.VOUsageLicense oldVO = new org.oscm.vo.VOUsageLicense();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOUsageLicense newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOUsageLicense_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOUsageLicense) null));
    }

    @Test
    public void VOUsageLicense_convertToApi() throws Exception {
        org.oscm.internal.vo.VOUsageLicense oldVO = new org.oscm.internal.vo.VOUsageLicense();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOUsageLicense newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOUser_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOUser) null));
    }

    @Test
    public void VOUser_convertToUp() throws Exception {
        org.oscm.vo.VOUser oldVO = new org.oscm.vo.VOUser();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOUser newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOUser_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOUser) null));
    }

    @Test
    public void VOUser_convertToApi() throws Exception {
        org.oscm.internal.vo.VOUser oldVO = new org.oscm.internal.vo.VOUser();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOUser newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOUserDetails_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOUserDetails) null));
    }

    @Test
    public void VOUserDetails_convertToUp() throws Exception {
        org.oscm.vo.VOUserDetails oldVO = new org.oscm.vo.VOUserDetails();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOUserDetails newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOUserDetails_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOUserDetails) null));
    }

    @Test
    public void VOUserDetails_convertToApi() throws Exception {
        org.oscm.internal.vo.VOUserDetails oldVO = new org.oscm.internal.vo.VOUserDetails();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOUserDetails newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOUserSubscription_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOUserSubscription) null));
    }

    @Test
    public void VOUserSubscription_convertToUp() throws Exception {
        org.oscm.vo.VOUserSubscription oldVO = new org.oscm.vo.VOUserSubscription();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOUserSubscription newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOUserSubscription_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOUserSubscription) null));
    }

    @Test
    public void VOUserSubscription_convertToApi() throws Exception {
        org.oscm.internal.vo.VOUserSubscription oldVO = new org.oscm.internal.vo.VOUserSubscription();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOUserSubscription newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOVatRate_convertToUp_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToUp((org.oscm.vo.VOVatRate) null));
    }

    @Test
    public void VOVatRate_convertToUp() throws Exception {
        org.oscm.vo.VOVatRate oldVO = new org.oscm.vo.VOVatRate();
        VOInitializer.initialize(oldVO);
        org.oscm.internal.vo.VOVatRate newVO = org.oscm.converter.api.VOConverter
                .convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOVatRate_convertToApi_NullCheck() throws Exception {
        assertNull(org.oscm.converter.api.VOConverter
                .convertToApi((org.oscm.internal.vo.VOVatRate) null));
    }

    @Test
    public void VOVatRate_convertToApi() throws Exception {
        org.oscm.internal.vo.VOVatRate oldVO = new org.oscm.internal.vo.VOVatRate();
        VOInitializer.initialize(oldVO);
        org.oscm.vo.VOVatRate newVO = org.oscm.converter.api.VOConverter
                .convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOTriggerProcessParameter_convertToApi() throws Exception {
        VOTriggerProcessParameter oldVO = new VOTriggerProcessParameter();
        VOInitializer.initialize(oldVO);
        oldVO.setValue(new VOService());
        org.oscm.vo.VOTriggerProcessParameter newVO = VOConverter.convertToApi(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOTriggerProcessParameter_convertToApi_NullCheck() throws Exception {
        assertNull(VOConverter.convertToApi((VOTriggerProcessParameter) null));
    }

    @Test
    public void VOTriggerProcessParameter_convertToUp() throws Exception {
        org.oscm.vo.VOTriggerProcessParameter oldVO = new org.oscm.vo.VOTriggerProcessParameter();
        VOInitializer.initialize(oldVO);
        oldVO.setValue(null); // randomly initialized xml is not valid then we have to set null
        VOTriggerProcessParameter newVO = VOConverter.convertToUp(oldVO);
        voAssert.assertValueObjects(oldVO, newVO);
    }

    @Test
    public void VOTriggerProcessParameter_convertToUp_NullCheck() throws Exception {
        assertNull(VOConverter.convertToUp((org.oscm.vo.VOTriggerProcessParameter) null));
    }

}
