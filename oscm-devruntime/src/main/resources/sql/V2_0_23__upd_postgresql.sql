-----------------------------------------------------
-- Add Supplier Revenue Report to the list of reports
-----------------------------------------------------
INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 13, 0, 'Supplier_Revenue', 4);

INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('de', 13, 'REPORT_DESC', 'Anbieter-Einkommensbericht');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('en', 13, 'REPORT_DESC', 'Supplier revenue report');


ALTER TABLE "technicalproduct" ADD COLUMN "onlyonesubscriptionallowed" BOOLEAN DEFAULT FALSE;
ALTER TABLE "technicalproducthistory" ADD COLUMN "onlyonesubscriptionallowed" BOOLEAN DEFAULT FALSE; 

