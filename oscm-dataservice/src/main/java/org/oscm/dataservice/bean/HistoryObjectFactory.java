/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.dataservice.bean;

import java.lang.reflect.Constructor;
import java.util.Date;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.converter.PropertiesLoader;
import org.oscm.domobjects.DomainHistoryObject;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.interceptor.DateFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * Factory to create history objects for domain objects.
 * 
 * @author hoffmann
 */
class HistoryObjectFactory {

	private static final Log4jLogger logger = LoggerFactory
			.getLogger(PropertiesLoader.class);

	/**
	 * Returns a history object for the corresponding domain object.
	 * 
	 * @param obj
	 *            Domain object to create a history object for.
	 * @param type
	 *            Modification type for this history entry.
	 * @param user
	 *            User creating the change.
	 * @return history Object
	 */
	public static DomainHistoryObject<?> create(DomainObject<?> obj,
			ModificationType type, String user) {
		final DomainHistoryObject<?> history = createHistoryObject(obj);
		history.setObjVersion(getVersion(obj, type));
		history.setModtype(type);
		final Long modificationTime = obj.getHistoryModificationTime();
		Date currentDate = DateFactory.getInstance().getTransactionDate();
		if (currentDate == null) {
			currentDate = new Date();
		}
		if (modificationTime == null) {
			history.setModdate(currentDate);
		} else {
			history.setModdate(new Date(modificationTime.longValue()));
		}
		history.setInvocationDate(currentDate);
		history.setModuser(user);
		return history;
	}

	/**
	 * Calculates the history version retrieved from the given object.
	 * 
	 * @param obj
	 * @param type
	 * @return
	 * @throws AssertionError
	 */
	protected static int getVersion(DomainObject<?> obj, ModificationType type)
			throws AssertionError {
		switch (type) {
		case ADD:
			// Hibernate sometimes reports version 1 for newly created objects:
			return 0;
		case MODIFY:
			return obj.getVersion();
		case DELETE:
			return obj.getVersion() + 1;
		default:
			throw new AssertionError(type);
		}
	}

	/**
	 * Helper method to create a history object for a given domain object. The
	 * history object's class name is dynamically constructed from the domain
	 * object's class name followed by the term "History".
	 * 
	 * @param obj
	 *            domain object
	 * @return the history domain object
	 */
	protected static DomainHistoryObject<?> createHistoryObject(
			DomainObject<?> obj) {
		final String histClassName = obj.getClass().getName() + "History";
		Class<?> clazz = obj.getClass();
		final ClassLoader classLoader = getClassLoader(clazz);
		try {
			final Class<?> histClass = classLoader.loadClass(histClassName);
			final Constructor<?> constructor = histClass
					.getConstructor(new Class[] { obj.getClass() });
			return (DomainHistoryObject<?>) constructor
					.newInstance(new Object[] { obj });
		} catch (Exception e) {
			throw new SaaSSystemException("Exception creating History Object "
					+ histClassName, e);
		}
	}

	/**
	 * Tries to get the {@link ClassLoader} from the provided {@link Class}.
	 * Throws a {@link SaaSSystemException} in case the returned
	 * {@link ClassLoader} is <code>null</code>.
	 * 
	 * @param clazz
	 *            the {@link Class} to get the {@link ClassLoader} from
	 * @return the {@link ClassLoader}
	 */
	protected static ClassLoader getClassLoader(final Class<?> clazz) {
		final ClassLoader classLoader = clazz.getClassLoader();
		if (classLoader == null) {
			SaaSSystemException e = new SaaSSystemException(
					"No classloader found.");
			logger.logError(Log4jLogger.SYSTEM_LOG, e,
					LogMessageIdentifier.ERROR_NO_CLASS_LOADER_RETURNED,
					String.valueOf(clazz));
			throw e;
		}
		return classLoader;
	}

}
