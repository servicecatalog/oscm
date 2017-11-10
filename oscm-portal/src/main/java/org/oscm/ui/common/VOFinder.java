/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.common;

import java.util.Iterator;
import java.util.List;

import org.oscm.internal.vo.BaseVO;
import org.oscm.internal.vo.VOEventDefinition;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterOption;
import org.oscm.internal.vo.VOPricedEvent;
import org.oscm.internal.vo.VOPricedOption;
import org.oscm.internal.vo.VOPricedParameter;

/**
 * Utility class to find a value object in a list.
 * 
 */
public class VOFinder {

    /**
     * Find the value object by the key.
     * 
     * @param list
     *            the list with all value object.
     * @param key
     *            the key of the value which we want to find.
     * 
     * @returns the value object from the list with the requested key or null.
     */
    public static <E extends BaseVO> E findByKey(List<E> list, long key) {
        if (list == null) {
            return null;
        }
        Iterator<E> it = list.iterator();
        while (it.hasNext()) {
            E e = it.next();
            if (e.getKey() == key) {
                return e;
            }
        }
        return null;
    }

    /**
     * Find the priced parameter for the given parameter in the priced parameter
     * list.
     * 
     * @param list
     *            the list with all priced parameters.
     * @param parameter
     *            the parameter for which we want to find the priced parameter.
     * 
     * @returns the priced parameter for the the given parameter or null.
     */
    public static VOPricedParameter findPricedParameter(
            List<VOPricedParameter> list, VOParameter parameter) {
        if (list == null || parameter == null) {
            return null;
        }
        for (VOPricedParameter pricedParameter : list) {
            if (pricedParameter.getParameterKey() == parameter.getKey()) {
                return pricedParameter;
            }
        }
        return null;
    }

    /**
     * Find the priced option for the given option in the priced option list.
     * 
     * @param list
     *            the list with all priced options.
     * @param option
     *            the option for which we want to find the priced option.
     * 
     * @returns the priced option for the the given option or null.
     */
    public static VOPricedOption findPricedOption(List<VOPricedOption> list,
            VOParameterOption option) {
        if (list == null || option == null) {
            return null;
        }
        for (VOPricedOption pricedOption : list) {
            if (pricedOption.getParameterOptionKey() == option.getKey()) {
                return pricedOption;
            }
        }
        return null;
    }

    /**
     * Find the priced event for the given event in the priced event list.
     * 
     * @param list
     *            the list with all priced events.
     * @param option
     *            the event for which we want to find the priced event.
     * 
     * @returns the priced event for the the given event or null.
     */
    public static VOPricedEvent findPricedEvent(List<VOPricedEvent> list,
            VOEventDefinition event) {
        if (list == null || event == null) {
            return null;
        }
        for (VOPricedEvent pricedEvent : list) {
            if (pricedEvent.getEventDefinition().getKey() == event.getKey()) {
                return pricedEvent;
            }
        }
        return null;
    }

}
