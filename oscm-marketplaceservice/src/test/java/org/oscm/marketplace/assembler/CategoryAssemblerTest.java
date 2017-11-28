/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                     
 *                                                                              
 *  Creation Date: 15.02.2012                                                      
 *                                                                              
 *  Completion Time: 15.02.2012                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Category;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.test.BaseAdmUmTest;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCategory;

/**
 * Test class for CategoryAssembler
 * 
 * @author cheld
 * 
 */
public class CategoryAssemblerTest {

    private LocalizerFacade localizer;

    Category categoryDomainObject;

    @Before
    public void setUp() {
        categoryDomainObject = new Category();
        categoryDomainObject.setKey(1);
        categoryDomainObject.setCategoryId("cat1234");
        Marketplace mp = new Marketplace();
        mp.setMarketplaceId("mp1234");
        categoryDomainObject.setMarketplace(mp);

        LocalizerServiceStub localizerServiceStub = new LocalizerServiceStub() {
            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                if (objectKey == 1
                        && objectType == LocalizedObjectTypes.CATEGORY_NAME) {
                    return "localizedName";
                }
                return null;
            }
        };
        localizer = new LocalizerFacade(localizerServiceStub, "en");
    }

    /**
     * Create transfer object - NPE check
     */
    @Test
    public void toVOMarketplace_NullInput() {
        assertNull(CategoryAssembler.toVOCategory(null, localizer));
    }

    /**
     * Create transfer object from domain object
     */
    @Test
    public void toVOCategory() {

        // when converting domain object
        VOCategory resultingTransferObject = CategoryAssembler.toVOCategory(
                categoryDomainObject, localizer);

        // then all fields are set in the transfer object
        assertEquals("cat1234", resultingTransferObject.getCategoryId());
        assertEquals("mp1234", resultingTransferObject.getMarketplaceId());
        assertEquals("localizedName", resultingTransferObject.getName());
    }

    /**
     * Create transfer object without localization
     */
    @Test
    public void toVOCategory_noLocalization() {
        // given
        Category categoryWithoutLocalization = new Category();
        categoryWithoutLocalization.setMarketplace(new Marketplace());
        // when
        VOCategory resultingTransferObject = CategoryAssembler.toVOCategory(
                categoryWithoutLocalization, localizer);
        // then
        assertNull(resultingTransferObject.getName());
    }

    /**
     * Update domain object.
     * 
     * @throws Exception
     */
    @Test
    public void updateCategory() throws Exception {

        // given transfer object
        VOCategory transferObject = CategoryAssembler.toVOCategory(
                categoryDomainObject, localizer);
        transferObject.setCategoryId("newCatID");

        // when updated
        CategoryAssembler.updateCategory(categoryDomainObject, transferObject);

        // then domain object is updated
        assertEquals("newCatID", categoryDomainObject.getCategoryId());
    }

    /**
     * Update domain object - concurrent modification
     * 
     * @throws Exception
     */
    @Test(expected = ConcurrentModificationException.class)
    public void updateCategory_concurrentModification() throws Exception {

        // given transfer object with a version that is lower than in the
        // repository
        VOCategory outdatedTransferObject = CategoryAssembler.toVOCategory(
                categoryDomainObject, localizer);
        outdatedTransferObject.setVersion(-1);

        // when trying to update, an exception is thrown
        CategoryAssembler.updateCategory(categoryDomainObject,
                outdatedTransferObject);

    }

    @Test(expected = IllegalArgumentException.class)
    public void updateCategory_null() throws Exception {
        CategoryAssembler.updateCategory(categoryDomainObject, null);
    }

    /**
     * Update domain object - use transfer object that does not match the domain
     * object
     * 
     * @throws Exception
     */
    @Test(expected = SaaSSystemException.class)
    public void updateCategory_wrongKey() throws Exception {
        // given
        VOCategory wrongTransferObject = new VOCategory();
        wrongTransferObject.setKey(8);
        // when
        CategoryAssembler.updateCategory(categoryDomainObject,
                wrongTransferObject);
        // then
    }

    /**
     * Update domain object - use too long category id
     * 
     * @throws Exception
     */
    @Test(expected = ValidationException.class)
    public void updateMarketplace_updateTooLongCategoryId() throws Exception {
        // given
        VOCategory illegalTransferObject = CategoryAssembler.toVOCategory(
                categoryDomainObject, localizer);
        illegalTransferObject.setCategoryId(BaseAdmUmTest.TOO_LONG_ID);
        // when
        CategoryAssembler.updateCategory(categoryDomainObject,
                illegalTransferObject);
        // then
    }

    @Test
    public void verifyCategoryUpdated_ok() throws Exception {
        // given
        VOCategory illegalTransferObject = CategoryAssembler.toVOCategory(
                categoryDomainObject, localizer);
        illegalTransferObject.setCategoryId(BaseAdmUmTest.TOO_LONG_ID);
        // when
        CategoryAssembler.verifyCategoryUpdated(categoryDomainObject,
                illegalTransferObject, localizer);
        // then
    }

    @Test(expected = ConcurrentModificationException.class)
    public void verifyCategoryUpdated_ConcurrentModificationException_DifVersion()
            throws Exception {
        // given
        VOCategory illegalTransferObject = CategoryAssembler.toVOCategory(
                categoryDomainObject, localizer);
        illegalTransferObject.setVersion(-1);
        illegalTransferObject.setCategoryId(BaseAdmUmTest.TOO_LONG_ID);
        try {
            // when
            CategoryAssembler.verifyCategoryUpdated(categoryDomainObject,
                    illegalTransferObject, localizer);
            // then
            fail();
        } catch (Exception e) {
            throw e;
        }
    }

    @Test(expected = ConcurrentModificationException.class)
    public void verifyCategoryUpdated_ConcurrentModificationException_DifText()
            throws Exception {
        // given
        VOCategory illegalTransferObject = CategoryAssembler.toVOCategory(
                categoryDomainObject, localizer);
        illegalTransferObject.setName("InvalidName");
        illegalTransferObject.setCategoryId(BaseAdmUmTest.TOO_LONG_ID);
        try {
            // when
            CategoryAssembler.verifyCategoryUpdated(categoryDomainObject,
                    illegalTransferObject, localizer);
            // then
            fail();
        } catch (Exception e) {
            throw e;
        }
    }
}
