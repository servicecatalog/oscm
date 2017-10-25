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

public class ToExtServiceParameterStrategy extends AbstractConversionStrategy implements
        ConversionStrategy<VOService, String> {

    @Override
    public String convert(VOService oldVO) {

        org.oscm.vo.VOService newVO = VOConverter.convertToApi(oldVO);
        return JaxbConverter.toXML(newVO);
    }
}
