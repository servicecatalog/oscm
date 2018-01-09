/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 29.03.2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.types.enumtypes.PaymentInfoType;

/**
 * Tests for the organization reference domain object.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class OrganizationReferenceTest {

    private OrganizationReference orgRef;

    @Before
    public void setUp() throws Exception {
        orgRef = new OrganizationReference();
        OrganizationRefToPaymentType ortpt = new OrganizationRefToPaymentType();
        PaymentType paymentType = new PaymentType();
        paymentType.setPaymentTypeId(PaymentInfoType.INVOICE.name());
        ortpt.setPaymentType(paymentType);
        ortpt.setKey(11);
        List<OrganizationRefToPaymentType> paymentRefs = new ArrayList<OrganizationRefToPaymentType>();
        paymentRefs.add(ortpt);
        orgRef.setPaymentTypes(paymentRefs);
    }

    @Test
    public void testGetPaymentReferencesForType_NoHit() throws Exception {
        OrganizationRefToPaymentType result = orgRef
                .getPaymentReferenceForType(PaymentInfoType.CREDIT_CARD.name());
        assertNull(result);
    }

    @Test
    public void testGetPaymentReferencesForType_OneHit() throws Exception {
        OrganizationRefToPaymentType result = orgRef
                .getPaymentReferenceForType(PaymentInfoType.INVOICE.name());
        assertNotNull(result);
        assertEquals(11, result.getKey());
    }

}
