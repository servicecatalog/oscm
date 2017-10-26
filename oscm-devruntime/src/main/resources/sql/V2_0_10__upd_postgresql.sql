
------------------------------------------
-- Migrate OrganizationReference
------------------------------------------

-- Schema
ALTER TABLE "organizationreference" DROP CONSTRAINT "organizationreference_to_supplier_fk";
ALTER TABLE "organizationreference" DROP CONSTRAINT "organizationreference_to_tp_fk";	
ALTER TABLE "organizationreference" ADD COLUMN "referencetype" VARCHAR(255) NOT NULL DEFAULT 'TECHNOLOGY_PROVIDER_TO_SUPPLIER';
ALTER TABLE "organizationreference" RENAME COLUMN "supplierkey" TO "targetkey";
ALTER TABLE "organizationreference" RENAME COLUMN "technologyproviderkey" TO "sourcekey";
ALTER TABLE "organizationreference" ADD CONSTRAINT "organizationreference_to_source_fk" FOREIGN KEY ("sourcekey")
	REFERENCES "organization" ("tkey");
ALTER TABLE "organizationreference" ADD CONSTRAINT "organizationreference_to_target_fk" FOREIGN KEY ("targetkey")
	REFERENCES "organization" ("tkey");	

ALTER TABLE "organizationreferencehistory" ADD COLUMN "referencetype" VARCHAR(255) NOT NULL DEFAULT 'TECHNOLOGY_PROVIDER_TO_SUPPLIER';
ALTER TABLE "organizationreferencehistory" RENAME COLUMN "suppliertkey" TO "targetobjkey";
ALTER TABLE "organizationreferencehistory" RENAME COLUMN "technologyprovidertkey" TO "sourceobjkey";



-- Migration: create organizationreference for each supplier and customer
CREATE TABLE "organizationreference_temp" (
	"tkey" serial,
	"version" INTEGER NOT NULL,
	"targetkey" BIGINT NOT NULL,
	"sourcekey" BIGINT NOT NULL,
	"referencetype" VARCHAR(255) NOT NULL DEFAULT 'TECHNOLOGY_PROVIDER_TO_SUPPLIER'
);	

INSERT INTO organizationreference_temp (   version,  targetkey, sourcekey, referencetype )
  	SELECT  '0' AS version, org.tkey AS targetkey, '1' AS sourcekey, 'PLATFORM_OPERATOR_TO_SUPPLIER' AS referencetype 
  	FROM "organization" AS org, "organizationtorole" AS orgtorole, "organizationrole"  AS orgrole 
  	WHERE org.tkey=orgtorole.organization_tkey AND orgrole.tkey=orgtorole.organizationrole_tkey AND orgrole.rolename='SUPPLIER';
INSERT INTO organizationreference_temp (   version,  targetkey, sourcekey, referencetype )
  	SELECT  '0' AS version, org.tkey AS targetkey, org.supplierkey AS sourcekey, 'SUPPLIER_TO_CUSTOMER' AS referencetype 
  	FROM "organization" AS org, "organizationtorole" AS orgtorole, "organizationrole"  AS orgrole 
  	WHERE org.tkey=orgtorole.organization_tkey AND orgrole.tkey=orgtorole.organizationrole_tkey AND orgrole.rolename='CUSTOMER';


INSERT INTO organizationreference (tkey,  version,  targetkey, sourcekey, referencetype )
  SELECT (temp.tkey + (SELECT COALESCE((SELECT max(tkey) FROM organizationreference),0))),
          temp.version,
          temp.targetkey,
          temp.sourcekey,
          temp.referencetype 
  FROM organizationreference_temp AS temp;
  
INSERT INTO organizationreferencehistory ("tkey", "moddate", "modtype", "moduser", "objkey", "objversion", "targetobjkey", "sourceobjkey", "referencetype" ) 
  SELECT tkey, now(), 'ADD', '1000', tkey, 0, targetkey, sourcekey, referencetype 
  FROM organizationreference WHERE "tkey" > (SELECT max("tkey") FROM "organizationreference") - (SELECT count(*) FROM "organizationreference_temp");;

CREATE UNIQUE INDEX "organizationreference_source_target_type_uidx" ON "organizationreference" ("sourcekey", "targetkey", "referencetype");

-- hibernate
UPDATE "hibernate_sequences" SET "sequence_next_hi_value" = ( 
	SELECT COALESCE((MAX(tkey)/1000),0)+10 FROM "organizationreference") WHERE "sequence_name" = 'OrganizationReference';
UPDATE "hibernate_sequences" SET "sequence_next_hi_value" = ( 
	SELECT COALESCE((MAX(tkey)/1000),0)+10 FROM "organizationreferencehistory") WHERE "sequence_name" = 'OrganizationReferenceHistory';	

-- Clean up	
DROP TABLE "organizationreference_temp";
DROP INDEX "organization_sup_nuidx";
ALTER TABLE "organization" DROP CONSTRAINT "cust_to_supplier_fk";
ALTER TABLE "organization" DROP COLUMN "supplierkey";
ALTER TABLE "organizationhistory" DROP COLUMN "supplierobjkey";