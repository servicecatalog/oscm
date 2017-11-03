/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                   
 *                                                                              
 *******************************************************************************/

package org.oscm.test.matchers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

import java.util.Collection;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import org.junit.internal.matchers.TypeSafeMatcher;

import org.oscm.validation.Invariants;

/**
 * Hamcrest matchers for the jdk libraries.
 * 
 * @author cheld
 * 
 */
public class JavaMatchers {

    /**
     * Base class for custom matchers that are created inline with anonymous
     * classes
     * 
     * @author cheld
     */
    public abstract static class CustomMatcher<T> extends BaseMatcher<T> {

        private final String fixedDescription;

        public CustomMatcher(String descriptionOfWhatIsWanted) {
            Invariants.assertNotNull(descriptionOfWhatIsWanted);
            this.fixedDescription = "expected " + descriptionOfWhatIsWanted;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(fixedDescription);
        }

        @Override
        public abstract boolean matches(Object arg0);

    }

    /**
     * Checks if object has proper toString defined. This matcher can be
     * replaced with JUnit 4.8
     */
    public static Matcher<Object> hasToString() {
        return new TypeSafeMatcher<Object>() {

            @Override
            public boolean matchesSafely(Object objectToTest) {
                try {
                    objectToTest.getClass().getDeclaredMethod("toString");
                } catch (Exception e) {
                    return false;
                }
                String s = objectToTest.toString();
                if (s == null || s.length() == 0) {
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("proper toString()");
            }

        };
    }

    /**
     * Checks if array is non-empty. This matcher can be replaced with JUnit 4.8
     */
    public static Matcher<Object[]> hasItemInArray() {
        return new TypeSafeMatcher<Object[]>() {

            @Override
            public boolean matchesSafely(Object[] arrayToTest) {
                if (arrayToTest == null || arrayToTest.length == 0) {
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("non-empty array");
            }

        };
    }

    /**
     * Checks if list is non-empty. This matcher can be replaced with JUnit 4.8
     */
    public static Matcher<Collection<?>> hasItems() {
        return new TypeSafeMatcher<Collection<?>>() {

            @Override
            public boolean matchesSafely(Collection<?> collection) {
                if (collection == null || collection.isEmpty()) {
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("non-empty list");
            }

        };
    }

    /**
     * Checks if collection has the given number of items.
     */
    public static Matcher<Collection<?>> hasItems(final int numberOfItems) {
        return new TypeSafeMatcher<Collection<?>>() {

            @Override
            public boolean matchesSafely(Collection<?> collection) {
                if (collection == null || collection.size() != numberOfItems) {
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected a collection with "
                        + numberOfItems + " items");
            }

        };
    }

    /**
     * Checks if the collection is empty
     */
    public static Matcher<Collection<?>> hasNoItems() {
        return hasItems(0);
    }

    /**
     * Checks if the collection has exactly one item
     */
    public static Matcher<Collection<?>> hasOneItem() {
        return hasItems(1);
    }

    /**
     * Checks if the object is serializable
     */
    public static Matcher<Object> isSerializable() {
        return new BaseMatcher<Object>() {
            @Override
            public boolean matches(Object instance) {
                return Serialization.isSerializable(instance);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("serializable object");
            }
        };
    }

    /**
     * Checks if value is null. Shortcut for is(nullValue()).
     */
    public static Matcher<Object> isNullValue() {
        return is(nullValue());
    }

}
