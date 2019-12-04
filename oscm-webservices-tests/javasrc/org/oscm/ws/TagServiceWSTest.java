/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Author: cheld
 *
 *  Creation Date: 16.12.2011
 *
 *  Completion Time: 16.12.2011
 *
 *******************************************************************************/
package org.oscm.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;

import java.util.Arrays;
import java.util.List;

import javax.xml.ws.soap.SOAPFaultException;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.ws.base.WebserviceTestSetup;
import org.oscm.vo.VOTag;
import org.oscm.vo.VOTechnicalService;
import org.oscm.intf.TagService;

/** @author cheld */
public class TagServiceWSTest {

  private static WebserviceTestSetup setup;
  private static VOTechnicalService technicalService;
  private static TagService tagService;

  @BeforeClass
  public static void setUp() throws Exception {
    assumeFalse(ServiceFactory.getDefault().isSSOMode());
    // clean the mails
    WebserviceTestBase.getMailReader().deleteMails();
    setup = new WebserviceTestSetup();
    setup.createSupplier("Supplier1");
    technicalService = setup.createTechnicalService();
    setup.addTags(technicalService, Arrays.asList("aaa", "abb", "bbb", "ccc"));
    tagService =
        ServiceFactory.getDefault()
            .getTagService(setup.getSupplierUserKey(), WebserviceTestBase.DEFAULT_PASSWORD);
  }

  @Ignore
  @Test
  public void getTagsByLocale() {
    List<VOTag> tags = tagService.getTagsByLocale("en");
    assertEquals(4, tags.size());
  }

  @Test
  public void getTagsByLocale_differentLocale() {
    List<VOTag> tags = tagService.getTagsByLocale("ja");
    assertEquals(0, tags.size());
  }

  @Test(expected = SOAPFaultException.class)
  public void getTagsByLocale_null() {
    List<VOTag> tags = tagService.getTagsByLocale(null);
    assertEquals(0, tags.size());
  }

  @Test
  public void getTagsByPattern_wildcard() {
    List<String> tags = tagService.getTagsByPattern("en", "a%", 5);
    assertEquals(2, tags.size());
  }

  @Test
  public void getTagsByPattern_wildcard2() {
    List<String> tags = tagService.getTagsByPattern("en", "_bb", 5);
    assertEquals(2, tags.size());
  }

  @Test
  public void getTagsByPattern_differentLocale() {
    List<String> tags = tagService.getTagsByPattern("ja", "_bb", 5);
    assertEquals(0, tags.size());
  }

  @Test
  public void getTagsByPattern_emptyPattern() {
    List<String> tags = tagService.getTagsByPattern("en", "", 5);
    assertEquals(0, tags.size());
  }

  @Test
  public void getTagsByPattern_limit() {
    List<String> tags = tagService.getTagsByPattern("en", "%", 3);
    assertEquals(3, tags.size());
  }
}
