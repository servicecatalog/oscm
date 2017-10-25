/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 17.06.15 09:56
 *
 *******************************************************************************/

package org.oscm.triggerservice.validator;

import java.util.HashMap;
import java.util.Map;

import org.oscm.internal.types.enumtypes.ParameterValueType;

public class ParameterValidatorFactory {

    /**
     * As Validator objects are stateless then they can be created once and
     * retrieved from the map by factory later.
     */
    private static final Map<Class<? extends Validator>, Validator> VALIDATORS_MAP = new HashMap<Class<? extends Validator>, Validator>() {
        private static final long serialVersionUID = -7581881526751544395L;

        {
            put(BooleanParameterValidator.class,
                    new BooleanParameterValidator());
            put(DurationParameterValidator.class,
                    new DurationParameterValidator());
            put(EnumerationParameterValidator.class,
                    new EnumerationParameterValidator());
            put(IntegerParameterValidator.class,
                    new IntegerParameterValidator());
            put(LongParameterValidator.class, new LongParameterValidator());
            put(StringParameterValidator.class, new StringParameterValidator());
        }
    };

    private static final Map<ParameterValueType, Class<? extends Validator>> VALUE_TYPE_MAP = new HashMap<ParameterValueType, Class<? extends Validator>>() {
        private static final long serialVersionUID = 3451638695310555753L;

        {
            put(ParameterValueType.BOOLEAN, BooleanParameterValidator.class);
            put(ParameterValueType.DURATION, DurationParameterValidator.class);
            put(ParameterValueType.ENUMERATION,
                    EnumerationParameterValidator.class);
            put(ParameterValueType.INTEGER, IntegerParameterValidator.class);
            put(ParameterValueType.LONG, LongParameterValidator.class);
            put(ParameterValueType.STRING, StringParameterValidator.class);
        }
    };

    private ParameterValidatorFactory() {
    }

    public static Validator getValidator(Class<? extends Validator> cls) {
        Validator validator = VALIDATORS_MAP.get(cls);

        if (validator == null) {
            throw new UnsupportedOperationException("Class not supported: "
                    + cls.getCanonicalName());
        }

        return validator;
    }

    public static Validator getValidator(ParameterValueType valueType) {
        if (valueType == null) {
            throw new UnsupportedOperationException("Value type is null");
        }

        Class<? extends Validator> cls = VALUE_TYPE_MAP.get(valueType);
        Validator validator = VALIDATORS_MAP.get(cls);

        if (validator == null) {
            throw new UnsupportedOperationException(
                    "Value type not supported: " + cls.getCanonicalName());
        }

        return validator;
    }
}
