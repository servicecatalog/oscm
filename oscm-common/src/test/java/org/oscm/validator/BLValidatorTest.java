/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.oscm.converter.PriceConverter;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;

public class BLValidatorTest {

    static final String baseUrl = "http://www.oscm-portal";

    static private String createRelativeUrl(int length) {
        String url = "/";
        for (int i = 1; i < length; i++) {

            url = url + "x";
        }
        return url;
    }

    @Test
    public void isRelativeUrlOk() throws Exception {
        BLValidator.isRelativeUrl("isRelativeURL", "/xy/123", baseUrl, false);
        BLValidator.isRelativeUrl("isRelativeURL", "", baseUrl, false);
        BLValidator.isRelativeUrl("isRelativeURL", null, baseUrl, false);
        BLValidator.isRelativeUrl("isRelativeURL", "", baseUrl, false);
        BLValidator.isRelativeUrl("isRelativeURL", "   ", baseUrl, false);
        BLValidator.isRelativeUrl("isRelativeURL",
                createRelativeUrl(255 - baseUrl.length()), baseUrl, false);

        BLValidator.isRelativeUrl("isRelativeURL", "/xy/123", baseUrl, true);
        BLValidator.isRelativeUrl("isRelativeURL", "/", baseUrl, true);
        BLValidator.isRelativeUrl("isRelativeURL",
                createRelativeUrl(255 - baseUrl.length()), baseUrl, true);
    }

    @Test(expected = ValidationException.class)
    public void isRelativeUrlTooLongUrl() throws Exception {
        BLValidator.isRelativeUrl("isRelativeURL",
                createRelativeUrl(256 - baseUrl.length()), baseUrl, false);
    }

    @Test(expected = ValidationException.class)
    public void isRelativeUrlAbsoluteUrl() throws Exception {
        BLValidator.isRelativeUrl("isRelativeURL", "http://www.gmx.de",
                baseUrl, false);
    }

    @Test(expected = ValidationException.class)
    public void isRelativeUrlEmpty() throws Exception {
        BLValidator.isRelativeUrl("isRelativeURL", "", baseUrl, true);
    }

    @Test(expected = ValidationException.class)
    public void isRelativeUrlWhitespaces() throws Exception {
        BLValidator.isRelativeUrl("isRelativeURL", "  ", baseUrl, true);
    }

    @Test(expected = ValidationException.class)
    public void isRelativeUrlNull() throws Exception {
        BLValidator.isRelativeUrl("isRelativeURL", null, baseUrl, true);
    }

    @Test
    public void isUrl1() throws Exception {
        BLValidator.isUrl("isURL", "http://www.gmx.de", false);
    }

    @Test
    public void isUrl2() throws Exception {
        BLValidator.isUrl("isURL", "ftp://123.45.67.80:1234", false);
    }

    @Test(expected = ValidationException.class)
    public void isUrlMissingProtocol1() throws Exception {
        BLValidator.isUrl("isURL", "www.gmx.de", false);
    }

    @Test(expected = ValidationException.class)
    public void isUrlMissingProtocol2() throws Exception {
        BLValidator.isUrl("isURL", "gmx.de", false);
    }

    @Test(expected = ValidationException.class)
    public void isUrlMissingPort() throws Exception {
        BLValidator.isUrl("isURL", "https://localhost:", false);
    }

    @Test(expected = ValidationException.class)
    public void isUrlUnknownProtocol() throws Exception {
        BLValidator.isUrl("isURL", "xyz://www.gmx.de", false);
    }

    @Test
    public void isUrlNull() throws Exception {
        BLValidator.isUrl("isURL", null, false);
    }

    @Test
    public void isUrlEmpty() throws Exception {
        BLValidator.isUrl("isURL", "", false);
    }

    @Test
    public void isUrlWhitespaces() throws Exception {
        BLValidator.isUrl("isURL", "   ", false);
    }

    @Test(expected = ValidationException.class)
    public void isUrlNullRequired() throws Exception {
        BLValidator.isUrl("isURL", null, true);
    }

    @Test(expected = ValidationException.class)
    public void isUrlEmptyRequired() throws Exception {
        BLValidator.isUrl("isURL", "", true);
    }

    @Test(expected = ValidationException.class)
    public void isUrlWhitespacesRequired() throws Exception {
        BLValidator.isUrl("isURL", "   ", true);
    }

    @Test
    public void isNonNegativeNumberZero() throws Exception {
        BLValidator.isNonNegativeNumber("isNonNegativeNumber", 0L);
    }

    @Test
    public void isNonNegativeNumberMax() throws Exception {
        BLValidator.isNonNegativeNumber("isNonNegativeNumber", Long.MAX_VALUE);
    }

    @Test(expected = ValidationException.class)
    public void isNonNegativeNumberMin() throws Exception {
        BLValidator.isNonNegativeNumber("isNonNegativeNumber", Long.MIN_VALUE);
    }

    @Test(expected = ValidationException.class)
    public void isPositiveAndNoneZeroNumber_NegativeNumber() throws Exception {
        BLValidator.isPositiveAndNonZeroNumber("isNegativeNumber",
                Long.MIN_VALUE);
    }

    @Test(expected = ValidationException.class)
    public void isPositiveAndNoneZeroNumber_Zero() throws Exception {
        BLValidator.isPositiveAndNonZeroNumber("isZero", 0L);
    }

    @Test
    public void isPositiveAndNoneZeroNumber_PositiveNumber() throws Exception {
        BLValidator.isPositiveAndNonZeroNumber("isPositiveNumber", 1L);
    }

    @Test
    public void isSecurityInfo() throws Exception {
        BLValidator.isSecurityInfo("Question", "Answer");
    }

    @Test
    public void isSecurityInfoBothNull() throws Exception {
        BLValidator.isSecurityInfo(null, null);
    }

    @Test
    public void isSecurityInfoBothEmpty() throws Exception {
        BLValidator.isSecurityInfo("", "");
    }

    @Test(expected = ValidationException.class)
    public void isSecurityInfoQuestionNull() throws Exception {
        BLValidator.isSecurityInfo(null, "Answer");
    }

    @Test(expected = ValidationException.class)
    public void isSecurityInfoQuestionEmpty() throws Exception {
        BLValidator.isSecurityInfo("", "Answer");
    }

    @Test(expected = ValidationException.class)
    public void isSecurityInfoAnswerNull() throws Exception {
        BLValidator.isSecurityInfo("Question", null);
    }

    @Test(expected = ValidationException.class)
    public void isSecurityInfoAnswerEmpty() throws Exception {
        BLValidator.isSecurityInfo("Question", "");
    }

    @Test
    public void isInRangeMinMax() throws Exception {
        BLValidator.isInRange("testIsInRange", 0, Long.valueOf(-1),
                Long.valueOf(1));
    }

    @Test
    public void isInRangeMax() throws Exception {
        BLValidator.isInRange("testIsInRange", 0, null, Long.valueOf(1));
    }

    @Test
    public void isInRangeMin() throws Exception {
        BLValidator.isInRange("testIsInRange", 0, Long.valueOf(-1), null);
    }

    @Test
    public void isInRange() throws Exception {
        BLValidator.isInRange("testIsInRange", 0, null, null);
    }

    @Test(expected = ValidationException.class)
    public void isInRangeOutMin() throws Exception {
        BLValidator.isInRange("testIsInRange", Long.MIN_VALUE, Long.valueOf(5),
                null);
    }

    @Test(expected = ValidationException.class)
    public void isInRangeOutMax() throws Exception {
        BLValidator.isInRange("testIsInRange", Long.MAX_VALUE, null,
                Long.valueOf(5));
    }

    @Test
    public void bigDecimalIsInRangeMinMax() throws Exception {
        BLValidator.isInRange("testIsInRange", BigDecimal.valueOf(0),
                BigDecimal.valueOf(-0.1), BigDecimal.valueOf(0.1));
    }

    @Test
    public void bigDecimalIsInRangeMax_WithScale() throws Exception {
        BLValidator.isInRange("testIsInRange", BigDecimal.valueOf(100.0),
                BigDecimal.valueOf(-0.1), BigDecimal.valueOf(100));
    }

    @Test
    public void bigDecimalIsInRangeMinMax_WithScale() throws Exception {
        BLValidator.isInRange("testIsInRange", BigDecimal.valueOf(0.0),
                BigDecimal.valueOf(-0.1), BigDecimal.valueOf(0.1));
    }

    @Test(expected = ValidationException.class)
    public void bigDecimalIsInRangeMinMax_MaxLessMin() throws Exception {
        BLValidator.isInRange("testIsInRange", BigDecimal.valueOf(-0.5),
                BigDecimal.valueOf(0.1), BigDecimal.valueOf(-0.1));
    }

    @Test(expected = ValidationException.class)
    public void bigDecimalIsInRangeMinMax_MaxLessMin2() throws Exception {
        BLValidator.isInRange("testIsInRange", BigDecimal.valueOf(0.5),
                BigDecimal.valueOf(0.1), BigDecimal.valueOf(-0.1));
    }

    @Test(expected = ValidationException.class)
    public void bigDecimalIsInRangeMinMax_lessMin() throws Exception {
        BLValidator.isInRange("testIsInRange", BigDecimal.valueOf(-0.2),
                BigDecimal.valueOf(-0.1), BigDecimal.valueOf(0.2));
    }

    @Test(expected = ValidationException.class)
    public void bigDecimalIsInRangeMinMax_greaterMax() throws Exception {
        BLValidator.isInRange("testIsInRange", BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(-0.1), BigDecimal.valueOf(0.1));
    }

    @Test
    public void bigDecimalIsInRangeMax() throws Exception {
        BLValidator.isInRange("testIsInRange", BigDecimal.valueOf(0), null,
                BigDecimal.valueOf(0.1));
    }

    @Test
    public void bigDecimalIsInRangeMin() throws Exception {
        BLValidator.isInRange("testIsInRange", BigDecimal.ZERO,
                BigDecimal.valueOf(-0.1), null);
    }

    @Test
    public void bigDecimalIsInRange() throws Exception {
        BLValidator.isInRange("testIsInRange", BigDecimal.ZERO, null, null);
    }

    @Test(expected = ValidationException.class)
    public void bigDecimalIsEquals() throws ValidationException {
        BLValidator.isEqual("testIsInRange", BigDecimal.ZERO, BigDecimal.ZERO);
    }

    @Test(expected = ValidationException.class)
    public void bigDecimalIsEquals_WithScale() throws ValidationException {
        BLValidator.isEqual("testIsInRange", BigDecimal.valueOf(0.0),
                BigDecimal.ZERO);
    }

    @Test
    public void bigDecimalIsNotEquals() throws ValidationException {
        BLValidator.isEqual("testIsInRange", BigDecimal.ZERO, BigDecimal.TEN);
    }

    /**
     * Empty id.
     * 
     * @throws Exception
     */
    @Test(expected = ValidationException.class)
    public void isIDIsEmpty() throws Exception {
        BLValidator.isId("isId", "", true, ADMValidator.LENGTH_ID);
    }

    /**
     * Only blanks.
     * 
     * @throws Exception
     */
    @Test(expected = ValidationException.class)
    public void isIDOnlyBlanks() throws Exception {
        BLValidator.isId("isId", "   ", true, ADMValidator.LENGTH_ID);
    }

    /**
     * Leading blanks.
     * 
     * @throws Exception
     */
    @Test(expected = ValidationException.class)
    public void isIDLeadingBlanks() throws Exception {
        BLValidator.isId("isId", "   abc", true, ADMValidator.LENGTH_ID);
    }

    /**
     * Trailing blanks.
     * 
     * @throws Exception
     */
    @Test(expected = ValidationException.class)
    public void isIDTrailingBlanks() throws Exception {
        BLValidator.isId("isId", "abc  ", true, ADMValidator.LENGTH_ID);
    }

    /**
     * Leading and trailing blanks.
     * 
     * @throws Exception
     */
    @Test(expected = ValidationException.class)
    public void isIDLeadingAndTrailingBlanks() throws Exception {
        String string = new String(new int[] { 0x19 }, 0, 1);
        BLValidator.isId("isId", "abc" + string, true, ADMValidator.LENGTH_ID);
    }

    /**
     * Leading and trailing blanks.
     * 
     * @throws Exception
     */
    @Test(expected = ValidationException.class)
    public void isIDInValidChars() throws Exception {
        BLValidator.isId("isId", "  abc  ", true, ADMValidator.LENGTH_ID);
    }

    /**
     * Blanks in middle is allowed.
     * 
     * @throws Exception
     */
    @Test
    public void isIDMiddleBlanks() throws Exception {
        BLValidator.isId("isId", "abc    def", true, ADMValidator.LENGTH_ID);
    }

    @Test(expected = ValidationException.class)
    public void isIDNewLine() throws Exception {
        BLValidator.isId("isId", "abc\ndef", true, ADMValidator.LENGTH_ID);
    }

    /**
     * Test Max length
     * 
     * @throws Exception
     */
    @Test
    public void isIDMaxLength() throws Exception {
        String testString = fillString('x', 5);
        BLValidator.isId("isUserId", testString, true, 5);
    }

    /**
     * Test Max length plus one
     * 
     * @throws Exception
     */
    @Test(expected = ValidationException.class)
    public void isIDMaxLengthPLusOne() throws Exception {
        String testString = fillString('x', 5 + 1);
        BLValidator.isId("isUserId", testString, true, 5);
    }

    /**
     * Test max length
     * 
     * @throws Exception
     */
    @Test
    public void isIdMaxlengthOK() throws Exception {
        String testString = fillString('x', ADMValidator.LENGTH_ID);
        BLValidator.isId("isUserId", testString, true);
    }

    @Test
    public void isId_Email() throws Exception {
        BLValidator.isId("isUserId", "test@test.de", true);
    }

    /**
     * Test max length +1
     * 
     * @throws Exception
     */
    @Test(expected = ValidationException.class)
    public void isIdMaxlengthPlusOne() throws Exception {
        String testString = fillString('x', ADMValidator.LENGTH_ID + 1);
        BLValidator.isId("isUserId", testString, true);
    }

    /**
     * Test max length
     * 
     * @throws Exception
     */
    @Test
    public void isUserIdMaxlengthOK() throws Exception {
        String testString = fillString('x', ADMValidator.LENGTH_USERID);
        BLValidator.isUserId("isUserId", testString, true);
    }

    /**
     * Test max length +1
     * 
     * @throws Exception
     */
    @Test(expected = ValidationException.class)
    public void isUserIdMaxlengthPlusOne() throws Exception {
        String testString = fillString('x', ADMValidator.LENGTH_USERID + 1);
        BLValidator.isUserId("isUserId", testString, true);
    }

    @Test
    public void isVatTrue() throws Exception {
        BLValidator.isVat("isVat", BigDecimal.valueOf(19));
    }

    @Test
    public void isVatFalse() {
        try {
            BLValidator.isVat("isVat", BigDecimal.valueOf(0.001));
        } catch (ValidationException e) {
            Assert.assertEquals(ValidationException.ReasonEnum.VAT,
                    e.getReason());
        }
    }

    private static String fillString(char fillChar, int count) {
        char[] chars = new char[count];
        while (count > 0)
            chars[--count] = fillChar;
        return new String(chars);
    }

    /**
     * Test max length
     * 
     * @throws Exception
     */
    @Test
    public void isTagMaxlengthOK() throws Exception {
        String testString = fillString('x', ADMValidator.LENGTH_TAG);
        BLValidator.isTag("isTag", testString);
    }

    /**
     * Test max length +1
     * 
     * @throws Exception
     */
    @Test(expected = ValidationException.class)
    public void isTagMaxlengthPlusOne() throws Exception {
        String testString = fillString('x', ADMValidator.LENGTH_TAG + 1);
        BLValidator.isTag("isTag", testString);
    }

    /**
     * Empty tag.
     * 
     * @throws Exception
     */
    @Test(expected = ValidationException.class)
    public void isTagIsEmpty() throws Exception {
        BLValidator.isTag("isTag", "");
    }

    /**
     * Empty tag.
     * 
     * @throws Exception
     */
    @Test(expected = ValidationException.class)
    public void isTagComma() throws Exception {
        BLValidator.isTag("isTag", ",");
    }

    @Test
    public void validLocale() throws Exception {
        BLValidator.isLocale("isLocale", "de", true);
    }

    @Test(expected = ValidationException.class)
    public void localeNull() throws Exception {
        BLValidator.isLocale("isLocale", null, true);
    }

    @Test(expected = ValidationException.class)
    public void localeBlank() throws Exception {
        BLValidator.isLocale("isLocale", " ", true);
    }

    @Test
    public void localeNull_False() throws Exception {
        BLValidator.isLocale("isLocale", null, false);
    }

    @Test
    public void localeBlank_False() throws Exception {
        BLValidator.isLocale("isLocale", " ", false);
    }

    @Test(expected = ValidationException.class)
    public void wrongLocale() throws Exception {
        BLValidator.isLocale("isLocale", "abcdef", true);
    }

    @Test(expected = ValidationException.class)
    public void password_ToShort() throws Exception {
        String newPassword = "test";
        BLValidator.isPassword("isPassword", newPassword);
    }

    @Test
    public void password_Null() throws Exception {
        BLValidator.isPassword("isPassword", null);
    }

    @Test
    public void password() throws Exception {
        BLValidator.isPassword("isPassword", "asd123F*");
    }

    @Test(expected = ValidationException.class)
    public void password_ToLong() throws Exception {
        String newPassword = "012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
        BLValidator.isPassword("isPassword", newPassword);
    }

    @Test
    public void name_NullFalse() throws Exception {
        BLValidator.isName("isName", null, false);
    }

    @Test(expected = ValidationException.class)
    public void name_NullTrue() throws Exception {
        BLValidator.isName("isName", null, true);
    }

    @Test(expected = ValidationException.class)
    public void name_ToLong() throws Exception {
        String name = "012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
        BLValidator.isName("isName", name, true);
    }

    @Test
    public void name_False() throws Exception {
        BLValidator.isName("isName", "test", false);
    }

    @Test
    public void name_True() throws Exception {
        BLValidator.isName("isName", "test", true);
    }

    @Test
    public void isUserGroupName_NullFalse() throws Exception {
        BLValidator.isUserGroupName("isUserGroupName", null, false);
    }

    @Test(expected = ValidationException.class)
    public void isUserGroupName_NullTrue() throws Exception {
        BLValidator.isUserGroupName("isUserGroupName", null, true);
    }

    @Test(expected = ValidationException.class)
    public void isUserGroupName_TooLong() throws Exception {
        String name = fillString('x', ADMValidator.LENGTH_USER_GROUP_NAME + 1);
        BLValidator.isUserGroupName("isUserGroupName", name, true);
    }

    @Test
    public void isUserGroupName_False() throws Exception {
        BLValidator.isUserGroupName("isUserGroupName", "test", false);
    }

    @Test
    public void isUserGroupName_True() throws Exception {
        BLValidator.isUserGroupName("isUserGroupName", "test", true);
    }

    @Test
    public void isUserGroupDescription_NullFalse() throws Exception {
        BLValidator.isDescription("isUserGroupDescription", null, false);
    }

    @Test(expected = ValidationException.class)
    public void isUserGroupDescription_NullTrue() throws Exception {
        BLValidator.isDescription("isUserGroupDescription", null, true);
    }

    @Test(expected = ValidationException.class)
    public void isUserGroupDescription_TooLong() throws Exception {
        String name = fillString('x', ADMValidator.LENGTH_DESCRIPTION + 1);
        BLValidator.isDescription("isUserGroupDescription", name, true);
    }

    @Test
    public void isUserGroupDescription_False() throws Exception {
        BLValidator.isDescription("isUserGroupName", "test", false);
    }

    @Test
    public void isUserGroupDescription_True() throws Exception {
        BLValidator.isDescription("isUserGroupDescription", "test", true);
    }

    @Test
    public void eMail_NullFalse() throws Exception {
        BLValidator.isEmail("isEmail", null, false);
    }

    @Test(expected = ValidationException.class)
    public void eMail_NullTrue() throws Exception {
        BLValidator.isEmail("isEmail", null, true);
    }

    @Test
    public void eMail_BlankFalse() throws Exception {
        BLValidator.isEmail("isEmail", " ", false);
    }

    @Test(expected = ValidationException.class)
    public void eMail_BlankTrue() throws Exception {
        BLValidator.isEmail("isEmail", " ", true);
    }

    @Test(expected = ValidationException.class)
    public void eMail_ToLong() throws Exception {
        String name = "012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
        BLValidator.isEmail("isEmail", name, true);
    }

    @Test
    public void eMail_False() throws Exception {
        BLValidator.isEmail("isEmail", "ab@test.de", false);
    }

    @Test
    public void eMail_True() throws Exception {
        BLValidator.isEmail("isEmail", "ab@test.de", true);
    }

    @Test
    public void comment_True() throws Exception {
        BLValidator.isComment("isComment", "abtest.de", true);
    }

    @Test
    public void comment_False() throws Exception {
        BLValidator.isComment("isComment", "abtest.de", false);
    }

    @Test(expected = ValidationException.class)
    public void notNull_Error() throws Exception {
        BLValidator.isNotNull("isNotNull", null);
    }

    @Test
    public void notNull() throws Exception {
        BLValidator.isNotNull("isNotNull", "test");
    }

    @Test
    public void dn() throws Exception {
        BLValidator.isDN("isDN", "test", true);
    }

    @Test(expected = ValidationException.class)
    public void dn_Null() throws Exception {
        BLValidator.isDN("isDN", null, true);
    }

    @Test(expected = ValidationException.class)
    public void dn_Blank() throws Exception {
        BLValidator.isDN("isDN", " ", true);
    }

    @Test
    public void dn_Null_False() throws Exception {
        BLValidator.isDN("isDN", null, false);
    }

    @Test
    public void dn_Blank_False() throws Exception {
        BLValidator.isDN("isDN", " ", false);
    }

    @Test
    public void integer_MaxInt() throws Exception {
        BLValidator.isInteger("isInteger", String.valueOf(Integer.MAX_VALUE));
    }

    @Test
    public void integer_MinInt() throws Exception {
        BLValidator.isInteger("isInteger", String.valueOf(Integer.MIN_VALUE));
    }

    @Test(expected = ValidationException.class)
    public void integer_moreMaxInt() throws Exception {
        long value = Long.MAX_VALUE;
        BLValidator.isInteger("isInteger", String.valueOf(value));
    }

    @Test(expected = ValidationException.class)
    public void integer_lessMinInt() throws Exception {
        long value = Long.MIN_VALUE;
        BLValidator.isInteger("isInteger", String.valueOf(value));
    }

    @Test(expected = ValidationException.class)
    public void integer_null() throws Exception {
        BLValidator.isInteger("isInteger", null);
    }

    @Test(expected = ValidationException.class)
    public void long_null() throws Exception {
        BLValidator.isLong("isLong", null);
    }

    @Test
    public void long_Max() throws Exception {
        long value = Long.MAX_VALUE;
        BLValidator.isLong("isLong", String.valueOf(value));
    }

    @Test
    public void long_Min() throws Exception {
        long value = Long.MIN_VALUE;
        BLValidator.isLong("isLong", String.valueOf(value));
    }

    @Test(expected = ValidationException.class)
    public void long_moreMax() throws Exception {
        BigDecimal value = BigDecimal.valueOf(Double.MAX_VALUE);
        BLValidator.isLong("isLong", String.valueOf(value));
    }

    @Test(expected = ValidationException.class)
    public void long_lessMin() throws Exception {
        BigDecimal value = BigDecimal.valueOf(Double.MIN_VALUE);
        BLValidator.isLong("isLong", String.valueOf(value));
    }

    @Test(expected = ValidationException.class)
    public void long_NonNegativNumber() throws Exception {
        BigDecimal value = BigDecimal.valueOf(-1);
        BLValidator.isNonNegativeNumber("isNonNegativeNumber", value);
    }

    @Test(expected = ValidationException.class)
    public void long_NonNegativNumber_Long() throws Exception {
        Long value = Long.valueOf(Long.MIN_VALUE);
        BLValidator.isNonNegativeNumber("isNonNegativeNumber",
                value.longValue());
    }

    @Test
    public void long_NonNegativNumber_0() throws Exception {
        BLValidator.isNonNegativeNumber("isNonNegativeNumber", BigDecimal.ZERO);
    }

    @Test
    public void long_NonNegativNumber_Long0() throws Exception {
        Long value = Long.valueOf(0);
        BLValidator.isNonNegativeNumber("isNonNegativeNumber",
                value.longValue());
    }

    @Test(expected = ValidationException.class)
    public void isRating() throws Exception {
        BLValidator.isRating("isRating", 0);
    }

    @Test(expected = ValidationException.class)
    public void isRating_1() throws Exception {
        BLValidator.isRating("isRating", -1);
    }

    @Test(expected = ValidationException.class)
    public void isRating_2() throws Exception {
        BLValidator.isRating("isRating", 6);
    }

    @Test
    public void isRating_3() throws Exception {
        BLValidator.isRating("isRating", 2);
    }

    @Test
    public void isNotEmpty() throws Exception {
        List<Object> params = new ArrayList<Object>();
        params.add(null);
        BLValidator.isNotEmpty("isNotEmpty", params);
    }

    @Test(expected = ValidationException.class)
    public void isNotEmpty_Empty() throws Exception {
        List<Object> params = new ArrayList<Object>();
        BLValidator.isNotEmpty("isNotEmpty", params);
    }

    @Test(expected = ValidationException.class)
    public void isBoolean_1() throws Exception {
        BLValidator.isBoolean("isBoolean", "0");
    }

    @Test(expected = ValidationException.class)
    public void isBoolean_3() throws Exception {
        BLValidator.isBoolean("isBoolean", "xxx");
    }

    @Test(expected = ValidationException.class)
    public void isBoolean_4() throws Exception {
        BLValidator.isBoolean("isBoolean", "tru@");
    }

    @Test(expected = ValidationException.class)
    public void isBoolean_5() throws Exception {
        BLValidator.isBoolean("isBoolean", "false1");
    }

    @Test
    public void isBoolean_6() throws Exception {
        BLValidator.isBoolean("isBoolean", "True");
    }

    @Test
    public void isBoolean_7() throws Exception {
        BLValidator.isBoolean("isBoolean", "tRue");
    }

    @Test
    public void isBoolean_8() throws Exception {
        BLValidator.isBoolean("isBoolean", "TrUe");
    }

    @Test
    public void isBoolean_9() throws Exception {
        BLValidator.isBoolean("isBoolean", "tRuE");
    }

    @Test
    public void isBoolean_10() throws Exception {
        BLValidator.isBoolean("isBoolean", "TRUE");
    }

    @Test
    public void isBoolean_11() throws Exception {
        BLValidator.isBoolean("isBoolean", "false");
    }

    @Test
    public void isBoolean_12() throws Exception {
        BLValidator.isBoolean("isBoolean", "FAlse");
    }

    @Test
    public void isBoolean_13() throws Exception {
        BLValidator.isBoolean("isBoolean", "falSe");
    }

    @Test
    public void isBoolean_14() throws Exception {
        BLValidator.isBoolean("isBoolean", "FALSE");
    }

    @Test
    public void isContentOfSupportEmail() throws Exception {
        BLValidator.isContentOfSupportEmail("isContentOfSupportEmail",
                "test for content of support email", true);
    }

    @Test(expected = ValidationException.class)
    public void isContentOfSupportEmail_NoContent() throws Exception {
        BLValidator
                .isContentOfSupportEmail("isContentOfSupportEmail", "", true);
    }

    @Test(expected = ValidationException.class)
    public void isContentOfSupportEmail_tooLongContent() throws Exception {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < 200; i++) {
            buf.append("too long content");
        }
        BLValidator.isContentOfSupportEmail("isContentOfSupportEmail",
                buf.toString(), true);
    }

    @Test
    public void isSubjectOfSupportEmail() throws Exception {
        BLValidator.isSubjectOfSupportEmail("isContentOfSupportEmail",
                "test subject of support email", true);
    }

    @Test(expected = ValidationException.class)
    public void isSubjectOfSupportEmail_NoSubject() throws Exception {
        BLValidator
                .isSubjectOfSupportEmail("isSubjectOfSupportEmail", "", true);
    }

    @Test(expected = ValidationException.class)
    public void isSubjectOfSupportEmail_TooLongSubject() throws Exception {
        BLValidator.isSubjectOfSupportEmail("isSubjectOfSupportEmail",
                "test subject of support email too long, too long!!!", false);
    }

    @Test
    public void isValidPriceScale() throws Exception {
        BLValidator.isValidPriceScale("member", BigDecimal.TEN.setScale(20));
    }

    @Test
    public void isValidPriceScale_equal() throws Exception {
        BLValidator.isValidPriceScale("member", BigDecimal.TEN
                .setScale(PriceConverter.NUMBER_OF_DECIMAL_PLACES));
    }

    @Test
    public void isValidPriceScale_tooLong() {
        try {
            BLValidator.isValidPriceScale("member", BigDecimal.TEN
                    .setScale(PriceConverter.NUMBER_OF_DECIMAL_PLACES + 1));
            fail();
        } catch (ValidationException e) {
            assertTrue(e.getReason() == ReasonEnum.SCALE_TO_LONG);
        }
    }

    @Test
    public void isValidDateRange_valid() throws ValidationException {

        // given valid range
        Date fromDate = new Date(System.currentTimeMillis());
        Date toDate = new Date(System.currentTimeMillis() + 24 * 60 * 60);

        // when validated then no exception is thrown
        BLValidator.isValidDateRange(fromDate, toDate);
    }

    @Test
    public void isValidDateRange_invalid() {
        Date fromDate = new Date(System.currentTimeMillis() + 24 * 60 * 60
                * 1000);
        Date toDate = new Date(System.currentTimeMillis());
        try {
            BLValidator.isValidDateRange(fromDate, toDate);
            fail();
        } catch (ValidationException e) {
            assertEquals(ReasonEnum.INVALID_DATE_RANGE, e.getReason());
        }
    }

    @Test
    public void isAccessinfo() throws Exception {
        BLValidator.isAccessinfo("isAccessinfo", "test", true);
    }

    @Test(expected = ValidationException.class)
    public void isAccessinfo_Null() throws Exception {
        BLValidator.isAccessinfo("isAccessinfo", null, true);
    }

    @Test(expected = ValidationException.class)
    public void isAccessinfo_Blank() throws Exception {
        BLValidator.isAccessinfo("isAccessinfo", " ", true);
    }

    @Test
    public void isAccessinfo_Null_False() throws Exception {
        BLValidator.isAccessinfo("isAccessinfo", null, false);
    }

    @Test
    public void isAccessinfo_Blank_False() throws Exception {
        BLValidator.isAccessinfo("isAccessinfo", " ", false);
    }
}
