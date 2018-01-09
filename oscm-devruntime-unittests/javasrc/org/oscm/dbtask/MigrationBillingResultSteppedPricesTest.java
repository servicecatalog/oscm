/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 19.08.2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.dbtask;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import org.oscm.stream.Streams;
import org.oscm.string.Strings;

public class MigrationBillingResultSteppedPricesTest {

    private static String xmlSetup;
    private static String xmlExpected;
    private static String xmlSetup2;
    private static String xmlExpected2;

    @BeforeClass
    public static void beforeClass() throws Exception {
        xmlSetup = readBillingResultFromFile(new File(
                "javares/SteppedPrices_Setup.xml"));
        xmlExpected = readBillingResultFromFile(new File(
                "javares/SteppedPrices_Expected.xml"));

        xmlSetup2 = readBillingResultFromFile(new File(
                "javares/SteppedPrices_Setup2.xml"));
        xmlExpected2 = readBillingResultFromFile(new File(
                "javares/SteppedPrices_Expected2.xml"));
    }

    private static String readBillingResultFromFile(File testFile)
            throws FileNotFoundException, InterruptedException, IOException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(testFile);
            String billingResult = Strings.toString(Streams.readFrom(is));
            return billingResult.replaceAll("\r\n", "\n");
        } finally {
            Streams.close(is);
        }

    }

    @Test(expected = SAXException.class)
    public void testMigrateInvalidXml() throws Exception {
        MigrationBillingResultGatheredEvents migrationTask = new MigrationBillingResultGatheredEvents();
        migrationTask.migrateBillingResultXml("invalid xml file");
    }

    /**
     * Tests:
     * <ul>
     * <li>value above limit
     * <li>value below smallest limit
     * <li>value between two limits
     * <li>value equals highest freeamount
     * </ul>
     * 
     * @throws Exception
     */
    @Test
    public void testMigrationOfSteppedPrices() throws Exception {
        MigrationBillingResultSteppedPrices migrationUtil = new MigrationBillingResultSteppedPrices();
        String migratedXml = migrationUtil.migrateBillingResultXml(xmlSetup);

        // assert
        assertEquals(xmlExpected.replaceAll("[ ,\n]", ""),
                migratedXml.replaceAll("[ ,\n,\r]", ""));
    }

    /**
     * Tests:
     * <ul>
     * <li>value below smallest limit
     * <li>no stepped price elements between SteppedPrices
     * <li>do not overwrite already existent amount
     * <li>value equals zero
     * </ul>
     * 
     * @throws Exception
     */
    @Test
    public void testMigrationOfSteppedPricesSpecialCases() throws Exception {
        MigrationBillingResultSteppedPrices migrationUtil = new MigrationBillingResultSteppedPrices();
        String migratedXml = migrationUtil.migrateBillingResultXml(xmlSetup2);

        // assert
        assertEquals(xmlExpected2.replaceAll("[ ,\n]", ""),
                migratedXml.replaceAll("[ ,\n,\r]", ""));
    }

}
