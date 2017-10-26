-------------------------------------------------------------------------
-- Bug 11917: Remove user group reference from terminated subscription -- 
-------------------------------------------------------------------------
UPDATE "subscription" SET "usergroup_tkey" = NULL WHERE "status" = 'DEACTIVATED';
UPDATE "subscriptionhistory" SET "usergroupobjkey" = NULL WHERE "status" = 'DEACTIVATED';
