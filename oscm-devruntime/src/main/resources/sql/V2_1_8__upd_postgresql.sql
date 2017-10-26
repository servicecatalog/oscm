CREATE TABLE "categorytocatalogentry" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"catalogentry_tkey" BIGINT,
		"category_tkey" BIGINT 	);

CREATE TABLE "categorytocatalogentryhistory" (
		"tkey" BIGINT NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00',
		"catalogentry_tkey" BIGINT,
		"category_tkey" BIGINT	);

-- primary keys
ALTER TABLE "categorytocatalogentry"        ADD CONSTRAINT "categorytocatalogentry_pk" PRIMARY KEY ("tkey");
ALTER TABLE "categorytocatalogentryhistory" ADD CONSTRAINT "categorytocatalogentryhistory_pk" PRIMARY KEY ("tkey");

 -- foreign keys
 ALTER TABLE "categorytocatalogentry" ADD CONSTRAINT "categorytocatalogentry_catalogentry_fk" FOREIGN KEY ("catalogentry_tkey")
	REFERENCES "catalogentry" ("tkey");
ALTER TABLE "categorytocatalogentry" ADD CONSTRAINT "categorytocatalogentry_category_fk" FOREIGN KEY ("category_tkey")
	REFERENCES "category" ("tkey");
 



	
