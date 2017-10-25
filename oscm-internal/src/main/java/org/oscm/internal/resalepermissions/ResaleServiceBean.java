/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.resalepermissions;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Product;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningPartnerServiceLocal;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.vo.VOService;

@Stateless
@Remote(ResaleService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class ResaleServiceBean implements ResaleService {

    @EJB(beanInterface = DataService.class)
    DataService dm;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    @EJB(beanInterface = ServiceProvisioningPartnerServiceLocal.class)
    ServiceProvisioningPartnerServiceLocal spPartnerServiceLocal;

    @RolesAllowed({ "BROKER_MANAGER", "RESELLER_MANAGER" })
    public Response getServicesForVendor() {

        Response response = new Response();

        List<Product> productList = spPartnerServiceLocal
                .getProductsForVendor();

        LocalizerFacade facade = new LocalizerFacade(localizer, dm
                .getCurrentUser().getLocale());

        List<VOService> voServicesList = new ArrayList<VOService>();
        for (Product product : productList) {
            voServicesList.add(ProductAssembler.toVOProduct(product, facade));
        }

        response.getResults().add(voServicesList);

        return response;
    }

}
