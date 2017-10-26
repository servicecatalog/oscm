--add a column which stores how ofter the userid of the specific row was used 
ALTER TABLE "platformuser" ADD COLUMN "useridcnt" BIGINT;

ALTER TABLE "platformuser" ADD COLUMN "olduserid" VARCHAR(255);
UPDATE platformuser AS pl SET olduserid = userid;

--fill the new column e.g.:
--row#|userid|useridcnt
--1|userA|1
--2|userB|1
--3|userB|2
--Since the ordering is done by tkey, it's ensured that the initial platformuser (tkey=1) will ever
--have a rowcount different from 1, which means it'll be never changed 
UPDATE platformuser p1 SET "useridcnt" = (
	SELECT COALESCE((SELECT count(*) FROM platformuser p2 WHERE p1.userid = p2.userid AND p2.tkey < p1.tkey) + 1, 1));
	
--change the userid by following the scheme:
--IF (!UNIQUE(user.userid)){
--	IF (!UNIQUE(user.email)){use "userid@orgid" as new userid}
--	ELSE {use email as new userid}	
--}
--The changes will be not applied on the first data record of non unique user ids 
UPDATE platformuser AS pl SET userid = (
	SELECT
	    CASE 
	        WHEN (
	                SELECT COUNT(userid)
	                FROM platformuser AS p2
	                WHERE p2.userid=p.userid
	                GROUP BY userid
	                HAVING ( COUNT(userid) > 1 )
	            ) > 1 AND p.useridcnt > 1
	        THEN (
	                CASE
	                	WHEN (
	                    	(
		                        SELECT COUNT(email)
		                        FROM platformuser AS p3
		                        WHERE p3.email=p.email
		                        GROUP BY email
		                        HAVING ( COUNT(email) > 1 )
	                        ) > 1
	                        OR (
                                SELECT COUNT(userid)
                                FROM platformuser AS p4
                                WHERE p4.userid=p.email
                                GROUP BY userid
                                HAVING ( COUNT(userid) > 0 )
                            ) > 0 
                        )
	                    THEN p.userid || '@' || (
	                        SELECT organizationid
	                        FROM organization AS o
	                        WHERE o.tkey = p.organizationkey
	                    )
	                    ELSE p.email
	                END
	            )
	        ELSE p.userid
	    END
	FROM platformuser AS p WHERE pl.tkey = p.tkey);

--updates the unique index (replaces old definition)
DROP INDEX platformuser_bk_idx;
CREATE UNIQUE INDEX "platformuser_bk_idx" ON "platformuser" ("userid" asc);

--Note: if there are problems setting the unique constraint because the table contains duplicate values after the migration
--uncomment the two lines below and comment out the UPDATE above.
--UPDATE platformuser AS p SET userid = (p.userid|| '@' || (SELECT organizationid FROM organization AS o WHERE o.tkey = p.organizationkey));
--UPDATE platformuser AS p SET useridcnt = 2;	
	
--increase version of the changed records
UPDATE platformuser AS pl SET version = (version+1) where useridcnt > 1;

--create a temp table, thhe tkey starts with 1 and will be auto incremented
CREATE TABLE "platformuserhistory_temp" (
		"tkey" SERIAL PRIMARY KEY,
		"additionalname" VARCHAR(255),
		"address" VARCHAR(255),
		"creationdate" BIGINT NOT NULL,
		"email" VARCHAR(255),
		"failedlogincounter" INTEGER NOT NULL,
		"firstname" VARCHAR(255),
		"lastname" VARCHAR(255),
		"locale" VARCHAR(255) NOT NULL,
		"organizationadmin" BOOLEAN NOT NULL,
		"phone" VARCHAR(255),
		"salutation" VARCHAR(255),
		"status" VARCHAR(255) NOT NULL,
		"passwordsalt" BIGINT NOT NULL DEFAULT 0,
		"passwordhash" bytea,
		"userid" VARCHAR(255) NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"organizationobjkey" BIGINT NOT NULL
	);

--create history entries in the temp table 
INSERT INTO platformuserhistory_temp ("additionalname","address","creationdate","email","failedlogincounter","firstname","lastname","locale","organizationadmin","phone","salutation","status","passwordsalt","passwordhash","userid","moddate","modtype","moduser","objkey","objversion","organizationobjkey") 
SELECT additionalname, address, creationdate, email, failedlogincounter, firstname, lastname, locale, organizationadmin, phone, salutation, status, passwordsalt, passwordhash, userid, now(),'MODIFY', 'ANONYMOUS', tkey, version, organizationkey FROM platformuser AS p WHERE useridcnt > 1;

--copy into the real table
INSERT INTO platformuserhistory ("tkey", "additionalname","address","creationdate","email","failedlogincounter","firstname","lastname","locale","organizationadmin","phone","salutation","status","passwordsalt","passwordhash","userid","moddate","modtype","moduser","objkey","objversion","organizationobjkey") 
SELECT (temp.tkey + (SELECT COALESCE((SELECT max(tkey) FROM platformuserhistory),0))),temp.additionalname, temp.address, temp.creationdate, temp.email, temp.failedlogincounter, temp.firstname, temp.lastname, temp.locale, temp.organizationadmin, temp.phone, temp.salutation, temp.status, temp.passwordsalt, temp.passwordhash, temp.userid, now(),'MODIFY', 'ANONYMOUS', temp.objkey, temp.objversion, temp.organizationobjkey FROM platformuserhistory_temp as temp;

DROP TABLE platformuserhistory_temp;

--Add columns which are reqired when using a third party realm
ALTER TABLE "platformuser" ADD COLUMN "realmuserid" VARCHAR(255);
UPDATE platformuser SET realmuserid = userid;
ALTER TABLE "platformuserhistory" ADD COLUMN "realmuserid" VARCHAR(255);
UPDATE platformuserhistory SET realmuserid = userid;
