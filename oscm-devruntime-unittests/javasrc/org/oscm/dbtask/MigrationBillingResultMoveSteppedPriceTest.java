/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: cheld                                                 
 *                                                                              
 *  Creation Date: 20.09.2011                                                      
 *                                                                              
 *  Completion Time: 20.09.2011                                                
 *                                                                              
 *******************************************************************************/

package org.oscm.dbtask;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import org.oscm.stream.Streams;
import org.oscm.string.Strings;

/**
 * @author cheld
 * 
 */
public class MigrationBillingResultMoveSteppedPriceTest {

    private String givenBillingResult;

    private String expectedBillingResult;

    @Before
    public void beforeClass() throws Exception {
        givenBillingResult = readBillingResultFromFile(new File(
                "javares/BillingResult_steppedPriceAtOldPosition.xml"));
        expectedBillingResult = readBillingResultFromFile(new File(
                "javares/BillingResult_steppedPriceAtNewPosition.xml"));
    }

    private static String readBillingResultFromFile(File testFile)
            throws FileNotFoundException, InterruptedException, IOException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(testFile);
            String billingResult = Strings.toString(Streams.readFrom(is));
            billingResult = billingResult.substring(billingResult
                    .indexOf("<BillingDetails>"));
            return billingResult.replaceAll("\r\n", "\n");
        } finally {
            Streams.close(is);
        }
    }

    @Test
    public void moveSteppedPrice() throws Exception {
        MigrationBillingResultMoveSteppedPrice migrationUtil = new MigrationBillingResultMoveSteppedPrice();
        String migratedXml = migrationUtil
                .migrateBillingResultXml(givenBillingResult);
        expectedBillingResult = expectedBillingResult.replaceAll(" ", "");
        expectedBillingResult = expectedBillingResult.replaceAll("\r\n", "\n");
        migratedXml = migratedXml.replaceAll(" ", "");
        migratedXml = migratedXml.replaceAll("\r\n", "\n");
        assertEquals(expectedBillingResult, migratedXml);
    }

}
