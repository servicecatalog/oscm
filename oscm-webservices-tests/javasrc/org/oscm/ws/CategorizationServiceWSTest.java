/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: cheld                                                     
 *                                                                              
 *  Creation Date: 20.02.2012                                                      
 *                                                                              
 *  Completion Time: 20.02.2012                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oscm.intf.CategorizationService;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOCategory;
import org.oscm.vo.VOMarketplace;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.WebserviceTestBase;

/**
 * Test cases for the web service calls of the categorization service
 *
 * @author cheld
 */
public class CategorizationServiceWSTest {

  private static CategorizationService categorizationService;
  private static VOMarketplace marketplace;

  @BeforeClass
  public static void beforeClass() throws Exception {
    assumeFalse(ServiceFactory.getDefault().isSSOMode());
    WebserviceTestBase.getMailReader().deleteMails();

    categorizationService = ServiceFactory.getDefault().getCategorizationService();

    marketplace = WebserviceTestBase.getGlobalMarketplace();
  }

  @Before
  public void setUp() throws Exception {
    // remove existing categories
    List<VOCategory> existingCategories =
        categorizationService.getCategories(marketplace.getMarketplaceId(), "de");
    categorizationService.saveCategories(null, existingCategories, null);
  }

  @Test
  public void getCategories_localization() throws Exception {

    // create category with english locale and localize to german
    VOCategory cat = createVOCategory("newCat", "new category");
    categorizationService.saveCategories(Collections.singletonList(cat), null, "en");
    VOCategory localizedCategory =
        categorizationService.getCategories(marketplace.getMarketplaceId(), "en").get(0);
    localizedCategory.setName("neue Kategorie");
    categorizationService.saveCategories(Collections.singletonList(localizedCategory), null, "de");

    // when loading with german locale
    List<VOCategory> persistedCategories =
        categorizationService.getCategories(marketplace.getMarketplaceId(), "de");

    // then german is returned
    assertEquals(1, persistedCategories.size());
    assertEquals("neue Kategorie", persistedCategories.get(0).getName());
  }

  @Test
  public void getCategories_defaultLocale() throws Exception {

    // given a category with english locale
    VOCategory cat = createVOCategory("newCat", "new category");
    categorizationService.saveCategories(Collections.singletonList(cat), null, "en");

    // when retrieving with german locale
    List<VOCategory> persistedCategories =
        categorizationService.getCategories(marketplace.getMarketplaceId(), "de");

    // then english (default) locale is returned
    assertEquals("new category", persistedCategories.get(0).getName());
  }

  @Test
  public void getCategories_missingDefaultLocalization() throws Exception {

    // create category with german locale
    VOCategory cat = createVOCategory("newCat", "neue Kategorie");
    categorizationService.saveCategories(Collections.singletonList(cat), null, "de");

    // when loading with english locale
    List<VOCategory> persistedCategories =
        categorizationService.getCategories(marketplace.getMarketplaceId(), "en");

    // then no name will be returned
    assertEquals("", persistedCategories.get(0).getName());
  }

  @Test
  public void getCategories_invalidID() throws Exception {
    assertTrue(categorizationService.getCategories("invalidMarketplaceId", "de").isEmpty());
  }

  @Test
  public void saveCategories() throws Exception {

    // create category
    VOCategory cat = createVOCategory("newCat", "new category");
    categorizationService.saveCategories(Collections.singletonList(cat), null, "en");

    // then all properties are persisted
    List<VOCategory> persistedCategories =
        categorizationService.getCategories(marketplace.getMarketplaceId(), "en");
    assertEquals(1, persistedCategories.size());
    assertEquals("newCat", persistedCategories.get(0).getCategoryId());
    assertEquals("new category", persistedCategories.get(0).getName());
  }

  @Test
  public void saveCategories_update() throws Exception {

    // create category
    VOCategory cat = createVOCategory("newCat", "new category");
    categorizationService.saveCategories(Collections.singletonList(cat), null, "en");

    // when saving persisted categories again
    List<VOCategory> persistedCategories =
        categorizationService.getCategories(marketplace.getMarketplaceId(), "en");
    categorizationService.saveCategories(persistedCategories, null, "en");

    // then existing categories must have been updated
    persistedCategories = categorizationService.getCategories(marketplace.getMarketplaceId(), "en");
    assertEquals(1, persistedCategories.size());
  }

  @Test(expected = ValidationException.class)
  public void saveCategories_emptyId() throws Exception {
    VOCategory cat = createVOCategory(" ", "new category");
    categorizationService.saveCategories(Collections.singletonList(cat), null, "en");
  }

  @Test(expected = ValidationException.class)
  public void saveCategories_invalidId() throws Exception {
    VOCategory cat =
        createVOCategory(
            "too long. too long. too long. too long. too long. too long. too long. too long. too long. too long. too long. too long. too long. too long. ",
            "new category");
    categorizationService.saveCategories(Collections.singletonList(cat), null, "en");
  }

  @Test(expected = NonUniqueBusinessKeyException.class)
  public void saveCategories_duplicateId() throws Exception {

    // create category
    VOCategory cat = createVOCategory("duplicateId", "new category");
    categorizationService.saveCategories(Collections.singletonList(cat), null, "en");

    // when creating the category with the same id again, then exception is
    // thrown
    categorizationService.saveCategories(Collections.singletonList(cat), null, "en");
  }

  @Test(expected = NonUniqueBusinessKeyException.class)
  public void saveCategories_duplicateName() throws Exception {
    VOCategory cat = createVOCategory("newCat1", "duplicateName");
    categorizationService.saveCategories(Collections.singletonList(cat), null, "en");
    VOCategory cat2 = createVOCategory("newCat2", "duplicateName");
    categorizationService.saveCategories(Collections.singletonList(cat2), null, "en");
  }

  @Test(expected = ValidationException.class)
  public void saveCategories_emptyName() throws Exception {
    VOCategory cat = createVOCategory("newCat1", " ");
    categorizationService.saveCategories(Collections.singletonList(cat), null, "en");
  }

  @Test(expected = ValidationException.class)
  public void saveCategories_missingLocale() throws Exception {
    VOCategory cat = createVOCategory("newCat1", "new category");
    categorizationService.saveCategories(Collections.singletonList(cat), null, null);
  }

  @Test(expected = ValidationException.class)
  public void saveCategories_invalidLocale() throws Exception {
    VOCategory cat = createVOCategory("newCat1", "new category");
    categorizationService.saveCategories(Collections.singletonList(cat), null, " ");
  }

  @Test
  public void saveCategories_twoCategories() throws Exception {
    List<VOCategory> categories = new ArrayList<VOCategory>();
    VOCategory cat = createVOCategory("newCat1", "new category1");
    categories.add(cat);
    VOCategory cat2 = createVOCategory("newCat2", "new category2");
    categories.add(cat2);
    categorizationService.saveCategories(categories, null, "de");
    List<VOCategory> persistedCategoreis =
        categorizationService.getCategories(marketplace.getMarketplaceId(), "en");
    assertEquals(2, persistedCategoreis.size());
  }

  private VOCategory createVOCategory(String id, String name) {
    VOCategory cat = new VOCategory();
    cat.setCategoryId(id);
    cat.setMarketplaceId(marketplace.getMarketplaceId());
    cat.setName(name);
    return cat;
  }
}
