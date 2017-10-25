/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.portallandingpage;

import java.util.Collections;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.landingpageService.local.LandingpageServiceLocal;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOService;

@Stateless
@Remote(LandingpageService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class LandingpageServiceBean implements LandingpageService {

    @EJB
    LandingpageServiceLocal landingpageService;

    public List<VOService> servicesForLandingpage(String marketplaceId,
            String locale) {
        List<VOService> services = null;
        try {
            services = landingpageService.servicesForPublicLandingpage(marketplaceId,
                    locale);
        } catch (ObjectNotFoundException onf) {
            services = Collections.emptyList();
        }
        return services;
    }

}
