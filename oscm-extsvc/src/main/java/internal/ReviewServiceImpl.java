/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                    
 *                                                                              
 *  Creation Date: 17.05.2011                                                      
 *                                                                              
 *  Completion Time: 17.05.2011                                                 
 *                                                                              
 *******************************************************************************/

package internal;

import javax.jws.WebService;

import org.oscm.intf.ReviewService;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.vo.VOServiceFeedback;
import org.oscm.vo.VOServiceReview;

/**
 * This is a stub implementation of the {@link ReviewService} as the Metro
 * jax-ws tools do not allow to generate WSDL files from the service interfaces.
 * 
 * <p>
 * <b>WARNING:</b> Do not use this class, all methods simply throw an
 * {@link UnsupportedOperationException}!
 * </p>
 * 
 * @author cheld
 */
@WebService(serviceName = "ReviewService", targetNamespace = "http://oscm.org/xsd", portName = "ReviewServicePort", endpointInterface = "org.oscm.intf.ReviewService")
public class ReviewServiceImpl implements ReviewService {

    @Override
    public VOServiceReview writeReview(VOServiceReview review) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteReview(VOServiceReview review) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceFeedback getServiceFeedback(long productKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteReviewByMarketplaceOwner(VOServiceReview review,
            String reason) throws OperationNotPermittedException,
            ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

}
