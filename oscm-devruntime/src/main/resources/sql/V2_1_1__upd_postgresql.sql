----------------------------------------------------------
-- Starting new sql scripts for release 15.1 with this one
----------------------------------------------------------

ALTER TABLE roleassignment ADD CONSTRAINT roleassgnment_to_user_fk  FOREIGN KEY (user_tkey) 
REFERENCES platformuser (tkey) ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE roleassignment ADD CONSTRAINT roleassgnment_to_role_fk FOREIGN KEY (userrole_tkey) 
REFERENCES userrole (tkey) ON UPDATE NO ACTION ON DELETE NO ACTION;