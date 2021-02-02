/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 29 Jul 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.internal.portallandingpage;

import org.junit.Before;
import org.junit.Test;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ImageResource;
import org.oscm.domobjects.Product;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.vo.VOImageResource;
import org.oscm.internal.vo.VOService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class LandingpageServiceBeanTest {

  private DataService ds;
  private ImageResourceServiceLocal irsl;
  private LandingpageServiceBean bean;
  private Product product;
  private Product productPartner;

  @Before
  public void setUp() {
    ds = mock(DataService.class);
    irsl = mock(ImageResourceServiceLocal.class);
    bean = spy(new LandingpageServiceBean());
    bean.ds = ds;
    bean.irsl = irsl;
  }

  @Test
  public void fillInServiceImages() {

    // given
    List<VOService> services = new ArrayList<>();
    VOService service = new VOService();
    service.setServiceId("test");
    service.setKey(123456789);
    service.setServiceType(ServiceType.TEMPLATE);
    services.add(service);
    byte[] bytes = new byte[]{11, 22, 33, 44};

    ImageResource imageResource = new ImageResource(123456789, ImageType.SERVICE_IMAGE);
    imageResource.setBuffer(bytes);

    product = new Product();
    product.setKey(987654321);
    product.setProductId("product");
    product.setType(ServiceType.TEMPLATE);

    doReturn(product).when(ds).find(any(), anyLong());
    doReturn(imageResource).when(irsl).read(anyLong(), anyObject());

    // when
    Map<Long, VOImageResource> result = bean.fillInServiceImages(services);

    // then
    assertEquals(ImageType.SERVICE_IMAGE, result.get(service.getKey()).getImageType());
    assertEquals(bytes, result.get(service.getKey()).getBuffer());
  }

  @Test
  public void fillInServiceImagesPartnerTemplate() {

    // given
    List<VOService> services = new ArrayList<>();
    VOService service = new VOService();
    service.setServiceId("test");
    service.setKey(123456789);
    service.setServiceType(ServiceType.TEMPLATE);
    services.add(service);
    byte[] bytes = new byte[]{11, 22, 33, 44};

    ImageResource imageResource = new ImageResource(123456789, ImageType.SERVICE_IMAGE);
    imageResource.setBuffer(bytes);

    product = new Product();
    product.setType(ServiceType.TEMPLATE);
    product.setProductId("template");
    product.setKey(987654321);
    productPartner = new Product();
    productPartner.setType(ServiceType.PARTNER_TEMPLATE);
    productPartner.setProductId("partner");
    productPartner.setKey(10000000);
    productPartner.setTemplate(product);

    doReturn(productPartner).when(ds).find(any(), anyLong());
    doReturn(imageResource).when(irsl).read(eq(987654321L), anyObject());

    // when
    Map<Long, VOImageResource> result = bean.fillInServiceImages(services);

    // then
    assertEquals(ImageType.SERVICE_IMAGE, result.get(service.getKey()).getImageType());
    assertEquals(bytes, result.get(service.getKey()).getBuffer());
  }
}
