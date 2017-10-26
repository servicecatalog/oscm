
-----------------------------------------------------
-- Create new table for user roles
-----------------------------------------------------
CREATE TABLE "userrole" (
		"tkey" BIGINT NOT NULL,
		"rolename" VARCHAR(255) NOT NULL,
		"version" INTEGER NOT NULL
	);
	
ALTER TABLE "userrole" ADD CONSTRAINT "userrole_pk" PRIMARY KEY ("tkey");	
CREATE UNIQUE INDEX "userrole_role_uidx" ON "userrole" ("rolename");


CREATE TABLE "roleassignment" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"user_tkey" BIGINT NOT NULL,
		"userrole_tkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "roleassignmenthistory" (
		"tkey" BIGINT NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"userobjkey" BIGINT NOT NULL,
		"userroleobjkey" BIGINT NOT NULL,
		"invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00'
	);	

ALTER TABLE "roleassignment" ADD CONSTRAINT "roleassignment_pk" PRIMARY KEY ("tkey");	
CREATE UNIQUE INDEX "roleassignment_org_uidx" ON "roleassignment" ("user_tkey" asc, "userrole_tkey" asc);
CREATE INDEX "roleassignment_org_nuidx" ON "roleassignment" ("user_tkey");
CREATE INDEX "roleassignment_orgrole_nuidx" ON "roleassignment" ("userrole_tkey");
ALTER TABLE "roleassignmenthistory" ADD CONSTRAINT "roleassignmenthistory_pk" PRIMARY KEY ("tkey");

-- Create all roles
INSERT INTO "userrole" ("tkey", "version", "rolename" ) VALUES (1, 0, 'ORGANIZATION_ADMIN');
INSERT INTO "userrole" ("tkey", "version", "rolename" ) VALUES (2, 0, 'SERVICE_MANAGER');
INSERT INTO "userrole" ("tkey", "version", "rolename" ) VALUES (3, 0, 'TECHNOLOGY_MANAGER');
INSERT INTO "userrole" ("tkey", "version", "rolename" ) VALUES (4, 0, 'PLATFORM_OPERATOR');

-- Migration
CREATE TABLE "roleassignment_temp" (
	"tkey" serial,
	"user_tkey" BIGINT NOT NULL,
	"userrole_tkey" BIGINT NOT NULL
);	
-- migrate admin flag
INSERT INTO "roleassignment_temp" ( user_tkey, userrole_tkey )
  	SELECT u.tkey,'1' FROM platformuser u WHERE u.organizationadmin='true';
-- migrate supplier  	
INSERT INTO "roleassignment_temp" ( user_tkey, userrole_tkey  )
  	SELECT u.tkey,'2' FROM platformuser u, organization o, organizationtorole otr, organizationrole r 
  	WHERE r.rolename='SUPPLIER' AND otr.organizationrole_tkey=r.tkey AND otr.organization_tkey=o.tkey AND u.organizationkey=o.tkey;
-- migrate TECHNOLOGY_PROVIDER
INSERT INTO "roleassignment_temp" ( user_tkey, userrole_tkey  )
  	SELECT u.tkey,'3' FROM platformuser u, organization o, organizationtorole otr, organizationrole r 
  	WHERE r.rolename='TECHNOLOGY_PROVIDER' AND otr.organizationrole_tkey=r.tkey AND otr.organization_tkey=o.tkey AND u.organizationkey=o.tkey;  	
-- migrate PLATFORM_OPERATOR
INSERT INTO "roleassignment_temp" ( user_tkey, userrole_tkey  )
  	SELECT u.tkey,'4' FROM platformuser u, organization o, organizationtorole otr, organizationrole r 
  	WHERE r.rolename='PLATFORM_OPERATOR' AND otr.organizationrole_tkey=r.tkey AND otr.organization_tkey=o.tkey AND u.organizationkey=o.tkey;  	
  	
 
INSERT INTO "roleassignment" ( tkey, version, user_tkey, userrole_tkey )
  	SELECT t.tkey, 0, t.user_tkey, t.userrole_tkey FROM roleassignment_temp t;	
INSERT INTO "roleassignmenthistory" ("tkey", "moddate", "modtype", "moduser", "objkey", "objversion", "userobjkey", "userroleobjkey" ) 
	SELECT t.tkey, now(), 'ADD', 'ANONYMOUS', 1, 0, t.user_tkey, 1 FROM roleassignment_temp t;

	
-- Drop old data  	
DROP TABLE "roleassignment_temp"; 	
ALTER TABLE "platformuser" DROP COLUMN "organizationadmin";
ALTER TABLE "platformuserhistory" DROP COLUMN "organizationadmin";

-- Hibernate
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES ('UserRole', 10);
INSERT INTO hibernate_sequences ("sequence_name", "sequence_next_hi_value") 
	SELECT 'RoleAssignment', COALESCE((MAX(tkey)/1000),0)+10 FROM roleassignment;
INSERT INTO hibernate_sequences ("sequence_name", "sequence_next_hi_value") 
	SELECT 'RoleAssignmentHistory', COALESCE((MAX(tkey)/1000),0)+10 FROM roleassignmenthistory;

