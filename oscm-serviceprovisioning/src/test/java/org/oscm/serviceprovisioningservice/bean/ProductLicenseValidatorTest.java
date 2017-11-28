/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import org.junit.Assert;
import org.junit.Test;

import org.oscm.domobjects.TechnicalProduct;

public class ProductLicenseValidatorTest {

    @Test
    public void testValidate() throws Exception {
        String oldLicenseText = "<p><strong>License Agreement</strong><br /><br />The license  §100 terms of (hereinafter called \"licensor\") are applied for the concession of the rights of use for the entire or partly use of the object code of the software SmartSVN (hereinafter called \"SOFTWARE\") to contractors, juristic persons under public law or official fund assets in terms of &sect;310 in conjunction with &sect;14 BGB [Civil Code] (hereinafter called \"licensee\"). Herewith the inclusion of the licensee's own terms and conditions is contradicted, unless their validity has explicitly been agreed to.<br /><br />2 Scope of the Rights of Use<br /><br />2.1 The following terms are valid for the assignment and use of the SOFTWARE for an unlimited period of time including any documentation and the license file (a file that is custom-made for each individual granting of a license, the file being necessary for the operation of the SOFTWARE).<br /><br />2.2 They are not valid for additional services such as installation, integration, parameterization and customization of the SOFTWARE to the licensee's requirements.</p>";

        String newLicenseText = "<p><strong>License Agreement</strong></p>"
                + "<p>&nbsp;</p>"
                + "<p>The license &sect;100 terms of (hereinafter called \"licensor\") are applied for the concession of the rights of use for the entire or partly use of the object code of the software SmartSVN (hereinafter called \"SOFTWARE\") to contractors, juristic persons under public law or official fund assets in terms of &sect;310 in conjunction with &sect;14 BGB [Civil Code] (hereinafter called \"licensee\"). Herewith the inclusion of the licensee's own terms and conditions is contradicted, unless their validity has explicitly been agreed to.<br /><br />2 Scope of the Rights of Use<br /><br />2.1 The following terms are valid for the assignment and use of the SOFTWARE for an unlimited period of time including any documentation and the license file (a file that is custom-made for each individual granting of a license, the file being necessary for the operation of the SOFTWARE).<br /><br />2.2 They are not valid for additional services such as installation, integration, parameterization and customization of the SOFTWARE to the licensee's requirements.</p>";

        boolean result = ProductLicenseValidator.equalsContent(oldLicenseText,
                newLicenseText);
        Assert.assertTrue("Content has to be the same.", result);
    }

    @Test
    public void testValidateOldLicenseNull() throws Exception {
        String newLicense = "newLicense";

        ProductLicenseValidator.validate(new TechnicalProduct(), null,
                newLicense);
    }

    @Test
    public void testValidateNewLicenseNull() throws Exception {
        String oldLicense = "oldLicense";

        ProductLicenseValidator.validate(new TechnicalProduct(), oldLicense,
                null);
    }

}
