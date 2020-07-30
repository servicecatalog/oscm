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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.ImageResource;
import org.oscm.domobjects.Product;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOImageResource;
import org.oscm.internal.vo.VOService;

/** @author worf */
public class LandingpageServiceBeanTest {

  private DataService ds;
  private ImageResourceServiceLocal irsl;
  private LandingpageServiceBean bean;

  @Before
  public void setUp() {
    ds = mock(DataService.class);
    irsl = mock(ImageResourceServiceLocal.class);
    bean = spy(new LandingpageServiceBean());
    bean.ds = ds;
    bean.irsl = irsl;
  }

  @Test
  public void fillInServiceImages() throws ObjectNotFoundException {

    // given
    List<VOService> services = new ArrayList<VOService>();
    VOService service = new VOService();
    service.setServiceId("test");
    service.setKey(123456789);
    services.add(service);
    byte[] bytes = new byte[] {11, 22, 33, 44};

    ImageResource imageResource = new ImageResource(123456789, ImageType.SERVICE_IMAGE);
    imageResource.setBuffer(bytes);

    DomainObject<?> product = new Product();

    doReturn(product).when(ds).find(any(), anyLong());
    doReturn(imageResource).when(irsl).read(anyLong(), anyObject());

    // when
    Map<VOService, VOImageResource> result = bean.fillInServiceImages(services);

    // then
    assertEquals(ImageType.SERVICE_IMAGE, result.get(service).getImageType());
    assertEquals(bytes, result.get(service).getBuffer());
  }
}
