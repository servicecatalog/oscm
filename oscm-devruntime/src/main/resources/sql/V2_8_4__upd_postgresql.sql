CREATE TABLE "billingsubscriptionstatus" (
  "tkey" BIGINT NOT NULL,
  "subscriptionkey" BIGINT NOT NULL,
  "endoflastbilledperiod" BIGINT NOT NULL,
  "version" INTEGER NOT NULL
);

ALTER TABLE "billingsubscriptionstatus" ADD CONSTRAINT "billingsubscriptionstatus_pk" PRIMARY KEY ("tkey");

CREATE UNIQUE INDEX "billingsubstatus_subkey_uidx" ON "billingsubscriptionstatus" ("subscriptionkey");

CREATE TABLE "billingsubscriptionstatus_temp" (
  "tkey" serial,
  "subscriptionkey" BIGINT NOT NULL,
  "endoflastbilledperiod" BIGINT NOT NULL,
  "version" INTEGER NOT NULL
);

INSERT INTO "billingsubscriptionstatus_temp" ("subscriptionkey", "endoflastbilledperiod", "version")
	SELECT br.subscriptionkey, MAX(br.periodendtime), 0 from billingresult br GROUP BY br.subscriptionkey ORDER BY br.subscriptionkey ASC;
	
INSERT INTO "billingsubscriptionstatus" ("tkey", "subscriptionkey", "endoflastbilledperiod", "version")
  SELECT  temp.tkey, 
          temp.subscriptionkey,
          temp.endoflastbilledperiod,
          temp.version
  FROM "billingsubscriptionstatus_temp" AS temp;
   
DROP TABLE "billingsubscriptionstatus_temp";

INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('BillingSubscriptionStatus', (select COALESCE((MAX(case when tkey is null then 0 else tkey end)/1000),0)+10 from billingsubscriptionstatus));
