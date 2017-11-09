/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 15.05.15 10:18
 *
 *******************************************************************************/

package org.oscm.json;

import java.util.Collection;

import org.oscm.ui.model.PricedParameterRow;
import org.oscm.ui.model.Service;
import org.oscm.internal.vo.VOParameter;

/**
 * Created by FlorekS on 2015-05-12.
 */
public class JsonUtils {

    public static PricedParameterRow findPricedParameterRowById(String id, Collection<PricedParameterRow> serviceParameters) {
        PricedParameterRow retVal = null;
        for (PricedParameterRow servicePar : serviceParameters) {
            if (servicePar.getParameterDefinition().getParameterId().equals(id)) {
                retVal = servicePar;
            }
        }
        return retVal;
    }

    public static VOParameter findParameterById(String id, Service service) {
        VOParameter retVal = null;
        for (VOParameter p : service.getVO().getParameters()) {
            if (p.getParameterDefinition().getParameterId().equals(id)) {
                retVal = p;
            }
        }
        return retVal;
    }

    /**
     * Copy all parameter values and -error flags from the given JSON response
     * into the given JSON request.
     *
     * @param jsonRequest
     * @param jsonResponse
     */
    public static void copyResponseParameters(JsonObject jsonRequest, JsonObject jsonResponse) {
        if (jsonResponse != null) {
            for (JsonParameter responseParameter : jsonResponse.getParameters()) {
                JsonParameter requestParameter = jsonRequest
                        .getParameter(responseParameter.getId());
                if (requestParameter != null) {
                    requestParameter.setValue(responseParameter.getValue());
                    requestParameter.setValueError(responseParameter
                            .isValueError());
                }
            }
        }
    }
}
