/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Author: Mao
 *
 * <p>Creation Date: 29.08.2013
 *
 * <p>Completion Time: 29.08.2013
 *
 * <p>*****************************************************************************
 */
package org.oscm.ui.common;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.junit.Test;

/** @author Mao */
public class RequestUrlHandlerTest {

  private static final String WHITE_LABEL_PATH = "http://localhost:8180/oscm-portal";

  private static final String INVALID_URL = "http://thisisaunittest.com";

  @Test
  public void isUrlAvailable_IOException() throws IOException {
    // when
    boolean result = RequestUrlHandler.isUrlAccessible(INVALID_URL);

    // then
    assertEquals(Boolean.FALSE, Boolean.valueOf(result));
  }

  @Test
  public void isUrlAvailable_NullOrEmpty() throws IOException {
    // when
    boolean result1 = RequestUrlHandler.isUrlAccessible(null);
    boolean result2 = RequestUrlHandler.isUrlAccessible(null);

    // then
    assertEquals(Boolean.FALSE, Boolean.valueOf(result1));
    assertEquals(Boolean.FALSE, Boolean.valueOf(result2));
  }
}
