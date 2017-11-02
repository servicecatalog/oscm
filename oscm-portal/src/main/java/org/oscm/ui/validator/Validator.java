/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2015-05-28
 *
 *******************************************************************************/

package org.oscm.ui.validator;

public interface Validator {

    boolean supports(Class<?> objCls, Class<?> paramCls);

    boolean validate(Object obj, Object param);
}
