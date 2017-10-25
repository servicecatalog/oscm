/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 09.12.2010                                                      
 *                                                                              
 *  Completion Time: 09.12.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.id;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

/**
 * Util class to generate artificial identifiers.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class IdGenerator {

    private static final Random RANDOM = new SecureRandom();

    /**
     * Generates a random string to be used as identifier for a domain object.
     * 
     * @return The generated, random identifier.
     */
    public static String generateArtificialIdentifier() {
        return Integer.toHexString(Integer.MAX_VALUE
                + RANDOM.nextInt(Integer.MAX_VALUE) + 1);
    }

    /**
     * Generates a type 4 (pseudo randomly generated) Universally Unique ID
     * (UUID).
     * 
     * @return a randomly generated <tt>UUID</tt>.
     */
    public static String generateRandomUUID() {
        return UUID.randomUUID().toString();
    }

}
