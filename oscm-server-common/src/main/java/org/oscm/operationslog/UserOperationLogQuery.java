/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: barzu                                       
 *                                                                              
 *  Creation Date: Oct 13, 2011                                        
 *                                                                              
 *  Completion Time: Oct 13, 2011                                            
 *                                                                              
 *******************************************************************************/

package org.oscm.operationslog;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.exception.IllegalArgumentException;

/**
 * Base for all queries for user operations log.
 * 
 * @author barzu
 */
public abstract class UserOperationLogQuery {

	public static final String DATE_FORMAT = "MM/dd/yyyy_HH:mm:ss.SSS";
	public static final String COMMON_COLUMN_MODDATE = "moddate";
	public static final String COMMON_COLUMN_OBJVERSION = "objversion";

	public abstract LogMessageIdentifier getLogMessageIdentifier();

	public abstract String getQuery();

	public abstract String[] getFieldNames();

	public abstract String getLogType();

	/**
	 * Formats the result into a user readable form.
	 * 
	 * @param result
	 *            the unformatted result.
	 * @return the formatted result.
	 * @throws IllegalArgumentException
	 *             if the result is not a List<Object[]>
	 */
	public List<Object[]> format(List<?> result) {
		List<Object[]> formattedResult = new ArrayList<Object[]>(result.size());
		for (Object rowObject : result) {
			if (!(rowObject instanceof Object[])) {
				throw new IllegalArgumentException(
						"The query result is not a List<Object[]>");
			}
			Object[] row = (Object[]) rowObject;
			formatRow(row);
			for (int i = 0; i < row.length; i++) {
				if (row[i] == null) {
					row[i] = "";
				} else if (row[i] instanceof Boolean) {
					row[i] = formatBoolean((Boolean) row[i]);
				}
			}
			formattedResult.add(row);
		}
		return formattedResult;
	}

	protected void formatRow(@SuppressWarnings("unused") Object[] row) {
		// to be overridden by subclasses
	}

	public static String formatDate(BigInteger dateInMillis) {
		if (dateInMillis != null) {
			return formatDate(new Date(dateInMillis.longValue()));
		}
		return null;
	}

	public static String formatDate(Date date) {
		if (date != null) {
			return createDateFormat().format(date);
		}
		return null;
	}

	public static String formatBoolean(Boolean bool) {
		if (bool != null) {
			return String.valueOf(bool.booleanValue()).toUpperCase();
		}
		return null;
	}

	public static SimpleDateFormat createDateFormat() {
		SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
		return format;
	}

	public static String formatYesNo(Boolean bool) {
		if (bool != null) {
			return bool.booleanValue() ? "YES" : "NO";
		}
		return null;
	}
}
