CREATE TABLE "operationrecord" (
    "tkey" BIGINT NOT NULL,
    "version" INTEGER NOT NULL,
    "transactionid" VARCHAR(255) NOT NULL, 
    "executiondate" BIGINT NOT NULL,
    "status" VARCHAR(255) NOT NULL,
    "technicalproductoperation_tkey" BIGINT NOT NULL,
    "subscription_tkey" BIGINT NOT NULL,
    "user_tkey" BIGINT NOT NULL
);

---------------------
-- primary keys
---------------------
ALTER TABLE "operationrecord" ADD CONSTRAINT "operationrecord_pk" PRIMARY KEY ("tkey");

---------------------
-- foreign keys
---------------------
ALTER TABLE "operationrecord" ADD CONSTRAINT "operationrecord_technicalproductoperation_fk" FOREIGN KEY ("technicalproductoperation_tkey") REFERENCES "technicalproductoperation"("tkey");
ALTER TABLE "operationrecord" ADD CONSTRAINT "operationrecord_subscription_fk" FOREIGN KEY ("subscription_tkey") REFERENCES "subscription" ("tkey");
ALTER TABLE "operationrecord" ADD CONSTRAINT "operationrecord_user_fk" FOREIGN KEY ("user_tkey") REFERENCES "platformuser" ("tkey");

------------------
-- Hibernate sequence
------------------
insert into hibernate_sequences ("sequence_name", "sequence_next_hi_value") select 'OperationRecord', COALESCE((MAX(tkey)/1000),0)+10 from operationrecord;

---------------------
-- indexes
---------------------
CREATE INDEX "operationrecord_subscription_nuidx" ON "operationrecord" ("subscription_tkey");
CREATE INDEX "operationrecord_user_nuidx" ON "operationrecord" ("user_tkey");
CREATE INDEX "operationrecord_transactionid_nuidx" ON "operationrecord" ("transactionid");
CREATE INDEX "operationrecord_usersubscription_idx" ON "operationrecord" ("user_tkey" asc, "subscription_tkey" asc);