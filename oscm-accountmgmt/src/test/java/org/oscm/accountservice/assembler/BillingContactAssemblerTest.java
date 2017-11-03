/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.accountservice.assembler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.BillingContact;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOBillingContact;

/**
 * @author weiser
 * 
 */
@SuppressWarnings("boxing")
public class BillingContactAssemblerTest {

    private BillingContact billingContact;
    private VOBillingContact voBillingContact;

    @Before
    public void setUp() {
        billingContact = new BillingContact();
        billingContact.setEmail("mail@test.de");
        billingContact.setKey(23456);
        billingContact.setAddress("companyAddress");
        billingContact.setCompanyName("Name");
        billingContact.setOrgAddressUsed(false);
        billingContact.setBillingContactId("billingContactId");

        voBillingContact = new VOBillingContact();
        voBillingContact.setAddress("address");
        voBillingContact.setCompanyName("companyName");
        voBillingContact.setEmail("test@mail.de");
        voBillingContact.setOrgAddressUsed(true);
        voBillingContact.setKey(billingContact.getKey());
        voBillingContact.setId("id");
    }

    @Test(expected = ValidationException.class)
    public void updateBillingContact_NoId() throws Exception {
        voBillingContact.setId(null);
        BillingContactAssembler.updateBillingContact(billingContact,
                voBillingContact);
    }

    @Test(expected = ValidationException.class)
    public void updateBillingContact_EmptyName() throws Exception {
        voBillingContact.setId("   ");
        BillingContactAssembler.updateBillingContact(billingContact,
                voBillingContact);
    }

    @Test
    public void updateBillingContact() throws Exception {
        BillingContact bc = BillingContactAssembler.updateBillingContact(
                billingContact, voBillingContact);
        Assert.assertNotNull("bc is null", bc);
        Assert.assertEquals(voBillingContact.getAddress(), bc.getAddress());
        Assert.assertEquals(voBillingContact.getCompanyName(),
                bc.getCompanyName());
        Assert.assertEquals(voBillingContact.getEmail(), bc.getEmail());
        Assert.assertEquals(voBillingContact.isOrgAddressUsed(),
                bc.isOrgAddressUsed());
        Assert.assertEquals(voBillingContact.getId(), bc.getBillingContactId());
    }

    @Test(expected = ValidationException.class)
    public void updateBillingContact_EmailNull() throws Exception {
        voBillingContact.setEmail(null);
        BillingContactAssembler.updateBillingContact(billingContact,
                voBillingContact);
    }

    @Test(expected = ValidationException.class)
    public void updateBillingContact_CompanyNameNull() throws Exception {
        voBillingContact.setCompanyName(null);
        BillingContactAssembler.updateBillingContact(billingContact,
                voBillingContact);
    }

    @Test(expected = ValidationException.class)
    public void updateBillingContact_AddressNull() throws Exception {
        voBillingContact.setAddress(null);
        BillingContactAssembler.updateBillingContact(billingContact,
                voBillingContact);
    }

    @Test(expected = ValidationException.class)
    public void updateBillingContact_CompanyNameEmpty() throws Exception {
        voBillingContact.setCompanyName("   ");
        BillingContactAssembler.updateBillingContact(billingContact,
                voBillingContact);
    }

    @Test(expected = ValidationException.class)
    public void updateBillingContact_AddressEmpty() throws Exception {
        voBillingContact.setAddress("   ");
        BillingContactAssembler.updateBillingContact(billingContact,
                voBillingContact);
    }

    @Test(expected = ValidationException.class)
    public void updateBillingContact_EmailEmpty() throws Exception {
        voBillingContact.setEmail("   ");
        BillingContactAssembler.updateBillingContact(billingContact,
                voBillingContact);
    }

    @Test(expected = ValidationException.class)
    public void updateBillingContact_EmailInvalid() throws Exception {
        voBillingContact.setEmail("invalidmail");
        BillingContactAssembler.updateBillingContact(billingContact,
                voBillingContact);
    }

    @Test
    public void testToVOBillingContact() throws Exception {
        VOBillingContact vobc = BillingContactAssembler
                .toVOBillingContact(billingContact);
        Assert.assertNotNull("vobc is null", vobc);
        Assert.assertEquals(billingContact.getAddress(), vobc.getAddress());
        Assert.assertEquals(billingContact.getCompanyName(),
                vobc.getCompanyName());
        Assert.assertEquals(billingContact.getEmail(), vobc.getEmail());
        Assert.assertEquals(billingContact.getKey(), vobc.getKey());
        Assert.assertEquals(billingContact.getVersion(), vobc.getVersion());
        Assert.assertEquals(billingContact.isOrgAddressUsed(),
                vobc.isOrgAddressUsed());
    }

    @Test
    public void testToVOBillingContact_Null() throws Exception {
        VOBillingContact vobc = BillingContactAssembler
                .toVOBillingContact(null);
        Assert.assertNotNull("vobc is null", vobc);
        Assert.assertEquals(null, vobc.getAddress());
        Assert.assertEquals(null, vobc.getCompanyName());
        Assert.assertEquals(null, vobc.getEmail());
        Assert.assertEquals(0, vobc.getKey());
        Assert.assertEquals(0, vobc.getVersion());
        Assert.assertEquals(true, vobc.isOrgAddressUsed());
    }

}
