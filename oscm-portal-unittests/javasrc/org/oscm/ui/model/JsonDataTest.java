/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2021
 *
 * <p>Creation Date: 05.03.2021
 *
 * <p>*****************************************************************************
 */
package org.oscm.ui.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.oscm.logging.Log4jLogger;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/** @author goebel */
public class JsonDataTest {
  DisplaySettings defaults = new DisplaySettings();
  Log4jLogger loggerMock;

  @Before
  public void setup() {
    loggerMock = mock(Log4jLogger.class);
    doReturn(Boolean.TRUE).when(loggerMock).isDebugLoggingEnabled();
    JsonData.LOGGER = loggerMock;
  }

  @Test
  public void toJson() {
    // given
    DisplaySettings d = givenDarkModeSettings();

    // when
    String json = JsonData.toJson(d);

    // then
    assertTrue(isValid(json));
  }

  @Test
  public void fromJson() {
    // given
    String json = JsonData.toJson(defaults);

    // when
    DisplaySettings d = JsonData.fromJson(json, DisplaySettings.class);

    // then
    assertNotNull(d);
  }

  @Test
  public void fromJson_invalid() {
    // given
    String json = "{invalidJson}";

    // when
    DisplaySettings d = JsonData.fromJson(json, DisplaySettings.class);

    // then
    assertParserErrorLogged();
    assertNotNull(d);
  }

  @Test
  public void isModified_defaults() {
    // when
    boolean modified = JsonData.isModified(defaults);

    // then
    assertFalse(modified);
  }

  @Test
  public void isModified_darkMode() {
    // when
    boolean modified = JsonData.isModified(givenDarkModeSettings());

    // then
    assertTrue(modified);
  }

  DisplaySettings givenDarkModeSettings() {
    DisplaySettings d = new DisplaySettings();
    d.setDarkMode(true);
    return d;
  }

  private void assertParserErrorLogged() {
    verify(loggerMock, times(1))
        .logWarn(
            eq(Log4jLogger.SYSTEM_LOG),
            anyObject(),
            eq(LogMessageIdentifier.ERROR),
            contains(String.format("Parsing to %s", DisplaySettings.class.getSimpleName())));
  }

  static boolean isValid(String data) {

    ObjectMapper om = new ObjectMapper();
    try {
      JsonParser parser = om.getFactory().createParser(data);
      while (parser.nextToken() != null) {}
      return true;
    } catch (IOException e) {
      return false;
    }
  }
}
