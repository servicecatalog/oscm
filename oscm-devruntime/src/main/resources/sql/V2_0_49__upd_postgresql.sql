------------------------------------------------------------------------------
-- RQ: FTS - Specify list of suppliers per technical service
------------------------------------------------------------------------------

-- create table marketing permission
CREATE TABLE "marketingpermission" (
	"tkey" BIGINT NOT NULL,
	"version" INTEGER NOT NULL,
	"technicalproduct_tkey" BIGINT NOT NULL,
	"organizationreference_tkey" BIGINT NOT NULL
);

-- primary key, foreign keys, index
ALTER TABLE "marketingpermission" ADD CONSTRAINT "marketingpermission_pk" PRIMARY KEY (tkey);
ALTER TABLE "marketingpermission" ADD CONSTRAINT "marketingpermission_technicalproduct_fk" 
	FOREIGN KEY ("technicalproduct_tkey") REFERENCES technicalproduct ("tkey");
ALTER TABLE "marketingpermission" ADD CONSTRAINT "marketingpermission_organizationreference_fk" 
	FOREIGN KEY ("organizationreference_tkey") REFERENCES "organizationreference" ("tkey");

CREATE UNIQUE INDEX "marketingpermission_uidx" 
	ON "marketingpermission" ("technicalproduct_tkey" asc, "organizationreference_tkey" asc);

	
	
------------------------------------------------------------------------------
-- HISTORY
------------------------------------------------------------------------------

-- create table
CREATE TABLE "marketingpermissionhistory" (
	"tkey" BIGINT NOT NULL,
	"objkey" BIGINT NOT NULL,
	"objversion" BIGINT NOT NULL,
	"technicalproductobjkey" BIGINT NOT NULL,
	"organizationreferenceobjkey" BIGINT NOT NULL,
	"invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00',
	"moddate" TIMESTAMP NOT NULL,
	"modtype" VARCHAR(255) NOT NULL,
	"moduser" VARCHAR(255) NOT NULL
);

-- primary key
ALTER TABLE "marketingpermissionhistory" ADD CONSTRAINT "marketingpermissionhistory_pk" PRIMARY KEY ("tkey");


------------------------------------------------------------------------------
-- Migration
------------------------------------------------------------------------------

CREATE TABLE "marketingpermissiontemp" (
	"tkey" serial NOT NULL,
	"version" INTEGER NOT NULL DEFAULT 0,
	"technicalproduct_tkey" BIGINT NOT NULL,
	"organizationreference_tkey" BIGINT NOT NULL
);

-- create entries
INSERT INTO "marketingpermissiontemp" ("technicalproduct_tkey", "organizationreference_tkey") SELECT tp.tkey, orgRef.tkey FROM "technicalproduct" tp, "organizationreference" orgRef WHERE tp.organizationkey = orgRef.sourcekey AND orgRef.referenceType = 'TECHNOLOGY_PROVIDER_TO_SUPPLIER' ORDER BY tp.organizationkey ASC, orgRef.targetkey ASC, tp.tkey ASC;

-- copy to real table
INSERT INTO "marketingpermission" ("tkey", "version", "technicalproduct_tkey", "organizationreference_tkey") SELECT "tkey", "version", "technicalproduct_tkey", "organizationreference_tkey" FROM "marketingpermissiontemp";

DROP TABLE "marketingpermissiontemp";

-- fill history data
INSERT INTO "marketingpermissionhistory" ("tkey", "moduser", "modtype", "moddate", "invocationdate", "objkey", "objversion", "technicalproductobjkey", "organizationreferenceobjkey") SELECT "tkey", '1000', 'ADD', now(), now(), "tkey", "version", "technicalproduct_tkey", "organizationreference_tkey" FROM "marketingpermission";

------------------------------------------------------------------------------
-- Organization Reference cleanup
------------------------------------------------------------------------------

-- create temp table
CREATE TABLE "orgrefhisttemp" (
  tkey serial NOT NULL,
  moddate timestamp without time zone NOT NULL,
  modtype character varying(255) NOT NULL default 'DELETE',
  moduser character varying(255) NOT NULL default '1000',
  objkey bigint NOT NULL,
  objversion bigint NOT NULL,
  targetobjkey bigint NOT NULL,
  sourceobjkey bigint NOT NULL,
  invocationdate timestamp without time zone NOT NULL DEFAULT '1970-01-01 00:00:00'::timestamp without time zone,
  referencetype character varying(255) NOT NULL DEFAULT 'TECHNOLOGY_PROVIDER_TO_SUPPLIER'::character varying
);

-- determine required org ref history entries for reference deletion
INSERT INTO "orgrefhisttemp" ("moddate", "objkey", "objversion", "targetobjkey", "sourceobjkey", "invocationdate", "referencetype")
	SELECT now(), orgref.tkey, orgref.version+1, orgref.targetkey, orgref.sourcekey, now(), orgref.referencetype FROM "organizationreference" orgref
	WHERE referencetype = 'TECHNOLOGY_PROVIDER_TO_SUPPLIER' AND NOT EXISTS (SELECT mp.tkey FROM "marketingpermission" mp WHERE mp.organizationreference_tkey = orgRef.tkey);

	
-- copy entries to real history table
INSERT INTO "organizationreferencehistory" ("tkey", "moduser", "modtype", "moddate", "invocationdate", "objkey", "objversion", "targetobjkey", "sourceobjkey", "referencetype") 
    SELECT "tkey" + (SELECT COALESCE(MAX(orh.tkey), 1) FROM "organizationreferencehistory" orh), "moduser", "modtype", "moddate", "invocationdate", "objkey", "objversion", "targetobjkey", "sourceobjkey", "referencetype" 
    FROM "orgrefhisttemp";
    
-- remove temp table
DROP TABLE "orgrefhisttemp";

-- adapt hibernate sequences table for organization reference changes
UPDATE "hibernate_sequences" SET "sequence_next_hi_value" = (SELECT COALESCE((MAX(tkey)/1000),0)+10 FROM "organizationreferencehistory") WHERE "sequence_name" = 'OrganizationReferenceHistory';

-- finally remove all non-referenced organization references
DELETE FROM "organizationreference" orgRef WHERE referencetype = 'TECHNOLOGY_PROVIDER_TO_SUPPLIER' AND NOT EXISTS (SELECT mp.tkey FROM "marketingpermission" mp WHERE mp.organizationreference_tkey = orgRef.tkey);

-- remove all trigger entries that refer to obsolete trigger types ADD_SUPPLIER_TO_TECHNOLOGY_PROVIDER and REMOVE_SUPPLIER_FROM_TECHNOLOGY_PROVIDER
-- determine the trigger process parameters that refer to such a trigger type and remove them
DELETE FROM triggerprocessparameter tpp WHERE tpp.triggerprocess_tkey IN 
	(SELECT tp.tkey FROM triggerprocess tp WHERE tp.triggerdefinition_tkey IN 
		(SELECT tkey FROM triggerdefinition WHERE "type" IN ('ADD_SUPPLIER_TO_TECHNOLOGY_PROVIDER', 'REMOVE_SUPPLIER_FROM_TECHNOLOGY_PROVIDER')));

-- determine the trigger processes that refer to such a trigger type and remove them and also create corresponding history entries
CREATE TABLE triggerprocesshistorytemp (
  tkey SERIAL NOT NULL,
  activationdate BIGINT NOT NULL,
  status VARCHAR(255) NOT NULL,
  moddate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  modtype VARCHAR(255) NOT NULL,
  moduser VARCHAR(255) NOT NULL,
  objkey BIGINT NOT NULL,
  objversion BIGINT NOT NULL,
  triggerdefinitionobjkey BIGINT NOT NULL,
  userobjkey BIGINT,
  invocationdate TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

INSERT INTO triggerprocesshistorytemp (activationdate, status, moddate, modtype, moduser, objkey, objversion, triggerdefinitionobjkey, userobjkey, invocationdate)
	SELECT activationdate, status, now(), 'DELETE', '1000', tkey, version, triggerdefinition_tkey, user_tkey, now() 
	FROM triggerprocess tp WHERE tp.triggerdefinition_tkey IN (SELECT tkey FROM triggerdefinition WHERE "type" IN ('ADD_SUPPLIER_TO_TECHNOLOGY_PROVIDER', 'REMOVE_SUPPLIER_FROM_TECHNOLOGY_PROVIDER')); 

INSERT INTO triggerprocesshistory (tkey, activationdate, status, moddate, modtype, moduser, objkey, objversion, triggerdefinitionobjkey, userobjkey, invocationdate)
	SELECT "tkey" + (SELECT COALESCE(MAX(tp1.tkey), 1) FROM "triggerprocesshistory" tp1), activationdate, status, moddate, modtype, moduser, objkey, objversion, triggerdefinitionobjkey, userobjkey, invocationdate
	FROM triggerprocesshistorytemp;
	
DELETE FROM triggerprocess tp WHERE tp.triggerdefinition_tkey IN (SELECT tkey FROM triggerdefinition WHERE "type" IN ('ADD_SUPPLIER_TO_TECHNOLOGY_PROVIDER', 'REMOVE_SUPPLIER_FROM_TECHNOLOGY_PROVIDER'));

DROP TABLE triggerprocesshistorytemp;

-- determine the trigger definitions that refer to such a trigger type and remove them and also create corresponding history entries
CREATE TABLE triggerdefinitionhistorytemp (
  tkey serial NOT NULL,
  suspendprocess BOOLEAN NOT NULL,
  target VARCHAR(255),
  targettype VARCHAR(255) NOT NULL,
  "type" VARCHAR(255) NOT NULL,
  moddate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  modtype VARCHAR(255) NOT NULL,
  moduser VARCHAR(255) NOT NULL,
  objkey BIGINT NOT NULL,
  objversion BIGINT NOT NULL,
  organizationobjkey BIGINT NOT NULL,
  invocationdate TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

INSERT INTO triggerdefinitionhistorytemp (suspendprocess, target, targettype, "type", moddate, modtype, moduser, objkey, objversion, organizationobjkey, invocationdate) 
	SELECT suspendprocess, target, targettype, "type", now(), 'DELETE', '1000', tkey, version, organization_tkey, now() FROM triggerdefinition 
	WHERE "type" IN ('ADD_SUPPLIER_TO_TECHNOLOGY_PROVIDER', 'REMOVE_SUPPLIER_FROM_TECHNOLOGY_PROVIDER');
	
INSERT INTO triggerdefinitionhistory (tkey, suspendprocess, target, targettype, "type", moddate, modtype, moduser, objkey, objversion, organizationobjkey, invocationdate)
	SELECT "tkey" + (SELECT COALESCE(MAX(tdh.tkey), 1) FROM "triggerdefinitionhistory" tdh), suspendprocess, target, targettype, "type", moddate, modtype, moduser, objkey, objversion, organizationobjkey, invocationdate
	FROM triggerdefinitionhistorytemp;
	
DELETE FROM triggerdefinition WHERE "type" IN ('ADD_SUPPLIER_TO_TECHNOLOGY_PROVIDER', 'REMOVE_SUPPLIER_FROM_TECHNOLOGY_PROVIDER');
	
DROP TABLE triggerdefinitionhistorytemp;

------------------------------------------------------------------------------
-- HIBERNATE sequence
------------------------------------------------------------------------------
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") SELECT 'MarketingPermission', COALESCE((MAX(tkey)/1000),0)+10 FROM "marketingpermission";
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") SELECT 'MarketingPermissionHistory', COALESCE((MAX(tkey)/1000),0)+10 FROM "marketingpermissionhistory";
UPDATE "hibernate_sequences" SET "sequence_next_hi_value" = (SELECT COALESCE((MAX(tkey)/1000),0)+10 FROM "triggerdefinitionhistory") WHERE "sequence_name" = 'TriggerDefinitionHistory';
UPDATE "hibernate_sequences" SET "sequence_next_hi_value" = (SELECT COALESCE((MAX(tkey)/1000),0)+10 FROM "triggerprocesshistory") WHERE "sequence_name" = 'TriggerProcessHistory';