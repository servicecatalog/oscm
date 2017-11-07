/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 22.07.15 12:02
 *
 *******************************************************************************/

package org.oscm.converter.api;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.UserGroup;
import org.oscm.converter.strategy.ConversionFactory;
import org.oscm.converter.strategy.ConversionStrategy;
import org.oscm.vo.VOOrganizationalUnit;
import org.oscm.vo.VOUser;

public class ConverterTest {

    private static final long VALID_KEY = 0;
    private static final String VALID_ID = "0";
    
    private static ConversionStrategy<?, ?> conversionMock = new ConversionStrategy() {
        @Override
        public void setDataService(DataService dataService) {

        }

        @Override
        public DataService getDataService() {
            return null;
        }

        @Override
        public Object convert(Object o) {
            return null;
        }
    };

    @Test
    public void converterNullParamConvert() {
        for (ConversionStrategy<?, ?> converter : ConversionFactory
                .getRegisteredConverters()) {
            // when
            Object obj = converter.convert(null);

            // then
            Assert.assertTrue(obj == null);
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void converterClassNotSupported() {
        // given        
        // when
        ConversionFactory.deregister(Object.class, Object.class);
        Converter.convert(null, Object.class, Object.class);

        // then exception
    }

    @Test
    public void converterRegistration() {
        // given
        // when
        ConversionFactory.register(Object.class, Object.class, conversionMock);
        Object obj = Converter.convert(null, Object.class, Object.class);

        // then
        Assert.assertTrue(obj == null);
    }

    @Test
    public void toDomUserConvert() {
        // given
        VOUser user = new VOUser();

        user.setKey(VALID_KEY);
        user.setUserId(VALID_ID);

        // when
        PlatformUser pUser = Converter.convert(user, VOUser.class,
                PlatformUser.class);

        Assert.assertEquals(user.getKey(), pUser.getKey());
        Assert.assertEquals(user.getUserId(), pUser.getUserId());
    }

    @Test
    public void toExtUserConvert() {
        // given
        PlatformUser user = new PlatformUser();

        user.setKey(VALID_KEY);
        user.setUserId(VALID_ID);

        // when
        VOUser vUser = Converter
                .convert(user, PlatformUser.class, VOUser.class);

        // then

        Assert.assertEquals(user.getKey(), vUser.getKey());
        Assert.assertEquals(user.getUserId(), vUser.getUserId());
    }

    @Test
    public void toDomUnitConvert() {
        // given
        UserGroup group = new UserGroup();
        Organization organization = new Organization();
        
        group.setKey(VALID_KEY);
        group.setReferenceId(VALID_ID);
        group.setIsDefault(true);
        group.setDescription("TEST");
        group.setOrganization(organization);
    
        organization.setOrganizationId(VALID_ID);

        // when
        VOOrganizationalUnit unit = Converter.convert(group, UserGroup.class,
                VOOrganizationalUnit.class);

        // then
        Assert.assertEquals(group.getKey(), unit.getKey());
        Assert.assertEquals(group.getReferenceId(), unit.getReferenceId());
        Assert.assertEquals(group.isDefault(), unit.isDefaultGroup());
        Assert.assertEquals(group.getDescription(), unit.getDescription());
        Assert.assertEquals(group.getOrganization().getOrganizationId(), unit.getOrganizationId());
    }

    @Test
    public void toExtUnitConvert() {
        // given
        VOOrganizationalUnit unit = new VOOrganizationalUnit();
    
        unit.setKey(VALID_KEY);

        // when
        UserGroup group = Converter.convert(unit, VOOrganizationalUnit.class, UserGroup.class);

        // then
        Assert.assertEquals(unit.getKey(), group.getKey());        
    }
    
    @AfterClass
    public static void tearDown() {
        ConversionFactory.deregister(Object.class, Object.class);
    }
}
