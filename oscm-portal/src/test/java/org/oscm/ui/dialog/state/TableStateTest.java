/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 22.01.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.faces.component.UIComponent;

import org.junit.Before;
import org.junit.Test;
import org.richfaces.event.DataScrollEvent;
import org.richfaces.component.SortOrder;

import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.dialog.state.TableState.ActivePageMap;
import org.oscm.ui.dialog.state.TableState.ColumnSortingMap;

/**
 * @author weiser
 * 
 */
public class TableStateTest {

    private static final Integer PAGE = Integer.valueOf(42);
    private static final String CLIENTID = "clientid";

    private TableState ts;

    @Before
    public void setup() {
        ts = new TableState();

        ts.ui = mock(UiDelegate.class);
    }

    @Test
    public void testActivePageMap_Default() {
        ActivePageMap map = new TableState.ActivePageMap();

        assertEquals(Integer.valueOf(1), map.get(CLIENTID));
    }

    @Test
    public void testActivePageMap() {
        ActivePageMap map = new TableState.ActivePageMap();
        map.put(CLIENTID, PAGE);

        assertSame(PAGE, map.get(CLIENTID));
    }

    @Test
    public void testColumnSortingMap_Default() {
        String key = CLIENTID
                + TableState.ColumnSortingMap.DEFAULT_COLUM_SUFFIX;
        ColumnSortingMap map = new TableState.ColumnSortingMap();

        assertEquals(SortOrder.ascending, map.get(key));
    }

    @Test
    public void testColumnSortingMap_DefaultNoSorting() {
        ColumnSortingMap map = new TableState.ColumnSortingMap();

        assertNull(map.get(CLIENTID));
    }

    @Test
    public void testColumnSortingMap() {
        SortOrder value = SortOrder.descending;
        ColumnSortingMap map = new TableState.ColumnSortingMap();
        map.put(CLIENTID, value);

        assertSame(value, map.get(CLIENTID));
    }

    @Test
    public void actionListener() {
        DataScrollEvent dse = setupEventAndMocks(ts.ui, PAGE);

        ts.actionListener(dse);

        assertEquals(PAGE, ts.getActivePages().get(CLIENTID));
        verify(ts.ui).getClientId(any(UIComponent.class));
    }

    @Test
    public void reset() {
        ts.getActivePages().put(CLIENTID, PAGE);

        ts.resetActivePages();

        assertTrue(ts.getActivePages().isEmpty());
    }

    private static DataScrollEvent setupEventAndMocks(UiDelegate ui,
            Integer page) {
        when(ui.getClientId(any(UIComponent.class))).thenReturn(CLIENTID);

        UIComponent uic = mock(UIComponent.class);
        DataScrollEvent dse = mock(DataScrollEvent.class);
        when(Integer.valueOf(dse.getPage())).thenReturn(page);
        when(dse.getComponent()).thenReturn(uic);
        return dse;
    }
}
