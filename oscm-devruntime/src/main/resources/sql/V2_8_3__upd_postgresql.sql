-- Bug11450: Remove invalid rows in category table and categorytocatalogentry table
CREATE TABLE categorytocatalogentryhistorytemp (
  tkey SERIAL NOT NULL,
  moddate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  modtype VARCHAR(255) NOT NULL,
  moduser VARCHAR(255) NOT NULL,
  objkey BIGINT NOT NULL,
  objversion BIGINT NOT NULL,
  invocationdate TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT '1970-01-01 00:00:00'::timestamp without time zone,
  catalogentry_tkey BIGINT,
  category_tkey BIGINT
);

INSERT INTO categorytocatalogentryhistorytemp (moddate, modtype, moduser, objkey, objversion, invocationdate,catalogentry_tkey ,category_tkey)
	SELECT now(), 'DELETE', '1000', tkey, cgcl.version+1, now(), catalogentry_tkey, category_tkey 
	FROM categorytocatalogentry cgcl WHERE cgcl.category_tkey IN (select cg.tkey from category cg where cg.marketplacekey not in (SELECT tkey FROM marketplace)); 

INSERT INTO categorytocatalogentryhistory (tkey, moddate, modtype, moduser, objkey, objversion, invocationdate, catalogentry_tkey, category_tkey)
	SELECT "tkey" + (SELECT COALESCE(MAX(cgclh.tkey), 1) FROM "categorytocatalogentryhistory" cgclh), moddate, modtype, moduser, objkey, objversion, invocationdate, catalogentry_tkey, category_tkey
	FROM categorytocatalogentryhistorytemp;
	
DELETE FROM categorytocatalogentry cgcl WHERE cgcl.category_tkey IN (select cg.tkey from category cg where cg.marketplacekey not in (SELECT tkey FROM marketplace));
DROP TABLE categorytocatalogentryhistorytemp;

CREATE TABLE categoryhistorytemp (
  tkey SERIAL NOT NULL,
  moddate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  modtype VARCHAR(255) NOT NULL,
  moduser VARCHAR(255) NOT NULL,
  objkey BIGINT NOT NULL,
  objversion BIGINT NOT NULL,
  invocationdate TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT '1970-01-01 00:00:00'::timestamp without time zone,
  marketplaceobjkey BIGINT NOT NULL,
  categoryid VARCHAR(255) NOT NULL
);

INSERT INTO categoryhistorytemp (moddate, modtype, moduser, objkey, objversion, invocationdate,marketplaceobjkey ,categoryid)
	SELECT now(), 'DELETE', '1000', tkey, cg.version+1, now(), marketplacekey, categoryid 
	FROM category cg WHERE cg.marketplacekey NOT IN (SELECT tkey FROM marketplace); 

INSERT INTO categoryhistory (tkey, moddate, modtype, moduser, objkey, objversion, invocationdate, marketplaceobjkey, categoryid)
	SELECT "tkey" + (SELECT COALESCE(MAX(cgh.tkey), 1) FROM "categoryhistory" cgh), moddate, modtype, moduser, objkey, objversion, invocationdate, marketplaceobjkey, categoryid
	FROM categoryhistorytemp;
	
DELETE FROM category cg WHERE cg.marketplacekey NOT IN (SELECT tkey FROM marketplace);
DROP TABLE categoryhistorytemp;

