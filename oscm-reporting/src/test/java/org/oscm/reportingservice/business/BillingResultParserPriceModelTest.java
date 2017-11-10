/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.12.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.oscm.converter.DateConverter;
import org.oscm.converter.XMLConverter;
import org.oscm.reportingservice.dao.BillingDao;
import org.oscm.types.constants.BillingResultXMLTags;
import org.oscm.internal.types.enumtypes.PriceModelType;

/**
 * @author baumann
 * 
 */
public class BillingResultParserPriceModelTest {

    public static String PRICEMODEL = "<PriceModel id=\"1\"> </PriceModel>";
    public static String PRICEMODEL_PRORATA_CET = "<PriceModel id=\"1\" calculationMode=\"PRO_RATA\" ><BillingDetails timezone=\"CET\"></BillingDetails> </PriceModel>";
    public static String PRICEMODEL_TIMESLICE_MACAO = "<PriceModel id=\"1\" calculationMode=\"PER_UNIT\"> </PriceModel>";

    private BillingResultParser billingResultParser;

    @Before
    public void setUp() throws Exception {
        billingResultParser = new BillingResultParser(new BillingDao(null));
    }

    private Node getPriceModelNode(String xml) throws Exception {
        Document document = XMLConverter.convertToDocument(xml, true);
        return XMLConverter.getLastChildNode(document,
                BillingResultXMLTags.PRICE_MODEL_NODE_NAME);
    }

    @Test
    public void evaluateCalculationMode_ProRata() throws Exception {
        // given
        Node priceModelNode = getPriceModelNode(PRICEMODEL_PRORATA_CET);

        // when
        PriceModelType priceModelType = billingResultParser
                .evaluateCalculationMode(priceModelNode);

        // then
        assertEquals("Price calculation 'pro rata' expected",
                PriceModelType.PRO_RATA, priceModelType);
    }

    @Test
    public void evaluateCalculationMode_Timeslice() throws Exception {
        // given
        Node priceModelNode = getPriceModelNode(PRICEMODEL_TIMESLICE_MACAO);

        // when
        PriceModelType priceModelType = billingResultParser
                .evaluateCalculationMode(priceModelNode);

        // then
        assertEquals("Price calculation 'per unit' expected",
                PriceModelType.PER_UNIT, priceModelType);
    }

    @Test
    public void evaluateCalculationMode_Default() throws Exception {
        // given
        Node priceModelNode = getPriceModelNode(PRICEMODEL);

        // when
        PriceModelType priceModelType = billingResultParser
                .evaluateCalculationMode(priceModelNode);

        // then
        assertEquals("Price calculation 'pro rata' expected",
                PriceModelType.PRO_RATA, priceModelType);
    }

    @Test
    public void evaluateTimeZone_CET() throws Exception {
        // given
        Node priceModelNode = getPriceModelNode(PRICEMODEL_PRORATA_CET);

        // when
        int timeZoneOffset = billingResultParser
                .readTimeZoneFromBillingDetails(priceModelNode);

        // then
        assertEquals("Timezone CET expected",
                DateConverter.MILLISECONDS_PER_HOUR, timeZoneOffset);
    }

    @Test
    public void rawOffsetFromTimzoneId_empty() throws Exception {
        // given
        String timezoneId = "";

        // when
        int timeZoneOffset = billingResultParser
                .rawOffsetFromTimzoneId(timezoneId);

        // then
        assertEquals("local timezone expected", Calendar.getInstance()
                .getTimeZone().getRawOffset(), timeZoneOffset);
    }

    @Test
    public void rawOffsetFromTimzoneId_null() throws Exception {
        // given
        String timezoneId = null;

        // when
        int timeZoneOffset = billingResultParser
                .rawOffsetFromTimzoneId(timezoneId);

        // then
        assertEquals("local timezone expected", Calendar.getInstance()
                .getTimeZone().getRawOffset(), timeZoneOffset);
    }

    @Test
    public void rawOffsetFromTimzoneId_cet() {
        // given
        String cet = "UTC+01:00";

        // when
        int rawOffsetFromTimzoneId = billingResultParser
                .rawOffsetFromTimzoneId(cet);

        // then
        assertEquals(DateConverter.MILLISECONDS_PER_HOUR,
                rawOffsetFromTimzoneId);
    }

    @Test
    public void rawOffsetFromTimzoneId_utc() {
        // given
        String utc = "UTC";

        // when
        int rawOffsetFromTimzoneId = billingResultParser
                .rawOffsetFromTimzoneId(utc);

        // then
        assertEquals(0, rawOffsetFromTimzoneId);
    }

    @Test
    public void rawOffsetFromTimzoneId_macao() throws Exception {
        // given
        String timezoneId = "UTC+8";

        // when
        int timeZoneOffset = billingResultParser
                .rawOffsetFromTimzoneId(timezoneId);

        // then
        assertEquals("Timezone Asia/Macao expected",
                8 * DateConverter.MILLISECONDS_PER_HOUR, timeZoneOffset);
    }

    @Test
    public void evaluateTimeZone_Default() throws Exception {
        // given
        Node priceModelNode = getPriceModelNode(PRICEMODEL);

        // when
        int timeZoneOffset = billingResultParser
                .readTimeZoneFromBillingDetails(priceModelNode);

        // then
        assertEquals("Server Timezone expected", Calendar.getInstance()
                .getTimeZone().getRawOffset(), timeZoneOffset);
    }
}
