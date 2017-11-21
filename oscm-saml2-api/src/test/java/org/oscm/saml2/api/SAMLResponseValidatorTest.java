/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: Jun 01, 2016
 *
 *******************************************************************************/

package org.oscm.saml2.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.types.exception.SAML2StatusCodeInvalidException;

/**
 * Tests the validating the SAML LogoutResponse status code.
 *
 * @author farmaki
 *
 */
public class SAMLResponseValidatorTest {

    // Deflated and Base64 encoded strings containing SAML LogoutResponse.
    // This is to simulate the identity provider behavior (we receive responses
    // in this format)
    private final String FILE_UNSIGNED_LOGOUT_RESPONSE = "rVNbS8MwFH4X/A+j72uT9JaGtTAcyGAXcUPFF0nTdKu0SelJwZ9vVx12YPcghjydJN/lfCcz4FVZs5U+6NY8Sqi1Ajn5qEoFrD+KrbZRTHMogCleSWBGsN18vWLERqxutNFCl9btzeTXtVzEFlDuysB1iRCRCLgXpCQLUkojlObSDZAXelnu+9QfRXmSDRRaxVbHOU4F0MqlAsOV6W4iHExRt/EeUUYow+7r6NOFBFMobnqOozE1c5x7RB5WL5vN9plEKAjtAyJ23r4XBlq71IKXjGKKHA2imta6Mbx0xqWpc2v3OraWi7csjDKcoxRTEgrKeUgDFIaSyky6qZ9hl3QtyjxiJV+Qs1MWrHfYDNK5Hg4HkM3Jk5V8e+p1HzUYRlGnfVtLNV9PsWsjG82cAceQtmY7w00Lf5iK5KchF1B3OvvPIetHhJetvI4DPTfbtUJIgAGac7brDEV2xXPh8nsknw==";
    private final String FILE_UNSIGNED_LOGOUT_RESPONSE_ERROR_STATUS = "rVNba4MwFH4f7D8U36u5eImhCmWFUehltGUbexlRY+vQxHki7OfPupVZWPswFvJ0knyX851MQFRlzRd6r1uzkVBrBXL0UZUKeH8UWW2juBZQAFeiksBNyrfT5YITG/G60UanurRub0a/rvkssoAJKn1KSZqGqS9cPyGZnzAWoiSX1Edu4Ga55zHvIsqjbKDQKrI6zstUAK2cKzBCme4mwv4YdRvvEOOEcUxfLj6dSTCFEqbnOBhTc8e5R+Rh8bxarZ9IiPzA3iNi5+1bYaC1S52KkjPMkKMhrca1bowoncvS1Km1Ox1Z89lrFoQZzlGCGQlSJkTAfBQEkslM0sTLMCVdizKXWPEX5OSYBe8dNoN0rocjAGRz9GTF35563QcNhjPUaV/XUk2XY0xtZKOJM+AY0tZ8a4Rp4Q9TEf805AzqTmf/OWT9iIiylddxoOfmG/nednnLZoDnnAw7Q5ld8VQ4/yDxJw==";
    private final String FILE_UNSIGNED_LOGOUT_RESPONSE_INVALID_STATUS = "rVNbS8MwFH4X/A+j72vT9JaGtTAsyGAXcUPFF0mTdKu0SelJQf+9tTrswO1BDHk6Sb7L+U5mwOqqoUu91525l9BoBXLyVlcK6HCUWF2rqGZQAlWslkANp9v5akmxjWjTaqO5rqzrq8mva5ElFhDmydDzMOcxD5kf5liEOSExygvphciPfFEEAQnOojzIFkqtEqvnPE8F0MmFAsOU6W8iN5yifrs7RCgm1PWezz7NJJhSMTNwHIxpqOPcIny3fFqvN484RmFk7xG2i+61NNDZleasosQlyNHA62mjW8Mq57w0dWztTifWInsRUSzcAuUuwREnjEUkRFEkiRTSywPherhvkfCxlX5Bzj6zoIPDdpTO5XAYgGw/PVnpt6dB90GDoQT12jeNVPPV1PVsZKOZM+IY0zZ0a5jp4A9Tkf405ATqRov/HLJhRFjVycs4MHDTrKvr9xGWczTrjCX2xWPh9HOkHw==";
    private SAMLLogoutResponseValidator samlLogoutResponseValidator;

    @Before
    public void setup() throws Exception {
        samlLogoutResponseValidator = new SAMLLogoutResponseValidator();
    }

    @Test
    public void responseStatusCodeSuccessful_noError() throws Exception {
        // when
        final boolean successful = samlLogoutResponseValidator
                .responseStatusCodeSuccessful(FILE_UNSIGNED_LOGOUT_RESPONSE);
        // then
        assertTrue(successful);
    }

    @Test(expected = SAML2StatusCodeInvalidException.class)
    public void responseStatusCodeSuccessful_invalidStatus() throws Exception {
        // when
        final boolean successful = samlLogoutResponseValidator
                .responseStatusCodeSuccessful(
                        FILE_UNSIGNED_LOGOUT_RESPONSE_INVALID_STATUS);
        // then
        assertFalse(successful);
    }

    @Test
    public void responseStatusCodeSuccessful_hasError() throws Exception {
        // when
        final boolean successful = samlLogoutResponseValidator
                .responseStatusCodeSuccessful(
                        FILE_UNSIGNED_LOGOUT_RESPONSE_ERROR_STATUS);
        // then
        assertFalse(successful);
    }
}
