-----------------------------------------------------
-- Drop technical product version
-----------------------------------------------------

-- migrate data
-- create temporary column holding the new concatenated name
ALTER TABLE technicalproduct ADD COLUMN "temp" VARCHAR(255);
UPDATE technicalproduct tp SET temp = tp.technicalproductid || ' ' || tp.technicalproductversion;

-- create temporary column holding the number of how often a name was already counted in the table
-- see also example upd_postgresql_02_00_22.sql
ALTER TABLE technicalproduct ADD COLUMN "tempcnt" BIGINT;
UPDATE technicalproduct tp1 SET tempcnt = (
	SELECT COALESCE((
		SELECT count(*) 
		FROM technicalproduct tp2 
		WHERE tp1.temp = tp2.temp AND tp2.tkey < tp1.tkey) + 1, 1));
	
-- update technicalproductid column
UPDATE technicalproduct AS tp SET technicalproductid = (
	SELECT
	    CASE 
	        WHEN (
	                SELECT COUNT(temp)
	                FROM technicalproduct AS tp2
	                WHERE tp2.temp=tp1.temp
	                GROUP BY temp
	                HAVING ( COUNT(temp) > 1 )
	            ) > 1 AND tp1.tempcnt > 1
	        THEN (tp1.temp || tp1.tempcnt)
	        ELSE tp1.temp
	    END
	FROM technicalproduct AS tp1 WHERE tp.tkey = tp1.tkey);

-- apply the same sql script as above to the history table!	
ALTER TABLE technicalproducthistory ADD COLUMN "temp" VARCHAR(255);
UPDATE technicalproducthistory tph SET temp = tph.technicalproductid || ' ' || tph.technicalproductversion;

ALTER TABLE technicalproducthistory ADD COLUMN "tempcnt" BIGINT;
UPDATE technicalproducthistory tph1 SET tempcnt = (
	SELECT COALESCE((
		SELECT count(*) 
		FROM technicalproducthistory tph2 
		WHERE tph1.temp = tph2.temp AND tph2.tkey < tph1.tkey) + 1, 1));
	
UPDATE technicalproducthistory AS tph SET technicalproductid = (
	SELECT
	    CASE 
	        WHEN (
	                SELECT COUNT(temp)
	                FROM technicalproducthistory AS tph2
	                WHERE tph2.temp=tph1.temp
	                GROUP BY temp
	                HAVING ( COUNT(temp) > 1 )
	            ) > 1 AND tph1.tempcnt > 1
	        THEN (tph1.temp || tph1.tempcnt)
	        ELSE tph1.temp
	    END
	FROM technicalproducthistory AS tph1 WHERE tph.tkey = tph1.tkey);	
	
-- drop columns
ALTER TABLE technicalproduct DROP COLUMN temp;
ALTER TABLE technicalproduct DROP COLUMN tempcnt;
ALTER TABLE technicalproduct DROP COLUMN technicalproductversion;
ALTER TABLE technicalproducthistory DROP COLUMN temp;
ALTER TABLE technicalproducthistory DROP COLUMN tempcnt;
ALTER TABLE technicalproducthistory DROP COLUMN technicalproductversion;