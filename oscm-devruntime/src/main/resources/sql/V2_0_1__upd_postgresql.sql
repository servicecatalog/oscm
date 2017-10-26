CREATE TABLE "vatrate" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"rate" NUMERIC(5, 2) NOT NULL,
		"owningorganization_tkey" BIGINT  NOT NULL,
		"targetcountry_tkey" BIGINT, 
		"targetorganization_tkey" BIGINT 
	)
;

CREATE TABLE "vatratehistory" (
		"tkey" BIGINT NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"rate" NUMERIC(5, 2) NOT NULL,
		"owningorganizationobjkey" BIGINT NOT NULL,
		"targetcountryobjkey" BIGINT, 
		"targetorganizationobjkey" BIGINT 
	)
;


---------------------
-- indexes
---------------------

CREATE INDEX "vatrate_orgkey_nuidx" ON "vatrate" ("owningorganization_tkey");

---------------------
-- primary keys
---------------------

ALTER TABLE "vatrate" ADD CONSTRAINT "vatrate_pk" PRIMARY KEY ("tkey");
ALTER TABLE "vatratehistory" ADD CONSTRAINT "vatratehistory_pk" PRIMARY KEY ("tkey");

---------------------
-- foreign keys
---------------------

ALTER TABLE "vatrate" ADD CONSTRAINT "vatrate_owningorganization_fk" FOREIGN KEY ("owningorganization_tkey")
	REFERENCES "organization" ("tkey");
ALTER TABLE "vatrate" ADD CONSTRAINT "vatrate_targetcountry_fk" FOREIGN KEY ("targetcountry_tkey")
	REFERENCES "organizationtocountry" ("tkey");
ALTER TABLE "vatrate" ADD CONSTRAINT "vatrate_targetganization_fk" FOREIGN KEY ("targetorganization_tkey")
	REFERENCES "organization" ("tkey");

---------------------
-- hibernate sequences
---------------------

INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('VatRate', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('VatRateHistory', 10);
