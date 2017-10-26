----------------------------------------------------------------------------
-- Migrate Catalogentry
-- 1) Move publivservice column from product to catalogentry and rename 
-- it to anonymousvisible
-- 2) Add marketplace_tkey column, set correct value and add foreign key constaint
--
-- Migrate CatalogentryHistory
-- 1) Move publivservice column from producthistory to catalogentryhistory
-- and rename it to anonymousvisible
-- 2) Add marketplaceobjkey column
----------------------------------------------------------------------------

-- add new column anonymousvisible to catalogentry
ALTER TABLE catalogentry ADD COLUMN anonymousvisible BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE catalogentry AS ce SET anonymousvisible = (
	SELECT pr.publicservice FROM product AS pr
	WHERE ce.product_tkey=pr.tkey);
ALTER TABLE product DROP COLUMN publicservice;

-- add new column marketplace_tkey to catalogentry
ALTER TABLE catalogentry ADD COLUMN marketplace_tkey BIGINT;
UPDATE catalogentry AS ce SET marketplace_tkey = (
	SELECT org.localmarketplace_tkey FROM organization AS org 
	WHERE ce.organization_tkey=org.tkey);
ALTER TABLE catalogentry ADD CONSTRAINT catalogentry_marketplace_fk 
	FOREIGN KEY (marketplace_tkey) REFERENCES marketplace(tkey);

----------------------------------------------------------------------------
-- add new column anonymousvisible to catalogentryhistory	
ALTER TABLE catalogentryhistory ADD COLUMN anonymousvisible BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE catalogentryhistory AS cathist SET anonymousvisible = (
	SELECT	publicservice FROM producthistory AS prdhist
	WHERE cathist.productobjkey=prdhist.objkey AND prdhist.moddate <= cathist.moddate ORDER BY prdhist.moddate DESC LIMIT 1
);
ALTER TABLE producthistory DROP COLUMN publicservice;

-- add new column marketplaceobjkey to catalogentryhistory
ALTER TABLE catalogentryhistory ADD COLUMN marketplaceobjkey BIGINT;
UPDATE catalogentryhistory AS cathist SET marketplaceobjkey = (
	SELECT localmarketplaceobjkey FROM organizationhistory AS orghist
	WHERE cathist.organizationobjkey=orghist.objkey AND orghist.moddate <= cathist.moddate ORDER BY orghist.moddate DESC LIMIT 1);