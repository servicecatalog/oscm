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
package internal;

import java.util.List;

import javax.jws.WebService;

import org.oscm.intf.TriggerService;
import org.oscm.types.enumtypes.TriggerProcessParameterType;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.TriggerProcessStatusException;
import org.oscm.vo.VOLocalizedText;
import org.oscm.vo.VOTriggerDefinition;
import org.oscm.vo.VOTriggerProcess;
import org.oscm.vo.VOTriggerProcessParameter;

/**
 * This is a stub implementation of the {@link TriggerService} as the Metro
 * jax-ws tools do not allow to generate WSDL files from the service interfaces.
 * 
 * <p>
 * <b>WARNING:</b> Do not use this class, all methods simply throw an
 * {@link UnsupportedOperationException}!
 * </p>
 * 
 * @author Aleh Khomich
 */
@WebService(serviceName = "TriggerService", targetNamespace = "http://oscm.org/xsd", portName = "TriggerServicePort", endpointInterface = "org.oscm.intf.TriggerService")
public class TriggerServiceImpl implements TriggerService {

    @Override
    public void approveAction(long triggerProcessKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOTriggerProcess> getAllActions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOTriggerProcess> getAllActionsForOrganization() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rejectAction(long triggerProcessKey,
            List<VOLocalizedText> reason) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOTriggerDefinition> getAllDefinitions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cancelActions(List<Long> keys, List<VOLocalizedText> reason) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteActions(List<Long> keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateActionParameters(long actionKey,
            List<VOTriggerProcessParameter> parameters)
            throws ObjectNotFoundException, OperationNotPermittedException,
            TriggerProcessStatusException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOTriggerProcessParameter getActionParameter(long key,
            TriggerProcessParameterType paramType)
            throws ObjectNotFoundException, OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }
}
