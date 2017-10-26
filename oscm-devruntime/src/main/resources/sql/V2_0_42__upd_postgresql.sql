-------------------------------------------------------------------
-- REQ 2099 Multiple marketplaces, user role "marketplace owner"
-------------------------------------------------------------------
INSERT INTO "userrole" ("tkey", "rolename", "version") VALUES (5, 'MARKETPLACE_OWNER', 0);

-- Assign the new mpl owner role to the platform admin by default   
INSERT INTO "roleassignment" ( "tkey", "version", "user_tkey", "userrole_tkey" )
  	VALUES ((SELECT COALESCE(MAX(tkey), 0) + 1 FROM "roleassignment"), 0, 1000, 5);	
INSERT INTO "roleassignmenthistory" ("tkey", "moddate", "modtype", "moduser", "objkey", "objversion", "userobjkey", "userroleobjkey" ) 
	VALUES ((SELECT COALESCE(MAX(tkey), 0) + 1 FROM "roleassignmenthistory"), now(), 'ADD', 'ANONYMOUS', 1, 0, 1000, 3);
