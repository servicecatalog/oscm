/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: October 10, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Utilities for accessing objects via the Java Reflection API
 * 
 * @author baumann
 */
public class ReflectiveAccess {

	private static final String FIELD_MODIFIERS = "modifiers";

	public static boolean set(Object object, String fieldName, Object fieldValue) {
		Class<?> clazz = object.getClass();
		while (clazz != null) {
			try {
				Field field = clazz.getDeclaredField(fieldName);
				field.setAccessible(true);
				removeFinalModifier(field);
				field.set(object, fieldValue);
				return true;
			} catch (NoSuchFieldException e) {
				clazz = clazz.getSuperclass();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	private static void removeFinalModifier(Field field) throws Exception {
		Field modifiers = Field.class.getDeclaredField(FIELD_MODIFIERS);
		modifiers.setAccessible(true);
		modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
	}
}
