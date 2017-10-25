/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 21.06.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.assembler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.TriggerProcessParameter;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.identityservice.assembler.UserDataAssembler;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.enumtypes.TriggerProcessParameterType;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.vo.*;

/**
 * Assembler to convert a trigger process domain objects into a trigger process
 * value object.
 * 
 * @author pock
 * 
 */
public class TriggerProcessAssembler extends BaseAssembler {

    /**
     * Creates a new VOTriggerProcess object and fills the fields with the
     * corresponding fields from the given domain object.
     * 
     * @param domObj
     *            The domain object containing the values to be set.
     * @param facade
     *            The facade to retrieve localized data.
     * @return The created value object or null if the domain object was null.
     */
    public static VOTriggerProcess toVOTriggerProcess(TriggerProcess domObj,
            LocalizerFacade facade) {
        if (domObj == null) {
            return null;
        }
        VOTriggerProcess vo = new VOTriggerProcess();
        vo.setUser(UserDataAssembler.toVOUser(domObj.getUser()));
        vo.setTriggerDefinition(TriggerDefinitionAssembler
                .toVOTriggerDefinition(domObj.getTriggerDefinition()));
        vo.setActivationDate(domObj.getActivationDate());
        vo.setReason(facade.getText(domObj.getKey(),
                LocalizedObjectTypes.TRIGGER_PROCESS_REASON));
        vo.setStatus(domObj.getStatus());

        List<String> names = new ArrayList<String>();

        TriggerProcessParameter param = domObj
                .getParamValueForName(org.oscm.types.enumtypes.TriggerProcessParameterName.OBJECT_ID);
        if (param != null) {
            names.add(param.getValue(String.class));
        }

        switch (domObj.getTriggerDefinition().getType()) {
        case SUBSCRIBE_TO_SERVICE:
        case UPGRADE_SUBSCRIPTION:
            appendSubscription(domObj, vo);
            appendService(domObj, vo);
            param = domObj
                    .getParamValueForName(org.oscm.types.enumtypes.TriggerProcessParameterName.PRODUCT);
            if (param != null) {
                names.add(param.getValue(VOService.class).getNameToDisplay());
            }
            break;
        case REGISTER_OWN_USER:
            param = domObj
                    .getParamValueForName(org.oscm.types.enumtypes.TriggerProcessParameterName.USER);
            if (param != null) {
                names.add(param.getValue(VOUserDetails.class).getUserId());
            }
            break;
        case SAVE_PAYMENT_CONFIGURATION:
            if (domObj
                    .getParamValueForName(org.oscm.types.enumtypes.TriggerProcessParameterName.DEFAULT_CONFIGURATION) != null) {
                vo.setParameter(org.oscm.types.enumtypes.TriggerProcessParameterName.DEFAULT_CONFIGURATION
                        .name());
            } else if (domObj
                    .getParamValueForName(org.oscm.types.enumtypes.TriggerProcessParameterName.DEFAULT_SERVICE_PAYMENT_CONFIGURATION) != null) {
                vo.setParameter(org.oscm.types.enumtypes.TriggerProcessParameterName.DEFAULT_SERVICE_PAYMENT_CONFIGURATION
                        .name());
            } else if (domObj
                    .getParamValueForName(org.oscm.types.enumtypes.TriggerProcessParameterName.CUSTOMER_CONFIGURATION) != null) {
                vo.setParameter(org.oscm.types.enumtypes.TriggerProcessParameterName.CUSTOMER_CONFIGURATION
                        .name());
                param = domObj
                        .getParamValueForName(org.oscm.types.enumtypes.TriggerProcessParameterName.CUSTOMER_CONFIGURATION);
                VOOrganization org = param.getValue(
                        VOOrganizationPaymentConfiguration.class)
                        .getOrganization();
                if (org != null) {
                    if (org.getName() != null && org.getName().length() > 0) {
                        names.add(org.getName() + " ("
                                + org.getOrganizationId() + ")");
                    } else {
                        names.add(org.getOrganizationId());
                    }
                }
            } else if (domObj
                    .getParamValueForName(org.oscm.types.enumtypes.TriggerProcessParameterName.SERVICE_PAYMENT_CONFIGURATION) != null) {
                param = domObj
                        .getParamValueForName(org.oscm.types.enumtypes.TriggerProcessParameterName.SERVICE_PAYMENT_CONFIGURATION);
                vo.setParameter(org.oscm.types.enumtypes.TriggerProcessParameterName.SERVICE_PAYMENT_CONFIGURATION
                        .name());
                VOService svc = param.getValue(
                        VOServicePaymentConfiguration.class).getService();
                if (svc != null) {
                    names.add(svc.getServiceId());
                }
            }
            break;
        default:
            break;
        }

        vo.setTargetNames(names);

        updateValueObject(vo, domObj);
        return vo;
    }

    /**
     * @param domObj
     * @param vo
     */
    private static void appendService(TriggerProcess domObj, VOTriggerProcess vo) {
        TriggerProcessParameter service = domObj
                .getParamValueForName(org.oscm.types.enumtypes.TriggerProcessParameterName.PRODUCT);
        if (service != null) {
            vo.setService(service.getValue(VOService.class));
        }
    }

    /**
     * @param domObj
     * @param vo
     */
    private static void appendSubscription(TriggerProcess domObj,
            VOTriggerProcess vo) {
        TriggerProcessParameter subscription = domObj
                .getParamValueForName(org.oscm.types.enumtypes.TriggerProcessParameterName.SUBSCRIPTION);
        if (subscription != null) {
            vo.setSubscription(subscription.getValue(VOSubscription.class));
        }
    }

    /**
     * Converts dom object TriggerProcessParameter to VOTriggerProcessParamter
     * object.
     *
     * @param parameter
     * @return
     * @throws OperationNotPermittedException
     */
    public static VOTriggerProcessParameter toVOTriggerProcessParameter(
            TriggerProcessParameter parameter)
            throws OperationNotPermittedException {

        if (parameter == null) {
            return null;
        }

        Map<org.oscm.types.enumtypes.TriggerProcessParameterName, Class<?>> PARAM_MAPPING = new HashMap<org.oscm.types.enumtypes.TriggerProcessParameterName, Class<?>>() {
            private static final long serialVersionUID = 6650406068235081279L;

            {
                put(org.oscm.types.enumtypes.TriggerProcessParameterName.PRODUCT,
                        VOService.class);
            }
        };

        VOTriggerProcessParameter result = new VOTriggerProcessParameter();

        result.setType(TriggerProcessParameterType.valueOf(parameter.getName()
                .name()));
        result.setTriggerProcessKey(Long.valueOf(parameter.getTriggerProcess()
                .getKey()));
        result.setValue(parameter.getValue(PARAM_MAPPING.get(parameter
                .getName())));
        result.setKey(parameter.getKey());
        result.setVersion(parameter.getVersion());

        return result;
    }
}
