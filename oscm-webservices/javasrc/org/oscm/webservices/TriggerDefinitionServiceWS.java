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

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.converter.api.EnumConverter;
import org.oscm.converter.api.ExceptionConverter;
import org.oscm.converter.api.VOConverter;
import org.oscm.converter.api.VOCollectionConverter;
import org.oscm.intf.TriggerDefinitionService;
import org.oscm.types.enumtypes.TriggerType;
import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.DeletionConstraintException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.TriggerDefinitionDataException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOTriggerDefinition;

/**
 * End point facade for WS.
 * 
 * 
 */
@WebService(name = "TriggerDefinitionService", serviceName = "TriggerDefinitionService", targetNamespace = "http://oscm.org/xsd", endpointInterface = "org.oscm.intf.TriggerDefinitionService")
public class TriggerDefinitionServiceWS implements TriggerDefinitionService {

    WebServiceLogger WS_LOGGER = new WebServiceLogger(
            LoggerFactory.getLogger(TriggerDefinitionServiceWS.class));

    org.oscm.internal.intf.TriggerDefinitionService delegate;
    DataService ds;
    WebServiceContext wsContext;

    @Override
    public void createTriggerDefinition(VOTriggerDefinition vo)
            throws TriggerDefinitionDataException, ValidationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.createTriggerDefinition(VOConverter.convertToUp(vo));
        } catch (org.oscm.internal.types.exception.TriggerDefinitionDataException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void deleteTriggerDefinition(long key)
            throws ObjectNotFoundException, DeletionConstraintException,
            OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.deleteTriggerDefinition(key);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.DeletionConstraintException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void updateTriggerDefinition(VOTriggerDefinition vo)
            throws ObjectNotFoundException, ValidationException,
            ConcurrentModificationException, TriggerDefinitionDataException,
            OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.updateTriggerDefinition(VOConverter.convertToUp(vo));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TriggerDefinitionDataException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOTriggerDefinition> getTriggerDefinitions() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(
                delegate.getTriggerDefinitions(),
                org.oscm.vo.VOTriggerDefinition.class);
    }

    @Override
    public List<TriggerType> getTriggerTypes() {
        WS_LOGGER.logAccess(wsContext, ds);
        List<TriggerType> types = new ArrayList<>();
        for (org.oscm.internal.types.enumtypes.TriggerType t : delegate
                .getTriggerTypes()) {
            types.add(EnumConverter.convert(t, TriggerType.class));
        }
        return types;
    }

}
