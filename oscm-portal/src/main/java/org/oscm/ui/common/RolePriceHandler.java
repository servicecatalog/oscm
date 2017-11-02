/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 30.09.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.oscm.ui.model.RoleSpecificPrice;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOParameterOption;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPricedOption;
import org.oscm.internal.vo.VOPricedParameter;
import org.oscm.internal.vo.VOPricedRole;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * Provides functionality to determine the role prices for a subscription.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class RolePriceHandler {
    /**
     * Evaluates the subscription details and prepares a list containing the
     * role specific prices suitable for the output.
     * 
     * @param subDetails
     *            The details on the subscription to determine the role prices
     *            for.
     * @return List of role prices.
     */
    public static List<RoleSpecificPrice> determineRolePricesForSubscription(
            VOSubscriptionDetails subDetails) {
        if (subDetails == null) {
            return null;
        }
        return determineRolePricesForPriceModel(subDetails.getPriceModel());
    }

    public static List<RoleSpecificPrice> determineRolePricesForPriceModel(
            VOPriceModel pm) {
        if (pm == null) {
            return null;
        }
        // add a row for each role
        List<RoleSpecificPrice> result = new ArrayList<RoleSpecificPrice>();
        for (VOPricedRole role : pm.getRoleSpecificUserPrices()) {
            RoleSpecificPrice row = new RoleSpecificPrice();
            row.setRole(role.getRole());
            if (role.getPricePerUser().compareTo(BigDecimal.ZERO) != 0) {
                row.setPrice(role.getPricePerUser());
            }
            result.add(row);
        }

        // add the role specific prices for the parameters
        for (VOPricedParameter pricedParam : pm.getSelectedParameters()) {
            VOParameterDefinition def = pricedParam.getVoParameterDef();
            if (pricedParam.getVoParameterDef().getValueType() == ParameterValueType.ENUMERATION) {
                for (VOPricedOption pricedOption : pricedParam
                        .getPricedOptions()) {
                    for (VOPricedRole role : pricedOption
                            .getRoleSpecificUserPrices()) {
                        VOParameterOption option = VOFinder.findByKey(
                                def.getParameterOptions(),
                                pricedOption.getParameterOptionKey());
                        String optionDesc = "";
                        if (option != null) {
                            optionDesc = option.getOptionDescription();
                        }
                        insertRoleSpecificPrice(result, role.getRole(),
                                def.getDescription(), optionDesc,
                                role.getPricePerUser());
                    }
                }
            } else {
                for (VOPricedRole role : pricedParam
                        .getRoleSpecificUserPrices()) {
                    insertRoleSpecificPrice(result, role.getRole(),
                            def.getDescription(), null, role.getPricePerUser());
                }
            }
        }

        // remove role rows if there are no prices defined for it
        int i = 0;
        Iterator<RoleSpecificPrice> it = result.iterator();
        while (it.hasNext()) {
            RoleSpecificPrice row = it.next();
            if (row.getParameterDescription() == null
                    && row.getOptionDescription() == null
                    && (row.getPrice().compareTo(BigDecimal.ZERO) == 0)) {
                if (i + 1 < result.size()) {
                    if (result.get(i + 1).getParameterDescription() == null
                            && result.get(i + 1).getOptionDescription() == null) {
                        it.remove();
                        i--;
                    }
                }
            }
            i++;
        }
        // bug fix 5793
        // delete last element
        int lastElementIndex = result.size() - 1;
        if (lastElementIndex >= 0) {
            RoleSpecificPrice row = result.get(lastElementIndex);
            if (row.getParameterDescription() == null
                    && row.getOptionDescription() == null
                    && (row.getPrice().compareTo(BigDecimal.ZERO) == 0)) {
                result.remove(lastElementIndex);
            }
        }

        return result;
    }

    /**
     * Creates a new RoleSpecificPrice and inserts it after the last entry for
     * the given role definition into the list.
     * 
     * @param list
     *            the List to which the new RoleSpecificPrice is added.
     * @param role
     *            the role definition of the new RoleSpecificPrice.
     * @param paramDesc
     *            the parameter description of the new RoleSpecificPrice.
     * @param optionDesc
     *            the option description of the new RoleSpecificPrice.
     * @param price
     *            the price of the new RoleSpecificPrice.
     */
    private static void insertRoleSpecificPrice(List<RoleSpecificPrice> list,
            VORoleDefinition role, String paramDesc, String optionDesc,
            BigDecimal price) {
        if (price.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        RoleSpecificPrice row = new RoleSpecificPrice();
        row.setRole(role);
        row.setParameterDescription(paramDesc);
        row.setOptionDescription(optionDesc);
        row.setPrice(price);

        boolean found = false;
        int i = 0;
        while (i < list.size()) {
            if (list.get(i).getRole().getRoleId()
                    .equals(row.getRole().getRoleId())) {
                found = true;
            } else if (found) {
                break;
            }
            i++;
        }
        list.add(i, row);
        if (optionDesc != null
                && (i == 0 || list.get(i - 1).getOptionDescription() == null || !paramDesc
                        .equals(list.get(i - 1).getParameterDescription()))) {
            // if we insert the first option we insert an additional row with
            // the parameter description
            row = new RoleSpecificPrice();
            row.setRole(role);
            row.setParameterDescription(paramDesc);
            row.setPrice(BigDecimal.ZERO);
            // bug fix 5873. Set a member which will be in xhtml page tested for
            // do not showing price for enumeration parameters
            row.setEnumerationParameter("true");
            list.add(i, row);
        }
    }

}
