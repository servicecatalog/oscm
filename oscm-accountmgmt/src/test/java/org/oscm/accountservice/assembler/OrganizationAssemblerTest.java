/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 02.03.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.test.BaseAdmUmTest;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOOperatorOrganization;
import org.oscm.internal.vo.VOOrganization;

/**
 * Test for the organization assembler.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class OrganizationAssemblerTest {

    private VOOrganization voOrganization;

    private long orgKey = 123L;
    private String description = "Organization Description";
    private boolean prefetchExecuted;

    private LocalizerFacade lf = new LocalizerFacade(null, description) {
        public String getText(long objkey, LocalizedObjectTypes objtype) {
            if (objkey == orgKey) {
                return description;
            }
            return null;
        }

        @Override
        public void prefetch(List<Long> keys, List<LocalizedObjectTypes> types) {
            prefetchExecuted = true;
        }

    };

    /**
     * Required as key and version cannot be set without assembler usage. Is
     * tested below nevertheless.
     */
    @Before
    public void setUp() {
        Organization org = new Organization();
        org.setKey(5L);
        org.setLocale("es");
        voOrganization = OrganizationAssembler.toVOOrganization(org, false, lf);
    }

    @Test
    public void testConstructor() throws Exception {
        new OrganizationAssembler();
    }

    @Test
    public void testToVOOrganizationNull() throws Exception {
        VOOrganization voOrg = OrganizationAssembler.toVOOrganization(null,
                false, lf);
        Assert.assertNull("Null parameter should cause return null", voOrg);
    }

    @Test
    public void testToVOOrganization() throws Exception {
        Organization org = new Organization();
        org.setKey(orgKey);
        org.setOrganizationId("orgId");
        org.setEmail("testuser@testdomain.test");
        org.setLocale("es");
        org.setName("Test organization");
        org.setPhone("0151/555555");
        org.setAddress("org address");
        org.setUrl("http://www.fujitsu.de");
        org.setSupportEmail("testSupport@mail.test");

        Organization supplier = new Organization();
        supplier.setOrganizationId("supplier id");

        OrganizationToRole supplierRelation = new OrganizationToRole();
        supplierRelation.setOrganization(supplier);
        supplierRelation.setOrganizationRole(new OrganizationRole(
                OrganizationRoleType.SUPPLIER));

        Set<OrganizationToRole> roles = new HashSet<OrganizationToRole>();
        roles.add(supplierRelation);

        OrganizationReference orgRef = new OrganizationReference(supplier, org,
                OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);

        OrganizationRole custRole = new OrganizationRole();
        custRole.setRoleName(OrganizationRoleType.CUSTOMER);

        List<OrganizationRefToPaymentType> addedPaymentTypes = new ArrayList<OrganizationRefToPaymentType>();

        for (String typeId : BaseAdmUmTest.PAYMENT_TYPE_IDS) {
            OrganizationRefToPaymentType orgToPt = new OrganizationRefToPaymentType();

            PaymentType type = new PaymentType();
            type.setPaymentTypeId(typeId);

            orgToPt.setOrganizationReference(orgRef);
            orgToPt.setOrganizationRole(custRole);
            orgToPt.setPaymentType(type);
            orgToPt.setUsedAsDefault(true);

            addedPaymentTypes.add(orgToPt);

        }

        orgRef.setPaymentTypes(addedPaymentTypes);

        org.getSources().add(orgRef);
        supplier.getTargets().add(orgRef);

        List<OrganizationRefToPaymentType> paymentTypes = org
                .getPaymentTypes(supplier.getOrganizationId());

        supplier.setGrantedRoles(roles);

        VOOrganization voOrg = OrganizationAssembler.toVOOrganization(org,
                false, lf);
        assertEquals("Key info missing", 123L, voOrg.getKey());
        assertEquals("Wrong version information", 0, voOrg.getVersion());
        assertEquals("Wrong organization id", "orgId",
                voOrg.getOrganizationId());
        assertEquals("Wrong email", "testuser@testdomain.test",
                voOrg.getEmail());
        assertEquals("Wrong locale", "es", voOrg.getLocale());
        assertEquals("Wrong organization name", "Test organization",
                voOrg.getName());
        assertEquals("Wrong phone", "0151/555555", voOrg.getPhone());
        assertEquals("Wrong address", "org address", voOrg.getAddress());
        assertEquals(org.getUrl(), voOrg.getUrl());

        assertEquals("PaymentTypes Count", addedPaymentTypes.size(),
                paymentTypes.size());
        assertEquals("Wrong supportEmail", "testSupport@mail.test",
                voOrg.getSupportEmail());

        assertEquals(description, voOrg.getDescription());
    }

    @Test
    public void toVOOrganization_Supplier_RevenueShare() throws Exception {
        Organization supplier = createOrganizationWithRevenueShare("orgId",
                "Test Supplier", orgKey);

        // when
        VOOrganization voOrg = OrganizationAssembler.toVOOrganization(supplier,
                false, lf);

        // then
        assertEquals("Key info missing", supplier.getKey(), voOrg.getKey());
        assertEquals("Wrong version information", 0, voOrg.getVersion());
        assertEquals("Wrong organization id", supplier.getOrganizationId(),
                voOrg.getOrganizationId());
        assertEquals("Wrong email", supplier.getEmail(), voOrg.getEmail());
        assertEquals("Wrong locale", supplier.getLocale(), voOrg.getLocale());
        assertEquals("Wrong organization name", supplier.getName(),
                voOrg.getName());
        assertEquals("Wrong phone", supplier.getPhone(), voOrg.getPhone());
        assertEquals("Wrong address", supplier.getAddress(), voOrg.getAddress());
        assertEquals("Wrong URL", supplier.getUrl(), voOrg.getUrl());
        assertEquals("Wrong supportEmail", supplier.getSupportEmail(),
                voOrg.getSupportEmail());
        assertEquals(description, voOrg.getDescription());
        assertEquals("Wrong operator revenue share", supplier
                .getOperatorPriceModel().getRevenueShare(),
                voOrg.getOperatorRevenueShare());
    }

    @Test
    public void toVOOrganizations() throws Exception {
        List<Organization> orgs = new ArrayList<Organization>();
        for (int i = 1; i <= 3; i++) {
            Organization org = new Organization();
            org.setKey(i);
            org.setOrganizationId("orgId" + (i));
            org.setEmail("testuser" + i + "@testdomain.test");
            org.setLocale("en");
            org.setName("Test Supplier " + i);
            org.setPhone("0151/555555");
            org.setAddress("Supplier address" + i);

            org.setSupportEmail("testSupport" + i + "@mail.test");

            orgs.add(org);
        }
        List<VOOrganization> result = OrganizationAssembler.toVOOrganizations(
                orgs, lf);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(prefetchExecuted);
        Map<Long, VOOrganization> resultMap = new HashMap<Long, VOOrganization>();
        for (VOOrganization org : result) {
            resultMap.put(Long.valueOf(org.getKey()), org);
        }
        for (int i = 1; i <= 3; i++) {
            VOOrganization org = resultMap.remove(Long.valueOf(i));
            assertEquals(i, org.getKey());
            assertEquals("orgId" + i, org.getOrganizationId());
            assertEquals("testuser" + i + "@testdomain.test", org.getEmail());
            assertEquals("en", org.getLocale());
            assertEquals("Test Supplier " + i, org.getName());
            assertEquals("0151/555555", org.getPhone());
            assertEquals("Supplier address" + i, org.getAddress());
            assertEquals("Wrong supportEmail",
                    "testSupport" + i + "@mail.test", org.getSupportEmail());
        }
    }

    @Test
    public void testToVOOperatorOrganization() throws Exception {
        // given
        Organization supplier = createOrganizationWithRevenueShare("orgId",
                "Test Supplier", 1L);

        Organization platformOp = new Organization();
        platformOp.setOrganizationId(OrganizationRoleType.PLATFORM_OPERATOR
                .name());

        OrganizationToRole operatorRelation = new OrganizationToRole();
        operatorRelation.setOrganization(supplier);
        operatorRelation.setOrganizationRole(new OrganizationRole(
                OrganizationRoleType.PLATFORM_OPERATOR));

        Set<OrganizationToRole> roles = new HashSet<OrganizationToRole>();
        roles.add(operatorRelation);

        OrganizationReference orgRef = new OrganizationReference(platformOp,
                supplier,
                OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER);

        OrganizationRole supplRole = new OrganizationRole();
        supplRole.setRoleName(OrganizationRoleType.SUPPLIER);

        List<OrganizationRefToPaymentType> addedPaymentTypes = new ArrayList<OrganizationRefToPaymentType>();

        for (String typeId : BaseAdmUmTest.PAYMENT_TYPE_IDS) {
            OrganizationRefToPaymentType orgToPt = new OrganizationRefToPaymentType();

            PaymentType type = new PaymentType();
            type.setPaymentTypeId(typeId);

            orgToPt.setOrganizationReference(orgRef);
            orgToPt.setOrganizationRole(supplRole);
            orgToPt.setPaymentType(type);
            orgToPt.setUsedAsDefault(true);

            addedPaymentTypes.add(orgToPt);

        }

        orgRef.setPaymentTypes(addedPaymentTypes);

        supplier.getSources().add(orgRef);
        platformOp.getTargets().add(orgRef);

        supplier.setGrantedRoles(roles);

        // when
        VOOperatorOrganization voOrg = OrganizationAssembler
                .toVOOperatorOrganization(supplier, false, lf);

        // then
        assertEquals("Key info missing", supplier.getKey(), voOrg.getKey());
        assertEquals("Wrong version information", 0, voOrg.getVersion());
        assertEquals("Wrong organization id", supplier.getOrganizationId(),
                voOrg.getOrganizationId());
        assertEquals("Wrong email", supplier.getEmail(), voOrg.getEmail());
        assertEquals("Wrong locale", supplier.getLocale(), voOrg.getLocale());
        // assertEquals("Wrong psp Identifier", "PSP_ID",
        // voOrg.getPspIdentifier());
        assertEquals("Wrong organization name", supplier.getName(),
                voOrg.getName());
        assertEquals("Wrong phone", supplier.getPhone(), voOrg.getPhone());
        assertEquals("Wrong address", supplier.getAddress(), voOrg.getAddress());
        assertEquals("Wrong supportEmail", supplier.getSupportEmail(),
                voOrg.getSupportEmail());
        assertEquals("Wrong operator revenue share", supplier
                .getOperatorPriceModel().getRevenueShare(),
                voOrg.getOperatorRevenueShare());

        assertEquals("PaymentTypes Count", addedPaymentTypes.size(), voOrg
                .getPaymentTypes().size());
    }

    private Organization createOrganizationWithRevenueShare(String orgId,
            String orgName, long key) {
        Organization org = new Organization();
        org.setKey(key);
        org.setOrganizationId(orgId);
        org.setEmail("testuser@testdomain.test");
        org.setLocale("es");
        org.setName(orgName);
        org.setPhone("0151/555555");
        org.setAddress("Org address");
        org.setSupportEmail("testSupport@mail.test");

        RevenueShareModel operatorPriceModel = new RevenueShareModel();
        operatorPriceModel
                .setRevenueShareModelType(RevenueShareModelType.OPERATOR_REVENUE_SHARE);
        operatorPriceModel.setRevenueShare(BigDecimal.valueOf(35));
        org.setOperatorPriceModel(operatorPriceModel);

        return org;
    }

    @Test(expected = ValidationException.class)
    public void testToCustomerMissingLocale() throws Exception {
        voOrganization.setLocale(null);
        OrganizationAssembler.toCustomer(voOrganization);
    }

    private void setUpFields() {
        voOrganization.setAddress("some address");
        voOrganization.setEmail("testUser@domain.uk");
        voOrganization.setPhone("012345/67689");
        voOrganization.setLocale("de");
        voOrganization.setName("org name");
        voOrganization.setOrganizationId("orgId");
        voOrganization.setDomicileCountry("DE");
        voOrganization.setUrl("http://www.tom.com");
    }

    @Test
    public void testToCustomer() throws Exception {
        setUpFields();

        Organization org = OrganizationAssembler.toCustomer(voOrganization);

        assertEquals("Wrong address data", "some address", org.getAddress());
        assertEquals("Wrong email", "testUser@domain.uk", org.getEmail());
        assertEquals("Wrong phone", "012345/67689", org.getPhone());
        assertEquals("Wrong locale", "de", org.getLocale());
        assertEquals("Wrong name", "org name", org.getName());
        Assert.assertNull(
                "Org id must not be set, is created by system and then fix",
                org.getOrganizationId());
    }

    @Test(expected = ValidationException.class)
    public void testToVendor_MandatoryEmail() throws Exception {
        setUpFields();
        voOrganization.setEmail(null);

        OrganizationAssembler.toVendor(voOrganization);
    }

    @Test(expected = ValidationException.class)
    public void testToVendor_MandatoryPhone() throws Exception {
        setUpFields();
        voOrganization.setPhone(null);

        OrganizationAssembler.toVendor(voOrganization);
    }

    @Test(expected = ValidationException.class)
    public void testToVendor_MandatoryUrl() throws Exception {
        setUpFields();
        voOrganization.setUrl(null);

        OrganizationAssembler.toVendor(voOrganization);
    }

    @Test(expected = ValidationException.class)
    public void testToVendor_MandatoryName() throws Exception {
        setUpFields();
        voOrganization.setName(null);

        OrganizationAssembler.toVendor(voOrganization);
    }

    @Test(expected = ValidationException.class)
    public void testToVendor_MandatoryAddress() throws Exception {
        setUpFields();
        voOrganization.setAddress(null);

        OrganizationAssembler.toVendor(voOrganization);
    }

    @Test
    public void toVendor_WrongOperatorRevenueShare() throws Exception {
        // given
        setUpFields();
        voOrganization.setOperatorRevenueShare(BigDecimal.valueOf(200));

        try {
            // when
            OrganizationAssembler.toVendor(voOrganization);
            fail("ValidationException expected");
        } catch (ValidationException e) {
            // then
            assertEquals(ReasonEnum.VALUE_NOT_IN_RANGE, e.getReason());
            assertEquals("operatorRevenueShare", e.getMember());
        }
    }

    private Organization setUpOrganization() {
        Organization baseOrg = new Organization();
        baseOrg.setKey(1000L);
        baseOrg.setOrganizationId("oldOrgId");

        voOrganization.setKey(1000L);
        setUpFields();
        return baseOrg;
    }

    @Test
    public void testUpdateCustomer() throws Exception {
        Organization baseOrg = setUpOrganization();

        Organization org = OrganizationAssembler.updateCustomer(baseOrg,
                voOrganization);

        assertEquals("Wrong address data", "some address", org.getAddress());
        assertEquals("Wrong email", "testUser@domain.uk", org.getEmail());
        assertEquals("Wrong phone", "012345/67689", org.getPhone());
        assertEquals("Wrong locale", "de", org.getLocale());
        assertEquals("Wrong name", "org name", org.getName());
        assertEquals("Org id must not be modified", "oldOrgId",
                org.getOrganizationId());
    }

    @Test(expected = ValidationException.class)
    public void testUpdateVendor_MandatoryEmail() throws Exception {
        Organization baseOrg = setUpOrganization();
        voOrganization.setEmail(null);

        OrganizationAssembler.updateVendor(baseOrg, voOrganization);
    }

    @Test(expected = ValidationException.class)
    public void testUpdateVendor_MandatoryPhone() throws Exception {
        Organization baseOrg = setUpOrganization();
        voOrganization.setPhone(null);

        OrganizationAssembler.updateVendor(baseOrg, voOrganization);
    }

    @Test(expected = ValidationException.class)
    public void testUpdateVendor_MandatoryUrl() throws Exception {
        Organization baseOrg = setUpOrganization();
        voOrganization.setUrl(null);

        OrganizationAssembler.updateVendor(baseOrg, voOrganization);
    }

    @Test(expected = ValidationException.class)
    public void testUpdateVendor_MandatoryName() throws Exception {
        Organization baseOrg = setUpOrganization();
        voOrganization.setName(null);

        OrganizationAssembler.updateVendor(baseOrg, voOrganization);
    }

    @Test(expected = ValidationException.class)
    public void testUpdateVendor_MandatoryAddress() throws Exception {
        Organization baseOrg = setUpOrganization();
        voOrganization.setAddress(null);

        OrganizationAssembler.updateVendor(baseOrg, voOrganization);
    }

    @Test
    public void updateVendor_WrongOperatorRevenueShare() throws Exception {
        // given
        Organization baseOrg = setUpOrganization();
        voOrganization.setOperatorRevenueShare(BigDecimal.valueOf(-10));

        try {
            // when
            OrganizationAssembler.updateVendor(baseOrg, voOrganization);
            fail("ValidationException expected");
        } catch (ValidationException e) {
            // then
            assertEquals(ReasonEnum.VALUE_NOT_IN_RANGE, e.getReason());
            assertEquals("operatorRevenueShare", e.getMember());
        }
    }

    @Test
    public void testToCustomerValidateEmail() throws Exception {
        voOrganization.setEmail("testUser");
        try {
            OrganizationAssembler.toCustomer(voOrganization);
            Assert.fail("Wrong email must lead to validation failure!");
        } catch (ValidationException e) {
            assertEquals("wrong field type",
                    OrganizationAssembler.FIELD_NAME_EMAIL, e.getMember());
        }
    }

    @Test
    public void testToOrganizationValidateSupportEmail() throws Exception {
        voOrganization.setSupportEmail("testUser");
        try {
            OrganizationAssembler.toCustomer(voOrganization);
            Assert.fail("Wrong email must lead to validation failure!");
        } catch (ValidationException e) {
            assertEquals("wrong field type",
                    OrganizationAssembler.FIELD_NAME_SUPPORT_EMAIL,
                    e.getMember());
        }
    }

    @Test
    public void testToCustomerValidateLocale() throws Exception {
        voOrganization.setLocale("invalidLocaleThatIsMuchTooLong");
        try {
            OrganizationAssembler.toCustomer(voOrganization);
            Assert.fail("Wrong locale must lead to validation failure!");
        } catch (ValidationException e) {
            assertEquals("wrong field type",
                    OrganizationAssembler.FIELD_NAME_LOCALE, e.getMember());
        }
    }

    @Test
    public void testToCustomerValidateName() throws Exception {
        voOrganization
                .setName("invalidNameThatIsMuchTooLonggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg");
        try {
            OrganizationAssembler.toCustomer(voOrganization);
            Assert.fail("Wrong name must lead to validation failure!");
        } catch (ValidationException e) {
            assertEquals("wrong field type",
                    OrganizationAssembler.FIELD_NAME_NAME, e.getMember());
        }
    }

    @Test
    public void testToCustomerValidatePhone() throws Exception {
        voOrganization
                .setPhone("invalidPhoneThatIsMuchTooLonggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg");
        try {
            OrganizationAssembler.toCustomer(voOrganization);
            Assert.fail("Wrong phone must lead to validation failure!");
        } catch (ValidationException e) {
            assertEquals("wrong field type",
                    OrganizationAssembler.FIELD_NAME_PHONE, e.getMember());
        }
    }

    @Test
    public void testToCustomerValidate_UrlNotSet() {
        try {
            // null value
            voOrganization.setUrl(null);
            Organization o = OrganizationAssembler.toCustomer(voOrganization);
            assertNull(o.getUrl());

            // empty string
            voOrganization.setUrl("");
            o = OrganizationAssembler.toCustomer(voOrganization);
            assertEquals("", o.getUrl());
        } catch (ValidationException e) {
            Assert.fail("Url is optional and can be unset");
        }
    }

    @Test
    public void testToCustomerValidate_WrongUrl() {
        voOrganization.setUrl("htp://www.fujitsu.com");
        try {
            OrganizationAssembler.toCustomer(voOrganization);
            Assert.fail("Invalid url must lead to validation error.");
        } catch (ValidationException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testToCustomerValidate_DescriptionTooLong() {
        voOrganization
                .setDescription("tolongdescriptiontolongdescriptiontolongdescriptiontolongdescriptiontolongdescriptiontolongdescriptiontolongdescriptiontolongdescriptiontolongdescriptiontolongdescriptiontolongdescriptiontolongdescriptiontolongdescriptiontolongdescriptiontolongdescriptiontolongdescription");
        try {
            OrganizationAssembler.toCustomer(voOrganization);
            Assert.fail("To long description must lead to validation error.");
        } catch (ValidationException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testToCustomerValidate_DescriptionNotSet() {
        try {
            // null value
            voOrganization.setDescription(null);
            OrganizationAssembler.toCustomer(voOrganization);

            // not set
            voOrganization.setDescription("");
            OrganizationAssembler.toCustomer(voOrganization);
        } catch (ValidationException e) {
            Assert.fail("Description is optional and can be unset");
        }
    }

    @Test
    public void testToCustomerValidateAddress() throws Exception {
        String address = "invalidAddressThatIsMuchTooLonggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg";
        address += address + address;
        voOrganization.setAddress(address);
        try {
            OrganizationAssembler.toCustomer(voOrganization);
            Assert.fail("Wrong address must lead to validation failure!");
        } catch (ValidationException e) {
            assertEquals("wrong field type",
                    OrganizationAssembler.FIELD_NAME_ADDRESS, e.getMember());
        }
    }

    @Test
    public void testUpdatePSPIdentifier() throws Exception {
        // Organization org = OrganizationAssembler.updatePSPIdentifier(new
        // Organization(),
        // "psp identifier");
        // assertEquals("psp identifier", org.getPspIdentifier());
    }

    @Test
    public void testUpdatePSPIdentifierNullValue() throws Exception {
        // Organization org = OrganizationAssembler.updatePSPIdentifier(new
        // Organization(),
        // null);
        // assertEquals(null, org.getPspIdentifier());
    }

    @Test
    public void testToCustomerValidate_InvalidLocale() {
        try {
            // ch_de value
            voOrganization.setLocale("de");
            OrganizationAssembler.toCustomer(voOrganization);
        } catch (ValidationException e) {
            Assert.fail("Invalid ValidationException thrown");
        }

        try {
            // invalid value set
            voOrganization.setLocale("de_ja");
            OrganizationAssembler.toCustomer(voOrganization);
        } catch (ValidationException e) {
            assertTrue(e.getLocalizedMessage().contains(
                    ValidationException.ReasonEnum.INVALID_LOCALE.name()));
        }
    }

    @Test(expected = ValidationException.class)
    public void testToVendor_ValidationException() throws Exception {
        VOOrganization voOrganization = new VOOrganization();
        voOrganization.setKey(2);
        OrganizationAssembler.toVendor(voOrganization);
    }

    @Test
    public void testToVendor_NoException() throws Exception {
        VOOrganization voOrganization = new VOOrganization();
        voOrganization.setEmail("testemail@testdomain.com");
        voOrganization.setPhone("2342342");
        voOrganization.setUrl("http://www.test.com");
        voOrganization.setName("testname");
        voOrganization.setAddress("testaddress");
        voOrganization.setLocale("de");
        voOrganization.setDistinguishedName("testDN");
        voOrganization.setDescription("testdescription");
        OrganizationAssembler.toVendor(voOrganization);
    }

    @Test
    public void toVOOrganization_listings() {
        // given
        Organization org = new Organization();
        org.setKey(orgKey);
        org.setOrganizationId("orgId");
        org.setEmail("testuser@testdomain.test");
        org.setLocale("es");
        org.setName("Test organization");
        org.setPhone("0151/555555");
        org.setAddress("org address");
        org.setUrl("http://www.fujitsu.de");
        org.setSupportEmail("testSupport@mail.test");
        // when
        VOOrganization result = OrganizationAssembler.toVOOrganization(org);
        // then
        assertEquals(org.getOrganizationId(), result.getOrganizationId());
        assertEquals(org.getName(), result.getName());
        assertEquals(org.getAddress(), result.getAddress());
        assertFalse(org.getEmail().equals(result.getEmail()));

    }

    @Test
    public void testToVOOrganizationWithDiscount_NullOrganization()
            throws Exception {
        VOOrganization voOrganization = OrganizationAssembler
                .toVOOrganizationWithDiscount(null, false, null, null);
        assertNull(voOrganization);
    }
}
