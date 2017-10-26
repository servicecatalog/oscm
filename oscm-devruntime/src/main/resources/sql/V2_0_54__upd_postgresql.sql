CREATE TABLE "organizationreftopaymenttype_temp" (
    "tkey" serial,
    "organizationreference_tkey" BIGINT NOT NULL
    );

    
INSERT INTO "organizationreftopaymenttype_temp" ("organizationreference_tkey")
  SELECT "tkey"
  FROM "organizationreference" AS ref
  WHERE "referencetype" = 'PLATFORM_OPERATOR_TO_SUPPLIER'
    AND NOT EXISTS ( select reftopay.tkey from organizationreftopaymenttype AS reftopay where ref.tkey = reftopay.organizationreference_tkey);

    
INSERT INTO "organizationreftopaymenttype" ("tkey", "usedasdefault", "version", "organizationreference_tkey", "organizationrole_tkey", "paymenttype_tkey", "usedasservicedefault") 
  SELECT ("tkey" + (SELECT COALESCE((SELECT max("tkey") FROM "organizationreftopaymenttype"), 1))),
          false,
          0,
          "organizationreference_tkey",
          1,
          3,
          false
  FROM "organizationreftopaymenttype_temp";


DROP TABLE "organizationreftopaymenttype_temp";

UPDATE "hibernate_sequences" SET "sequence_next_hi_value" = (select COALESCE((MAX("tkey")/1000),0)+10 FROM "organizationreftopaymenttype") WHERE "sequence_name" = 'OrganizationRefToPaymentType';
