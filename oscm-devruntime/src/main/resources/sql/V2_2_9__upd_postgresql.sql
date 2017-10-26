---------------------------------------------------------------
-- RQ: FTS Partner-Model - Billing
---------------------------------------------------------------
ALTER TABLE "billingresult" ADD COLUMN currency_tkey BIGINT;
ALTER TABLE "billingresult" ADD CONSTRAINT "billingresult_supportedcurrency_fk" FOREIGN KEY ("currency_tkey") REFERENCES "supportedcurrency" ("tkey");
ALTER TABLE "billingresult" ADD COLUMN grossamount NUMERIC(19,2);

CREATE TABLE "billingsharesresult" 
(
   "tkey" BIGINT NOT NULL,
   "periodstarttime" BIGINT, 
   "periodendtime" BIGINT, 
   "organizationtkey" BIGINT, 
   "resulttype" CHARACTER VARYING(255), 
   "resultxml" TEXT,
   "creationtime" BIGINT,
   "version" INTEGER NOT NULL,
   CONSTRAINT "billingsharesresult_pk" PRIMARY KEY ("tkey")
);

INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('BillingSharesResult', 10);