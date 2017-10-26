ALTER TABLE "organizationhistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "organizationhistory" SET "invocationdate" = "moddate";

ALTER TABLE "paymentinfohistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "paymentinfohistory" SET "invocationdate" = "moddate";

ALTER TABLE "platformuserhistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "platformuserhistory" SET "invocationdate" = "moddate";

ALTER TABLE "subscriptionhistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "subscriptionhistory" SET "invocationdate" = "moddate";

ALTER TABLE "usagelicensehistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "usagelicensehistory" SET "invocationdate" = "moddate";

ALTER TABLE "producthistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "producthistory" SET "invocationdate" = "moddate";

ALTER TABLE "pricemodelhistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "pricemodelhistory" SET "invocationdate" = "moddate";

ALTER TABLE "pricedeventhistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "pricedeventhistory" SET "invocationdate" = "moddate";

ALTER TABLE "eventhistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "eventhistory" SET "invocationdate" = "moddate";

ALTER TABLE "technicalproducthistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "technicalproducthistory" SET "invocationdate" = "moddate";

ALTER TABLE "productreferencehistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "productreferencehistory" SET "invocationdate" = "moddate";

ALTER TABLE "parametersethistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "parametersethistory" SET "invocationdate" = "moddate";

ALTER TABLE "parameterhistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "parameterhistory" SET "invocationdate" = "moddate";

ALTER TABLE "organizationreferencehistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "organizationreferencehistory" SET "invocationdate" = "moddate";

ALTER TABLE "shophistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "shophistory" SET "invocationdate" = "moddate";

ALTER TABLE "parameterdefinitionhistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "parameterdefinitionhistory" SET "invocationdate" = "moddate";

ALTER TABLE "parameteroptionhistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "parameteroptionhistory" SET "invocationdate" = "moddate";

ALTER TABLE "pricedparameterhistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "pricedparameterhistory" SET "invocationdate" = "moddate";

ALTER TABLE "pricedoptionhistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "pricedoptionhistory" SET "invocationdate" = "moddate";

ALTER TABLE "organizationtorolehistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "organizationtorolehistory" SET "invocationdate" = "moddate";

ALTER TABLE "paymentresulthistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "paymentresulthistory" SET "invocationdate" = "moddate";

ALTER TABLE "billingcontacthistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "billingcontacthistory" SET "invocationdate" = "moddate";

ALTER TABLE "discounthistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "discounthistory" SET "invocationdate" = "moddate";

ALTER TABLE "triggerdefinitionhistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "triggerdefinitionhistory" SET "invocationdate" = "moddate";

ALTER TABLE "triggerprocesshistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "triggerprocesshistory" SET "invocationdate" = "moddate";

ALTER TABLE "roledefinitionhistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "roledefinitionhistory" SET "invocationdate" = "moddate";

ALTER TABLE "pricedproductrolehistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "pricedproductrolehistory" SET "invocationdate" = "moddate";

ALTER TABLE "steppedpricehistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "steppedpricehistory" SET "invocationdate" = "moddate";

ALTER TABLE "technicalproductoperationhistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "technicalproductoperationhistory" SET "invocationdate" = "moddate";

ALTER TABLE "udadefinitionhistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "udadefinitionhistory" SET "invocationdate" = "moddate";

ALTER TABLE "udahistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "udahistory" SET "invocationdate" = "moddate";

