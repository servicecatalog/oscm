/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: May 4, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.rest.trigger;

import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.WebApplicationException;

import org.oscm.internal.intf.TriggerDefinitionService;
import org.oscm.internal.types.enumtypes.TriggerTargetType;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DeletionConstraintException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.TriggerDefinitionDataException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOTriggerDefinition;
import org.oscm.rest.common.*;
import org.oscm.rest.common.RestBackend.Get;
import org.oscm.rest.trigger.data.DefinitionRepresentation;

/**
 * Backend class for the trigger definition resource.
 * 
 * @author miethaner
 */
@Stateless
public class DefinitionBackend {

    @EJB
    private TriggerDefinitionService service;

    public RestBackend.Get<DefinitionRepresentation, RequestParameters> getItem()
            throws WebApplicationException {

        return params -> {

            VOTriggerDefinition definition;
            try {
                definition = getService().getTriggerDefinition(params.getId());
            } catch (ObjectNotFoundException e) {
                throw WebException.notFound().message(e.getMessage())
                        .build();
            } catch (OperationNotPermittedException e) {
                throw WebException.forbidden().message(e.getMessage())
                        .build();
            } catch (Exception e) {
                if (e instanceof javax.ejb.EJBAccessException) {
                    throw WebException.forbidden()
                            .message(CommonParams.ERROR_NOT_AUTHORIZED)
                            .build();
                } else {
                    throw e;
                }
            }

            return new DefinitionRepresentation(definition);
        };
    }

    public Get<RepresentationCollection<DefinitionRepresentation>, RequestParameters> getCollection()
            throws WebApplicationException {

        return params -> {

            Collection<VOTriggerDefinition> definitions;
            try {
                definitions = getService().getTriggerDefinitions();
            } catch (Exception e) {
                if (e instanceof javax.ejb.EJBAccessException) {
                    throw WebException.forbidden()
                            .message(CommonParams.ERROR_NOT_AUTHORIZED)
                            .build();
                } else {
                    throw e;
                }
            }

            Collection<DefinitionRepresentation> representationList = new ArrayList<>();

            for (VOTriggerDefinition d : definitions) {
                representationList.add(new DefinitionRepresentation(d));
            }

            return new RepresentationCollection<>(
                    representationList);
        };
    }

    public RestBackend.Post<DefinitionRepresentation, RequestParameters> postCollection()
            throws WebApplicationException {

        return (content, params) -> {

            try {
                return getService()
                        .createTriggerDefinition(tranferToVO(content));
            } catch (TriggerDefinitionDataException e) {
                throw WebException.conflict().message(e.getMessage())
                        .build();
            } catch (ValidationException e) {
                throw WebException.badRequest().message(e.getMessage())
                        .build();
            } catch (Exception e) {
                if (e instanceof javax.ejb.EJBAccessException) {
                    throw WebException.forbidden()
                            .message(CommonParams.ERROR_NOT_AUTHORIZED)
                            .build();
                } else {
                    throw e;
                }
            }
        };
    }

    public RestBackend.Put<DefinitionRepresentation, RequestParameters> putItem()
            throws WebApplicationException {

        return (content, params) -> {

            try {

                VOTriggerDefinition definition = tranferToVO(content);

                if (content.getETag() == null) {
                    definition.setVersion(getService().getTriggerDefinition(
                            params.getId()).getVersion());
                }

                getService().updateTriggerDefinition(definition);
            } catch (ObjectNotFoundException e) {
                throw WebException.notFound().message(e.getMessage())
                        .build();
            } catch (ValidationException e) {
                throw WebException.badRequest().message(e.getMessage())
                        .build();
            } catch (ConcurrentModificationException | TriggerDefinitionDataException e) {
                throw WebException.conflict().message(e.getMessage())
                        .build();
            } catch (OperationNotPermittedException e) {
                throw WebException.forbidden().message(e.getMessage())
                        .build();
            } catch (Exception e) {
                if (e instanceof javax.ejb.EJBAccessException) {
                    throw WebException.forbidden()
                            .message(CommonParams.ERROR_NOT_AUTHORIZED)
                            .build();
                } else {
                    throw e;
                }
            }
            return true;
        };
    }

    public RestBackend.Delete<RequestParameters> deleteItem()
            throws WebApplicationException {

        return params -> {

            try {
                getService().deleteTriggerDefinition(params.getId());
            } catch (ObjectNotFoundException e) {
                throw WebException.notFound().message(e.getMessage())
                        .build();
            } catch (DeletionConstraintException e) {
                throw WebException.conflict().message(e.getMessage())
                        .build();
            } catch (OperationNotPermittedException e) {
                throw WebException.forbidden().message(e.getMessage())
                        .build();
            } catch (Exception e) {
                if (e instanceof javax.ejb.EJBAccessException) {
                    throw WebException.forbidden()
                            .message(CommonParams.ERROR_NOT_AUTHORIZED)
                            .build();
                } else {
                    throw e;
                }
            }
            return true;
        };
    }

    private VOTriggerDefinition tranferToVO(DefinitionRepresentation rep) {
        VOTriggerDefinition definition = new VOTriggerDefinition();

        if (rep.getId() != null) {
            definition.setKey(rep.getId());
        }

        if (rep.getETag() != null) {
            definition.setVersion(rep.getETag().intValue());
        }

        if (rep.getDescription() != null) {
            definition.setName(rep.getDescription());
        }

        if (rep.getAction() != null) {
            definition.setType(TriggerType.valueOf(rep.getAction()));
        }

        if (rep.getType() != null) {
            definition.setTargetType(TriggerTargetType.valueOf(rep.getType()));
        }

        if (rep.isSuspending() != null) {
            definition.setSuspendProcess(rep.isSuspending());
        }

        if (rep.getTargetURL() != null) {
            definition.setTarget(rep.getTargetURL());
        }

        return definition;
    }

    public TriggerDefinitionService getService() {
        return service;
    }
}
