/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.authorization;

import static org.oscm.ui.authorization.Conditions.ALWAYS;
import static org.oscm.ui.authorization.Conditions.NEVER;
import static org.oscm.ui.authorization.Conditions.and;
import static org.oscm.ui.authorization.Conditions.not;
import static org.oscm.ui.authorization.Conditions.or;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.oscm.ui.authorization.Condition;
import org.oscm.ui.authorization.Conditions.Cache;

public class ConditionsTest {

    @Test
    public void testALLWAYS() {
        assertTrue(ALWAYS.eval());
    }

    @Test
    public void testFALSE() {
        assertFalse(NEVER.eval());
    }

    @Test
    public void testOrEmpty() {
        assertFalse(or().eval());
    }

    @Test
    public void testOrFalse() {
        assertFalse(or(NEVER, NEVER, NEVER).eval());
    }

    @Test
    public void testOrTrue() {
        assertTrue(or(NEVER, NEVER, ALWAYS).eval());
    }

    @Test
    public void testAndEmpty() {
        assertTrue(and().eval());
    }

    @Test
    public void testAndFalse() {
        assertFalse(and(ALWAYS, ALWAYS, NEVER).eval());
    }

    @Test
    public void testAndTrue() {
        assertTrue(and(ALWAYS, ALWAYS, ALWAYS).eval());
    }

    @Test
    public void testNotTrue() {
        assertTrue(not(NEVER).eval());
    }

    @Test
    public void testNotFalse() {
        assertFalse(not(ALWAYS).eval());
    }

    @Test
    public void testCacheTrue() {
        testCache(true);
    }

    @Test
    public void testCacheFalse() {
        testCache(false);
    }

    private void testCache(final boolean b) {
        Condition cond = new Condition() {
            int calls = 0;

            public boolean eval() {
                assertTrue("Multiple calls.", ++calls <= 1);
                return b;
            }
        };
        Cache cache = new Cache();
        Condition cachedcond = cache.get(cond);
        assertTrue(b == cachedcond.eval());
        // Must not cause a second call to the original condition:
        assertTrue(b == cachedcond.eval());
    }

    @Test
    public void testCacheReset() {
        final boolean[] value = new boolean[] { false };
        Condition cond = new Condition() {
            public boolean eval() {
                return value[0];
            }
        };
        Cache cache = new Cache();
        Condition cachedcond = cache.get(cond);
        assertFalse(cachedcond.eval());

        // Must stay the same even until a reset is performed:
        value[0] = true;
        assertFalse(cachedcond.eval());
        cache.reset();
        assertTrue(cachedcond.eval());
    }

}
