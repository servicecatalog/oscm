-----------------------------------------------------
-- Schema and data migration for On-Behalf-Of feature
-----------------------------------------------------
ALTER TABLE technicalproduct ADD COLUMN "allowingonbehalfacting" BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE technicalproducthistory ADD COLUMN "allowingonbehalfacting" BOOLEAN NOT NULL DEFAULT FALSE;

-----------------------------------------------------
-- create tables: OnBehalfUserReference, OnBehalfUserReferenceHistory
-----------------------------------------------------
CREATE TABLE "onbehalfuserreference" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"lastaccesstime" BIGINT NOT NULL,
        "masteruser_tkey" BIGINT NOT NULL,
        "slaveuser_tkey" BIGINT NOT NULL
);
CREATE TABLE "onbehalfuserreferencehistory" (
		"tkey" BIGINT NOT NULL,
        "objversion" BIGINT NOT NULL,
        "objkey" BIGINT NOT NULL,
        "invocationdate" TIMESTAMP NOT NULL,
        "moddate" TIMESTAMP NOT NULL,
        "modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"lastaccesstime" BIGINT NOT NULL,
        "masteruserobjkey" BIGINT NOT NULL,
        "slaveuserobjkey" BIGINT NOT NULL
);

---------------------
-- primary keys
---------------------

ALTER TABLE "onbehalfuserreference" ADD CONSTRAINT "onbehalfuserreference_pk" PRIMARY KEY ("tkey");
ALTER TABLE "onbehalfuserreferencehistory" ADD CONSTRAINT "onbehalfuserreferencehistory_pk" PRIMARY KEY ("tkey");

---------------------
-- foreign keys
---------------------

ALTER TABLE "onbehalfuserreference" ADD CONSTRAINT "onbehalfuserreference_masteruser_fk" FOREIGN KEY ("masteruser_tkey")
	REFERENCES "platformuser" ("tkey");
ALTER TABLE "onbehalfuserreference" ADD CONSTRAINT "onbehalfuserreference_slaveuser_fk" FOREIGN KEY ("slaveuser_tkey")
	REFERENCES "platformuser" ("tkey");
	
---------------------
-- indexes
---------------------
	
CREATE UNIQUE INDEX "onbehalfuserreference_usrs_uidx" ON "onbehalfuserreference" ("masteruser_tkey" ASC, "slaveuser_tkey" ASC);

---------------------
-- hibernate
---------------------
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('OnBehalfUserReference', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('OnBehalfUserReferenceHistory', 10);