/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich                                                     
 *                                                                              
 *  Creation Date: 22.06.2010                                                      
 *                                                                              
 *  Completion Time: 22.06.2010                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.webservices;

import java.util.List;

import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.converter.api.EnumConverter;
import org.oscm.converter.api.ExceptionConverter;
import org.oscm.converter.api.VOCollectionConverter;
import org.oscm.converter.api.VOConverter;
import org.oscm.intf.TriggerService;
import org.oscm.types.enumtypes.TriggerProcessParameterType;
import org.oscm.types.exceptions.ExecutionTargetException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.TriggerProcessStatusException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOLocalizedText;
import org.oscm.vo.VOTriggerDefinition;
import org.oscm.vo.VOTriggerProcess;
import org.oscm.vo.VOTriggerProcessParameter;

/**
 * End point facade for WS.
 * 
 * @author Aleh Khomich.
 * 
 */
@WebService(name = "TriggerService", serviceName = "TriggerService", targetNamespace = "http://oscm.org/xsd", endpointInterface = "org.oscm.intf.TriggerService")
public class TriggerServiceWS implements TriggerService {

    WebServiceLogger WS_LOGGER = new WebServiceLogger(
            LoggerFactory.getLogger(TriggerServiceWS.class));

    org.oscm.internal.intf.TriggerService delegate;
    DataService ds;
    WebServiceContext wsContext;

    @Override
    public void approveAction(long key)
            throws ObjectNotFoundException, OperationNotPermittedException,
            TriggerProcessStatusException, ExecutionTargetException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.approveAction(key);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TriggerProcessStatusException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ExecutionTargetException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void cancelActions(List<Long> keys, List<VOLocalizedText> reason)
            throws ObjectNotFoundException, OperationNotPermittedException,
            TriggerProcessStatusException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.cancelActions(keys, VOCollectionConverter.convertList(
                    reason, org.oscm.internal.vo.VOLocalizedText.class));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TriggerProcessStatusException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void deleteActions(List<Long> keys) throws ObjectNotFoundException,
            OperationNotPermittedException, TriggerProcessStatusException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.deleteActions(keys);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TriggerProcessStatusException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOTriggerProcess> getAllActions() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(delegate.getAllActions(),
                VOTriggerProcess.class);
    }

    @Override
    public List<VOTriggerProcess> getAllActionsForOrganization() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(
                delegate.getAllActionsForOrganization(),
                VOTriggerProcess.class);
    }

    @Override
    public List<VOTriggerDefinition> getAllDefinitions() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(delegate.getAllDefinitions(),
                VOTriggerDefinition.class);
    }

    @Override
    public void rejectAction(long key, List<VOLocalizedText> reason)
            throws ObjectNotFoundException, OperationNotPermittedException,
            TriggerProcessStatusException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.rejectAction(key, VOCollectionConverter.convertList(reason,
                    org.oscm.internal.vo.VOLocalizedText.class));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TriggerProcessStatusException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void updateActionParameters(long actionKey,
            List<VOTriggerProcessParameter> parameters)
            throws ObjectNotFoundException, OperationNotPermittedException,
            TriggerProcessStatusException, ValidationException {
        WS_LOGGER.logAccess(wsContext, ds);

        try {
            delegate.updateActionParameters(actionKey,
                    VOCollectionConverter.convertList(parameters,
                            org.oscm.internal.vo.VOTriggerProcessParameter.class));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TriggerProcessStatusException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOTriggerProcessParameter getActionParameter(long actionKey,
            TriggerProcessParameterType paramType)
            throws ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);

        try {
            org.oscm.internal.vo.VOTriggerProcessParameter actionParameter = delegate
                    .getActionParameter(actionKey, EnumConverter.convert(
                            paramType,
                            org.oscm.internal.types.enumtypes.TriggerProcessParameterType.class));
            return VOConverter.convertToApi(actionParameter);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }
}
