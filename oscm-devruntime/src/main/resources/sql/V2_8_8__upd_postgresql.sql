------------------------------------
-- Add reference id to user group -- 
------------------------------------
ALTER TABLE "usergroup" ADD COLUMN "referenceid" VARCHAR(255) DEFAULT NULL;

ALTER TABLE "usergrouphistory" ADD COLUMN "referenceid" VARCHAR(255) DEFAULT NULL;
