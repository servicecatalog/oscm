------------------------------------------------------------
-- Starting new sql scripts for release 15.3 with this one
------------------------------------------------------------
INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 20, 0, 'Supplier_BillingOfASupplier', 4);
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'en', 20, 'REPORT_DESC', 'Billing report (of a supplier)');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'de', 20, 'REPORT_DESC', 'Rechnungsbericht (eines Serviceanbieters)');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'ja', 20, 'REPORT_DESC', '請求書情報 (サービス提供部門)');

INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 21, 0, 'Supplier_BillingDetailsOfASupplier', 4);
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'en', 21, 'REPORT_DESC', 'Detailed billing report for an existing invoice of a supplier''s customer');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'de', 21, 'REPORT_DESC', 'Detaillierter Rechnungsbericht für eine existierende Rechnung von einem Kunden eines Serviceanbieters');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'ja', 21, 'REPORT_DESC', 'サービス利用部門の請求書明細 (サービス提供部門)');

INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 22, 0, 'Supplier_CustomerOfASupplier', 4);
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'en', 22, 'REPORT_DESC', 'Customer report (of a supplier)');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'de', 22, 'REPORT_DESC', 'Kundenbericht (eines Serviceanbieters)');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'ja', 22, 'REPORT_DESC', 'サービス利用部門情報 (サービス提供部門)');

INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 23, 0, 'Supplier_ProductOfASupplier', 4);
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'en', 23, 'REPORT_DESC', 'Service report (of a supplier)');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'de', 23, 'REPORT_DESC', 'Servicebericht (eines Serviceanbieters)');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'ja', 23, 'REPORT_DESC', 'サービス情報 (サービス提供部門)');