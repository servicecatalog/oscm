/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: Feb 3, 2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import java.util.List;

/**
 * Utility class to facilitate operations on a list.
 * 
 * @author groch
 * 
 */
public class ListHandler {

    /**
     * Shifts a list element from position oldPos to position newPos and returns
     * the updated list.
     * 
     * @param <E>
     *            The type of the objects this list contains.
     * @param list
     *            The list which is modified.
     * @param oldPos
     *            The old position of the element to move.
     * @param newPos
     *            The new position of the element after it has been moved.
     * @return The new position where the element to be moved was finally
     *         inserted. This will usually be correspond to the suggested
     *         newPos, however it may differ in case a value out of the bounds
     *         of the given list has been suggested as newPos.
     */
    public static <E> int moveElement(List<E> list, int oldPos, int newPos) {
        if (list == null) {
            throw new IllegalArgumentException(
                    "List must have been initialized.");
        }
        if (list.isEmpty() || list.size() == 1) {
            throw new IllegalArgumentException(
                    "List contains not enough elements, move makes no sense.");
        }
        if (oldPos < 0) {
            throw new IllegalArgumentException(
                    "Referred position of the element to move must not be negative.");
        }
        if (oldPos >= list.size()) {
            throw new IllegalArgumentException(
                    "List has fewer elements than the referred position of the element to move.");
        }
        if (oldPos != newPos) {
            E itemToBeMoved = list.remove(oldPos);
            if (newPos < 0) {
                newPos = 0;
            }
            if (newPos >= list.size()) {
                newPos = list.size();
            }
            list.add(newPos, itemToBeMoved);
        }
        return newPos;
    }

}
