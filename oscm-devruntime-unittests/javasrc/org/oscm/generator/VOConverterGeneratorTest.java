/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Aleh Khomich                 
 *                                                                              
 *  Creation Date: 10.11.2010                                                     
 *                                                                              
 *******************************************************************************/

package org.oscm.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for VOConverterGenerator.
 * 
 * @author Aleh Khomich.
 * 
 */
public class VOConverterGeneratorTest {

    final String VO_LIST_FILE = "javares/volist.properties";
    private String propertiesFileName = null;

    @Before
    public void setUp() {
        // prepare file name for properties
        File currentDir = new File(".");
        String canonicalPath = null;
        try {
            canonicalPath = currentDir.getCanonicalPath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        propertiesFileName = canonicalPath + "/" + VO_LIST_FILE;
    }

    @Test
    public void testGetVOList() {
        final VOConverterGenerator generator = new VOConverterGenerator();

        ArrayList<String> voList = new ArrayList<String>();
        List<String> actualList = generator.getVOList(propertiesFileName);
        // remove comment
        actualList.remove(0);
        voList.add("VOBillingContact");
        voList.add("VOCustomerService");
        voList.add("VOEventDefinition");
        voList.add("VOGatheredEvent");
        voList.add("VOImageResource");
        voList.add("VOInstanceInfo");
        voList.add("VOLocalizedText");
        voList.add("VOOrganization");
        voList.add("VOOrganizationPaymentConfiguration");
        voList.add("VOParameterDefinition");
        voList.add("VOParameter");
        voList.add("VOParameterOption");
        voList.add("VOPaymentType");
        voList.add("VOPricedEvent");
        voList.add("VOPricedOption");
        voList.add("VOPricedParameter");
        voList.add("VOPricedRole");
        voList.add("VOPriceModel");
        voList.add("VOPriceModelLocalization");
        voList.add("VOReport");
        voList.add("VORoleDefinition");
        voList.add("VOServiceDetails");
        voList.add("VOService");
        voList.add("VOServiceLocalization");
        voList.add("VOSteppedPrice");
        voList.add("VOSubscriptionDetails");
        voList.add("VOSubscriptionIdAndOrganizations");
        voList.add("VOSubscription");
        voList.add("VOTechnicalService");
        voList.add("VOTechnicalServiceOperation");
        voList.add("VOTriggerDefinition");
        voList.add("VOTriggerProcess");
        voList.add("VOUdaDefinition");
        voList.add("VOUda");
        voList.add("VOUsageLicense");
        voList.add("VOUserDetails");
        voList.add("VOUser");
        voList.add("VOUserSubscription");

        for (int i = 0; i < voList.size(); i++) {

            String expected = voList.get(i);
            String actual = actualList.get(i);
            System.out.println(actual);
            Assert.assertEquals("Wrong VO", expected, actual);
        }

    }

    @Test
    public void testGetVOListFileName() {
        final VOConverterGenerator generator = new VOConverterGenerator();

        String actual = generator.getVOListFileName();
        Assert.assertEquals("Wrong name of file", propertiesFileName, actual);
    }

    @Test
    public void testUpperFirstLetter() {
        final VOConverterGenerator generator = new VOConverterGenerator();

        String actual = generator.upperFirstLetter("abcd");
        Assert.assertEquals("Wrong convertion to upper case the first letter",
                "Abcd", actual);
    }

    @Test
    public void testGetVOMethods() {
        final VOConverterGenerator generator = new VOConverterGenerator();

        ArrayList<String> expectedList = new ArrayList<String>();
        expectedList.add("getDescription");
        expectedList.add("getCurrency");
        expectedList.add("getConsideredEvents");
        expectedList.add("setConsideredEvents");
        expectedList.add("setDescription");
        expectedList.add("isChargeable");
        expectedList.add("setType");
        expectedList.add("getType");
        expectedList.add("getPeriod");
        expectedList.add("getPricePerPeriod");
        expectedList.add("getPricePerUserAssignment");
        expectedList.add("getCurrencyISOCode");
        expectedList.add("setPeriod");
        expectedList.add("setPricePerPeriod");
        expectedList.add("setPricePerUserAssignment");
        expectedList.add("setCurrencyISOCode");
        expectedList.add("getOneTimeFee");
        expectedList.add("setOneTimeFee");
        expectedList.add("getSelectedParameters");
        expectedList.add("setSelectedParameters");
        expectedList.add("getRoleSpecificUserPrices");
        expectedList.add("setRoleSpecificUserPrices");
        expectedList.add("setSteppedPrices");
        expectedList.add("getSteppedPrices");
        expectedList.add("getKey");
        expectedList.add("getVersion");
        expectedList.add("setKey");
        expectedList.add("setVersion");
        expectedList.add("hashCode");
        expectedList.add("getClass");
        expectedList.add("wait");
        expectedList.add("wait");
        expectedList.add("wait");
        expectedList.add("equals");
        expectedList.add("notify");
        expectedList.add("notifyAll");
        expectedList.add("toString");
        expectedList.add("setLicense");
        expectedList.add("getLicense");
        expectedList.add("getFreePeriod");
        expectedList.add("setFreePeriod");
        expectedList.add("setPresentationDataType");
        expectedList.add("getPresentationDataType");  
        expectedList.add("getPresentation");
        expectedList.add("setPresentation");
        expectedList.add("setExternal");
        expectedList.add("isExternal");
        expectedList.add("getAsJSON");
        expectedList.add("getUuid");
        expectedList.add("setUuid");
        expectedList.add("isFree");
        expectedList.add("isPricePerPeriodSet");
        expectedList.add("isPricePerUserAssignmentSet");
        expectedList.add("isOneTimeFeeSet");
        expectedList.add("setRelatedSubscription");
        expectedList.add("isRelatedSubscription");

        Collections.sort(expectedList);

        List<String> actualList = generator.getVOMethods("VOPriceModel");
        Collections.sort(actualList);

        Assert.assertEquals("Not the same size of lists", expectedList.size(),
                actualList.size());

        for (int i = 0; i < actualList.size(); i++) {
            String actual = actualList.get(i);
            String expected = expectedList.get(i);
            Assert.assertEquals("No method in VO ", expected, actual);
        }
    }

    @Test
    public void testGetVOProperties() {
        final VOConverterGenerator generator = new VOConverterGenerator();

        List<VOPropertyDescription> actualList = generator
                .getVOProperties("VOUser");

        ArrayList<VOPropertyDescription> voList = new ArrayList<VOPropertyDescription>();
        VOPropertyDescription descrBaseKey = new VOPropertyDescription();
        descrBaseKey.setName("key");
        descrBaseKey.setGenericType("long");
        descrBaseKey.setType("long");
        descrBaseKey.setTypeParameter("");
        descrBaseKey.setTypeParameterWithoutPackage("");
        voList.add(descrBaseKey);

        VOPropertyDescription descrBaseVersion = new VOPropertyDescription();
        descrBaseVersion.setName("version");
        descrBaseVersion.setGenericType("int");
        descrBaseVersion.setType("int");
        descrBaseVersion.setTypeParameter("");
        descrBaseVersion.setTypeParameterWithoutPackage("");
        voList.add(descrBaseVersion);

        VOPropertyDescription tmp1 = new VOPropertyDescription();
        tmp1.setName("organizationId");
        tmp1.setGenericType("class java.lang.String");
        tmp1.setType("class");
        tmp1.setTypeParameter("java.lang.String");
        voList.add(tmp1);

        VOPropertyDescription tmp2 = new VOPropertyDescription();
        tmp2.setName("organizationName");
        tmp2.setGenericType("class java.lang.String");
        tmp2.setType("class");
        tmp2.setTypeParameter("java.lang.String");
        voList.add(tmp2);

        VOPropertyDescription tmp3 = new VOPropertyDescription();
        tmp3.setName("userId");
        tmp3.setGenericType("class java.lang.String");
        tmp3.setType("class");
        tmp3.setTypeParameter("java.lang.String");
        voList.add(tmp3);

        VOPropertyDescription tmp4 = new VOPropertyDescription();
        tmp4.setName("status");
        tmp4.setGenericType("class org.oscm.types.enumtypes.UserAccountStatus");
        tmp4.setType("class");
        tmp4.setTypeParameter("org.oscm.types.enumtypes.UserAccountStatus");
        voList.add(tmp4);

        VOPropertyDescription tmp5 = new VOPropertyDescription();
        tmp5.setName("organizationRoles");
        tmp5.setGenericType("java.util.Set<org.oscm.types.enumtypes.OrganizationRoleType>");
        tmp5.setType("java.util.Set");
        tmp5.setTypeParameter("org.oscm.types.enumtypes.OrganizationRoleType");
        voList.add(tmp5);

        for (int i = 0; i < 4; i++) {
            String expected = voList.get(i).getName();
            String actual = actualList.get(i).getName();
            Assert.assertEquals("Wrong name property on step " + i, expected,
                    actual);

            expected = voList.get(i).getGenericType();
            actual = actualList.get(i).getGenericType();
            Assert.assertEquals("Wrong GenericType property on step " + i,
                    expected, actual);

            expected = voList.get(i).getType();
            actual = actualList.get(i).getType();
            Assert.assertEquals("Wrong Type property on step " + i, expected,
                    actual);

            expected = voList.get(i).getTypeParameter();
            actual = actualList.get(i).getTypeParameter();
            Assert.assertEquals("Wrong TypeParameter property on step " + i,
                    expected, actual);
        }
    }

}
