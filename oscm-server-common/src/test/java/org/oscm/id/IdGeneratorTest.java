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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * Tests for the id generator class.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class IdGeneratorTest {

    @Test
    public void testIdGeneratorConstructor() throws Exception {
        new IdGenerator();
    }

    @Test
    public void testGenerateArtificialIdentifier_Length() throws Exception {
        String result = IdGenerator.generateArtificialIdentifier();
        assertEquals(8, result.length());
    }

    @Test
    public void testGenerateArtificialIdentifier_NonRepeatingIds()
            throws Exception {
        String result1 = IdGenerator.generateArtificialIdentifier();
        String result2 = IdGenerator.generateArtificialIdentifier();
        String result3 = IdGenerator.generateArtificialIdentifier();
        assertFalse(result1.equals(result2));
        assertFalse(result1.equals(result3));
        assertFalse(result3.equals(result2));
    }

    @Test
    public void testGenerateArtificialIdentifier_Threaded() throws Exception {
        final Set<String> ids = Collections
                .synchronizedSet(new HashSet<String>());
        final int gens = 10;
        final Runnable r = new Runnable() {

            public void run() {
                final String[] temp = new String[gens];
                for (int i = 0; i < gens; i++) {
                    temp[i] = IdGenerator.generateArtificialIdentifier();
                }
                ids.addAll(Arrays.asList(temp));
            }
        };
        final ThreadGroup tg = new ThreadGroup("Test");
        final int size = 100;
        for (int i = 0; i < size; i++) {
            Thread t = new Thread(tg, r);
            t.start();
        }
        while (tg.activeCount() > 0) {
            Thread.sleep(50);
        }
        assertEquals(size * gens, ids.size());
    }
}
