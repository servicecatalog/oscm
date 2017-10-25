/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.validation;

/**
 * The class <code>Invariants</code> is useful for for embedding runtime sanity
 * checks in code. The asserts enforce the invariants defined by the class
 * specification (javadoc). All asserting methods throw some type of unchecked
 * exception if the condition does not hold.
 * <p>
 * Asserts should be inserted into the code to make the system fail fast. In
 * other words, an assert may only be inserted if the system would fail later
 * also. But, the later error condition makes the problem harder to understand
 * and debug. A typical usage of asserts is to protect an object construction
 * with null values.
 * </p>
 */
public final class Invariants {

	/**
	 * Asserts that the given object is not <code>null</code>. If this is not
	 * the case, a <code>NullPointerException</code> is thrown.
	 * 
	 * @param object
	 *            the value to test
	 * 
	 */
	public static void assertNotNull(Object object) {
		assertNotNull(object, "");
	}

	/**
	 * Asserts that the given object is not <code>null</code>. If this is not
	 * the case, a <code>NullPointerException</code> is thrown. The given
	 * message is included in that exception, to aid debugging.
	 * 
	 * @param object
	 *            the value to test
	 * @param message
	 *            the message to include in the exception
	 */
	public static void assertNotNull(Object object, String message) {
		if (object == null) {
			throw new NullPointerException("assertion failed:" + message);
		}
	}

	/**
	 * Asserts that the given object is <code>null</code>. If this is not the
	 * case, a <code>IllegalArgumentException</code> is thrown.
	 * 
	 * @param object
	 *            the value to test
	 * 
	 */
	public static void assertNull(Object object) {
		assertNull(object, "");
	}

	/**
	 * Asserts that the given object is <code>null</code>. If this is not the
	 * case, a <code>IllegalArgumentException</code> is thrown. The given
	 * message is included in that exception, to aid debugging.
	 * 
	 * @param object
	 *            the value to test
	 * @param message
	 *            the message to include in the exception
	 */
	public static void assertNull(Object object, String message) {
		if (object != null) {
			throw new IllegalArgumentException("assertion failed:" + message);
		}
	}

	/**
	 * Asserts that the given boolean is <code>true</code>. If this is not the
	 * case, a <code>IllegalArgumentException</code> is thrown.
	 * 
	 * @param expression
	 *            the value to test
	 */
	public static void assertTrue(boolean expression) {
		assertTrue(expression, "");
	}

	/**
	 * Asserts that the given boolean is <code>true</code>. If this is not the
	 * case, a <code>IllegalArgumentException</code> is thrown. The given
	 * message is included in that exception, to aid debugging.
	 * 
	 * @param expression
	 *            the value to test
	 * @param message
	 *            the message to include in the exception
	 */
	public static void assertTrue(boolean expression, String message) {
		if (!expression) {
			throw new IllegalArgumentException("assertion failed: " + message);
		}
	}

	/**
	 * Asserts that the given text is not empty. In other words the text has
	 * characters not equal to space.In case the given text is <code>null</code>
	 * , then a <code>NullPointerException</code> is thrown. In case the given
	 * text is empty, then a <code>IllegalArgumentException</code> is thrown.
	 * 
	 * @param text
	 *            the value to test
	 */
	public static void assertTextNotEmpty(String text) {
		assertTextNotEmpty(text, "");
	}

	/**
	 * Asserts that the given text is not empty or null. In other words the text
	 * has characters not equal to space. In case the given text is
	 * <code>null</code>, then a <code>NullPointerException</code> is thrown. In
	 * case the given text is empty, then a
	 * <code>IllegalArgumentException</code> is thrown.
	 * 
	 * @param text
	 *            the value to test
	 * @param message
	 *            the message to include in the exception
	 */
	public static void assertTextNotEmpty(String text, String message) {
		if (text == null) {
			throw new NullPointerException("assertion failed: " + message);
		}
		if (text.trim().length() == 0) {
			throw new IllegalArgumentException("assertion failed: " + message);
		}
	}

	/**
	 * Asserts that <code>great > small</code>.
	 * 
	 * @param great
	 *            the greater value
	 * @param small
	 *            the smaller value
	 * @throws IllegalArgumentException
	 *             if <code>great <= small</code>.
	 */
	public static void assertGreaterThan(long great, long small) {
		assertGreaterThan(great, small, "");
	}

	/**
	 * Asserts that <code>great > small</code>.
	 * 
	 * @param great
	 *            the greater value
	 * @param small
	 *            the smaller value
	 * @param message
	 *            the message to be included into the assertion failure
	 *            exception message
	 * @throws IllegalArgumentException
	 *             if <code>great <= small</code> containing the specified
	 *             message.
	 */
	public static void assertGreaterThan(long great, long small, String message) {
		if (great <= small) {
			throw new IllegalArgumentException("assertion failed: " + message);
		}
	}

	/**
	 * Asserts that the value is in between the given bounds. If this is not the
	 * case, a <code>IllegalArgumentException</code> is thrown.
	 * 
	 * @param value
	 *            the value to be asserted
	 * @param lowerBound
	 *            lower bound of check
	 * @param upperBound
	 *            upper bound of check
	 */
	public static void assertBetween(int value, int lowerBound, int upperBound) {
		assertBetween(value, lowerBound, upperBound, "");
	}

	/**
	 * Asserts that the value is in between the given bounds. If this is not the
	 * case, a <code>IllegalArgumentException</code> is thrown.
	 * 
	 * @param value
	 *            the value to be asserted
	 * @param lowerBound
	 *            lower bound of check
	 * @param upperBound
	 *            upper bound of check
	 * @param message
	 *            the message to be included into the assertion failure
	 *            exception message
	 */
	public static void assertBetween(int value, int lowerBound, int upperBound,
			String message) {
		if (value < lowerBound || value > upperBound) {
			throw new IllegalArgumentException("assertion failed: " + message);
		}
	}

	/**
	 * Asserts that the given object has the given type. If this is not the
	 * case, a <code>IllegalArgumentException</code> is thrown.
	 * 
	 * @param object
	 *            The object to be checked
	 * @param classToBeChecked
	 *            The type the object must have
	 */
	public static void asserType(Object object, Class<?> classToBeChecked) {
		asserType(object, classToBeChecked, "");
	}

	/**
	 * Asserts that the given object has the given type. If this is not the
	 * case, a <code>IllegalArgumentException</code> is thrown.
	 * 
	 * @param object
	 *            The object to be checked
	 * @param classToBeChecked
	 *            The type the object must have
	 */
	public static void asserType(Object object, Class<?> classToBeChecked,
			String message) {
		if (!classToBeChecked.isAssignableFrom(object.getClass())) {
			throw new IllegalArgumentException(
					"assertion failed: The object has type "
							+ object.getClass().getSimpleName()
							+ ", but should have type "
							+ classToBeChecked.getSimpleName() + ". " + message);
		}
	}

}
