/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 13.12.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.oscm.internal.types.enumtypes.BillingSharesResultType;

/**
 * @author barzu
 */
public class BillingSharesResultIT extends DomainObjectTestBase {

    @Test
    public void persist_VeryLongResultXML() throws Exception {
        // given
        String veryLongString = createVeryLongString(10); // 10MB
        BillingSharesResult result = givenBillingSharesResult();
        result.setResultXML(veryLongString);

        // when
        long key = persist(result).longValue();

        // then
        BillingSharesResult storedResult = load(BillingSharesResult.class, key);
        assertEquals("Stored result string is corrupt", veryLongString,
                storedResult.getResultXML());
    }

    private String createVeryLongString(int sizeInMB) {
        char[] veryLongString = new char[1024 * 1024 * sizeInMB];
        Arrays.fill(veryLongString, '*');
        return new String(veryLongString);
    }

    private BillingSharesResult givenBillingSharesResult() {
        BillingSharesResult bsr = new BillingSharesResult();
        bsr.setResultType(BillingSharesResultType.BROKER);
        return bsr;
    }

}
