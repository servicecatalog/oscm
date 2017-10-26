DELETE FROM "localizedresource" WHERE objecttype ='REPORT_DESC' and objectkey <24 and locale in ('en', 'de', 'ja');
DELETE FROM "localizedresource" WHERE objecttype ='PAYMENT_TYPE_NAME' and objectkey <4 and locale in ('en', 'de', 'ja');
DELETE FROM "localizedresource" WHERE objecttype ='PARAMETER_DEF_DESC' and objectkey <1003 and locale in ('en', 'de', 'ja');
DELETE FROM "localizedresource" WHERE objecttype ='EVENT_DESC' and objectkey <1002 and locale in ('en', 'de', 'ja');