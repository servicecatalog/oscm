-----------------------------------------------------
-- Add Customer Role to Platform Operator
-----------------------------------------------------
                  
INSERT INTO "organizationtorole" ("tkey", "version", "organizationrole_tkey", "organization_tkey" ) VALUES (2, 0, 3, 1);
INSERT INTO "organizationtorolehistory" ("tkey", "moddate", "modtype", "moduser", "objkey", "objversion", "organizationroletkey", "organizationtkey" ) VALUES (2, now(), 'ADD', 'ANONYMOUS', 2, 0, 3, 1);



