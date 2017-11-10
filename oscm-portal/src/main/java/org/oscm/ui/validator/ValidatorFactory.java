/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2015-05-28
 *
 *******************************************************************************/

package org.oscm.ui.validator;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class ValidatorFactory {

    /**
     * As Validator objects are stateless then they can be created once and retrieved from the map
     * by factory later.
     */
    private static final Map<Class<?>, Validator> VALIDATORS_MAP = ImmutableMap.<Class<?>, Validator>builder()
            .put(SubscriptionActivationValidator.class, new SubscriptionActivationValidator())
            .put(SubscriptionStatusValidator.class, new SubscriptionStatusValidator())
            .put(MySubscriptionActivationValidator.class, new MySubscriptionActivationValidator())
            .put(MySubscriptionStatusValidator.class, new MySubscriptionStatusValidator())
            .build();

    private ValidatorFactory() {}

    public static Validator getValidator(Class<? extends Validator> cls) {
        Validator validator = VALIDATORS_MAP.get(cls);

        if(validator == null) {
            throw new UnsupportedOperationException("Class not supported: " + cls.getCanonicalName());
        }

        return validator;
    }
}
