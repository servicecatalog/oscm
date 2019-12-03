/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Enes Sejfi                                               
 *                                                                              
 *  Creation Date: 09.01.2012                                                     
 *                                                                              
 *  Completion Time:                                               
 *                                                                              
 *******************************************************************************/
package org.oscm.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oscm.intf.AccountService;
import org.oscm.intf.MarketplaceService;
import org.oscm.intf.SearchService;
import org.oscm.intf.ServiceProvisioningService;
import org.oscm.types.enumtypes.OrganizationRoleType;
import org.oscm.types.enumtypes.PriceModelType;
import org.oscm.types.enumtypes.PricingPeriod;
import org.oscm.types.enumtypes.Sorting;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.vo.ListCriteria;
import org.oscm.vo.VOMarketplace;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOPriceModel;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServiceDetails;
import org.oscm.vo.VOServiceListResult;
import org.oscm.vo.VOTechnicalService;
import org.oscm.vo.VOUserDetails;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.TSXMLForWebService;
import org.oscm.ws.base.VOFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.ws.base.WebserviceTestSetup;

/**
 * Tests the search service webservice.
 *
 * @author Enes Sejfi
 */
public class SearchServiceWSTest {

  private static WebserviceTestSetup setup;
  private static SearchService searchService_Supplier;
  private static AccountService accountService_TechnologyProvider;
  private static MarketplaceService mpService_Operator;
  private static VOFactory factory = new VOFactory();
  private static List<AccountService> accService_SupplierList;
  private static List<ServiceProvisioningService> spService_SupplierList;
  private static List<MarketplaceService> mpService_SupplierList;
  private static ServiceProvisioningService spService_TechnologyProvider;

  private static List<VOOrganization> supplierList;
  private static List<VOMarketplace> marketplaces;
  private static List<VOService> servicesForMarketplace1;

  private static int NUMBER_SERVICES = 5;
  private static final int INITIAL_NUMBER_SERVICES = NUMBER_SERVICES;
  private static int UNLIMITED = -1; // neg. limits return all services

  /**
   * domain: integration-master-index <br>
   * glassfish settings: JVM options <br>
   * -Dhibernate.search.default.refresh=5 <br>
   * time in seconds
   */
  private static int masterIndexRefreshTime = 5;

  /**
   * empirical determined <br>
   * time in seconds
   */
  private static int slaveIndexRefreshTime = 55;

  /** to temporarily switch off the delay */
  private static boolean waitForRefresh = true;

  @BeforeClass
  public static void setup() throws Exception {
    init();
    sleep();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    deleteAllServices();
    WebserviceTestBase.deleteMarketplaces();
  }

  /**
   * Prepares test data if needed in order to get an consistent status for each test. Prevent issues
   * due to running test method in different sequence.
   */
  @Before
  public void prepareTestDataIfNeeded() throws Exception {
    if (marketplaces.size() != 2) {
      WebserviceTestBase.deleteMarketplaces();
      create2Marketplaces();
    }

    if (NUMBER_SERVICES != INITIAL_NUMBER_SERVICES) {
      System.out.println("----------------------------------------");
      System.out.println("Re-new services... in order to get consistent test data!");
      deleteAllServices();
      createServicesForMarketplace1(INITIAL_NUMBER_SERVICES);

      NUMBER_SERVICES = INITIAL_NUMBER_SERVICES;
      System.out.println("----------------------------------------");
      sleep();
    }
    assertTrue(INITIAL_NUMBER_SERVICES == NUMBER_SERVICES);
    assertTrue(INITIAL_NUMBER_SERVICES == servicesForMarketplace1.size());
  }

  /**
   * Initialize and setup the testcases. <br>
   * 1. All mails from mailserver are deleted.<br>
   * 2. A technology provider and a supplier are created<br>
   * 3. Two marketplaces are created<br>
   * 4. Services are created for marketplace one but not for marketplace two
   */
  private static void init() throws Exception {
    WebserviceTestBase.getMailReader().deleteMails();

    setup = new WebserviceTestSetup();

    // needed for deleting marketplaces
    mpService_Operator =
        ServiceFactory.getDefault()
            .getMarketPlaceService(
                WebserviceTestBase.getPlatformOperatorKey(),
                WebserviceTestBase.getPlatformOperatorPassword());

    // needed to add supplier to technical services
    String technologyProviderUserKey = createTechnologyProvider();
    accountService_TechnologyProvider =
        ServiceFactory.getDefault()
            .getAccountService(technologyProviderUserKey, WebserviceTestBase.DEFAULT_PASSWORD);

    // needed to create technical services
    spService_TechnologyProvider =
        ServiceFactory.getDefault()
            .getServiceProvisioningService(
                technologyProviderUserKey, WebserviceTestBase.DEFAULT_PASSWORD);

    WebserviceTestBase.getOperator().addCurrency("EUR");

    create1Supplier();
    create2Marketplaces();
    createServicesForMarketplace1(INITIAL_NUMBER_SERVICES + 1);

    // remove last service
    ServiceProvisioningService sps = spService_SupplierList.get(0);
    VOService serviceToDelete =
        sps.deactivateService(servicesForMarketplace1.get(servicesForMarketplace1.size() - 1));
    sps.deleteService(serviceToDelete);
    servicesForMarketplace1.remove(servicesForMarketplace1.size() - 1);
    System.out.println("Available services after delete: " + servicesForMarketplace1.size());
  }

  @Test
  public void testSearchServicesWithTagOnMarketplace1() throws Exception {
    VOServiceListResult result =
        searchService_Supplier.searchServices(
            getMarketplace("1").getMarketplaceId(), "de", "enterprise_de");

    assertEquals(NUMBER_SERVICES, result.getServices().size());
    assertEquals(NUMBER_SERVICES, result.getResultSize());
  }

  @Test
  public void testSearchServicesWithPartialShortDescription() throws Exception {
    searchServicesAndValidate("shortdescription", Integer.valueOf(0), 5, 5);
    searchServicesAndValidate("shortdescription", Integer.valueOf(2), 5, 5);
  }

  @Test
  public void testSearchServicesWithShortDescription_CaseSentivitiy() throws Exception {
    searchServicesAndValidate("Shortdescription", Integer.valueOf(0), 5, 5);
    searchServicesAndValidate("Shortdescription", Integer.valueOf(2), 5, 5);
  }

  @Test
  public void testSearchServicesWithServiceId() throws Exception {
    searchServicesAndValidate(
        servicesForMarketplace1.get(0).getServiceId(), Integer.valueOf(0), 1, 1);
  }

  @Test
  public void testSearchServicesWithPriceModelDescription() throws Exception {
    searchServicesAndValidate("pricemodel", Integer.valueOf(0), 5, 5);
  }

  @Test
  public void testSearchServicesWithCompleteShortDescription() throws Exception {
    String searchPhrase = "shortdescription_1";
    VOServiceListResult result =
        searchService_Supplier.searchServices(
            getMarketplace("1").getMarketplaceId(), "de", searchPhrase);

    assertEquals(1, result.getServices().size());
    assertEquals(1, result.getResultSize());
    VOService service = result.getServices().get(0);

    assertTrue(service.getName().endsWith("_1"));
    assertEquals(searchPhrase, service.getShortDescription());
    assertEquals("description_1", service.getDescription());
  }

  @Test
  public void testSearchServicesWithAndCondition() throws Exception {
    String shortDescription = "shortdescription_1";
    String searchPhrase = "enterprise_de " + shortDescription;

    searchServicesWithCondition(searchPhrase.toLowerCase(), 1, 1, shortDescription);

    searchServicesWithCondition(searchPhrase.toUpperCase(), 1, 1, shortDescription);
  }

  @Test
  public void testSearchServicesWithDescription() throws Exception {
    searchServicesAndValidate("description", Integer.valueOf(2), 5, 5);
    searchServicesAndValidate("description", Integer.valueOf(4), 5, 5);
  }

  @Test
  public void testSearchServicesWithLanguageEn() throws Exception {
    VOServiceListResult result =
        searchService_Supplier.searchServices(
            getMarketplace("1").getMarketplaceId(), "en", "enterprise_de");

    assertEquals(0, result.getServices().size());
    assertEquals(0, result.getResultSize());
  }

  @Test(expected = ObjectNotFoundException.class)
  public void testSearchServicesWithNonExistingMarketplace() throws Exception {
    searchService_Supplier.searchServices("invalidMarketplaceId", "de", "enterprise_de");
  }

  @Test
  public void testGetServicesByCriteriaOnMarketplace1() throws Exception {
    ListCriteria c = new ListCriteria();
    c.setLimit(UNLIMITED);

    VOServiceListResult result =
        searchService_Supplier.getServicesByCriteria(
            getMarketplace("1").getMarketplaceId(), "de", c);
    assertNotNull(result);
    assertEquals(NUMBER_SERVICES, result.getServices().size());
    assertEquals(NUMBER_SERVICES, result.getResultSize());
  }

  @Test
  public void testGetServicesByCriteriaOnMarketplace2() throws Exception {
    ListCriteria c = new ListCriteria();
    c.setLimit(UNLIMITED);

    VOServiceListResult result =
        searchService_Supplier.getServicesByCriteria(
            getMarketplace("2").getMarketplaceId(), "de", c);
    assertNotNull(result);

    // No service was created for marketplace 2. No service should be found.
    assertEquals(0, result.getServices().size());
    assertEquals(0, result.getResultSize());
  }

  @Test
  public void testGetServicesByCriteriaWithLocaleDe() throws Exception {
    ListCriteria c = new ListCriteria();
    c.setLimit(UNLIMITED);

    // Services are created with profile language 'de', so ALL services
    // should be found
    VOServiceListResult result =
        searchService_Supplier.getServicesByCriteria(
            getMarketplace("1").getMarketplaceId(), "de", c);
    assertNotNull(result);
    assertEquals(NUMBER_SERVICES, result.getServices().size());
    assertEquals(NUMBER_SERVICES, result.getResultSize());

    // result is sorted by activation date
    for (int i = 0; i < result.getServices().size(); i++) {
      VOService service = result.getServices().get(i);

      assertNotNull(service.getName());

      // default sorting is ACTIVATION_ASCENDING. Make sure that the
      // services are in order 4, 3, 2, 1, 0
      assertTrue(service.getName().endsWith("_" + (result.getServices().size() - i - 1)));
    }
  }

  @Test
  public void testGetServicesByCriteriaWithLocaleEn_Bug8781() throws Exception {
    ListCriteria c = new ListCriteria();
    c.setLimit(UNLIMITED);

    // Services are created with profile language 'de', so NO services
    // should be found
    VOServiceListResult result =
        searchService_Supplier.getServicesByCriteria(
            getMarketplace("1").getMarketplaceId(), "en", c);
    assertNotNull(result);
    assertEquals(NUMBER_SERVICES, result.getServices().size());
    assertEquals(NUMBER_SERVICES, result.getResultSize());

    // result is sorted by activation date
    for (int i = 0; i < result.getServices().size(); i++) {
      VOService service = result.getServices().get(i);

      assertEquals("", service.getName());
      assertEquals("", service.getNameToDisplay());
    }
  }

  @Test
  public void testGetServicesByCriteria_Paging() throws Exception {
    getServicesByCriteriaAndValidate(0, 2, 2); // result : 0,1
    getServicesByCriteriaAndValidate(3, 1, 1); // result : 3
    getServicesByCriteriaAndValidate(3, 3, 2); // result : 3,4
    getServicesByCriteriaAndValidate(4, 2, 1); // result : 4
    getServicesByCriteriaAndValidate(5, 2, 0); // result : 0

    // deletes the first service and executes the search again
    deactivateAndDeleteService(servicesForMarketplace1.get(0));
    VOServiceListResult result = getServicesByCriteria_SortedByActivationAsc(0, 1, 1);

    // should not end with _0 !
    assertTrue(result.getServices().get(0).getName().endsWith("_1"));
  }

  @Test
  public void testGetServicesByCriteriaWithExistingTagName() throws Exception {
    ListCriteria c = new ListCriteria();
    c.setLimit(UNLIMITED);
    c.setFilter("enterprise_de");

    VOServiceListResult result =
        searchService_Supplier.getServicesByCriteria(
            getMarketplace("1").getMarketplaceId(), "de", c);
    assertNotNull(result);
    assertEquals(NUMBER_SERVICES, result.getServices().size());
    assertEquals(NUMBER_SERVICES, result.getResultSize());
  }

  @Test(expected = ObjectNotFoundException.class)
  public void testGetServicesByCriteriaWithInvalidTagName() throws Exception {
    ListCriteria c = new ListCriteria();
    c.setLimit(UNLIMITED);
    c.setFilter("enterprise_de g");

    searchService_Supplier.getServicesByCriteria(getMarketplace("1").getMarketplaceId(), "de", c);
  }

  @Test
  public void testGetServicesByCriteria_SetLimit() throws Exception {
    int pageSize = 3;

    ListCriteria c = new ListCriteria();
    c.setLimit(pageSize);

    assertEquals(0, c.getOffset());
    assertEquals(pageSize, c.getLimit());
    assertNull(c.getFilter());
    assertNull(c.getSorting());

    VOServiceListResult result =
        searchService_Supplier.getServicesByCriteria(
            getMarketplace("1").getMarketplaceId(), "de", c);

    assertNotNull(result);
    assertEquals(pageSize, result.getServices().size());
    assertEquals(NUMBER_SERVICES, result.getResultSize());
  }

  @Test
  public void testGetServicesByCriteria_SetLimit0() throws Exception {
    ListCriteria c = new ListCriteria();
    c.setLimit(0);

    assertEquals(0, c.getOffset());
    assertEquals(0, c.getLimit());
    assertNull(c.getFilter());
    assertNull(c.getSorting());

    VOServiceListResult result =
        searchService_Supplier.getServicesByCriteria(
            getMarketplace("1").getMarketplaceId(), "de", c);

    assertNotNull(result);
    assertEquals(0, result.getServices().size());
    assertEquals(NUMBER_SERVICES, result.getResultSize());
  }

  @Test
  public void testGetServicesByCriteria_GetAllServices() throws Exception {
    ListCriteria c = new ListCriteria();
    c.setLimit(UNLIMITED);

    assertEquals(0, c.getOffset());
    assertEquals(UNLIMITED, c.getLimit());
    assertNull(c.getFilter());
    assertNull(c.getSorting());

    VOServiceListResult result =
        searchService_Supplier.getServicesByCriteria(
            getMarketplace("1").getMarketplaceId(), "de", c);

    assertNotNull(result);
    assertEquals(NUMBER_SERVICES, result.getServices().size());
    assertEquals(NUMBER_SERVICES, result.getResultSize());
  }

  @Test
  public void testGetServicesByCriteria_SetNegativeOffset() throws Exception {
    int newOffset = -3;
    int pageSize = 4;

    ListCriteria c = new ListCriteria();
    c.setOffset(newOffset);
    c.setLimit(pageSize);

    assertEquals(newOffset, c.getOffset());
    assertEquals(pageSize, c.getLimit());
    assertNull(c.getFilter());
    assertNull(c.getSorting());

    VOServiceListResult result =
        searchService_Supplier.getServicesByCriteria(
            getMarketplace("1").getMarketplaceId(), "de", c);

    assertNotNull(result);
    assertEquals(pageSize, result.getServices().size());
    assertEquals(NUMBER_SERVICES, result.getResultSize());
  }

  @Test
  public void testGetServicesByCriteria_OffsetIsGreaterThanResultSize() throws Exception {
    int newOffset = NUMBER_SERVICES * 2;
    int newLimit = -1;

    ListCriteria c = new ListCriteria();
    c.setOffset(newOffset);
    c.setLimit(newLimit);

    assertEquals(newOffset, c.getOffset());
    assertEquals(newLimit, c.getLimit());
    assertNull(c.getFilter());
    assertNull(c.getSorting());

    VOServiceListResult result =
        searchService_Supplier.getServicesByCriteria(
            getMarketplace("1").getMarketplaceId(), "de", c);

    assertNotNull(result);
    assertEquals(0, result.getServices().size());
    assertEquals(NUMBER_SERVICES, result.getResultSize());
  }

  @Test(expected = ObjectNotFoundException.class)
  public void testGetServicesByCriteriaWithInvalidFilter() throws Exception {
    ListCriteria c = new ListCriteria();
    c.setFilter("f° ¬ gz66 3s &/($=P= !\")(/&% $ § 7894!$");

    searchService_Supplier.getServicesByCriteria(getMarketplace("1").getMarketplaceId(), "de", c);
  }

  @Test(expected = ObjectNotFoundException.class)
  public void testGetServicesByCriteria_DeleteMarketplace() throws Exception {

    VOServiceListResult result =
        searchService_Supplier.getServicesByCriteria(
            "nonExistingMarketplaceId", "de", new ListCriteria());

    assertEquals(0, result.getServices().size());
    assertEquals(0, result.getResultSize());
  }

  /** Creates a new supplier with profile language 'de'. */
  private static void create1Supplier() throws Exception {
    // initialize some lists
    supplierList = new LinkedList<VOOrganization>();
    spService_SupplierList = new LinkedList<ServiceProvisioningService>();
    mpService_SupplierList = new LinkedList<MarketplaceService>();
    accService_SupplierList = new LinkedList<AccountService>();

    // creates a supplier
    VOOrganization supplier = setup.createSupplier("supplier1");
    supplierList.add(supplier);

    // new Marketplace webservice reference
    mpService_SupplierList.add(
        ServiceFactory.getDefault()
            .getMarketPlaceService(
                setup.getSupplierUserKey(), WebserviceTestBase.DEFAULT_PASSWORD));

    // new Account webservice reference
    accService_SupplierList.add(
        ServiceFactory.getDefault()
            .getAccountService(setup.getSupplierUserKey(), WebserviceTestBase.DEFAULT_PASSWORD));

    // Changes the profile language to 'de'. Initial language was 'en'
    supplier.setLocale("de");
    VOUserDetails userDetails = setup.getIdentitySrvAsSupplier().getCurrentUserDetails();
    userDetails.setLocale("de");
    setup.getAccountServiceAsSupplier().updateAccountInformation(supplier, userDetails, null, null);

    // new ServiceProvisioning webservice reference
    spService_SupplierList.add(
        ServiceFactory.getDefault()
            .getServiceProvisioningService(
                setup.getSupplierUserKey(), WebserviceTestBase.DEFAULT_PASSWORD));

    // needed for searching services
    searchService_Supplier =
        ServiceFactory.getDefault()
            .getSearchService(
                Long.toString(userDetails.getKey()), WebserviceTestBase.DEFAULT_PASSWORD);
  }

  /** Creates two marketplaces and adds supplier to the marketplaces. */
  private static void create2Marketplaces() throws Exception {
    marketplaces = new LinkedList<VOMarketplace>();
    for (int i = 0; i < 2; i++) {
      VOMarketplace marketplace = factory.createMarketplaceVO(null, false, "mp");
      marketplaces.add(mpService_Operator.createMarketplace(marketplace));
    }

    // add supplier to marketplaces
    List<String> supplierIds = new LinkedList<String>();
    supplierIds.add(supplierList.get(0).getOrganizationId());

    mpService_Operator.addOrganizationsToMarketplace(
        supplierIds, marketplaces.get(0).getMarketplaceId());

    mpService_Operator.addOrganizationsToMarketplace(
        supplierIds, marketplaces.get(1).getMarketplaceId());
  }

  /** Creates a new technical service and marketable service for marketplace1 by given count. */
  private static void createServicesForMarketplace1(int marketableServiceCount) throws Exception {
    servicesForMarketplace1 = new LinkedList<VOService>();

    VOTechnicalService technicalService =
        createTechnicalService("tp1", spService_TechnologyProvider);

    accountService_TechnologyProvider.addSuppliersForTechnicalService(
        technicalService, getOrganizationIds(supplierList));

    VOMarketplace mp1 = getMarketplace("1");
    for (int i = 0; i < marketableServiceCount; i++) {
      VOService service =
          createService(
              "service",
              Integer.toString(i),
              mp1,
              spService_SupplierList.get(0),
              mpService_SupplierList.get(0),
              technicalService);
      servicesForMarketplace1.add(service);
    }
  }

  private static VOService createService(
      String namePrefix,
      String namePostfix,
      VOMarketplace voMarketPlace,
      ServiceProvisioningService sps,
      MarketplaceService mpService,
      VOTechnicalService technicalService)
      throws Exception {
    // creates a new marketable service
    VOService newService =
        factory.createMarketableServiceVO(
            namePrefix + "_" + WebserviceTestBase.createUniqueKey() + "_" + namePostfix);

    newService.setShortDescription("shortdescription_" + namePostfix);
    newService.setDescription("description_" + namePostfix);

    VOServiceDetails serviceDetails = sps.createService(technicalService, newService, null);

    // creates a new price model
    VOPriceModel priceModel = new VOPriceModel();
    priceModel.setType(PriceModelType.PRO_RATA);
    priceModel.setPeriod(PricingPeriod.DAY);
    priceModel.setPricePerPeriod(new BigDecimal(1));
    priceModel.setDescription("pricemodel_" + namePostfix);
    priceModel.setCurrencyISOCode("EUR");
    serviceDetails = setup.savePriceModel(serviceDetails, priceModel);

    // publishes the service to the marketplace
    WebserviceTestBase.publishToMarketplace(serviceDetails, true, mpService, voMarketPlace);

    // activates the service
    return sps.activateService(serviceDetails);
  }

  private static VOTechnicalService createTechnicalService(
      String technicalProductId, ServiceProvisioningService sps) throws Exception {
    String tsxml = TSXMLForWebService.createTSXMLWithTags(technicalProductId);
    return WebserviceTestBase.createTechnicalService(tsxml, sps);
  }

  /** Creates a list of organization ids with given list of organizations. */
  private static List<String> getOrganizationIds(List<VOOrganization> organizations) {
    List<String> result = new LinkedList<String>();
    for (VOOrganization organization : organizations) {
      result.add(organization.getOrganizationId());
    }
    return result;
  }

  private static String createTechnologyProvider() throws Exception {
    WebserviceTestBase.createOrganization(
        "newTechnologyProvider" + WebserviceTestBase.createUniqueKey(),
        "newTechnologyProvider" + WebserviceTestBase.createUniqueKey(),
        OrganizationRoleType.TECHNOLOGY_PROVIDER);
    return WebserviceTestBase.readLastMailAndSetCommonPassword();
  }

  private static void deactivateAndDeleteService(VOService serviceToDelete) throws Exception {
    ServiceProvisioningService sps = spService_SupplierList.get(0);
    serviceToDelete = sps.deactivateService(serviceToDelete);
    sps.deleteService(serviceToDelete);

    // decrement the number of available services
    NUMBER_SERVICES--;

    sleep();
  }

  private void getServicesByCriteriaAndValidate(int offset, int limit, int expectedSize)
      throws Exception {

    VOServiceListResult result =
        getServicesByCriteria_SortedByActivationAsc(offset, limit, expectedSize);

    for (int i = 0, offsetCnt = offset; i < result.getServices().size(); i++) {
      assertTrue(result.getServices().get(i).getName().endsWith("_" + (offsetCnt + i)));
    }
  }

  private VOServiceListResult getServicesByCriteria_SortedByActivationAsc(
      int offset, int limit, int expectedSize) throws Exception {
    ListCriteria c = new ListCriteria();
    c.setOffset(offset);
    c.setLimit(limit);
    c.setSorting(Sorting.ACTIVATION_ASCENDING);

    assertEquals(offset, c.getOffset());
    assertEquals(limit, c.getLimit());
    assertNull(c.getFilter());
    assertEquals(Sorting.ACTIVATION_ASCENDING, c.getSorting());

    VOServiceListResult result =
        searchService_Supplier.getServicesByCriteria(
            getMarketplace("1").getMarketplaceId(), "de", c);

    assertNotNull(result);
    assertEquals(NUMBER_SERVICES, result.getResultSize());
    assertEquals(expectedSize, result.getServices().size());

    return result;
  }

  /** Deletes all services from supplier. */
  private static void deleteAllServices() throws Exception {
    if (spService_SupplierList != null) {
      for (ServiceProvisioningService sps : spService_SupplierList) {
        for (VOMarketplace marketplace : marketplaces) {
          // select all services from marketplace
          List<VOService> servicesToDelete =
              sps.getServicesForMarketplace(marketplace.getMarketplaceId());

          waitForRefresh = false;
          for (VOService serviceToDelete : servicesToDelete) {
            deactivateAndDeleteService(serviceToDelete);
          }
          waitForRefresh = true;
        }
      }
    }

    if (spService_TechnologyProvider != null) {
      List<VOTechnicalService> technicalServices =
          spService_TechnologyProvider.getTechnicalServices(
              OrganizationRoleType.TECHNOLOGY_PROVIDER);
      for (VOTechnicalService voTechnicalService : technicalServices) {
        spService_TechnologyProvider.deleteTechnicalService(voTechnicalService);
      }
    }
  }

  private void searchServicesAndValidate(
      String searchPhrase, Integer searchPhrasePostfix, int serviceSize, int resultSize)
      throws Exception {
    VOServiceListResult result =
        searchService_Supplier.searchServices(
            getMarketplace("1").getMarketplaceId(), "de", searchPhrase);

    assertEquals(serviceSize, result.getServices().size());
    assertEquals(resultSize, result.getResultSize());

    VOService service = result.getServices().get(searchPhrasePostfix.intValue());

    assertTrue(service.getName().endsWith("_" + searchPhrasePostfix));
    assertEquals("shortdescription_" + searchPhrasePostfix, service.getShortDescription());
    assertEquals("description_" + searchPhrasePostfix, service.getDescription());
  }

  private static VOMarketplace getMarketplace(String id) {
    if (id.equals("1")) {
      return marketplaces.get(Integer.valueOf(0).intValue());

    } else if (id.equals("2")) {
      return marketplaces.get(Integer.valueOf(1).intValue());
    }
    return null;
  }

  private void searchServicesWithCondition(
      String searchPhrase, int serviceSize, int resultSize, String shortDescription)
      throws Exception {
    VOServiceListResult result =
        searchService_Supplier.searchServices(
            getMarketplace("1").getMarketplaceId(), "de", searchPhrase);

    assertEquals(serviceSize, result.getServices().size());
    assertEquals(resultSize, result.getResultSize());
    VOService service = result.getServices().get(0);

    assertTrue(service.getName().endsWith("_1"));
    assertEquals(shortDescription, service.getShortDescription());
  }

  /**
   * Call this method after every database change. This is necessary because the index is created
   * asynchronous and searchService does not return the correct number of services.
   */
  private static void sleep() throws InterruptedException {
    int delay = masterIndexRefreshTime + slaveIndexRefreshTime; // in seconds
    if (waitForRefresh) {
      System.out.println(
          "wait "
              + delay
              + " seconds for index update (maybe this is not enough; check glassfish settings)");
      Thread.sleep(delay * 1000); // in milliseconds
    }
  }
}
