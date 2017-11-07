/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 04.03.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.pwdgen;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the password generation tool.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PasswordGeneratorTest {

    @Test
    public void testGeneratePassword() {
        PasswordGenerator gen = new PasswordGenerator();
        String generatedPassword = gen.generatePassword();
        Assert.assertEquals(8, generatedPassword.length());
        Assert.assertEquals(2, getNumberOfDigits(generatedPassword));
        Assert.assertEquals(2, getNumberOfLowerCaseChars(generatedPassword));
        Assert.assertEquals(2, getNumberOfUpperCaseChars(generatedPassword));
    }

    private int getNumberOfDigits(String generatedPassword) {
        int count = 0;
        for (int i = 0; i < generatedPassword.length(); i++) {
            char c = generatedPassword.charAt(i);
            if (Character.isDigit(c)) {
                count++;
            }
        }
        return count;
    }

    private int getNumberOfLowerCaseChars(String generatedPassword) {
        int count = 0;
        for (int i = 0; i < generatedPassword.length(); i++) {
            char c = generatedPassword.charAt(i);
            if (Character.isLowerCase(c)) {
                count++;
            }
        }
        return count;
    }

    private int getNumberOfUpperCaseChars(String generatedPassword) {
        int count = 0;
        for (int i = 0; i < generatedPassword.length(); i++) {
            char c = generatedPassword.charAt(i);
            if (Character.isUpperCase(c)) {
                count++;
            }
        }
        return count;
    }

}
