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

package org.oscm.webservices;

import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.converter.api.ExceptionConverter;
import org.oscm.converter.api.VOConverter;
import org.oscm.intf.SearchService;
import org.oscm.types.exceptions.InvalidPhraseException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.vo.ListCriteria;
import org.oscm.vo.VOServiceListResult;

/**
 * End point facade for WS.
 * 
 * @author Dirk Bernsau
 */
@WebService(name = "SearchService", serviceName = "SearchService", targetNamespace = "http://oscm.org/xsd", endpointInterface = "org.oscm.intf.SearchService")
public class SearchServiceWS implements SearchService {

    WebServiceLogger WS_LOGGER = new WebServiceLogger(
            LoggerFactory.getLogger(SearchServiceWS.class));

    org.oscm.internal.intf.SearchService delegate;
    DataService ds;
    WebServiceContext wsContext;

    @Override
    public VOServiceListResult searchServices(String marketplaceId,
            String locale, String searchPhrase)
            throws InvalidPhraseException, ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate
                    .searchServices(marketplaceId, locale, searchPhrase));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.InvalidPhraseException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOServiceListResult getServicesByCriteria(String marketplaceId,
            String locale, ListCriteria listCriteria)
            throws ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter
                    .convertToApi(delegate.getServicesByCriteria(marketplaceId,
                            locale, VOConverter.convertToUp(listCriteria)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

}
