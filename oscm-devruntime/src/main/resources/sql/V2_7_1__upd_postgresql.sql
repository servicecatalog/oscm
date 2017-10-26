CREATE TABLE "usergroup" (
    "tkey" BIGINT NOT NULL,
    "version" INTEGER NOT NULL,
    "name" VARCHAR(256) NOT NULL,
    "description" VARCHAR(255),
    "isdefault" BOOLEAN NOT NULL,
    "organization_tkey" BIGINT NOT NULL
);

---------------------
-- primary keys
---------------------

ALTER TABLE "usergroup" ADD CONSTRAINT "usergroup_pk" PRIMARY KEY ("tkey");
ALTER TABLE "usergroup" ADD CONSTRAINT "usergroup_organization_fk" FOREIGN KEY ("organization_tkey") REFERENCES "organization" ("tkey");

----------------------------------------------------------------------------
-- add relation to already existing products
----------------------------------------------------------------------------
CREATE TABLE usergrouptemp (
	tkey serial,
	version INTEGER DEFAULT 0,
	name VARCHAR(256) NOT NULL,
    isdefault BOOLEAN DEFAULT TRUE,
	organization_tkey BIGINT NOT NULL
);
	
INSERT INTO usergrouptemp(name, organization_tkey)
SELECT 'default', org.tkey FROM "organization" org WHERE org.tkey <> 0;

INSERT INTO usergroup(tkey, version, name, isdefault, organization_tkey) 
SELECT tkey, version, name, isdefault, organization_tkey FROM usergrouptemp;


------------------
-- Hibernate sequence
------------------

insert into hibernate_sequences ("sequence_name", "sequence_next_hi_value") select 'UserGroup', COALESCE((MAX(tkey)/1000),0)+10 from usergroup;




CREATE TABLE "usergrouptouser" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"usergroup_tkey" BIGINT,
		"platformuser_tkey" BIGINT 
	)
;



---------------------
-- indexes
---------------------

CREATE UNIQUE INDEX "usergrouptouser_groupuser_nuidx" ON "usergrouptouser" ("usergroup_tkey" asc, "platformuser_tkey" asc);

---------------------
-- primary keys
---------------------

ALTER TABLE "usergrouptouser" ADD CONSTRAINT "usergrouptouser_pk" PRIMARY KEY ("tkey");


---------------------
-- foreign keys
---------------------

ALTER TABLE "usergrouptouser" ADD CONSTRAINT "usergrouptouser_group_fk" FOREIGN KEY ("usergroup_tkey")
	REFERENCES "usergroup" ("tkey");
ALTER TABLE "usergrouptouser" ADD CONSTRAINT "usergrouptouser_platformuser_fk" FOREIGN KEY ("platformuser_tkey")
	REFERENCES "platformuser" ("tkey");
	

------------------
-- Hibernate sequence
------------------

insert into hibernate_sequences ("sequence_name", "sequence_next_hi_value") select 'UserGroupToUser', COALESCE((MAX(tkey)/1000),0)+10 from usergrouptouser;



	
----------------------------------------------------------------------------
-- drop temporary tables
----------------------------------------------------------------------------
DROP TABLE usergrouptemp;



CREATE TABLE "usergrouptoinvisibleproduct" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"product_tkey" BIGINT,
		"usergroup_tkey" BIGINT 
	)
;



---------------------
-- indexes
---------------------

CREATE UNIQUE INDEX "usergrouptoinvisibleproduct_productgroup_nuidx" ON "usergrouptoinvisibleproduct" ("product_tkey" asc, "usergroup_tkey" asc);

---------------------
-- primary keys
---------------------

ALTER TABLE "usergrouptoinvisibleproduct" ADD CONSTRAINT "usergrouptoinvisibleproduct_pk" PRIMARY KEY ("tkey");


---------------------
-- foreign keys
---------------------

ALTER TABLE "usergrouptoinvisibleproduct" ADD CONSTRAINT "usergrouptoinvisibleproduct_product_fk" FOREIGN KEY ("product_tkey")
	REFERENCES "product" ("tkey");
ALTER TABLE "usergrouptoinvisibleproduct" ADD CONSTRAINT "usergrouptoinvisibleproduct_usergroup_fk" FOREIGN KEY ("usergroup_tkey")
	REFERENCES "usergroup" ("tkey");

------------------
-- Hibernate sequence
------------------

insert into hibernate_sequences ("sequence_name", "sequence_next_hi_value") select 'UserGroupToInvisibleProduct', COALESCE((MAX(tkey)/1000),0)+10 from usergrouptoinvisibleproduct;



