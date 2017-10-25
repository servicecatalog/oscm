/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                    
 *                                                                              
 *  Creation Date: 20.02.2012                                                      
 *                                                                              
 *  Completion Time: 20.02.2012                                                
 *                                                                              
 *******************************************************************************/

package internal;

import java.util.List;

import javax.jws.WebService;

import org.oscm.intf.CategorizationService;
import org.oscm.vo.VOCategory;
import org.oscm.vo.VOService;

/**
 * This is a stub implementation of the {@link CategorizationService} as the
 * Metro jax-ws tools do not allow to generate WSDL files from the service
 * interfaces.
 * 
 * <p>
 * <b>WARNING:</b> Do not use this class, all methods simply throw an
 * {@link UnsupportedOperationException}!
 * </p>
 * 
 * @author cheld
 */
@WebService(serviceName = "CategorizationService", targetNamespace = "http://oscm.org/xsd", portName = "CategorizationServicePort", endpointInterface = "org.oscm.intf.CategorizationService")
public class CategorizationServiceImpl implements CategorizationService {

    @Override
    public List<VOCategory> getCategories(String marketplaceId, String local) {
        return null;
    }

    @Override
    public void saveCategories(List<VOCategory> toBeSaved,
            List<VOCategory> toBeDeleted, String local) {
    }

    @Override
    public List<VOService> getServicesForCategory(long categoryKey) {
        return null;
    }
}
