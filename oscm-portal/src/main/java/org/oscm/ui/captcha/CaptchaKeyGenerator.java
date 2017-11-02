/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: Aug 4, 2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.captcha;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * This is a helper class to generate the random number for the captcha.
 * 
 * @author pravi
 * 
 */
public class CaptchaKeyGenerator {

    private static final int DIGIT = 0;

    private static final int LOWERCASECHAR = 1;

    private static final int UPPERCASECHAR = 2;

    private static final int SPECIALCHAR = 3;

    private static final char[] SPECIALCHARS = { '\043', '\044', '\045',
            '\046', '\100', };

    private static final BitSet FORBIDDEN = new BitSet();

    static {
        FORBIDDEN.set('i');
        FORBIDDEN.set('1');
        FORBIDDEN.set('0');
        FORBIDDEN.set('o');
        FORBIDDEN.set('O');
        FORBIDDEN.set('e');
        FORBIDDEN.set('f');
        FORBIDDEN.set('g');
        FORBIDDEN.set('l');
        FORBIDDEN.set('j');
    }

    /**
     * 
     * @param wordLength
     * @return
     */
    public String createCaptchaKey(final int wordLength) {
        final StringBuilder sbuilder = new StringBuilder();
        Set<Integer> set = new HashSet<Integer>();
        for (int ii = 0; ii < wordLength; ii++) {
            createKey(sbuilder, set);
        }
        return sbuilder.toString();
    }

    /**
     * @param sbuilder
     * @param set
     */
    private void createKey(final StringBuilder sbuilder, Set<Integer> set) {
        int seed = 3;
        while (true) {
            final int chartype = getRandom(seed);
            if (!set.contains(Integer.valueOf(chartype)) || set.size() == seed) {
                set.add(Integer.valueOf(chartype));
                final char character = getNext(chartype);
                sbuilder.append(character);
                break;
            }
        }
    }

    /**
     * 
     * @param seed
     * @return
     */
    private int getRandom(final int seed) {
        final Random random = new Random();
        return random.nextInt(seed);
    }

    private char getNext(int charType) {
        switch (charType) {
        case LOWERCASECHAR:
            return getNextUniqueChar(charType, 0);
        case UPPERCASECHAR:
            return getNextUniqueChar(charType, 0);
        case DIGIT:
            return getNextUniqueChar(charType, 9);
            // case SPECIALCHAR:
            // return getNextUniqueChar(charType, SPECIALCHARS.length - 1);
        default:
            throw new IllegalArgumentException("unknown char type");
        }
    }

    /**
     * @param sbuilder
     * @param chartype
     */
    private char getNextUniqueChar(final int chartype, final int randomLimit) {
        while (true) {
            char c = getNextCharacter(chartype,
                    getRandomIntegerInRange(0, randomLimit));
            if (!FORBIDDEN.get(c)) {
                return c;
            }
        }
    }

    /**
     * @param random
     * @return
     */
    private char getNextCharacter(int charType, int random) {
        switch (charType) {
        case LOWERCASECHAR:
            return getChar(65, 84); // don't use U, V, W, X, Y, Z
        case UPPERCASECHAR:
            return getChar(97, 122);
        case DIGIT:
            return getChar(49, 57);
        case SPECIALCHAR:
            return SPECIALCHARS[random];
        default:
            return 0;
        }
    }

    /**
     * @return
     */
    private char getChar(final int min, final int max) {
        int randomIntegerInRange = getRandomIntegerInRange(min, max);
        char[] chars = Character.toChars(randomIntegerInRange);
        return chars[0];
    }

    /**
     * 
     * @param start
     * @param end
     * @return
     */
    private int getRandomIntegerInRange(final int start, final int end) {
        final Random random = new Random();
        if (start > end) {
            throw new IllegalArgumentException("Start cannot exceed End.");
        }
        long range = (long) end - (long) start + 1;
        long fraction = (long) (range * random.nextDouble());
        int randomNumber = (int) (fraction + start);
        return randomNumber;
    }
}
