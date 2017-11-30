/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.common;

import static org.junit.Assert.assertTrue;
import static org.oscm.internal.types.exception.AssertionValidationException.ReasonEnum.WRONG_TENANT;
import static org.oscm.internal.types.exception.NotExistentTenantException.Reason.MISSING_TEANT_ID_IN_SAML;
import static org.oscm.internal.types.exception.NotExistentTenantException.Reason.MISSING_TENANT_PARAMETER;

import java.io.IOException;
import java.util.*;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.test.ClassFilter;
import org.oscm.test.PackageClassReader;
import org.oscm.types.enumtypes.PlatformParameterIdentifiers;
import org.oscm.ui.resources.DefaultMessages;
import org.oscm.ui.resources.DefaultMessages_en;

/**
 * Checks that for all exceptions from the public API a corresponding message
 * text is defined in English locale.
 * 
 * @author groch
 * 
 */
@Ignore
public class MessagePropertiesForExceptionsInSyncTest {

    private boolean showDebugOutput = true;

    // exceptions from the rule: would be misinterpreted by generic algorithm,
    // add them here to exclude them from generic algorithm, don't forget to add
    // custom key(s) to mustBeDeclaredInAddition instead
    private static final List<String> hasSpecialPattern = Arrays
            .asList(ServiceParameterException.class.getSimpleName());

    // exceptions from the rule: theoretically thinkable, practically not
    // thus, no message text must be defined for them (only add them if you are
    // sure they (or rather the corresponding text) will not be shown in the UI)
    private static final List<String> notThrownInPractice = Arrays.asList(
            ExecutionTargetException.class.getSimpleName(),
            SaaSApplicationException.class.getSimpleName(),

            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.BILLING_CONTACT,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.CATEGORY,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.EVENT,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.MARKETING_PERMISSION,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.MARKETPLACE,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.MARKETPLACE_TO_ORGANIZATION,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.OPERATION_RECORD,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.ORGANIZATION_REFERENCE,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.ORGANIZATION_ROLE,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.ORGANIZATION_SETTING,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.ORGANIZATION_TO_COUNTRY,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.PARAMETER,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.PARAMETER_DEFINITION,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.PAYMENT_INFO,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.PAYMENT_TYPE,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.PLATFORM_SETTING,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.PRICED_PARAMETER,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.PRODUCT_REVIEW,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.PRODUCT_TO_PAYMENTTYPE,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.PSP,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.REPORT,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.SERVICE,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.SUBSCRIPTION,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.SUBSCRIPTION_TO_MARKETPLACE,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.SUPPORTED_COUNTRY,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.SUPPORTED_CURRENCY,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.TAG,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.TECHNICAL_SERVICE_TAG,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.TRIGGER_PROCESS,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.UDA,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.UDA_DEFINITION,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.USER,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.USER_GROUP,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.USER_GROUP_TO_USER,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.USER_GROUP_TO_INVISIBLE_PRODUCT,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.USER_ROLE,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.VAT_RATES,
            DeletionConstraintException.class.getSimpleName() + "."
                    + ClassEnum.MARKETPLACE_ACCESS,
                    
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.BILLING_CONTACT,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.CATEGORY,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.EVENT,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.OPERATION_RECORD,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.MARKETING_PERMISSION,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.MARKETPLACE,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.MARKETPLACE_TO_ORGANIZATION,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.ORGANIZATION_REFERENCE,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.ORGANIZATION_ROLE,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.ORGANIZATION,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.ORGANIZATION_SETTING,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.ORGANIZATION_TO_COUNTRY,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.PARAMETER,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.PARAMETER_DEFINITION,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.PAYMENT_INFO,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.PAYMENT_TYPE,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.PLATFORM_SETTING,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.PRICED_PARAMETER,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.PRODUCT_REVIEW,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.PRODUCT_TO_PAYMENTTYPE,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.PSP,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.REPORT,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.SERVICE,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.SUBSCRIPTION,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.SUBSCRIPTION_TO_MARKETPLACE,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.SUPPORTED_COUNTRY,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.SUPPORTED_CURRENCY,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.TAG,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.TECHNICAL_SERVICE_TAG,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.TRIGGER_DEFINITION,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.TRIGGER_PROCESS,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.UDA,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.UDA_DEFINITION,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.USER,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.USER_GROUP,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.USER_GROUP_TO_USER,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.USER_GROUP_TO_INVISIBLE_PRODUCT,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.USER_ROLE,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.VAT_RATES,
            AssertionValidationException.class.getSimpleName() + "."
                    + WRONG_TENANT,
            NotExistentTenantException.class.getSimpleName() + "."
                    + MISSING_TENANT_PARAMETER,
            NotExistentTenantException.class.getSimpleName() + "."
                    + MISSING_TEANT_ID_IN_SAML,
            UpdateConstraintException.class.getSimpleName() + "."
                    + ClassEnum.MARKETPLACE_ACCESS,
            WrongTenantConfigurationException.class.getSimpleName(),
                    
            ObjectNotFoundException.class.getSimpleName() + "."
                    + ClassEnum.USER_GROUP_TO_USER,
            ObjectNotFoundException.class.getSimpleName() + "."
                    + ClassEnum.USER_GROUP_TO_INVISIBLE_PRODUCT,
            ObjectNotFoundException.class.getSimpleName() + "."
                    + ClassEnum.MARKETPLACE_ACCESS);

    // exceptions from the rule: not covered by or contradict generic algorithm,
    // so they must be declared manually (to get aware of absent message keys)
    private static final List<String> mustBeDeclaredInAddition = Arrays.asList(
            ObjectNotFoundException.class.getSimpleName(),
            ServiceParameterException.class.getSimpleName() + "."
                    + ParameterType.PLATFORM_PARAMETER + "."
                    + PlatformParameterIdentifiers.CONCURRENT_USER,
            ServiceParameterException.class.getSimpleName() + "."
                    + ParameterType.PLATFORM_PARAMETER + "."
                    + PlatformParameterIdentifiers.NAMED_USER,
            ServiceParameterException.class.getSimpleName() + "."
                    + ParameterType.PLATFORM_PARAMETER + "."
                    + PlatformParameterIdentifiers.PERIOD);

    private static final List<String> expectedExceptionMessageKeys = new ArrayList<String>();
    private static final List<String> propfileMessageKeys = new ArrayList<String>();
    private static final List<String> additionalKeysWithSpecialPattern = new ArrayList<String>();

    @BeforeClass
    public static void setUpBC() throws Exception {
        fillExpectedMessageKeys();
        // assertTrue(expectedExceptionMessageKeys.size() > 0);
        fillPropFileMessageKeys();
    }

    private static void fillExpectedMessageKeys() throws Exception {
        // first read from SaasApplicationException package
        List<Class<?>> classes = PackageClassReader.getClasses(
                SaaSApplicationException.class, Throwable.class,
                ClassFilter.CLASSES_ONLY);

        for (Class<?> clazz : classes) {
            final String className = clazz.getSimpleName();
            if (!(clazz.newInstance() instanceof RuntimeException
                    || hasSpecialPattern.contains(className) || notThrownInPractice
                        .contains(className))) {
                boolean hasEnum = false;

                Class<?>[] innerClasses = clazz.getClasses();
                for (Class<?> inner : innerClasses) {
                    if (inner.isEnum()) {
                        hasEnum = true;
                        Object[] enumConstants = inner.getEnumConstants();
                        for (Object enumConstant : enumConstants) {
                            String msgKey = clazz.getSimpleName() + "."
                                    + enumConstant.toString();
                            if (enumConstant instanceof ReasonEnum) {
                                String keyFromEnumConstructor = ((ReasonEnum) enumConstant)
                                        .getMessageKey();
                                if (keyFromEnumConstructor != null) {
                                    additionalKeysWithSpecialPattern
                                            .add(keyFromEnumConstructor);
                                    expectedExceptionMessageKeys
                                            .add(keyFromEnumConstructor);
                                }
                            }
                            if (!notThrownInPractice.contains(msgKey)) {
                                addExpectedMessageKey(msgKey);
                            }
                        }
                    }
                }

                if (!hasEnum) {
                    addExpectedMessageKey(className);
                }
            }
        }

        // finally add further expected exceptions (which are exceptions from
        // the generic rule)
        for (String exc : mustBeDeclaredInAddition) {
            addExpectedMessageKey(exc);
        }
        Collections.sort(expectedExceptionMessageKeys);
    }

    private static void addExpectedMessageKey(String name) throws Exception {
        expectedExceptionMessageKeys
                .add(SaaSApplicationException.MESSAGE_PREFIX + name);
    }

    protected static void fillPropFileMessageKeys() throws IOException {
        DefaultMessages messages = new DefaultMessages_en();
        Set<String> keySet = ParameterizedTypes.set(messages.keySet(),
                String.class);
        for (String key : keySet) {
            if (key.startsWith(SaaSApplicationException.MESSAGE_PREFIX)
                    || additionalKeysWithSpecialPattern.contains(key)) {
                propfileMessageKeys.add(key);
            }
        }
        Collections.sort(propfileMessageKeys);
    }

    @Test
    public void missingPropKeys() throws Exception {
        if (showDebugOutput) {
            System.out
                    .println("\n\nMissing message texts in Messages_en.properties:");
        }
        Set<String> missingKeys = new HashSet<String>();
        for (String expected : expectedExceptionMessageKeys) {
            if (!propfileMessageKeys.contains(expected)) {
                missingKeys.add(expected);
                if (showDebugOutput) {
                    System.out.println("  " + expected);
                }
            }
        }
        assertTrue(missingKeys.size()
                + " missing key(s) in log message properties file: "
                + missingKeys.toString(), missingKeys.isEmpty());
    }

    @Ignore
    @Test
    public void obsoletePropKeys() throws Exception {
        if (showDebugOutput) {
            System.out
                    .println("\n\nObsolete message texts in Messages_en.properties:");
        }
        Set<String> obsoleteKeys = new HashSet<String>();
        for (String actual : propfileMessageKeys) {
            if (!expectedExceptionMessageKeys.contains(actual)) {
                obsoleteKeys.add(actual);
                if (showDebugOutput) {
                    System.out.println("  " + actual);
                }
            }
        }
        assertTrue(obsoleteKeys.size()
                + " obsolete key(s) in log message properties file: "
                + obsoleteKeys.toString(), obsoleteKeys.isEmpty());
    }

}
