/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.06.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.types.exception.AssertionValidationException;
import org.oscm.string.Strings;

/**
 * @author kulle
 * 
 */
public class AssertionConsumerServiceTest {

    private AssertionConsumerService acs;
    private final String FILE_OPENAM_RESPONSE = "javares/openamResponse.xml";
    private final String FILE_KEYSTORE_OPENAM = "javares/openam.jks";
    private final String acsUrl = "http://estkulle:8680/test/jsp/showPostResponse.jsp";
    private final String acsUrlHttps = "https://estkulle:8681/test/jsp/showPostResponse.jsp";
    public static final String tenantID = "8f96dede";

    @Before
    public void setup() {
    }

    @Test
    public void validateResponse_Http() throws Exception {
        // given
        acs = new AssertionConsumerService(acsUrl, acsUrlHttps,
                FILE_KEYSTORE_OPENAM, "changeit");
        String response = Strings.textFileToString(FILE_OPENAM_RESPONSE);
        response = response.replace("2013-05-29T10:53:36Z", (Calendar
                .getInstance().get(Calendar.YEAR) + 1) + "-05-29T10:53:36Z");
        response = response.replace("@RECIPIENT", acsUrl);

        // when
        acs.validateResponse(response, "4040406c-1530-11e0-e869-0110283f4jj6", tenantID);

        // then no exception expected
    }

    @Test
    public void validateResponse_Https() throws Exception {
        // given
        acs = new AssertionConsumerService(acsUrl, acsUrlHttps,
                FILE_KEYSTORE_OPENAM, "changeit");
        String response = Strings.textFileToString(FILE_OPENAM_RESPONSE);
        response = response.replace("2013-05-29T10:53:36Z", (Calendar
                .getInstance().get(Calendar.YEAR) + 1) + "-05-29T10:53:36Z");
        response = response.replace("@RECIPIENT", acsUrlHttps);

        // when
        acs.validateResponse(response, "4040406c-1530-11e0-e869-0110283f4jj6", tenantID);

        // then no exception expected
    }

    @Test(expected = AssertionValidationException.class)
    public void validateResponse_wrongRecipient() throws Exception {
        // given
        acs = new AssertionConsumerService(acsUrl, acsUrlHttps,
                FILE_KEYSTORE_OPENAM, "changeit");
        String response = Strings.textFileToString(FILE_OPENAM_RESPONSE);
        response = response.replace("2013-05-29T10:53:36Z", (Calendar
                .getInstance().get(Calendar.YEAR) + 1) + "-05-29T10:53:36Z");
        response = response.replace("@RECIPIENT", "https://something.else.de");

        // when
        acs.validateResponse(response, "4040406c-1530-11e0-e869-0110283f4jj6", tenantID);

        // then exception
    }

}
