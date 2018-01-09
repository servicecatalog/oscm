/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 08.01.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.dbtask;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import org.oscm.stream.Streams;
import org.oscm.string.Strings;

/**
 * @author stavreva
 */
public class MigrationBillingSharesResultSubscriptionPeriodTest {

    private static String xmlSetup;
    private static String xmlExpected;

    @BeforeClass
    public static void beforeClass() throws Exception {
        xmlSetup = readBillingSharesResultFromFile(new File(
                "javares/BillingSharesResult_Setup.xml"));
        xmlExpected = readBillingSharesResultFromFile(new File(
                "javares/BillingSharesResult_Expected.xml"));
    }

    @Before
    public void setUp() throws Exception {

    }

    private static String readBillingSharesResultFromFile(File testFile)
            throws FileNotFoundException, InterruptedException, IOException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(testFile);
            String billingSharesResult = Strings.toString(Streams.readFrom(is));
            return billingSharesResult.replaceAll("\r\n", "\n");
        } finally {
            if (is != null) {
                Streams.close(is);
            }
        }

    }

    @Test
    public void testMigration() throws Exception {
        MigrationBillingSharesResultSubscriptionPeriod migrationTask = new MigrationBillingSharesResultSubscriptionPeriod();
        String migratedXml = migrationTask
                .migrateBillingSharesResultXml(xmlSetup);

        // assert
        assertEquals(xmlExpected.replaceAll("[ ,\n]", ""),
                migratedXml.replaceAll("[ ,\n,\r]", ""));
    }

    @Test(expected = SAXException.class)
    public void testMigrateInvalidXml() throws Exception {
        MigrationBillingSharesResultSubscriptionPeriod migrationTask = new MigrationBillingSharesResultSubscriptionPeriod();
        migrationTask.migrateBillingSharesResultXml("invalid xml file");
    }

}
