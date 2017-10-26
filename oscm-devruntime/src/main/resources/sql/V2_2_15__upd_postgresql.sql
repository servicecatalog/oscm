INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 15, 0, 'Broker_RevenueShare', 6);
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'de', 15, 'REPORT_DESC', 'Bericht über Umsatzbeteiligung der Vermittler');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'en', 15, 'REPORT_DESC', 'Broker revenue share report');

INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 16, 0, 'Reseller_RevenueShare', 7);
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'de', 16, 'REPORT_DESC', 'Bericht über Umsatzbeteiligung der Wiederverkäufer');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'en', 16, 'REPORT_DESC', 'Reseller revenue share report');

INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 17, 0, 'Partner_RevenueShare', 4);
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'de', 17, 'REPORT_DESC', 'Bericht über Umsatzbeteiligung der Vermittler und Wiederverkäufer');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'en', 17, 'REPORT_DESC', 'Broker/reseller revenue share report');

INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 18, 0, 'Supplier_RevenueShare', 1);
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'de', 18, 'REPORT_DESC', 'Bericht über Umsatzbeteiligung');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'en', 18, 'REPORT_DESC', 'Revenue share report');

INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 19, 0, 'Suppliers_RevenueShare', 4);
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'de', 19, 'REPORT_DESC', 'Bericht über Umsatzbeteiligung der Serviceanbieter');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'en', 19, 'REPORT_DESC', 'Supplier revenue share report');