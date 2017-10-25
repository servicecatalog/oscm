/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 25.06.15 11:30
 *
 *******************************************************************************/

package org.oscm.converter.strategy;

import org.oscm.converter.api.VOConverter;
import org.oscm.converter.strategy.api.AbstractConversionStrategy;
import org.oscm.converter.utils.JaxbConverter;
import org.oscm.internal.vo.VOService;

public class ToIntServiceParameterStrategy extends AbstractConversionStrategy implements
        ConversionStrategy<String, VOService> {

    @Override
    public VOService convert(String serializedString) {

        if(serializedString == null) {
            return null;
        }

        org.oscm.vo.VOService oldVO = JaxbConverter.fromXML(
                serializedString, org.oscm.vo.VOService.class);

        return VOConverter.convertToUp(oldVO);
    }
}
