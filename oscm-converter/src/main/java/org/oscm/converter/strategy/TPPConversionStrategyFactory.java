/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 25.06.15 11:23
 *
 *******************************************************************************/

package org.oscm.converter.strategy;

import org.oscm.types.enumtypes.TriggerProcessParameterType;

@SuppressWarnings("rawtypes")
public class TPPConversionStrategyFactory {

    private TPPConversionStrategyFactory() {
    }

    public static ConversionStrategy getStrategy(
            TriggerProcessParameterType extParamType) {
        if (TriggerProcessParameterType.PRODUCT.equals(extParamType)) {
            return new ToIntServiceParameterStrategy();
        } else {
            throw new IllegalArgumentException("Parameter type: "
                    + extParamType.name() + " not supported.");
        }
    }

    public static ConversionStrategy getStrategy(
            org.oscm.internal.types.enumtypes.TriggerProcessParameterType intParamType) {
        if (org.oscm.internal.types.enumtypes.TriggerProcessParameterType.PRODUCT
                .equals(intParamType)) {
            return new ToExtServiceParameterStrategy();
        } else {
            throw new IllegalArgumentException("Parameter type: "
                    + intParamType.name() + " not supported.");
        }
    }
}
