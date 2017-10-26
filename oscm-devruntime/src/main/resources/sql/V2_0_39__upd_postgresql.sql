-------------------------------------------------------------------
-- REQ 2098 Multiple marketplaces, organization role "marketplace owner"
-------------------------------------------------------------------
INSERT INTO "organizationrole" ("tkey", "version", "rolename") VALUES (5, 0, 'MARKETPLACE_OWNER');

-- Assign the new mpl owner role to the platform operator organization   
INSERT INTO "organizationtorole" ("tkey", "version", "organizationrole_tkey", "organization_tkey" ) VALUES (3, 0, 5, 1);
INSERT INTO "organizationtorolehistory" ("tkey", "moddate", "modtype", "moduser", "objkey", "objversion", "organizationroletkey", "organizationtkey" ) VALUES (3, now(), 'ADD', 'ANONYMOUS', 3, 0, 5, 1);

-- Remove the predifined marketplace descriptions  
DELETE FROM "localizedresource" WHERE objecttype = 'MARKETPLACE_DESCRIPTION';