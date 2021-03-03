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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/** @author goebel */
public interface JsonData extends Serializable {

  public static <T> T fromJson(String s, Class<T> clazz) {
    ObjectMapper om = new ObjectMapper();
    try {
      return om.readValue(s, clazz);
    } catch (IOException e) {
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
}
