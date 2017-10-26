DELETE FROM localizedresource lr WHERE lr.objecttype IN ('EVENT_DESC') 
AND NOT EXISTS (SELECT t.tkey FROM event t WHERE t.tkey = lr.objectkey);

DELETE FROM localizedresource lr WHERE lr.objecttype IN ('MARKETPLACE_NAME', 'MARKETPLACE_STAGE', 'SHOP_MESSAGE_PROPERTIES') 
AND NOT EXISTS (SELECT t.tkey FROM marketplace t WHERE t.tkey = lr.objectkey);

DELETE FROM localizedresource lr WHERE lr.objecttype IN ('ORGANIZATION_DESCRIPTION') 
AND NOT EXISTS (SELECT t.tkey FROM organization t WHERE t.tkey = lr.objectkey);

DELETE FROM localizedresource lr WHERE lr.objecttype IN ('PARAMETER_DEF_DESC') 
AND NOT EXISTS (SELECT t.tkey FROM parameterdefinition t WHERE t.tkey = lr.objectkey);

DELETE FROM localizedresource lr WHERE lr.objecttype IN ('OPTION_PARAMETER_DEF_DESC')
AND NOT EXISTS (SELECT t.tkey FROM parameteroption t WHERE t.tkey = lr.objectkey);

DELETE FROM localizedresource lr WHERE lr.objecttype IN ('PAYMENT_TYPE_NAME')
AND NOT EXISTS (SELECT t.tkey FROM paymenttype t WHERE t.tkey = lr.objectkey);

DELETE FROM localizedresource lr WHERE lr.objecttype IN ('PRICEMODEL_DESCRIPTION', 'PRICEMODEL_SHORT_DESCRIPTION', 'PRICEMODEL_LICENSE') 
AND NOT EXISTS (SELECT t.tkey FROM pricemodel t WHERE t.tkey = lr.objectkey);

DELETE FROM localizedresource lr WHERE lr.objecttype IN ('PRODUCT_MARKETING_DESC', 'PRODUCT_MARKETING_NAME', 'PRODUCT_SHORT_DESCRIPTION') 
AND NOT EXISTS (SELECT t.tkey FROM product t WHERE t.tkey = lr.objectkey);

DELETE FROM localizedresource lr WHERE lr.objecttype IN ('REPORT_DESC') 
AND NOT EXISTS (SELECT t.tkey FROM report t WHERE t.tkey = lr.objectkey);

DELETE FROM localizedresource lr WHERE lr.objecttype IN ('ROLE_DEF_DESC', 'ROLE_DEF_NAME') 
AND NOT EXISTS (SELECT t.tkey FROM roledefinition t WHERE t.tkey = lr.objectkey);

DELETE FROM localizedresource lr WHERE lr.objecttype IN ('SUBSCRIPTION_PROVISIONING_PROGRESS') 
AND NOT EXISTS (SELECT t.tkey FROM subscription t WHERE t.tkey = lr.objectkey);

DELETE FROM localizedresource lr WHERE lr.objecttype IN ('TEC_PRODUCT_LOGIN_ACCESS_DESC', 'TEC_PRODUCT_TECHNICAL_DESC', 'PRODUCT_LICENSE_DESC')
AND NOT EXISTS (SELECT t.tkey FROM technicalproduct t WHERE t.tkey = lr.objectkey);

DELETE FROM localizedresource lr WHERE lr.objecttype IN ('TEC_PRODUCT_LOGIN_SUBS_USER_DESC', 'TEC_PRODUCT_LOGIN_PROV_DESC');

DELETE FROM localizedresource lr WHERE lr.objecttype IN ('TECHNICAL_PRODUCT_OPERATION_NAME', 'TECHNICAL_PRODUCT_OPERATION_DESCRIPTION')
AND NOT EXISTS (SELECT t.tkey FROM technicalproductoperation t WHERE t.tkey = lr.objectkey);

DELETE FROM localizedresource lr WHERE lr.objecttype IN ('TRIGGER_PROCESS_REASON') 
AND NOT EXISTS (SELECT t.tkey FROM triggerprocess t WHERE t.tkey = lr.objectkey);