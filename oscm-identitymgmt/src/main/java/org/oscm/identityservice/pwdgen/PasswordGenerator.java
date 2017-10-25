/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 12.02.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.pwdgen;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple class to generate a one-time password for a new user account.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PasswordGenerator {

    private static final int NO_SPECIAL_CHARS = 2;
    private static final int NO_DIGITS = 2;
    private static final int NO_UPPER_CHARS = 2;
    private static final int NO_LOWER_CHARS = 2;

    public String generatePassword() {
        // first create random elements for each possible type
        List<Character> characters = createRandomCharacters();
        String pwd = mixRandomChars(characters);
        return pwd;
    }

    private List<Character> createRandomCharacters() {
        List<Character> characters = new ArrayList<Character>();
        characters = getLowerChars(characters);
        characters = getNumbers(characters);
        characters = getUpperChars(characters);
        characters = getSpecialChars(characters);

        return characters;
    }

    private String mixRandomChars(List<Character> characters) {
        // now mix up the random characters and concatenate them to get a
        // password
        StringBuffer pwd = new StringBuffer();
        while (characters.size() > 0) {
            int pos = (int) (Math.random() * (characters.size()));
            pwd.append(characters.remove(pos));
        }
        return pwd.toString();
    }

    private List<Character> getSpecialChars(List<Character> characters) {
        for (int i = 0; i < NO_SPECIAL_CHARS; i++) {
            int codePoint = (33 + (int) (Math.random() * 15));
            addCharsToList(characters, codePoint);
        }
        return characters;
    }

    private List<Character> getUpperChars(List<Character> characters) {
        for (int i = 0; i < NO_UPPER_CHARS; i++) {
            int codePoint = (65 + (int) (Math.random() * 26));
            addCharsToList(characters, codePoint);
        }
        return characters;
    }

    private List<Character> getNumbers(List<Character> characters) {
        for (int i = 0; i < NO_DIGITS; i++) {
            int codePoint = (48 + (int) (Math.random() * 10));
            addCharsToList(characters, codePoint);
        }
        return characters;
    }

    private List<Character> getLowerChars(List<Character> characters) {
        for (int i = 0; i < NO_LOWER_CHARS; i++) {
            int codePoint = 97 + (int) (Math.random() * 26);
            addCharsToList(characters, codePoint);
        }
        return characters;
    }

    private void addCharsToList(List<Character> characters, int codePoint) {
        char[] chars = Character.toChars(codePoint);
        for (char c : chars) {
            characters.add(Character.valueOf(c));
        }
    }
}
