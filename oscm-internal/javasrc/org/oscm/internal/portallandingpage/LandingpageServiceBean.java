/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.internal.portallandingpage;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ImageResource;
import org.oscm.domobjects.Product;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOImageResource;
import org.oscm.internal.vo.VOService;
import org.oscm.landingpageService.local.LandingpageServiceLocal;

@Stateless
@Remote(LandingpageService.class)
@Interceptors({InvocationDateContainer.class, ExceptionMapper.class})
public class LandingpageServiceBean implements LandingpageService {

  @EJB LandingpageServiceLocal landingpageService;

  @EJB DataService ds;

  @EJB ImageResourceServiceLocal irsl;

  public List<VOService> servicesForLandingpage(String marketplaceId, String locale) {
    List<VOService> services = null;
    try {
      services = landingpageService.servicesForPublicLandingpage(marketplaceId, locale);
    } catch (ObjectNotFoundException onf) {
      services = Collections.emptyList();
    }
    return services;
  }

  public Map<Long, VOImageResource> fillInServiceImages(List<VOService> services) {
    Map<Long, VOImageResource> images = new HashMap<Long, VOImageResource>();
    for (VOService service : services) {
      VOImageResource vo = null;

      Product product = ds.find(Product.class, service.getKey());

      if (product != null) {

        AtomicLong templateKey = new AtomicLong(product.getKey());

        Optional.ofNullable(product.getType())
            .filter(ServiceType.PARTNER_TEMPLATE::equals)
            .ifPresent(isPartner -> templateKey.set(product.getTemplate().getKey()));

        ImageResource imageResource = irsl.read(templateKey.get(), ImageType.SERVICE_IMAGE);
        if (imageResource != null) {
          vo = new VOImageResource();
          vo.setBuffer(imageResource.getBuffer());
          vo.setContentType(imageResource.getContentType());
          vo.setImageType(ImageType.SERVICE_IMAGE);
          images.put(service.getKey(), vo);
        }
      }
    }
    return images;
  }
}
