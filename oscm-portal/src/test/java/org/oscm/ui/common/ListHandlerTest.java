/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: Feb 3, 2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * @author groch
 * 
 */
public class ListHandlerTest {

    private List<String> myList;
    private static final String A = "A";
    private static final String B = "B";
    private static final String C = "C";
    private static final String D = "D";
    private static final String E = "E";

    @Before
    public void setUp() {
        myList = new ArrayList<String>();
        myList.addAll(Arrays.asList(A, B, C, D, E));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMove_NullList() {
        myList = null;
        ListHandler.moveElement(myList, 2, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMove_EmptyList() {
        myList = new ArrayList<String>();
        ListHandler.moveElement(myList, 2, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMove_OneListElement() {
        myList = Arrays.asList(A);
        ListHandler.moveElement(myList, 2, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMove_MoveOldPosTooLow() {
        ListHandler.moveElement(myList, -1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMove_MoveOldPosTooHigh() {
        ListHandler.moveElement(myList, 5, 2);
    }

    @Test
    public void testMove_MoveToSamePos() {
        ListHandler.moveElement(myList, 2, 2);
        assertExpectedOrder(Arrays.asList(A, B, C, D, E), myList);
    }

    @Test
    public void testMove_MoveUpOne() {
        ListHandler.moveElement(myList, 1, 2);
        assertExpectedOrder(Arrays.asList(A, C, B, D, E), myList);
    }

    @Test
    public void testMove_MoveUpTwo() {
        ListHandler.moveElement(myList, 1, 3);
        assertExpectedOrder(Arrays.asList(A, C, D, B, E), myList);
    }

    @Test
    public void testMove_MoveUpToEnd() {
        ListHandler.moveElement(myList, 1, 4);
        assertExpectedOrder(Arrays.asList(A, C, D, E, B), myList);
    }

    @Test
    public void testMove_MoveUpOutOfBounds() {
        ListHandler.moveElement(myList, 1, 10);
        assertExpectedOrder(Arrays.asList(A, C, D, E, B), myList);
    }

    @Test
    public void testMove_MoveDownOne() {
        ListHandler.moveElement(myList, 3, 2);
        assertExpectedOrder(Arrays.asList(A, B, D, C, E), myList);
    }

    @Test
    public void testMove_MoveDownTwo() {
        ListHandler.moveElement(myList, 3, 1);
        assertExpectedOrder(Arrays.asList(A, D, B, C, E), myList);
    }

    @Test
    public void testMove_MoveDownToBeginning() {
        ListHandler.moveElement(myList, 3, 0);
        assertExpectedOrder(Arrays.asList(D, A, B, C, E), myList);
    }

    @Test
    public void testMove_MoveDownOutOfBounds() {
        ListHandler.moveElement(myList, 3, -3);
        assertExpectedOrder(Arrays.asList(D, A, B, C, E), myList);
    }

    @Test
    public void testMove_MoveUpFirstToEnd() {
        ListHandler.moveElement(myList, 0, 4);
        assertExpectedOrder(Arrays.asList(B, C, D, E, A), myList);
    }

    @Test
    public void testMove_MoveDownlastToBeginning() {
        ListHandler.moveElement(myList, 4, 0);
        assertExpectedOrder(Arrays.asList(E, A, B, C, D), myList);
    }

    private void assertExpectedOrder(List<String> expected, List<String> actual) {
        Assert.assertNotNull(expected);
        Assert.assertNotNull(actual);
        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            Assert.assertEquals(expected.get(i), actual.get(i));
        }
    }
}
