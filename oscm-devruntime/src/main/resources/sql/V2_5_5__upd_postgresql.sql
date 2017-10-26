CREATE TABLE "modifiedentity" (
    "tkey" BIGINT NOT NULL,
    "version" INTEGER NOT NULL,
    "targetobjectkey" BIGINT NOT NULL,
    "targetobjecttype" VARCHAR(255) NOT NULL,
    "value" VARCHAR(255)
);

ALTER TABLE "modifiedentity" ADD CONSTRAINT "modifiedentity_pk" PRIMARY KEY ("tkey");
ALTER TABLE "modifiedentity" ADD CONSTRAINT "modifiedentity_objkey_type_uc" UNIQUE ("targetobjectkey", "targetobjecttype");

CREATE TABLE "modifiedentityhistory" (
    "tkey" BIGINT NOT NULL,
    "moddate" TIMESTAMP NOT NULL,
    "modtype" VARCHAR(255) NOT NULL,
    "moduser" VARCHAR(255) NOT NULL,
    "objkey" BIGINT NOT NULL,
    "objversion" BIGINT NOT NULL,
    "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00',
    "targetobjectkey" BIGINT NOT NULL,
    "targetobjecttype" VARCHAR(255) NOT NULL,
    "value" VARCHAR(255)
);

ALTER TABLE "modifiedentityhistory" ADD CONSTRAINT "modifiedentityhistory_pk" PRIMARY KEY ("tkey");

insert into "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") select 'ModifiedEntity', COALESCE((MAX(tkey)/1000),0)+10 from modifiedentity;
insert into "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") select 'ModifiedEntityHistory', COALESCE((MAX(tkey)/1000),0)+10 from modifiedentityhistory;