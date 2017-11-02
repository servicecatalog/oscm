/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: hoffmann                                                      
 *                                                                              
 *  Creation Date: 21.10.2010                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.authorization;

import java.util.HashMap;
import java.util.Map;

/**
 * Collection of utilities to work with conditions.
 * 
 * @author hoffmann
 */
public class Conditions {

    private Conditions() {
    }

    public static final Condition ALWAYS = new Condition() {
        public boolean eval() {
            return true;
        }
    };

    public static final Condition NEVER = new Condition() {
        public boolean eval() {
            return false;
        }
    };

    public static Condition or(final Condition... conditions) {
        return new Condition() {
            public boolean eval() {
                for (final Condition c : conditions) {
                    if (c.eval()) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static Condition and(final Condition... conditions) {
        return new Condition() {
            public boolean eval() {
                for (final Condition c : conditions) {
                    if (!c.eval()) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    public static Condition not(final Condition check) {
        return new Condition() {
            public boolean eval() {
                return !check.eval();
            }
        };
    }

    public static class Cache {

        private Map<Condition, Boolean> cache = new HashMap<Condition, Boolean>();

        public Condition get(final Condition c) {
            return new Condition() {
                public boolean eval() {
                    Boolean flag = cache.get(c);
                    if (flag == null) {
                        flag = Boolean.valueOf(c.eval());
                        cache.put(c, flag);
                    }
                    return flag.booleanValue();
                }
            };
        }

        public void reset() {
            cache.clear();
        }

    }

}
