CREATE TABLE "organizationtorole_temp" (
    "tkey" serial,
    "organization_tkey" BIGINT NOT NULL
  );


INSERT INTO "organizationtorole_temp" ("organization_tkey")
  SELECT DISTINCT "organization_tkey"
  FROM "organizationtorole" AS orgrole WHERE ("organizationrole_tkey" = 1 or "organizationrole_tkey" = 2 or "organizationrole_tkey" = 4) and
  (select orgrole2.tkey from organizationtorole AS orgrole2 where
    orgrole2.organizationrole_tkey = 3 and orgrole2.organization_tkey = orgrole.organization_tkey) IS NULL ORDER BY "organization_tkey";


INSERT INTO "organizationtorole" ("tkey", "version", "organization_tkey", "organizationrole_tkey")
  SELECT ("tkey" + (SELECT COALESCE((SELECT max("tkey") FROM "organizationtorole"), 1))),
          0,
          "organization_tkey",
          3
  FROM "organizationtorole_temp";


INSERT INTO "organizationtorolehistory" ("tkey", "moddate", "modtype", "moduser", "objkey", "objversion", "organizationtkey", "organizationroletkey") 
  SELECT ("tkey" + (SELECT COALESCE((SELECT max("tkey") FROM "organizationtorolehistory"), 1))), now(), 'ADD', '1000', "tkey", 0, "organization_tkey", 3
  FROM "organizationtorole" where "tkey" > (SELECT max("tkey") FROM "organizationtorole") - (SELECT count(*) FROM "organizationtorole_temp");


DROP TABLE "organizationtorole_temp";

UPDATE "hibernate_sequences" SET "sequence_next_hi_value" = (select COALESCE((MAX(tkey)/1000),0)+10 FROM "organizationtorole") where "sequence_name" = 'OrganizationToRole';
UPDATE "hibernate_sequences" SET "sequence_next_hi_value" = (select COALESCE((MAX(tkey)/1000),0)+10 FROM "organizationtorolehistory") where "sequence_name" = 'OrganizationToRoleHistory';
