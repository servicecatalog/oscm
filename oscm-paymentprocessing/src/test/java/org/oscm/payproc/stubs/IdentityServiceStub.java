/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 18.12.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.payproc.stubs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.UnitUserRole;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OperationPendingException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.UserActiveException;
import org.oscm.internal.types.exception.UserDeletionConstraintException;
import org.oscm.internal.types.exception.UserModificationConstraintException;
import org.oscm.internal.types.exception.UserRoleAssignmentException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.test.BaseAdmUmTest;

/**
 * @author Mike J&auml;ger
 * 
 */
public class IdentityServiceStub implements IdentityServiceLocal {

    public IdentityServiceStub() {
        super();
    }

    public Organization customer;
    public Organization supplier;
    public PaymentInfo paymentInfo;
    public QueryStub query;
    private final PlatformUser user = new PlatformUser();
    private boolean useDirectDebit = true;
    private boolean useCreditCard = false;
    private boolean useInvoice = false;
    private boolean initializePaymentRequiredSettings = true;
    private boolean initializePaymentInfo = true;

    @Override
    public void createOrganizationAdmin(VOUserDetails userDetails,
            Organization organization, String password, Long serviceKey,
            Marketplace marketplace) throws NonUniqueBusinessKeyException,
            ObjectNotFoundException, ValidationException {
    }

    @Override
    public PlatformUser getPlatformUser(String userId, String tenantKey, boolean validateOrganization)
        throws ObjectNotFoundException, OperationNotPermittedException {
        return null;
    }

    @Override
    public void deletePlatformUser(PlatformUser user, Marketplace marketplace)
            throws UserDeletionConstraintException, ObjectNotFoundException {
    }

    public PlatformUser getCurrentUser() {

        user.setOrganization(customer);
        user.setLocale("en");

        OrganizationRole customerRole = new OrganizationRole();
        customerRole.setRoleName(OrganizationRoleType.CUSTOMER);

        List<OrganizationRefToPaymentType> availablePayments = new ArrayList<OrganizationRefToPaymentType>();
        if (useCreditCard) {
            OrganizationRefToPaymentType apt = new OrganizationRefToPaymentType();
            apt.setOrganizationReference(customer.getSources().get(0));
            apt.setOrganizationRole(customerRole);
            apt.setUsedAsDefault(false);
            PaymentType pt = new PaymentType();
            pt.setKey(1);
            pt.setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
            pt.setPaymentTypeId(BaseAdmUmTest.CREDIT_CARD);
            apt.setPaymentType(pt);
            availablePayments.add(apt);
        }
        if (useDirectDebit) {
            OrganizationRefToPaymentType apt = new OrganizationRefToPaymentType();
            apt.setOrganizationReference(customer.getSources().get(0));
            apt.setOrganizationRole(customerRole);
            apt.setUsedAsDefault(false);
            PaymentType pt = new PaymentType();
            pt.setKey(2);
            pt.setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
            pt.setPaymentTypeId(BaseAdmUmTest.DIRECT_DEBIT);
            apt.setPaymentType(pt);
            availablePayments.add(apt);
        }

        if (useInvoice) {
            OrganizationRefToPaymentType apt = new OrganizationRefToPaymentType();
            apt.setOrganizationReference(customer.getSources().get(0));
            apt.setOrganizationRole(customerRole);
            apt.setUsedAsDefault(false);
            PaymentType pt = new PaymentType();
            pt.setCollectionType(PaymentCollectionType.ORGANIZATION);
            pt.setPaymentTypeId(BaseAdmUmTest.INVOICE);
            apt.setPaymentType(pt);
            availablePayments.add(apt);
        }

        if (initializePaymentRequiredSettings) {
            // name and email is already set, so add email and address
            customer.setAddress("address of TestOrganization");
        }
        customer.getSources().get(0).getPaymentTypes()
                .addAll(availablePayments);
        if (initializePaymentInfo && availablePayments.size() > 0) {
            query.setPaymentInfo(paymentInfo);
        } else {
            query.setPaymentInfo(null);
        }
        return user;
    }

    @Override
    public List<PlatformUser> getOverdueOrganizationAdmins(long currentTime) {
        return null;
    }

    @Override
    public PlatformUser getPlatformUser(String userId,
            boolean validateOrganization) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public void setUserAccountStatus(PlatformUser user,
            UserAccountStatus newStatus) {
    }

    public void setUseDirectDebit(boolean useDirectDebit) {
        this.useDirectDebit = useDirectDebit;
    }

    public void setUseCreditCard(boolean useCreditCard) {
        this.useCreditCard = useCreditCard;
    }

    public void setUseInvoice(boolean useInvoice) {
        this.useInvoice = useInvoice;
    }

    public void setInitializePaymentRequiredSettings(
            boolean initializePaymentRequiredSettings) {
        this.initializePaymentRequiredSettings = initializePaymentRequiredSettings;
    }

    public void setInitializePaymentInfo(boolean initializePaymentInfo) {
        this.initializePaymentInfo = initializePaymentInfo;
    }

    @Override
    public PlatformUser modifyUserData(PlatformUser existingUser,
            VOUserDetails tempUser, boolean modifyOwnUser, boolean sendMail)
            throws OperationNotPermittedException,
            NonUniqueBusinessKeyException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        return null;
    }

    @Override
    public void resetPasswordForUser(PlatformUser user, Marketplace marketplace) {
    }

    @Override
    public boolean removeInactiveOnBehalfUsers() {
        return false;
    }

    @Override
    public boolean removeInactiveOnBehalfUsersImpl() {
        return false;
    }

    @Override
    public void grantUserRoles(PlatformUser user, List<UserRoleType> roles)
            throws ObjectNotFoundException, OperationNotPermittedException,
            UserRoleAssignmentException {
    }

    @Override
    public VOUserDetails createUserInt(TriggerProcess tp)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException {
        return null;
    }

    @Override
    public List<PlatformUser> getOrganizationUsers() {
        return null;
    }

    @Override
    public Set<UserRoleType> getAvailableUserRolesForUser(PlatformUser pu) {
        return null;
    }

    @Override
    public void resetUserPassword(PlatformUser platformUser,
            String marketplaceId) throws UserActiveException,
            MailOperationException {

    }

    @Override
    public void deleteUser(PlatformUser pUser, String marketplaceId)
            throws OperationNotPermittedException,
            UserDeletionConstraintException {
    }

    @Override
    public void sendUserUpdatedMail(PlatformUser existingUser,
            PlatformUser oldUser) {
    }

    @Override
    public void notifySubscriptionsAboutUserUpdate(PlatformUser existingUser) {
    }

    @Override
    public void setUserRolesInt(Set<UserRoleType> roles, PlatformUser pUser)
            throws UserModificationConstraintException, UserActiveException,
            OperationNotPermittedException, UserRoleAssignmentException,
            ObjectNotFoundException {
    }

    @Override
    public void verifyIdUniquenessAndLdapAttributes(PlatformUser existingUser,
            PlatformUser modUser) throws NonUniqueBusinessKeyException {
    }

    @Override
    public void sendMailToCreatedUser(String password, boolean userLocalLdap,
            Marketplace marketplace, PlatformUser pu)
            throws MailOperationException {
    }

    @Override
    public boolean isUserLoggedIn(long userKey) {
        return false;
    }

    @Override
    public VOUserDetails createUser(VOUserDetails user, String marketplaceId)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException,
            OperationPendingException {
        return null;
    }

    @Override
    public void importUser(VOUserDetails user, String marketplaceId)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException,
            ObjectNotFoundException {
    }

    @Override
    public void sendAdministratorNotifyMail(PlatformUser administrator,
            String userId) {

    }

    @Override
    public VOUserDetails createUserWithGroups(VOUserDetails user,
            List<UserRoleType> roles, String marketplaceId,
            Map<Long, UnitUserRole> groups)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException,
            OperationPendingException {
        return null;
    }

    @Override
    public void grantUnitRole(PlatformUser user, UserRoleType role) {
    }

    @Override
    public void revokeUnitRole(PlatformUser user, UserRoleType role)
            throws UserModificationConstraintException {
    }

    @Override
    public PlatformUser getPlatformUserByOrganization(String userId,
            String orgId) throws ObjectNotFoundException {
        return null;
    }
}
