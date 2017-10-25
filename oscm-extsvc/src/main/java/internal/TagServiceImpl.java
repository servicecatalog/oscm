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
package internal;

import java.util.List;

import javax.jws.WebService;

import org.oscm.intf.TagService;
import org.oscm.vo.VOTag;

/**
 * This is a stub implementation of the {@link TagService} as the Metro jax-ws
 * tools do not allow to generate WSDL files from the service interfaces.
 * 
 * <p>
 * <b>WARNING:</b> Do not use this class, all methods simply throw an
 * {@link UnsupportedOperationException}!
 * </p>
 * 
 * @author goebel
 */
@WebService(serviceName = "TagService", targetNamespace = "http://oscm.org/xsd", portName = "TagServicePort", endpointInterface = "org.oscm.intf.TagService")
public class TagServiceImpl implements TagService {

    /**
     * @see TagService#getTagsByLocale(java.lang.String)
     */
    @Override
    public List<VOTag> getTagsByLocale(String locale) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.oscm.intf.TagService#getTagsForMarketplace(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public List<VOTag> getTagsForMarketplace(String locale, String marketplaceId) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see TagService#getTagsByPattern(java.lang.String, java.lang.String, int)
     */
    @Override
    public List<String> getTagsByPattern(String locale, String pattern,
            int maxTags) {
        throw new UnsupportedOperationException();
    }

}
