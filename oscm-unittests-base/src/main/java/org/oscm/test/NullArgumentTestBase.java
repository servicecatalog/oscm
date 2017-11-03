/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jws.WebParam;

import org.junit.Test;

import org.oscm.string.Strings;
import org.oscm.test.ejb.TestContainer;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.types.enumtypes.PaymentInfoType;
import org.oscm.types.enumtypes.UserAccountStatus;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.vo.LdapProperties;
import org.oscm.vo.ListCriteria;
import org.oscm.vo.VOBillingContact;
import org.oscm.vo.VOGatheredEvent;
import org.oscm.vo.VOImageResource;
import org.oscm.vo.VOInstanceInfo;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOPaymentInfo;
import org.oscm.vo.VOPriceModel;
import org.oscm.vo.VOPriceModelLocalization;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServiceDetails;
import org.oscm.vo.VOServiceLocalization;
import org.oscm.vo.VOServiceReview;
import org.oscm.vo.VOSubscription;
import org.oscm.vo.VOTechnicalService;
import org.oscm.vo.VOTechnicalServiceOperation;
import org.oscm.vo.VOTriggerDefinition;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;

/**
 * Automated test for null parameters passed to remote interfaces.
 * 
 * @author hoffmann
 */
public abstract class NullArgumentTestBase<R> extends EJBTestBase {

    private final Class<R> remoteInterface;

    private R instance;

    private final Set<String> nullAllowed = new HashSet<String>();

    protected NullArgumentTestBase(Class<R> remoteInterface) {
        this.remoteInterface = remoteInterface;
    }

    @Override
    protected final void setup(TestContainer container) throws Exception {
        instance = createInstance(container);
    }

    protected abstract R createInstance(TestContainer container)
            throws Exception;

    protected final void addNullAllowed(String method, String parameter) {
        nullAllowed.add(createKey(method, parameter));
    }

    private String createKey(String method, String parameter) {
        return method + "/" + parameter;
    }

    @Test
    public void testNullArguments() throws Throwable {
        for (final Method method : remoteInterface.getMethods()) {
            verifyMethod(method);
        }
    }

    private void verifyMethod(final Method method) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Annotation[][] paramAnnotations = method
                .getParameterAnnotations();
        for (int index = 0; index < parameterTypes.length; index++) {
            final String paramName = getParameterName(paramAnnotations[index],
                    method);
            if (!parameterTypes[index].isPrimitive()
                    && !nullAllowed.contains(createKey(method.getName(),
                            paramName))) {
                verifyArgument(method, parameterTypes, index, paramName);
            }
        }
    }

    private void verifyArgument(Method method, Class<?>[] parameterTypes,
            int index, String paramName) {
        final Object[] parameters = createParameters(parameterTypes, index);
        try {
            method.invoke(instance, parameters);
            fail(String
                    .format("Exception expected while calling %s with null for parameter %s.",
                            method.getName(), paramName));
        } catch (InvocationTargetException e) {
            final Throwable cause = findIllegalArgumentExceptionInCauses(e);
            if (cause instanceof IllegalArgumentException) {
                final String message = cause.getMessage();
                // special handling for deprecated add and revoke users to user
                // group
                if (method.getName().contains("UserGroup")
                        && "groupName".equals(paramName)) {
                    paramName = "unitName";
                }
                if (!message.endsWith("Parameter " + paramName
                        + " must not be null.")) {
                    fail(String
                            .format("Unexpected message while calling %s with null for parameter %s: %s",
                                    method.getName(), paramName, message));
                }
                return;
            }
            fail(String
                    .format("Unexpected exception while calling %s with null for parameter %s: %s",
                            method.getName(), paramName, cause));
        } catch (Exception e) {
            fail(String
                    .format("Unexpected exception while calling %s with null for parameter %s: %s",
                            method.getName(), paramName, e));
        }
    }

    private Throwable findIllegalArgumentExceptionInCauses(
            InvocationTargetException e) {
        if (e.getCause() instanceof IllegalArgumentException) {
            return e.getCause();
        }
        if (e.getCause().getCause() instanceof IllegalArgumentException) {
            return e.getCause().getCause();
        }
        if (e.getTargetException() instanceof IllegalArgumentException) {
            return e.getTargetException();
        }
        return e;
    }

    private Object[] createParameters(Class<?>[] parameterTypes, int index) {
        final Object[] parameters = new Object[parameterTypes.length];
        for (int i = 0; i < parameters.length; i++) {
            if (i != index) {
                final Class<?> type = parameterTypes[i];
                Object obj = null;
                obj = INSTANCES.get(type);
                if (obj == null) {
                    try {
                        if (type.isEnum())
                            obj = type.getDeclaredFields()[0].get(type);
                        else
                            obj = type.newInstance();
                    } catch (Exception ex) {
                        throw new RuntimeException(
                                "Unable to instantiate class " + type.getName());
                    }
                }
                assertNotNull("No instance for " + type, obj);
                parameters[i] = obj;
            }
        }
        return parameters;
    }

    private static final Map<Class<?>, Object> INSTANCES = new HashMap<Class<?>, Object>();

    private static byte[] bytes(String value) {
        return Strings.toBytes(value);
    }

    static {
        INSTANCES.put(Boolean.TYPE, Boolean.FALSE);
        INSTANCES.put(Long.TYPE, Long.valueOf(0));
        INSTANCES.put(Long.class, Long.valueOf(0));
        INSTANCES.put(String.class, "string");
        INSTANCES.put(Properties.class, new Properties());
        INSTANCES.put(List.class, Collections.EMPTY_LIST);
        INSTANCES.put(Set.class, Collections.EMPTY_SET);
        INSTANCES.put(Integer.TYPE, Integer.valueOf(5));
        INSTANCES.put(byte[].class,
                bytes("user,user1@org.com,en,MR,John,Doe,ORGANIZATION_ADMIN"));

        INSTANCES.put(PaymentInfoType.class, PaymentInfoType.INVOICE);
        INSTANCES.put(UserAccountStatus.class, UserAccountStatus.ACTIVE);
        INSTANCES.put(UserRoleType.class, UserRoleType.ORGANIZATION_ADMIN);

        INSTANCES.put(VOGatheredEvent.class, new VOGatheredEvent());
        INSTANCES.put(VOImageResource.class, new VOImageResource());
        INSTANCES.put(VOInstanceInfo.class, new VOInstanceInfo());
        INSTANCES.put(VOOrganization.class, new VOOrganization());
        INSTANCES.put(VOUser.class, new VOUser());
        INSTANCES.put(VOUserDetails.class, new VOUserDetails());
        INSTANCES.put(VOPriceModel.class, new VOPriceModel());
        INSTANCES.put(VOPriceModelLocalization.class,
                new VOPriceModelLocalization());
        INSTANCES.put(VOService.class, new VOService());
        INSTANCES.put(VOServiceDetails.class, new VOServiceDetails());
        INSTANCES.put(VOServiceLocalization.class, new VOServiceLocalization());
        INSTANCES.put(VOServiceReview.class, new VOServiceReview());
        INSTANCES.put(VOSubscription.class, new VOSubscription());
        INSTANCES.put(VOTechnicalService.class, new VOTechnicalService());
        INSTANCES.put(VOTechnicalServiceOperation.class,
                new VOTechnicalServiceOperation());
        INSTANCES.put(VOPaymentInfo.class, new VOPaymentInfo());
        INSTANCES.put(VOBillingContact.class, new VOBillingContact());
        INSTANCES.put(ListCriteria.class, new ListCriteria());
        INSTANCES.put(VOTriggerDefinition.class, new VOTriggerDefinition());
        INSTANCES.put(LdapProperties.class, new LdapProperties());
    }

    private String getParameterName(Annotation[] paramAnnotations, Method method) {
        for (Annotation a : paramAnnotations) {
            if (a instanceof WebParam) {
                WebParam webparam = (WebParam) a;
                return webparam.name();
            }
        }
        fail("Missing WebParam annotation in method " + method.getName());
        return null;
    }
}
