CREATE TABLE "modifieduda" (
    "tkey" BIGINT NOT NULL,
    "version" INTEGER NOT NULL,
    "targetobjectkey" BIGINT NOT NULL,
    "targetobjecttype" VARCHAR(255) NOT NULL,
    "subscriptionkey" BIGINT NOT NULL,
    "value" VARCHAR(255)
);

ALTER TABLE "modifieduda" ADD CONSTRAINT "modifieduda_pk" PRIMARY KEY ("tkey");
ALTER TABLE "modifieduda" ADD CONSTRAINT "modifieduda_uc" UNIQUE ("targetobjectkey", "targetobjecttype", "subscriptionkey");

CREATE TABLE "modifiedudahistory" (
    "tkey" BIGINT NOT NULL,
    "moddate" TIMESTAMP NOT NULL,
    "modtype" VARCHAR(255) NOT NULL,
    "moduser" VARCHAR(255) NOT NULL,
    "objkey" BIGINT NOT NULL,
    "objversion" BIGINT NOT NULL,
    "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00',
    "targetobjectkey" BIGINT NOT NULL,
    "targetobjecttype" VARCHAR(255) NOT NULL,
    "subscriptionkey" BIGINT NOT NULL,
    "value" VARCHAR(255)
);

ALTER TABLE "modifiedudahistory" ADD CONSTRAINT "modifiedudahistory_pk" PRIMARY KEY ("tkey");

insert into "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") select 'ModifiedUda', COALESCE((MAX(tkey)/1000),0)+10 from modifieduda;
insert into "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") select 'ModifiedUdaHistory', COALESCE((MAX(tkey)/1000),0)+10 from modifiedudahistory;