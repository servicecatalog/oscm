-----------------------------------------
-- Add usergroup_tkey to billingresult --
-----------------------------------------

ALTER TABLE billingresult ADD COLUMN usergroup_tkey BIGINT DEFAULT NULL;

ALTER TABLE billingresult ADD CONSTRAINT billingresult_to_usergroup_fk
FOREIGN KEY (usergroup_tkey) REFERENCES usergroup (tkey) ON DELETE SET NULL;