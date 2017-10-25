/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: goebel                                                     
 *                                                                              
 *  Creation Date: 03.05.2011                                                      
 *                                                                              
 *  Completion Time: 03.05.2011                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.webservices;

import java.util.List;

import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.converter.api.VOCollectionConverter;
import org.oscm.intf.TagService;
import org.oscm.vo.VOTag;

/**
 * End point facade for WS.
 * 
 * @author goebel
 * 
 */
@WebService(name = "TagService", serviceName = "TagService", targetNamespace = "http://oscm.org/xsd", endpointInterface = "org.oscm.intf.TagService")
public class TagServiceWS implements TagService {

    WebServiceLogger WS_LOGGER = new WebServiceLogger(
            LoggerFactory.getLogger(TagServiceWS.class));

    org.oscm.internal.intf.TagService delegate;
    DataService ds;
    WebServiceContext wsContext;

    /**
     * @see TagService#getTagsByLocale(java.lang.String, boolean)
     */
    @Override
    public List<VOTag> getTagsByLocale(String locale) {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(
                delegate.getTagsByLocale(locale), org.oscm.vo.VOTag.class);
    }

    /**
     * @see org.oscm.intf.TagService#getTagsForMarketplace(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public List<VOTag> getTagsForMarketplace(String locale,
            String marketplaceId) {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(
                delegate.getTagsForMarketplace(locale, marketplaceId),
                org.oscm.vo.VOTag.class);
    }

    /**
     * @see TagService#getTagsByPattern(java.lang.String, java.lang.String, int)
     */
    @Override
    public List<String> getTagsByPattern(String locale, String pattern,
            int maxTags) {
        WS_LOGGER.logAccess(wsContext, ds);
        return delegate.getTagsByPattern(locale, pattern, maxTags);
    }

}
