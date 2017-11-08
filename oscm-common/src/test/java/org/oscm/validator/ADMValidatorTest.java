/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

public class ADMValidatorTest {

    @Test
    public void testContainsOnlyValidIdCharsInputNull() {
        boolean result = ADMValidator.containsOnlyValidIdChars(null);
        assertTrue(result);
    }

    @Test
    public void testValidateLimits0Blank() {
        String string = new String(new int[] { 0x20 }, 0, 1);
        boolean result = ADMValidator.containsOnlyValidIdChars(string);
        assertTrue(result);
    }

    @Test
    public void testValidateLimits() {
        String string = new String(new int[] { 0x19 }, 0, 1);
        boolean result = ADMValidator.containsOnlyValidIdChars(string);
        assertFalse(result);
    }

    @Test
    public void testValidateLimits0() {
        String string = " ";
        boolean result = ADMValidator.containsOnlyValidIdChars(string);
        assertTrue(result);
    }

    @Test
    public void testValidateLimits1() {
        String string = new String(new int[] { 0xE000 }, 0, 1);
        boolean result = ADMValidator.containsOnlyValidIdChars(string);
        assertTrue(result);
    }

    @Test
    public void testValidateLimits2() {
        String string = new String(new int[] { 0xFFFD }, 0, 1);
        boolean result = ADMValidator.containsOnlyValidIdChars(string);
        assertTrue(result);
    }

    @Test
    public void testValidateLimits3() {
        String string = new String(new int[] { 0x10000 }, 0, 1);
        boolean result = ADMValidator.containsOnlyValidIdChars(string);
        assertTrue(result);
    }

    @Test
    public void testValidateLimits4() {
        String string = new String(new int[] { 0x10FFFF }, 0, 1);
        boolean result = ADMValidator.containsOnlyValidIdChars(string);
        assertTrue(result);
    }

    @Test
    public void testValidateLimits5() {
        String string = new String(new int[] { 0x28 }, 0, 1);
        boolean result = ADMValidator.containsOnlyValidIdChars(string);
        assertTrue(result);
    }

    @Test
    public void testValidateLimits6() {
        String string = new String(new int[] { 0x29 }, 0, 1);
        boolean result = ADMValidator.containsOnlyValidIdChars(string);
        assertTrue(result);
    }

    @Test
    public void testValidateLimits7() {
        String string = new String(new int[] { 0x2D }, 0, 1);
        boolean result = ADMValidator.containsOnlyValidIdChars(string);
        assertTrue(result);
    }

    @Test
    public void testValidateLimits8() {
        String string = new String(new int[] { 0x2E }, 0, 1);
        boolean result = ADMValidator.containsOnlyValidIdChars(string);
        assertTrue(result);
    }

    @Test
    public void testValidateLimits9() {
        String string = new String(new int[] { 0x29 }, 0, 1);
        boolean result = ADMValidator.containsOnlyValidIdChars(string);
        assertTrue(result);
    }

    @Test
    public void testValidateLimits10() {
        String string = new String(new int[] { 0x30 }, 0, 1);
        boolean result = ADMValidator.containsOnlyValidIdChars(string);
        assertTrue(result);
    }

    @Test
    public void testValidateLimits11() {
        String string = new String(new int[] { 0x39 }, 0, 1);
        boolean result = ADMValidator.containsOnlyValidIdChars(string);
        assertTrue(result);
    }

    @Test
    public void testValidateLimits12() {
        String string = new String(new int[] { 0x40 }, 0, 1);
        boolean result = ADMValidator.containsOnlyValidIdChars(string);
        assertTrue(result);
    }

    @Test
    public void testValidateLimits13() {
        String string = new String(new int[] { 0x5B }, 0, 1);
        boolean result = ADMValidator.containsOnlyValidIdChars(string);
        assertTrue(result);
    }

    @Test
    public void testValidateLimits14() {
        String string = new String(new int[] { 0x5D }, 0, 1);
        boolean result = ADMValidator.containsOnlyValidIdChars(string);
        assertTrue(result);
    }

    @Test
    public void testValidateLimits15() {
        String string = new String(new int[] { 0x5F }, 0, 1);
        boolean result = ADMValidator.containsOnlyValidIdChars(string);
        assertTrue(result);
    }

    @Test
    public void testValidateLimits16() {
        String string = new String(new int[] { 0x61 }, 0, 1);
        boolean result = ADMValidator.containsOnlyValidIdChars(string);
        assertTrue(result);
    }

    @Test
    public void testValidateLimits17() {
        String string = new String(new int[] { 0xD7FF }, 0, 1);
        boolean result = ADMValidator.containsOnlyValidIdChars(string);
        assertTrue(result);
    }

    @Test
    public void testValidate() {
        boolean result = ADMValidator.containsOnlyValidIdChars("some_valid_id");
        assertTrue(result);
    }

    @Test
    public void testValidateEmptyBetween() {
        boolean result = ADMValidator.containsOnlyValidIdChars("1 2");
        assertTrue(result);
    }

    @Test
    public void testValidateEmpty() {
        boolean result = ADMValidator.containsOnlyValidIdChars("");
        assertTrue(result);
    }

    @Test
    public void testValidateWhitespaces() {
        boolean result = ADMValidator.containsOnlyValidIdChars("   ");
        assertTrue(result);
    }

    @Test
    public void testValidateLineBreak() {
        boolean result = ADMValidator.containsOnlyValidIdChars("hallo\nyou");
        assertFalse(result);
    }

    @Test
    public void testValidateUrl_TwoDoublePoints() {
        boolean result = ADMValidator.isUrl("http:://localhost:8080:/tralala");
        assertFalse(result);
    }

    @Test
    public void testValidateUrl_OneSlash() {
        boolean result = ADMValidator.isUrl("http:/localhost:8080:/tralala");
        assertFalse(result);
    }

    @Test
    public void testValidateUrl_OnlyProtocol() {
        boolean result = ADMValidator.isUrl("http://");
        assertFalse(result);
    }

    @Test
    public void testValidateUrl() {
        boolean result = ADMValidator.isUrl("http://gmx.de");
        assertTrue(result);
    }

    @Test
    public void testValidateUrl_IP() {
        boolean result = ADMValidator.isUrl("http://127.0.0.1:8080");
        assertTrue(result);
    }

    @Test
    public void testValidateUrl_IPV6() {
        boolean result = ADMValidator
                .isUrl("http://[FDA0:0:0:1:0:0:0:1234]:8080");
        assertTrue(result);
    }

    @Test
    public void testValidateUrl_IPV6_2() {
        boolean result = ADMValidator.isUrl("http://[FDA0:0:0:1::1234]:8080");
        assertTrue(result);
    }

    @Test
    public void testValidateUrl_IPV6_3() {
        boolean result = ADMValidator.isUrl("http://[::FDA0:1:1234]:8080");
        assertTrue(result);
    }

    @Test
    public void testValidateUrl_IPV6_4() {
        boolean result = ADMValidator.isUrl("http://[FDA0::]:8080");
        assertTrue(result);
    }

    @Test
    public void testValidateUrl_IPV6_5() {
        boolean result = ADMValidator
                .isUrl("http://[fd00:0:a:8c::12:93]:8180/oscm-portal");
        assertTrue(result);
    }

    @Test
    public void testValidateUrl_invalid_IPV6() {
        boolean result = ADMValidator
                .isUrl("http://[FDA0:::1:0:0:0:1234]:8080");
        assertFalse(result);
    }

    @Test
    public void testValidateUrl_invalid_IPV6_2() {
        boolean result = ADMValidator.isUrl("http://[FDA0::1:2:3::1234]:8080");
        assertFalse(result);
    }

    @Test
    public void testValidateUrl_invalid_IPV6_3() {
        boolean result = ADMValidator.isUrl("http://[fd00:0:a:8g::12:93]:8080");
        assertFalse(result);
    }

    @Test
    public void testValidateUrl_invalid_IPV6_4() {
        boolean result = ADMValidator.isUrl("http://[FDG0:0:A:8C::12:93]:8080");
        assertFalse(result);
    }

    @Test
    public void testValidateUrl_invalid_IPV6_5() {
        boolean result = ADMValidator
                .isUrl("http://fd00:0:a:8c::12:93:8180/oscm-portal");
        assertFalse(result);
    }

    @Test
    public void testValidateUrl_invalid_IPV6_port() {
        boolean result = ADMValidator
                .isUrl("http://[FDA0:::1:0:0:0:1234]:808077");
        assertFalse(result);
    }

    @Test
    public void testValidateUrl_Localhost8080() {
        boolean result = ADMValidator.isUrl("http://localhost:8080");
        assertTrue(result);
    }

    @Test
    public void testValidateUrl_LocalhostInvalidPort() {
        boolean result = ADMValidator.isUrl("http://localhost:800800");
        assertFalse(result);
    }

    @Test
    public void testValidateUrl_Mail() {
        boolean result = ADMValidator.isUrl("developer.user@est.fujitsu.com");
        assertFalse(result);
    }

    @Test
    public void testValidateUrl_Localhost() {
        boolean result = ADMValidator.isUrl("http://localhost");
        assertTrue(result);
    }

    @Test
    public void testValidateUrl_LocalSystem8080() {
        boolean result = ADMValidator.isUrl("http://localsystem:8080");
        assertTrue(result);
    }

    @Test
    public void testValidateUrl_SubdomainAndPath() {
        boolean result = ADMValidator.isUrl("http://subdomain.domain.de/path");
        assertTrue(result);
    }

    @Test
    public void isUrl() {
        boolean result = ADMValidator.isUrl("/fujitsu-layout/css/mp.css");
        assertFalse(result);
    }
    
    @Test
    public void isUrl_HostWithoutTld_OK() {
        boolean result = ADMValidator.isUrl("http://g08fnstd110825.g08.fujitsu.local:8080/path");
        assertTrue(result);
    }

    @Test
    public void isUrl_HostWithoutTld_NotValid() {
        boolean result = ADMValidator.isUrl("http://g08fnstd110825.g08-.fujitsu.local:8080/path");
        assertFalse(result);
    }

    @Test
    public void isUrl_HostNoDot_OK() {
        boolean result = ADMValidator.isUrl("http://g08fnstd110825:8080/path");
        assertTrue(result);
    }

    @Test
    public void isUrl_HostNoDot_NotValid() {
        boolean result = ADMValidator.isUrl("http://g08fnstd110825-:8080/path");
        assertFalse(result);
    }

    @Test
    public void isAbsoluteOrRelativeUrl() {
        boolean result = ADMValidator
                .isAbsoluteOrRelativeUrl("/fujitsu-layout/css/mp.css");
        assertTrue(result);
    }

    @Test
    public void isAbsoluteOrRelativeUrl_Absolute() {
        boolean result = ADMValidator
                .isAbsoluteOrRelativeUrl("http://localhost:8080/fujitsu-layout/css/mp.css");
        assertTrue(result);
    }

    @Test
    public void testValidateVat1() {
        boolean result = ADMValidator.isVat(BigDecimal.valueOf(19));
        assertTrue(result);
    }

    @Test
    public void testValidateVat2() {
        boolean result = ADMValidator.isVat(BigDecimal.valueOf(10000, 2));
        assertTrue(result);
    }

    @Test
    public void testValidateVat3() {
        boolean result = ADMValidator.isVat(BigDecimal.valueOf(10000, 3));
        assertFalse(result);
    }

    @Test
    public void testValidateVat4() {
        boolean result = ADMValidator.isVat(BigDecimal.valueOf(19.33F));
        assertFalse(result);
    }

    @Test
    public void testValidateVat5() {
        boolean result = ADMValidator.isVat(BigDecimal.valueOf(1000));
        assertFalse(result);
    }

    @Test
    public void testValidateVat6() {
        boolean result = ADMValidator.isVat(null);
        assertFalse(result);
    }

    @Test
    public void testValidateEMail() {
        assertFalse(ADMValidator.isEmail(null));
    }

    @Test
    public void testValidateEMail1() {
        String mailPattern = "a@bc";
        assertFalse(ADMValidator.isEmail(mailPattern));
    }

    @Test
    public void testValidateEMail2() {
        String mailPattern = "a.@bc";
        assertFalse(ADMValidator.isEmail(mailPattern));
    }

    @Test
    public void testValidateEMail3() {
        String mailPattern = "a@bc.de.de";
        assertTrue(ADMValidator.isEmail(mailPattern));
    }

    @Test
    public void testValidateEMail4() {
        String mailPattern = "a@bc.de@";
        assertFalse(ADMValidator.isEmail(mailPattern));
    }

    @Test
    public void testValidateEMail5() {
        String mailPattern = "a@bc.de@.de";
        assertFalse(ADMValidator.isEmail(mailPattern));
    }

    @Test
    public void testValidateEMail6() {
        String mailPattern = "abc.de@";
        assertFalse(ADMValidator.isEmail(mailPattern));
    }

    @Test
    public void testValidateEMail_OK() {
        String mailPattern = "abc@xy.ad";
        assertTrue(ADMValidator.isEmail(mailPattern));
    }

    @Test
    public void testIsBoolean() {
        assertTrue(ADMValidator.isBoolean("true"));
        assertTrue(ADMValidator.isBoolean("TRUE"));
        assertTrue(ADMValidator.isBoolean("false"));
        assertTrue(ADMValidator.isBoolean("FALSE"));
        assertFalse(ADMValidator.isBoolean(null));
        assertFalse(ADMValidator.isBoolean(""));
        assertFalse(ADMValidator.isBoolean(" "));
        assertFalse(ADMValidator.isBoolean("yes"));
        assertFalse(ADMValidator.isBoolean("YES"));
        assertFalse(ADMValidator.isBoolean("no"));
        assertFalse(ADMValidator.isBoolean("NO"));
    }

    @Test
    public void testIsHttpsScheme() {
        assertTrue(ADMValidator.isHttpsScheme("https://test.de"));
        assertFalse(ADMValidator.isHttpsScheme("ftp://test.de"));
        assertFalse(ADMValidator.isHttpsScheme("http://test.de"));
        assertFalse(ADMValidator.isHttpsScheme("hTtps://test.de"));
        assertFalse(ADMValidator.isHttpsScheme(null));
    }

}
