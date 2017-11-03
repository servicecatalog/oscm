/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.data;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.persistence.Query;

import org.junit.Assert;
import org.oscm.authorization.PasswordHash;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.test.BaseAdmUmTest;

public class Organizations {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static int organizationId = 1000;

    public static PlatformUser createUserForOrg(DataService mgr,
            Organization org, boolean isAdmin, String userId)
            throws NonUniqueBusinessKeyException {
        return createUserForOrg(mgr, org, isAdmin, userId, "en");
    }

    public static PlatformUser createUserForOrg(DataService mgr,
            Organization org, boolean isAdmin, String userId, String locale)
            throws NonUniqueBusinessKeyException {
        PlatformUser admin = prepareUser(mgr, org, isAdmin, userId, locale);
        mgr.persist(admin);
        if (isAdmin) {
            PlatformUsers.grantAdminRole(mgr, admin);
        }
        return admin;
    }

    protected static PlatformUser prepareUser(DataService mgr, Organization org, boolean isAdmin, String userId, String locale) throws NonUniqueBusinessKeyException {
        PlatformUser admin = new PlatformUser();
        admin.setAdditionalName("AddName Admin");
        admin.setAddress("Address");
        admin.setCreationDate(GregorianCalendar.getInstance().getTime());
        admin.setEmail("admin@organization.com");
        admin.setFirstName("Mister");
        // create system wide unique userId
        admin.setUserId(userId + "_" + org.getOrganizationId());
        admin.setRealmUserId(admin.getUserId());
        admin.setLastName("Knowitall");
        admin.setPhone("111111/111111");
        admin.setStatus(UserAccountStatus.ACTIVE);
        admin.setOrganization(org);
        admin.setLocale(locale);
        byte[] passwordHash = PasswordHash.calculateHash(1, "secret");
        admin.setPasswordHash(passwordHash);
        admin.setPasswordSalt(1);
        return admin;
    }

    public static PlatformUser createNormalUserForOrg(DataService mgr,
                                                      Organization org, boolean isAdmin, String userId, String locale, String tenantID)
            throws NonUniqueBusinessKeyException {
        PlatformUser admin = prepareUser(mgr, org, isAdmin, userId, locale);
        admin.setUserId(userId);
        admin.setTenantId(tenantID);
        mgr.persist(admin);
        if (isAdmin) {
            PlatformUsers.grantAdminRole(mgr, admin);
        }
        return admin;
    }
    
    public static PlatformUser createUserForOrgWithGivenId(DataService mgr, String userId, Organization org )
            throws NonUniqueBusinessKeyException {
        PlatformUser admin = new PlatformUser();
        admin.setAdditionalName("AddName Admin");
        admin.setAddress("Address");
        admin.setCreationDate(GregorianCalendar.getInstance().getTime());
        admin.setEmail("admin@organization.com");
        admin.setFirstName("Mister");
        // create system wide unique userId
        admin.setUserId(userId);
        admin.setRealmUserId(admin.getUserId());
        admin.setLastName("Knowitall");
        admin.setPhone("111111/111111");
        admin.setStatus(UserAccountStatus.ACTIVE);
        admin.setOrganization(org);
        admin.setLocale("en");
        byte[] passwordHash = PasswordHash.calculateHash(1, "secret");
        admin.setPasswordHash(passwordHash);
        admin.setPasswordSalt(1);
        mgr.persist(admin);
        return admin;
    }

    public static Organization findOrganization(DataService mgr, String id) {
        Organization organization = new Organization();
        organization.setOrganizationId(id);
        return (Organization) mgr.find(organization);
    }

    public static void removeOrganization(DataService mgr, String id) {
        Organization organization = findOrganization(mgr, id);
        if (organization != null) {
            List<PlatformUser> users = organization.getPlatformUsers();
            if (users != null) {
                for (PlatformUser user : users) {
                    mgr.remove(user);
                }
            }
            for (Subscription sub : organization.getSubscriptions()) {
                PaymentInfo info = sub.getPaymentInfo();
                mgr.remove(info);
            }
            for (BillingContact contact : organization.getBillingContacts()) {
                mgr.remove(contact);
            }
            for (OrganizationReference ref : organization.getSources()) {
                for (OrganizationRefToPaymentType payment : ref
                        .getPaymentTypes()) {
                    mgr.remove(payment);
                }
                mgr.remove(ref);
            }
            for (OrganizationReference ref : organization.getTargets()) {
                for (OrganizationRefToPaymentType payment : ref
                        .getPaymentTypes()) {
                    mgr.remove(payment);
                }
                mgr.remove(ref);
            }
            organization.getSources().clear();
            organization.getTargets().clear();
            mgr.remove(organization);
            mgr.flush();
        }
    }

    public static void addOrganizationToRole(DataService mgr, Organization org,
            OrganizationRoleType type) throws NonUniqueBusinessKeyException {
        OrganizationRole orgRole = new OrganizationRole();
        orgRole.setRoleName(type);
        orgRole = (OrganizationRole) mgr.find(orgRole);
        OrganizationToRole orgToRole = new OrganizationToRole();
        orgToRole.setOrganization(org);
        orgToRole.setOrganizationRole(orgRole);
        mgr.persist(orgToRole);
    }

    public static void grantOrganizationRole(Organization org,
            OrganizationRoleType type) {
        OrganizationRole orgRole = new OrganizationRole();
        orgRole.setRoleName(type);
        OrganizationToRole orgToRole = new OrganizationToRole();
        orgToRole.setOrganization(org);
        orgToRole.setOrganizationRole(orgRole);
        org.getGrantedRoles().add(orgToRole);
    }

    /**
     * Adds the given organization role to the given organization, in case it
     * does not have this role yet.
     */
    public static void grantOrganizationRoles(DataService mgr,
            Organization org, OrganizationRoleType... roles)
            throws NonUniqueBusinessKeyException {
        for (OrganizationRoleType role : roles) {
            if (!org.hasRole(role)) {
                addOrganizationToRole(mgr, org, role);
            }
        }
    }

    public static Organization createPlatformOperator(DataService ds)
            throws Exception {
        return createOrganization(ds,
                OrganizationRoleType.PLATFORM_OPERATOR.name(),
                OrganizationRoleType.PLATFORM_OPERATOR);
    }

    public static Organization createOrganization(DataService mgr)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        return Organizations.createOrganization(mgr,
                OrganizationRoleType.CUSTOMER);
    }

    public static Organization createOrganization(DataService mgr,
            OrganizationRoleType... roles)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        return createOrganization(mgr, String.valueOf(organizationId++), roles);
    }

    public static Organization createOrganization(DataService mgr, Organization org,
            OrganizationRoleType... roles) throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        if (Arrays.asList(roles).contains(OrganizationRoleType.SUPPLIER)) {
            createOperatorRevenueShare(mgr, org, BigDecimal.ZERO);
        }

        mgr.persist(org);

        if (roles != null) {
            createRoles(mgr, org, roles);
        }
        // assign CUSTOMER role for any organization if is not assigned
        if (!org.hasRole(OrganizationRoleType.CUSTOMER)) {
            createRole(mgr, org, OrganizationRoleType.CUSTOMER);
        }
        if (org.hasRole(OrganizationRoleType.CUSTOMER)) {
            SupportedCountries.createOneSupportedCountry(mgr);
            setDomicileCountry(org, "DE", mgr);
        }
        if (org.hasRole(OrganizationRoleType.SUPPLIER)) {
            addSupplierToCustomer(mgr, org, org);
            if (org.hasRole(OrganizationRoleType.TECHNOLOGY_PROVIDER)) {
                addSupplierToCustomer(mgr, org, org);
                supportAllCountries(mgr, org);
            }
        }
        if (org.hasRole(OrganizationRoleType.BROKER)
                || org.hasRole(OrganizationRoleType.RESELLER)) {
            SupportedCountries.createOneSupportedCountry(mgr);
            setDomicileCountry(org, "DE", mgr);
        }
        return org;
    }

    public static Organization createOrganization(DataService mgr,
            String orgId, OrganizationRoleType... roles)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        Organization org = createOrganization(orgId);
        if (Arrays.asList(roles).contains(OrganizationRoleType.SUPPLIER)) {
            createOperatorRevenueShare(mgr, org, BigDecimal.ZERO);
        }

        mgr.persist(org);

        if (roles != null) {
            createRoles(mgr, org, roles);
        }
        // assign CUSTOMER role for any organization if is not assigned
        if (!org.hasRole(OrganizationRoleType.CUSTOMER)) {
            createRole(mgr, org, OrganizationRoleType.CUSTOMER);
        }
        if (org.hasRole(OrganizationRoleType.CUSTOMER)) {
            SupportedCountries.createOneSupportedCountry(mgr);
            setDomicileCountry(org, "DE", mgr);
        }
        if (org.hasRole(OrganizationRoleType.SUPPLIER)) {
            addSupplierToCustomer(mgr, org, org);
            if (org.hasRole(OrganizationRoleType.TECHNOLOGY_PROVIDER)) {
                addSupplierToCustomer(mgr, org, org);
                supportAllCountries(mgr, org);
            }
        }
        if (org.hasRole(OrganizationRoleType.BROKER)
                || org.hasRole(OrganizationRoleType.RESELLER)) {
            SupportedCountries.createOneSupportedCountry(mgr);
            setDomicileCountry(org, "DE", mgr);
        }
        return org;
    }

    public static Organization createOrganizationWithTenant(DataService mgr,
            String orgId, String tenantID, OrganizationRoleType... roles)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        Organization org = createOrganizationWithTenant(orgId, tenantID, mgr);
        if (Arrays.asList(roles).contains(OrganizationRoleType.SUPPLIER)) {
            createOperatorRevenueShare(mgr, org, BigDecimal.ZERO);
        }

        mgr.persist(org);

        if (roles != null) {
            createRoles(mgr, org, roles);
        }
        // assign CUSTOMER role for any organization if is not assigned
        if (!org.hasRole(OrganizationRoleType.CUSTOMER)) {
            createRole(mgr, org, OrganizationRoleType.CUSTOMER);
        }
        if (org.hasRole(OrganizationRoleType.CUSTOMER)) {
            SupportedCountries.createOneSupportedCountry(mgr);
            setDomicileCountry(org, "DE", mgr);
        }
        if (org.hasRole(OrganizationRoleType.SUPPLIER)) {
            addSupplierToCustomer(mgr, org, org);
            if (org.hasRole(OrganizationRoleType.TECHNOLOGY_PROVIDER)) {
                addSupplierToCustomer(mgr, org, org);
                supportAllCountries(mgr, org);
            }
        }
        if (org.hasRole(OrganizationRoleType.BROKER)
                || org.hasRole(OrganizationRoleType.RESELLER)) {
            SupportedCountries.createOneSupportedCountry(mgr);
            setDomicileCountry(org, "DE", mgr);
        }
        return org;
    }

    public static void createRoles(DataService mgr, Organization org,
            OrganizationRoleType... roles) throws NonUniqueBusinessKeyException {
        for (OrganizationRoleType roleToBeAssigned : roles) {
            createRole(mgr, org, roleToBeAssigned);
        }
    }

    public static void createRole(DataService mgr, Organization org,
            OrganizationRoleType roleToBeAssigned)
            throws NonUniqueBusinessKeyException {
        OrganizationRole orgRoleTemplate = new OrganizationRole();
        orgRoleTemplate.setRoleName(roleToBeAssigned);
        OrganizationRole orgRole;
        try {
            orgRole = (OrganizationRole) mgr
                    .getReferenceByBusinessKey(orgRoleTemplate);
        } catch (ObjectNotFoundException e) {
            mgr.persist(orgRoleTemplate);
            orgRole = mgr
                    .find(OrganizationRole.class, orgRoleTemplate.getKey());
        }

        // mock DataService could deliver a null object
        if (orgRole != null) {
            OrganizationToRole orgToRole = new OrganizationToRole();
            orgToRole.setOrganization(org);
            orgToRole.setOrganizationRole(orgRole);
            mgr.persist(orgToRole);
            org.getGrantedRoles().add(orgToRole);
        }
    }

    public static void createOperatorRevenueShare(DataService mgr,
            Organization org, BigDecimal operatorRevenueShare)
            throws NonUniqueBusinessKeyException {
        RevenueShareModel operatorPriceModel = new RevenueShareModel();
        operatorPriceModel.setRevenueShare(operatorRevenueShare);
        operatorPriceModel
                .setRevenueShareModelType(RevenueShareModelType.OPERATOR_REVENUE_SHARE);

        mgr.persist(operatorPriceModel);
        org.setOperatorPriceModel(operatorPriceModel);
    }

    public static Organization createOrganization(String orgId) {
        Organization org = new Organization();
        org.setOrganizationId(orgId);
        org.setName("Name of organization " + org.getOrganizationId());
        org.setAddress("Address of organization " + org.getOrganizationId());
        org.setEmail(org.getOrganizationId() + "@organization.com");
        org.setPhone("012345/678" + org.getOrganizationId());
        org.setLocale("en");
        org.setUrl("http://www.organization.com");
        org.setCutOffDay(1);
        return org;
    }

    public static Organization createOrganizationWithTenant(String orgId, String tenantID, DataService mgr) {
        Organization org = new Organization();
        org.setOrganizationId(orgId);
        org.setName("Name of organization " + org.getOrganizationId());
        org.setAddress("Address of organization " + org.getOrganizationId());
        org.setEmail(org.getOrganizationId() + "@organization.com");
        org.setPhone("012345/678" + org.getOrganizationId());
        org.setLocale("en");
        org.setUrl("http://www.organization.com");
        org.setCutOffDay(1);
        if (tenantID != null && !tenantID.equals("")) {
            Tenant tenant = new Tenant();
            tenant.setTenantId(tenantID);
            tenant.getDataContainer().setName(tenantID);
            org.setTenant(tenant);
            try {
                mgr.persist(tenant);
            } catch (NonUniqueBusinessKeyException e) {
                e.printStackTrace();
            }
        }
        return org;
    }

    public static OrganizationReference createSupplierToTechnologyProviderReference(
            DataService mgr, Organization tp, Organization sup)
            throws NonUniqueBusinessKeyException {
        Assert.assertTrue("tp must have the role TECHNOLOGY_PROVIDER",
                tp.hasRole(OrganizationRoleType.TECHNOLOGY_PROVIDER));
        Assert.assertTrue("tp must  have the role CUSTOMER",
                tp.hasRole(OrganizationRoleType.CUSTOMER));
        Assert.assertTrue("sup must have the role SUPPLIER",
                sup.hasRole(OrganizationRoleType.SUPPLIER));
        Assert.assertTrue("sup must have the role CUSTOMER",
                sup.hasRole(OrganizationRoleType.CUSTOMER));

        // create organization reference from tp to supplier
        OrganizationReference organizationReference = new OrganizationReference(
                tp, sup,
                OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER);
        mgr.persist(organizationReference);
        mgr.flush();
        mgr.refresh(organizationReference);
        tp.getTargets().add(organizationReference);
        sup.getSources().add(organizationReference);
        return organizationReference;
    }

    public static OrganizationReference addSupplierForTechnicalProduct(
            DataService mgr, Organization tp, Organization sup,
            TechnicalProduct technicalProduct)
            throws NonUniqueBusinessKeyException {
        Assert.assertTrue("tp must have the role TECHNOLOGY_PROVIDER",
                tp.hasRole(OrganizationRoleType.TECHNOLOGY_PROVIDER));
        Assert.assertTrue("tp must  have the role CUSTOMER",
                tp.hasRole(OrganizationRoleType.CUSTOMER));
        Assert.assertTrue("sup must have the role SUPPLIER",
                sup.hasRole(OrganizationRoleType.SUPPLIER));
        Assert.assertTrue("sup must have the role CUSTOMER",
                sup.hasRole(OrganizationRoleType.CUSTOMER));

        // create organization reference from tp to supplier
        OrganizationReference organizationReference = new OrganizationReference(
                tp, sup,
                OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER);
        mgr.persist(organizationReference);
        mgr.flush();
        mgr.refresh(organizationReference);
        tp.getTargets().add(organizationReference);
        sup.getSources().add(organizationReference);

        // create marketing permission for technical product
        MarketingPermission mp = new MarketingPermission();
        mp.setOrganizationReference(organizationReference);
        mp.setTechnicalProduct(technicalProduct);
        mgr.persist(mp);

        return organizationReference;
    }

    public static void addSupplierToCustomer(DataService mgr,
            Organization supplier, Organization customer)
            throws NonUniqueBusinessKeyException {

        Assert.assertTrue("cu must have the role CUSTOMER",
                customer.hasRole(OrganizationRoleType.CUSTOMER));
        Assert.assertTrue("sup must have the role SUPPLIER",
                supplier.hasRole(OrganizationRoleType.SUPPLIER));

        OrganizationReference orgRef = new OrganizationReference(supplier,
                customer, OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
        try {
            orgRef = (OrganizationReference) mgr.find(orgRef);
        } catch (Exception e) {
            // ignore
            orgRef = null;
        }

        if (orgRef == null) {
            orgRef = new OrganizationReference(supplier, customer,
                    OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
            mgr.persist(orgRef);
            mgr.flush();
            mgr.refresh(orgRef);
            supplier.getTargets().add(orgRef);
            customer.getSources().add(orgRef);
        }
    }

    public static Organization createCustomer(DataService mgr,
            Organization vendor, boolean remoteLdapActive)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        return createCustomer(mgr, vendor, String.valueOf(organizationId++),
                remoteLdapActive);
    }

    public static Organization createCustomer(DataService mgr,
            Organization vendor, String customerId, boolean remoteLdapActive)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        Assert.assertTrue(
                "Organisation must have the role SUPPLIER or BROKER or RESELLER",
                vendor.hasRole(OrganizationRoleType.SUPPLIER)
                        || vendor.hasRole(OrganizationRoleType.BROKER)
                        || vendor.hasRole(OrganizationRoleType.RESELLER));
        Organization customer = Organizations.createOrganization(mgr,
                customerId, OrganizationRoleType.CUSTOMER);
        customer.setRemoteLdapActive(remoteLdapActive);
        OrganizationReferenceType orgRefType = OrganizationReferenceType.SUPPLIER_TO_CUSTOMER;
        if (vendor.hasRole(OrganizationRoleType.BROKER)) {
            orgRefType = OrganizationReferenceType.BROKER_TO_CUSTOMER;
        } else if (vendor.hasRole(OrganizationRoleType.RESELLER)) {
            orgRefType = OrganizationReferenceType.RESELLER_TO_CUSTOMER;
        }
        OrganizationReference organizationReference = new OrganizationReference(
                vendor, customer, orgRefType);
        mgr.persist(organizationReference);
        mgr.flush();
        mgr.refresh(customer);
        setDomicileCountry(customer, "DE", mgr);
        return customer;
    }

    public static Organization createCustomer(DataService mgr,
            Organization sourceOrg) throws NonUniqueBusinessKeyException,
            ObjectNotFoundException {
        return createCustomer(mgr, sourceOrg, false);
    }

    public static void addPaymentTypesToOrganizationRef(DataService mgr,
            String orgId, OrganizationRoleType roleType) throws Exception {
        boolean[] usedAsServiceDefPaymentType = { false, false, false };
        boolean[] usedAseDefPaymentType = { false, false, false };
        addPaymentTypesToOrganizationRef(mgr, orgId, roleType,
                BaseAdmUmTest.PAYMENT_TYPE_IDS, usedAseDefPaymentType,
                usedAsServiceDefPaymentType);
    }

    public static void addPaymentTypesToOrganizationRef(DataService mgr,
            String orgId, OrganizationRoleType roleType, String[] paymentTypes,
            boolean[] usedAsDefaultPaymentType,
            boolean[] usedAsServiceDefPaymentType) throws Exception {
        Organization org = new Organization();
        org.setOrganizationId(orgId);
        org = (Organization) mgr.getReferenceByBusinessKey(org);

        OrganizationRole role = new OrganizationRole();
        role.setRoleName(roleType);
        role = (OrganizationRole) mgr.getReferenceByBusinessKey(role);
        int i = 0;
        for (String typeId : paymentTypes) {
            PaymentType type = new PaymentType();
            type.setPaymentTypeId(typeId);
            type = (PaymentType) mgr.getReferenceByBusinessKey(type);

            OrganizationRefToPaymentType orgToPt = new OrganizationRefToPaymentType();
            if (roleType == OrganizationRoleType.CUSTOMER) {
                orgToPt.setOrganizationReference(org.getSources().get(0));
            } else if (roleType == OrganizationRoleType.RESELLER) {
                // get the platform operator org reference to the broker
                orgToPt.setOrganizationReference(getOpToSuppOrgRef(org, mgr));
            } else if (roleType == OrganizationRoleType.SUPPLIER) {
                // get the platform operator org reference to the supplier
                orgToPt.setOrganizationReference(getOpToSuppOrgRef(org, mgr));
            }
            orgToPt.setOrganizationRole(role);
            orgToPt.setPaymentType(type);
            orgToPt.setUsedAsDefault(usedAsDefaultPaymentType == null ? false
                    : usedAsDefaultPaymentType[i]);
            orgToPt.setUsedAsServiceDefault(usedAsServiceDefPaymentType == null ? false
                    : usedAsServiceDefPaymentType[i]);
            i++;
            Query query = mgr
                    .createQuery("select r from OrganizationRefToPaymentType r where r.organizationReference.key = :orgKey and r.paymentType.key = :ptKey");
            query.setParameter("orgKey", new Long(orgToPt
                    .getOrganizationReference().getKey()));
            query.setParameter("ptKey", new Long(type.getKey()));
            if (query.getResultList().isEmpty()) {
                mgr.persist(orgToPt);
            }
        }
    }

    /**
     * Determines the platform operator organization and its reference to the
     * given supplier. If it does not exist, it will be created.
     * 
     * @param supplier
     *            The supplier to retrieve the organization reference for.
     * @param mgr
     *            The data service used to perform the changes.
     * @return The organization reference from the platform operator to the
     *         supplier.
     * @throws ObjectNotFoundException
     * @throws NonUniqueBusinessKeyException
     */
    private static OrganizationReference getOpToSuppOrgRef(
            Organization supplier, DataService mgr)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException {
        Organization org = new Organization();
        org.setOrganizationId(OrganizationRoleType.PLATFORM_OPERATOR.name());
        Organization platformOperatorOrg = (Organization) mgr
                .getReferenceByBusinessKey(org);
        OrganizationReference orgRef = new OrganizationReference(
                platformOperatorOrg,
                supplier,
                supplier.getGrantedRoleTypes().contains(
                        OrganizationRoleType.SUPPLIER) ? OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER
                        : OrganizationReferenceType.PLATFORM_OPERATOR_TO_RESELLER);
        OrganizationReference result = (OrganizationReference) mgr.find(orgRef);
        if (result == null) {
            result = orgRef;
            mgr.persist(result);
            mgr.flush();
        }
        return result;
    }

    /**
     * Set the given domicile country. The domicile country will only be set if
     * a supplier exists with supported country code that matches the given
     * country code. The main intention of this method is to set a value that
     * will pass the validation checks.
     * 
     * @param customer
     * @param countryCode
     * @throws ObjectNotFoundException
     */
    public static void setDomicileCountry(Organization customer,
            String countryCode, DataService mgr) throws ObjectNotFoundException {
        SupportedCountry sc = new SupportedCountry(countryCode);
        sc = (SupportedCountry) mgr.getReferenceByBusinessKey(sc);
        customer.setDomicileCountry(sc);
    }

    public static void supportAllCountries(DataService mgr, Organization org) {
        for (String countryCode : Locale.getISOCountries()) {
            SupportedCountry country = SupportedCountries
                    .find(mgr, countryCode);
            if (country != null) {
                org.setSupportedCountry(country);
            }
        }
    }

    public static OrganizationReference createOrganizationReference(
            String sourceOrgId, Organization targetOrganization,
            OrganizationReferenceType orfgRefType, DataService mgr)
            throws NonUniqueBusinessKeyException {

        Organization source = findOrganization(mgr, sourceOrgId);
        OrganizationReference orgRef = new OrganizationReference(source,
                targetOrganization, orfgRefType);

        mgr.persist(orgRef);
        return orgRef;
    }

    public static OrganizationReference createOrganizationReference(
            Organization sourceOrganization, Organization targetOrganization,
            OrganizationReferenceType orfgRefType, DataService mgr)
            throws NonUniqueBusinessKeyException {

        OrganizationReference orgRef = new OrganizationReference(
                sourceOrganization, targetOrganization, orfgRefType);

        mgr.persist(orgRef);
        return orgRef;
    }

    public static OrganizationRole findOrganizationRole(
            OrganizationRoleType roleName, DataService mgr)
            throws ObjectNotFoundException {
        OrganizationRole orgRole = new OrganizationRole(roleName);
        return (OrganizationRole) mgr.getReferenceByBusinessKey(orgRole);
    }

    public static OrganizationHistory createOrganizationHistory(DataService ds,
            long objKey, String modDate, int version, long domicileCountryObjKey)
            throws ParseException, Exception {
        OrganizationHistory hist = new OrganizationHistory();
        hist.setObjKey(objKey);
        hist.setModdate(new SimpleDateFormat(DATE_PATTERN).parse(modDate));
        hist.setInvocationDate(new SimpleDateFormat(DATE_PATTERN)
                .parse(modDate));
        hist.setModtype(ModificationType.ADD);
        hist.setModuser("moduser");
        hist.setObjVersion(version);

        hist.setDomicileCountryObjKey(Long.valueOf(domicileCountryObjKey));
        hist.getDataContainer().setOrganizationId("organizationId");
        hist.getDataContainer()
                .setRegistrationDate(hist.getModdate().getTime());
        hist.getDataContainer().setRemoteLdapActive(false);
        hist.setCutOffDay(1);

        ds.persist(hist);

        return hist;
    }
}
