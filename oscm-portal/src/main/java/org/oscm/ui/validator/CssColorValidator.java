/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 01.04.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.validator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.oscm.ui.common.JSFUtils;
import org.oscm.internal.types.exception.ValidationException;

/**
 * CSS color validator
 * 
 * Supported color definitions
 * 
 * #rrggbb hex RGB value
 * 
 * #rgb short hex RGB value
 * 
 * colorname
 * 
 * rgb(R,G,B) decimal RGB value (0-255,0-255,0-255)
 * 
 * rgb(%,%,%) percentaged RGB value (0-100%,0-100%,0-100%)
 * 
 */
public class CssColorValidator implements Validator {

    final private static String colors[] = { "black", "gray", "maroon", "red",
            "green", "lime", "olive", "yellow", "navy", "blue", "purple",
            "fuchsia", "teal", "aqua", "silver", "white", "transparent" };

    final private static Set<String> colorSet = new HashSet<String>(
            Arrays.asList(colors));

    /**
     * Validates that the string array contains only numbers (based on the given
     * radix) between min and max.
     * 
     * @param a
     *            the string array to validate
     * @param the
     *            minimum value
     * @param the
     *            maximum value
     */
    private boolean containsNumbersInRange(String[] a, int radix, int min,
            int max) {
        if (a == null || a.length != 3) {
            return false;
        }
        try {
            for (int i = 0; i < 3; i++) {
                int n = Integer.parseInt(a[i], radix);
                if (n < min || n > max) {
                    return false;
                }

            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Validates that the given value contains a css color definition.
     * 
     * @param context
     *            FacesContext for the request we are processing
     * @param component
     *            UIComponent we are checking for correctness
     * @param value
     *            the value to validate
     * @throws ValidatorException
     *             if validation fails
     */
    public void validate(FacesContext facesContext, UIComponent component,
            Object value) throws ValidatorException {
        if (value == null) {
            return;
        }
        String color = value.toString();
        String orig_value = color;
        if (color.length() == 0) {
            return;
        }
        // remove all white spaces also inside the string
        color = color.replaceAll("\\s", "");

        // hex
        if (color.matches("#[a-fA-F0-9]*")
                && (color.length() == 4 || color.length() == 7)) {
            return;
        }

        // rgb
        if (color.matches("[rR][gG][bB]\\([\\d,%]*\\)")) {
            color = color.substring(4, color.length() - 1);
            String a[] = null;
            int max;
            if (color.indexOf("%") >= 0) {
                color = color.replaceAll("%", "");
                max = 100;
            } else {
                max = 255;
            }
            a = color.split(",");
            if (containsNumbersInRange(a, 10, 0, max)) {
                return;
            }
        }

        if (colorSet.contains(color.toLowerCase())) {
            return;
        }

        Object[] args = new Object[] { orig_value };
        // Object[] args = null;
        String label = JSFUtils.getLabel(component);
        // if (label != null) {
        // args = new Object[] { label, orig_value };
        // } else {
        // args = new Object[] { "", orig_value };
        // }
        ValidationException e = new ValidationException(
                ValidationException.ReasonEnum.CSS_COLOR, label, null);
        String text = JSFUtils.getText(e.getMessageKey(), args, facesContext);
        throw new ValidatorException(new FacesMessage(
                FacesMessage.SEVERITY_ERROR, text, null));
    }
}
