/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 17.08.2011                                                      
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
 * @author kulle
 */
public class MigrationBillingResultGatheredEventsTest {

    private static String xmlSetup1;
    private static String xmlExpected1;
    private static String xmlSetup2;
    private static String xmlExpected2;

    @BeforeClass
    public static void beforeClass() throws Exception {
        xmlSetup1 = readBillingResultFromFile(new File(
                "javares/BillingResult_Setup.xml"));
        xmlExpected1 = readBillingResultFromFile(new File(
                "javares/BillingResult_Expected.xml"));

        xmlSetup2 = readBillingResultFromFile(new File(
                "javares/BillingResult_Setup2.xml"));
        xmlExpected2 = readBillingResultFromFile(new File(
                "javares/BillingResult_Expected2.xml"));
    }

    @Before
    public void setUp() throws Exception {

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

    @Test
    public void testMigration() throws Exception {
        MigrationBillingResultGatheredEvents migrationTask = new MigrationBillingResultGatheredEvents();
        String migratedXml = migrationTask.migrateBillingResultXml(xmlSetup1);

        // assert
        assertEquals(xmlExpected1.replaceAll("[ ,\n]", ""),
                migratedXml.replaceAll("[ ,\n,\r]", ""));
    }

    @Test
    public void testMigrationWithSteppedPrices() throws Exception {
        MigrationBillingResultGatheredEvents migrationTask = new MigrationBillingResultGatheredEvents();
        String migratedXml = migrationTask.migrateBillingResultXml(xmlSetup2);

        // assert
        assertEquals(xmlExpected2.replaceAll("[ ,\n]", ""),
                migratedXml.replaceAll("[ ,\n,\r]", ""));
    }

    @Test(expected = SAXException.class)
    public void testMigrateInvalidXml() throws Exception {
        MigrationBillingResultGatheredEvents migrationTask = new MigrationBillingResultGatheredEvents();
        migrationTask.migrateBillingResultXml("invalid xml file");
    }

}
