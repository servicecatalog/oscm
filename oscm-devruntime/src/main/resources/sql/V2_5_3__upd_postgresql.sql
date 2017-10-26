------------------------------------------------------------
-- modifications for service operation parameters
------------------------------------------------------------

-- new parameter table
CREATE TABLE "operationparameter" (
	"tkey" BIGINT NOT NULL,
	"version" INTEGER NOT NULL,
	"technicalproductoperation_tkey" BIGINT NOT NULL, 
	"id" VARCHAR(255) NOT NULL,
	"type" VARCHAR(255) NOT NULL,
	"mandatory" BOOLEAN
);

ALTER TABLE "operationparameter" ADD CONSTRAINT "operationparameter_pk" PRIMARY KEY ("tkey");
ALTER TABLE "operationparameter" ADD CONSTRAINT "operationparameter_technicalproductoperation_fk" FOREIGN KEY ("technicalproductoperation_tkey") REFERENCES "technicalproductoperation"("tkey");
ALTER TABLE "operationparameter" ADD CONSTRAINT "operationparameter_uc" UNIQUE ("technicalproductoperation_tkey", "id");

-- new parameter history table
CREATE TABLE "operationparameterhistory" (
	"tkey" BIGINT NOT NULL,
	"moddate" TIMESTAMP NOT NULL,
	"invocationdate" TIMESTAMP NOT NULL,
	"modtype" VARCHAR(255) NOT NULL,
	"moduser" VARCHAR(255) NOT NULL,
	"objkey" BIGINT NOT NULL,
	"objversion" BIGINT NOT NULL,
	"technicalproductoperationobjkey" BIGINT NOT NULL, 
	"id" VARCHAR(255) NOT NULL,
	"type" VARCHAR(255) NOT NULL,
	"mandatory" BOOLEAN
);

ALTER TABLE "operationparameterhistory" ADD CONSTRAINT "operationparameterhistory_pk" PRIMARY KEY ("tkey");

-- update hibernate sequences for new tables
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('OperationParameter', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('OperationParameterHistory', 10);
