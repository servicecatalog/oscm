/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 17.06.15 09:56
 *
 *******************************************************************************/

package org.oscm.triggerservice.validator;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOParameter;

/**
 * Created by FlorekS
 */
public class DurationParameterValidator extends DefaultParameterValidator {

    public final static long MILLISECONDS_PER_DAY = 24L * 3600000L;
    private static final String DURATION_FORMAT = "#0";
    private static final int DEC_PLACES_BOUND = 0;
    private static final String DECIMAL_SEPARATOR = ".";

    @Override
    public boolean supports(Object obj) {
        if (super.supports(obj)) {
            VOParameter parameter = (VOParameter) obj;
            return ParameterValueType.DURATION.equals(parameter
                    .getParameterDefinition().getValueType());
        }

        return false;
    }

    @Override
    public void validate(Object obj) throws ValidationException {
        super.validate(obj);
        VOParameter parameter = (VOParameter) obj;

        if(isOptionalAndNullOrEmpty(parameter)) {
            return;
        }
        
        if (!isValidNumber(parameter.getValue())
                || !hasValidPrecision(parameter.getValue())
                || convertDuration(parameter.getValue()) == null) {
            throw new ValidationException(
                    ValidationException.ReasonEnum.DURATION, null,
                    new Object[] { parameter.getParameterDefinition()
                            .getParameterId() });
        }
    }

    private boolean isValidNumber(String valueString) {
        valueString = valueString.replaceAll("\\s", ""); // remove spaces
        valueString = valueString.replace(",", DECIMAL_SEPARATOR); // change ','
                                                                   // to dot if
                                                                   // double

        try {
            Double.parseDouble(valueString);
        } catch (NumberFormatException ignored) {
            return false;
        }

        try {
            Long.parseLong(valueString);
        } catch (NumberFormatException ignored) {
            return false;
        }

        return true;
    }

    private boolean hasValidPrecision(String valueString) {
        int pos = valueString.lastIndexOf(DECIMAL_SEPARATOR);
        return !(pos + DEC_PLACES_BOUND >= valueString.length() - 1);
    }

    /**
     * Taken from org.oscm.ui.common.DurationValidation
     * 
     * @param valueToCheck
     * @return
     */
    private Number getParsedDuration(String valueToCheck) {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.getDefault());
        DecimalFormat df = new DecimalFormat(DURATION_FORMAT, dfs);
        df.setGroupingUsed(true);
        try {
            return df.parse(valueToCheck);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Taken from org.oscm.ui.common.DurationValidation
     * 
     * @param durationValue
     * @return
     */
    private Long convertDuration(String durationValue) {
        Number parsedDuration = getParsedDuration(durationValue);
        if (parsedDuration != null) {
            // as validation passed, the value is not null
            double milliseconds = parsedDuration.doubleValue();
            Long msLong = Long.valueOf(Math.round(milliseconds));
            if (milliseconds > Long.MAX_VALUE
                    || msLong.longValue() % MILLISECONDS_PER_DAY != 0) {
                return null;
            } else {
                return Long.valueOf(Math.round(milliseconds));
            }
        } else {
            return null;
        }
    }
}
