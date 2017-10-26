----------------------------------------------------------------------------
-- Migrate Vatrate
-- The relation vatrate_targetcountry_tkey points to a different table,
-- SupportedCountry, now.
--
-- Migrate VatrateHistory
-- Adapt data of targetcountryobjkey column 
----------------------------------------------------------------------------

-- remove foreign key constraint from vatrate_targetcountry
ALTER TABLE vatrate DROP CONSTRAINT vatrate_targetcountry_fk;

-- update supportedcountry_tkey column
ALTER TABLE vatrate ADD migration BIGINT;
UPDATE vatrate AS vr SET migration = (
	SELECT otc.supportedcountry_tkey FROM organizationtocountry AS otc, supportedcountry AS sc 
	WHERE vr.targetcountry_tkey=otc.tkey AND otc.supportedcountry_tkey=sc.tkey);
UPDATE vatrate SET targetcountry_tkey = migration;
ALTER TABLE vatrate DROP COLUMN migration;

-- add foreign key constraint to vatrate_targetcountry
ALTER TABLE vatrate ADD CONSTRAINT vatrate_targetcountry_fk FOREIGN KEY (targetcountry_tkey) REFERENCES supportedcountry(tkey);

-----------------------------------------------------------------------------
ALTER TABLE vatratehistory ADD migration BIGINT;
UPDATE vatratehistory AS vrh SET migration = (
	SELECT otch.supportedcountryobjkey FROM organizationtocountryhistory AS otch
	WHERE vrh.targetcountryobjkey=otch.objkey AND otch.moddate <= vrh.moddate ORDER BY otch.moddate DESC LIMIT 1);
UPDATE vatratehistory SET targetcountryobjkey = migration;
ALTER TABLE vatratehistory DROP COLUMN migration;