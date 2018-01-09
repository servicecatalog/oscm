/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Apr 28, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.rest.trigger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.WebApplicationException;

import org.oscm.internal.intf.TriggerService;
import org.oscm.internal.types.exception.ExecutionTargetException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.TriggerProcessStatusException;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.rest.common.CommonParams;
import org.oscm.rest.common.RequestParameters;
import org.oscm.rest.common.RestBackend;
import org.oscm.rest.common.WebException;
import org.oscm.rest.trigger.data.ProcessRepresentation;

/**
 * Backend class for the trigger process resource.
 * 
 * @author miethaner
 */
@Stateless
public class ProcessBackend {

    @EJB
    private TriggerService service;

    public RestBackend.Put<ProcessRepresentation, RequestParameters> putApprove()
            throws WebApplicationException {

        return (content, params) -> {

            try {
                getService().approveAction(params.getId());
            } catch (ObjectNotFoundException e) {
                throw WebException.notFound().message(e.getMessage())
                        .build();
            } catch (OperationNotPermittedException e) {
                throw WebException.forbidden().message(e.getMessage())
                        .build();
            } catch (TriggerProcessStatusException | ExecutionTargetException e) {
                throw WebException.conflict().message(e.getMessage())
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

    public RestBackend.Put<ProcessRepresentation, RequestParameters> putReject()
            throws WebApplicationException {

        return (content, params) -> {

            try {
                List<VOLocalizedText> reason = new ArrayList<>();
                reason.add(new VOLocalizedText(
                        Locale.ENGLISH.getLanguage(), content.getComment()));

                getService().rejectAction(params.getId(), reason);
            } catch (ObjectNotFoundException e) {
                throw WebException.notFound().message(e.getMessage())
                        .build();
            } catch (OperationNotPermittedException e) {
                throw WebException.forbidden().message(e.getMessage())
                        .build();
            } catch (TriggerProcessStatusException e) {
                throw WebException.conflict().message(e.getMessage())
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

    public RestBackend.Put<ProcessRepresentation, RequestParameters> putCancel()
            throws WebApplicationException {

        return (content, params) -> {

            try {
                List<VOLocalizedText> reason = new ArrayList<VOLocalizedText>();
                reason.add(new VOLocalizedText(
                        Locale.ENGLISH.getLanguage(), content.getComment()));

                getService().cancelActions(Arrays.asList(params.getId()), reason);
            } catch (ObjectNotFoundException e) {
                throw WebException.notFound().message(e.getMessage())
                        .build();
            } catch (OperationNotPermittedException e) {
                throw WebException.forbidden().message(e.getMessage())
                        .build();
            } catch (TriggerProcessStatusException e) {
                throw WebException.conflict().message(e.getMessage())
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

    public TriggerService getService() {
        return service;
    }
}
