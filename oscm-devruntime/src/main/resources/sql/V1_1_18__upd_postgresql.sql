-- CREATE SCHEMA 

-- ----------------------------------------------
-- DDL-Anweisungen für Tabellen
-- ----------------------------------------------
CREATE TABLE "udadefinition" (
		"tkey" BIGINT NOT NULL,
		"defaultvalue" VARCHAR(255),
		"targettype" VARCHAR(255) NOT NULL,
		"udaid" VARCHAR(255) NOT NULL,
		"version" INTEGER NOT NULL,
		"organizationkey" BIGINT NOT NULL
	);
	
CREATE TABLE "udadefinitionhistory" (
		"tkey" BIGINT NOT NULL,
		"defaultvalue" VARCHAR(255),
		"targettype" VARCHAR(255) NOT NULL,
		"udaid" VARCHAR(255) NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"organizationobjkey" BIGINT NOT NULL
	);	

CREATE TABLE "uda" (
		"tkey" BIGINT NOT NULL,
		"udadefinitionkey" BIGINT NOT NULL,
		"targetobjectkey" BIGINT NOT NULL,
		"udavalue" VARCHAR(255),
		"version" INTEGER NOT NULL
	);
	
CREATE TABLE "udahistory" (
		"tkey" BIGINT NOT NULL,
		"targetobjectkey" BIGINT NOT NULL,
		"udavalue" VARCHAR(255),
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"udadefinitionobjkey" BIGINT NOT NULL
	);	

-- indexes

CREATE INDEX "udadefinition_orgkey_nuidx" ON "udadefinition" ("organizationkey");
CREATE UNIQUE INDEX "udadefinition_bk_idx" ON "udadefinition" ("organizationkey" asc, "udaid" asc, "targettype" asc);

CREATE INDEX "uda_udadefinitionkey_nuidx" ON "uda" ("udadefinitionkey");
CREATE UNIQUE INDEX "uda_bk_idx" ON "uda" ("udadefinitionkey" asc, "targetobjectkey" asc);

---------------------
-- primary keys
---------------------

ALTER TABLE "udadefinition" ADD CONSTRAINT "udadefinition_pk" PRIMARY KEY ("tkey");
ALTER TABLE "udadefinitionhistory" ADD CONSTRAINT "udadefinitionhistory_pk" PRIMARY KEY ("tkey");

ALTER TABLE "uda" ADD CONSTRAINT "uda_pk" PRIMARY KEY ("tkey");
ALTER TABLE "udahistory" ADD CONSTRAINT "udahistory_pk" PRIMARY KEY ("tkey");

---------------------
-- foreign keys
---------------------

ALTER TABLE "udadefinition" ADD CONSTRAINT "udadefinition_organization_fk" FOREIGN KEY ("organizationkey")
	REFERENCES "organization" ("tkey");
ALTER TABLE "uda" ADD CONSTRAINT "uda_udadefinition_fk" FOREIGN KEY ("udadefinitionkey")
	REFERENCES "udadefinition" ("tkey");


INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('UdaDefinition', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('UdaDefinitionHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('Uda', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('UdaHistory', 10);
