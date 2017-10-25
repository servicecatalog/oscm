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

import org.oscm.intf.TriggerDefinitionService;
import org.oscm.types.enumtypes.TriggerType;
import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.DeletionConstraintException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.TriggerDefinitionDataException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOTriggerDefinition;

/**
 * This is a stub implementation of the {@link TriggerDefinitionService} as the
 * Metro jax-ws tools do not allow to generate WSDL files from the service
 * interfaces.
 * 
 * <p>
 * <b>WARNING:</b> Do not use this class, all methods simply throw an
 * {@link UnsupportedOperationException}!
 * </p>
 * 
 */
@WebService(serviceName = "TriggerDefinitionService", targetNamespace = "http://oscm.org/xsd", portName = "TriggerDefinitionServicePort", endpointInterface = "org.oscm.intf.TriggerDefinitionService")
public class TriggerDefinitionServiceImpl implements TriggerDefinitionService {

    @Override
    public void createTriggerDefinition(VOTriggerDefinition vo)
            throws TriggerDefinitionDataException, ValidationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteTriggerDefinition(long key)
            throws ObjectNotFoundException, DeletionConstraintException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateTriggerDefinition(VOTriggerDefinition vo)
            throws ObjectNotFoundException, ValidationException,
            ConcurrentModificationException, TriggerDefinitionDataException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOTriggerDefinition> getTriggerDefinitions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<TriggerType> getTriggerTypes() {
        throw new UnsupportedOperationException();
    }

}
