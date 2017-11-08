/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 21.02.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.converter.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class VOInitializer {

	private static final String INTERFACE_LIST = "java.util.List";
	private static final String INTERFACE_SET = "java.util.Set";
	private static final String IMPLEMENTATION_LIST = "java.util.ArrayList";
	private static final String IMPLEMENTATION_SET = "java.util.HashSet";

	private static final String PREFIX_BSS = "org.oscm.internal.vo";
	private static final String PREFIX_SET = "set";
	private static final String CLASS_STRING = "java.lang.String";
	private static final int LIST_SIZE = 5;

	private static Random random = new Random();

	@SuppressWarnings({ "rawtypes" })
	public static void initializeList(List list, Class<?> voClazz)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, ClassNotFoundException,
			InstantiationException {
		initializeList(list, voClazz, 0);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void initializeList(List list, Class<?> voClazz,
			int enumElementIndex) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			ClassNotFoundException, InstantiationException {
		if (list == null) {
			throw new IllegalArgumentException("list must not be null");
		}
		if (voClazz == null) {
			throw new IllegalArgumentException("voClazz must not be null");
		}
		for (int i = 0; i < LIST_SIZE; i++) {
			Object voInstance = voClazz.newInstance();
			initialize(voInstance, enumElementIndex);
			list.add(voInstance);
		}
	}

	public static void initialize(Object obj) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			ClassNotFoundException, InstantiationException {
		initialize(obj, 0);
	}

	public static void initialize(Object obj, int enumElementIndex)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, ClassNotFoundException,
			InstantiationException {
		List<Method> setterMethods = getSetterMethods(obj);

		for (Method m : setterMethods) {
			Class<?>[] parameterTypes = m.getParameterTypes();

			if (parameterTypes.length != 1) {
				throw new RuntimeException(
						"Setter method "
								+ m.getName()
								+ " has more than one parameter. Object cannot be initialized!");
			}

			Class<?> clazz = parameterTypes[0];

			if (clazz == byte.class) {
				initializeByte(obj, m);
			} else if (clazz == short.class) {
				initializeShort(obj, m);
			} else if (clazz == int.class) {
				initializeInt(obj, m);
			} else if (clazz == long.class || clazz == java.lang.Long.class) {
				initializeLong(obj, m);
			} else if (clazz == float.class) {
				initializeFloat(obj, m);
			} else if (clazz == double.class) {
				initializeDouble(obj, m);
			} else if (clazz == boolean.class) {
				initializeBoolean(obj, m);
			} else if (clazz == char.class) {
				initializeChar(obj, m);
			} else if (clazz == java.lang.String.class) {
				initializeString(obj, m);
			} else if (clazz == java.math.BigDecimal.class) {
				initializeBigDecimal(obj, m);
			} else if (clazz.isEnum()) {
				initializeEnumeration(obj, m, clazz, enumElementIndex);
			} else if (clazz.getName().startsWith(PREFIX_BSS)) {
				initiliazeVo(obj, m, clazz);
			} else if (clazz.isInterface()
					&& clazz.getName().equals(INTERFACE_LIST)) {

				@SuppressWarnings("unchecked")
				List<Object> list = (List<Object>) Class.forName(
						IMPLEMENTATION_LIST).newInstance();

				initializeCollection(obj, m, list);
			} else if (clazz.isInterface()
					&& clazz.getName().equals(INTERFACE_SET)) {

				@SuppressWarnings("unchecked")
				Set<Object> set = (Set<Object>) Class.forName(
						IMPLEMENTATION_SET).newInstance();

				initializeCollection(obj, m, set);
			}

		}
	}

	private static void initializeByte(Object obj, Method m)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		byte[] b = new byte[1];
		random.nextBytes(b);
		m.invoke(obj, b);
	}

	private static void initializeShort(Object obj, Method m)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		m.invoke(obj, new Short((short) random.nextInt(32767)));
	}

	private static void initializeInt(Object obj, Method m)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		m.invoke(obj, new Integer(random.nextInt()));
	}

	private static void initializeLong(Object obj, Method m)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		m.invoke(obj, new Long(random.nextLong()));
	}

	private static void initializeFloat(Object obj, Method m)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		m.invoke(obj, new Float(random.nextFloat()));
	}

	private static void initializeDouble(Object obj, Method m)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		m.invoke(obj, new Double(random.nextDouble()));
	}

	private static void initializeBoolean(Object obj, Method m)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		m.invoke(obj, new Boolean(random.nextBoolean()));
	}

	private static void initializeChar(Object obj, Method m)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		char[] chars = Character.toChars(random.nextInt());
		m.invoke(obj, new Character(chars[0]));
	}

	private static void initializeString(Object obj, Method m)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {

		String characters = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFIJKLMNOPQRSTUVWXYZ";
		int stringLength = characters.length() - 1;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < random.nextInt(characters.length()); i++) {
			sb.append(Character.toString(characters.charAt(random
					.nextInt(stringLength))));
		}

		m.invoke(obj, sb.toString());
	}

	private static void initializeBigDecimal(Object obj, Method m)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		m.invoke(obj, new BigDecimal(random.nextInt()));
	}

	private static void initializeEnumeration(Object obj, Method m,
			Class<?> cls, int enumElementIndex) throws IllegalAccessException,
			InvocationTargetException {
		m.invoke(obj, cls.getEnumConstants()[enumElementIndex]);
	}

	private static void initializeCollection(Object obj, Method m,
			Collection<Object> t) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			ClassNotFoundException, InstantiationException {

		Class<?> actualType = (Class<?>) getActualTypeFromCollection(m);

		if (actualType.getName().startsWith(PREFIX_BSS)) {
			for (int i = 0; i < LIST_SIZE; i++) {
				Object instance = actualType.newInstance();
				initialize(instance);
				t.add(instance);
			}
		} else if (actualType.getName().startsWith(CLASS_STRING)) {
			for (int i = 0; i < LIST_SIZE; i++) {
				t.add("STRING " + i);
			}
		}

		m.invoke(obj, t);
	}

	private static Type getActualTypeFromCollection(Method m) {
		Type[] types = m.getGenericParameterTypes();
		ParameterizedType o = (ParameterizedType) types[0];
		Type[] actualTypes = o.getActualTypeArguments();
		return actualTypes[0];
	}

	private static List<Method> getSetterMethods(Object obj) {
		List<Method> setterMethods = new ArrayList<Method>();

		Class<?> clazz = obj.getClass();
		for (Method m : clazz.getMethods()) {
			if (m.getName().startsWith(PREFIX_SET)) {
				setterMethods.add(m);
			}
		}

		return setterMethods;
	}

	private static void initiliazeVo(Object obj, Method m, Class<?> clazz)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, IllegalArgumentException,
			InvocationTargetException {

		Object instance = Class.forName(clazz.getName()).newInstance();
		initialize(instance);
		m.invoke(obj, instance);
	}

}
