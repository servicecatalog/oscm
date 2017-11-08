/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                     
 *                                                                              
 *  Creation Date: 26.07.2011                                                      
 *                                                                              
 *  Completion Time: 26.07.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.validation;

import java.math.BigDecimal;

import org.junit.Test;

/**
 * Tests for class invariants
 * 
 * @author cheld
 * 
 */
public class InvariantsTest {

	@Test
	public void testConstructor() {
		new Invariants();
	}

	@Test
	public void asserType() {
		Invariants.asserType(new BigDecimal(2), BigDecimal.class);
	}

	@Test
	public void asserType_subtype() {
		Invariants.asserType(new BigDecimal(2), Number.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void asserType_negative() {
		Invariants.asserType(new BigDecimal(2), String.class);
	}

	@Test
	public void assertTrue_positive() {
		Invariants.assertTrue(true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void assertTrue_negative() {
		Invariants.assertTrue(false);
	}

	@Test
	public void assertTextNotEmpty_positive() {
		Invariants.assertTextNotEmpty("test");
	}

	@Test(expected = NullPointerException.class)
	public void assertTextNotEmpty_negative_null() {
		Invariants.assertTextNotEmpty(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void assertTextNotEmpty_negative_empty() {
		Invariants.assertTextNotEmpty("  ");
	}

	@Test
	public void assertGreaterThan_positive() {
		Invariants.assertGreaterThan(56700, 12);
	}

	@Test(expected = IllegalArgumentException.class)
	public void assertGreaterThan_negative() {
		Invariants.assertGreaterThan(10, 342343);
	}

	@Test
	public void assertNotNull_positive() {
		Invariants.assertNotNull(new Object(), "");
	}

	@Test(expected = NullPointerException.class)
	public void assertNotNull_negative() {
		Invariants.assertNotNull(null, "");
	}

	@Test
	public void assertBetween() {
		Invariants.assertBetween(2, 1, 5);
	}

	@Test(expected = IllegalArgumentException.class)
	public void assertBetween_lower() {
		Invariants.assertBetween(0, 1, 5);
	}

	public void assertBetween_higher() {
		Invariants.assertBetween(7, 1, 5);
	}
}
