/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2021
 *
 * <p>Creation Date: 03.03.2021
 *
 * <p>*****************************************************************************
 */
package org.oscm.ui.model;

import java.io.IOException;
import java.io.Serializable;

import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/** @author goebel */
public abstract class JsonData implements Serializable {
  /** */
  private static final long serialVersionUID = -2365628918429352583L;

  static Log4jLogger LOGGER = LoggerFactory.getLogger(JsonData.class);

  public static <T> T fromJson(String s, Class<T> clazz) {
    ObjectMapper om = new ObjectMapper();
    try {
      return om.readValue(s, clazz);
    } catch (IOException e) {
      if (LOGGER.isDebugLoggingEnabled()) {
        LOGGER.logWarn(
            Log4jLogger.SYSTEM_LOG,
            e,
            LogMessageIdentifier.ERROR,
            String.format("Parsing to %s: %s ", clazz.getSimpleName(), s));
      }
      try {
        return clazz.newInstance();
      } catch (InstantiationException | IllegalAccessException e1) {
        throw new SaaSSystemException(e1);
      }
    }
  }

  public static String toJson(Object obj) {
    ObjectMapper om = new ObjectMapper();
    try {
      return om.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new SaaSSystemException(e);
    }
  }

  public static boolean isModified(Object obj) {
    try {
      String defaults = toJson(obj.getClass().newInstance());
      String updates = toJson(obj);
      return !updates.equals(defaults);
    } catch (IllegalAccessException | InstantiationException e) { // TODO Auto-generated catch block
      if (LOGGER.isDebugLoggingEnabled()) {
        LOGGER.logWarn(
            Log4jLogger.SYSTEM_LOG,
            e,
            LogMessageIdentifier.ERROR,
            String.format("JSON conversion to %s failed ", obj.getClass().getSimpleName()));
      }
      return true;
    }
  }
}
