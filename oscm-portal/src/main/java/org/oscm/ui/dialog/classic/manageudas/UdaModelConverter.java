/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-6-13                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.manageudas;

import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOUdaDefinition;
import org.oscm.validation.ArgumentValidator;

/**
 * @author yuyin
 * 
 */
public class UdaModelConverter {

    /**
     * Creates a UdaDefinitionRowModel representing the given voUdaDefinition.
     * 
     * @param voUdaDefinition
     * @return convert result of input UdaDefinitionDetails
     * @throws ObjectNotFoundException
     */
    public static UdaDefinitionRowModel convertVoUdaDefinitionToRowModel(
            VOUdaDefinition voUdaDefinition) {
        // throw IllegalArgumentException for null input
        ArgumentValidator.notNull("VOUdaDefinition", voUdaDefinition);

        UdaDefinitionRowModel udaModel = new UdaDefinitionRowModel();
        udaModel.setDefaultValue(voUdaDefinition.getDefaultValue());
        udaModel.setUdaId(voUdaDefinition.getUdaId());
        udaModel.setKey(voUdaDefinition.getKey());
        udaModel.setVersion(voUdaDefinition.getVersion());
        udaModel.setEncrypted(voUdaDefinition.isEncrypted());
        udaModel.setControllerId(voUdaDefinition.getControllerId());
        udaModel.setName(voUdaDefinition.getName());
        udaModel.setLanguage(voUdaDefinition.getLanguage());
        // USER_OPTION_MANDATORY map to udaModel.mandatory is true and
        // udaModel.userOption is true
        if (voUdaDefinition.getConfigurationType()
                .equals(UdaConfigurationType.USER_OPTION_MANDATORY)) {
            udaModel.setMandatory(true);
            udaModel.setUserOption(true);
        }
        // USER_OPTION_OPTIONAL map to udaModel.mandatory is false and
        // udaModel.userOption is true
        else if (voUdaDefinition.getConfigurationType()
                .equals(UdaConfigurationType.USER_OPTION_OPTIONAL)) {
            udaModel.setMandatory(false);
            udaModel.setUserOption(true);
        }
        // SUPPLIER map to udaModel.mandatory is false and
        // udaModel.userOption is false
        else if (voUdaDefinition.getConfigurationType()
                .equals(UdaConfigurationType.SUPPLIER)) {
            udaModel.setMandatory(false);
            udaModel.setUserOption(false);
        }
        return udaModel;
    }

    /**
     * Creates a VOUdaDefinition representing the given UdaDefinitionDetails.
     * 
     * @param udaDetails
     * @return convert result of input UdaDefinitionDetails
     * @throws ObjectNotFoundException
     */
    public static VOUdaDefinition convertUdaDefDetailsToVoUdaDefinition(
            UdaDefinitionDetails udaDetails) {
        // udaDetails is null
        ArgumentValidator.notNull("UdaDefinitionDetails", udaDetails);

        VOUdaDefinition voUdaDefinition = new VOUdaDefinition();
        voUdaDefinition.setDefaultValue(udaDetails.getDefaultValue());
        voUdaDefinition.setUdaId(udaDetails.getUdaId());
        voUdaDefinition.setKey(udaDetails.getKey());
        voUdaDefinition.setVersion(udaDetails.getVersion());
        voUdaDefinition.setEncrypted(udaDetails.isEncrypted());
        voUdaDefinition.setControllerId(udaDetails.getControllerId());
        voUdaDefinition.setName(udaDetails.getName());
        voUdaDefinition.setLanguage(udaDetails.getLanguage());
        if (udaDetails.isMandatory()) {
            voUdaDefinition.setConfigurationType(
                    UdaConfigurationType.USER_OPTION_MANDATORY);
        } else if (udaDetails.isUserOption()) {
            voUdaDefinition.setConfigurationType(
                    UdaConfigurationType.USER_OPTION_OPTIONAL);
        } else {
            voUdaDefinition.setConfigurationType(UdaConfigurationType.SUPPLIER);
        }
        return voUdaDefinition;
    }

    public static UdaDefinitionRowModel convertUdaDefDetailsToUdaDefinitionRowModel(
            UdaDefinitionDetails udaDetails) {
        // udaDetails is null
        ArgumentValidator.notNull("UdaDefinitionDetails", udaDetails);

        UdaDefinitionRowModel udaDefinitionRowModel = new UdaDefinitionRowModel();
        udaDefinitionRowModel.setDefaultValue(udaDetails.getDefaultValue());
        udaDefinitionRowModel.setUdaId(udaDetails.getUdaId());
        udaDefinitionRowModel.setKey(udaDetails.getKey());
        udaDefinitionRowModel.setVersion(udaDetails.getVersion());
        udaDefinitionRowModel.setMandatory(udaDetails.isMandatory());
        udaDefinitionRowModel.setUserOption(udaDetails.isUserOption());
        udaDefinitionRowModel.setEncrypted(udaDetails.isEncrypted());
        udaDefinitionRowModel.setName(udaDetails.getName());
        udaDefinitionRowModel.setLanguage(udaDetails.getLanguage());
        udaDefinitionRowModel.setControllerId(udaDetails.getControllerId());
        return udaDefinitionRowModel;
    }

    public static UdaDefinitionDetails convertUdaDefinitionRowModelToUdaDefDetails(
            UdaDefinitionRowModel rowModel) {
        // udaDetails is null
        ArgumentValidator.notNull("UdaDefinitionDetails", rowModel);

        UdaDefinitionDetails udaDetails = new UdaDefinitionDetails();
        udaDetails.setDefaultValue(rowModel.getDefaultValue());
        udaDetails.setUdaId(rowModel.getUdaId());
        udaDetails.setKey(rowModel.getKey());
        udaDetails.setVersion(rowModel.getVersion());
        udaDetails.setMandatory(rowModel.isMandatory());
        udaDetails.setUserOption(rowModel.isUserOption());
        udaDetails.setName(rowModel.getName());
        udaDetails.setLanguage(rowModel.getLanguage());
        udaDetails.setEncrypted(rowModel.isEncrypted());
        udaDetails.setControllerId(rowModel.getControllerId());
        return udaDetails;
    }

}
