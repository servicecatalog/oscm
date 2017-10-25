/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: July 15, 2011                                                      
 *                                                                              
 *  Completion Time: July 15, 2011                                              
 *                                                                              
 *******************************************************************************/

package internal;

import javax.jws.WebService;

import org.oscm.intf.SearchService;
import org.oscm.vo.ListCriteria;
import org.oscm.vo.VOServiceListResult;

/**
 * This is a stub implementation of the {@link SearchService} as the Metro
 * jax-ws tools do not allow to generate WSDL files from the service interfaces.
 * 
 * <p>
 * <b>WARNING:</b> Do not use this class, all methods simply throw an
 * {@link UnsupportedOperationException}!
 * </p>
 * 
 * @author Dirk Bernsau
 * 
 */
@WebService(serviceName = "SearchService", targetNamespace = "http://oscm.org/xsd", portName = "SearchServicePort", endpointInterface = "org.oscm.intf.SearchService")
public class SearchServiceImpl implements SearchService {

    @Override
    public VOServiceListResult getServicesByCriteria(String marketplaceId,
            String locale, ListCriteria listCriteria) {
        return null;
    }

    @Override
    public VOServiceListResult searchServices(String marketplaceId,
            String locale, String searchPhrase) {
        throw new UnsupportedOperationException();
    }
}
