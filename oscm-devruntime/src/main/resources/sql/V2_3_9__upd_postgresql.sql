----------------------------------------------------
-- REQ Introduce revenue share for platform operator 
----------------------------------------------------

-- Restore the CatalogEntry entries from history for deleted TEMPLATE products
-- fortunately the broker and reseller revenue shares were not deleted due to missing annotation option "cascade = CascadeType.REMOVE"
INSERT INTO "catalogentry" (tkey, version, product_tkey, marketplace_tkey, anonymousvisible, visibleincatalog, brokerpricemodel_tkey, resellerpricemodel_tkey)
	SELECT ceh.objkey, ceh.objversion, ceh.productobjkey, NULL, ceh.anonymousvisible, ceh.visibleincatalog, ceh.brokerpricemodelobjkey, ceh.resellerpricemodelobjkey
	FROM "catalogentryhistory" AS ceh, "product" AS p
	WHERE ceh.modtype = 'DELETE'
	AND p.tkey = ceh.productobjkey
	AND p.type = 'TEMPLATE'
	AND p.status = 'DELETED';

-- set the reference to the marketpalce only if that marketplace still exists
UPDATE "catalogentry" AS ce
	SET marketplace_tkey = (
		SELECT ceh.marketplaceobjkey FROM "catalogentryhistory" AS ceh, "product" AS p
		WHERE ce.tkey = ceh.objkey
		AND ceh.modtype = 'DELETE'
		AND p.tkey = ceh.productobjkey
		AND p.type = 'TEMPLATE'
		AND p.status = 'DELETED')
	WHERE tkey IN (
		SELECT ceh2.objkey FROM "catalogentryhistory" AS ceh2, "product" AS p, "marketplace" AS m
		WHERE ceh2.modtype = 'DELETE'
		AND p.tkey = ceh2.productobjkey
		AND p.type = 'TEMPLATE'
		AND p.status = 'DELETED'
		AND m.tkey = ceh2.marketplaceobjkey);
	
-- since CatalogEntries are restored, change the 'DELETE' CatalogEntryHistory entry to 'MODIFY'
UPDATE "catalogentryhistory" AS ceh
	SET modtype = 'MODIFY'
	WHERE ceh.tkey IN (
		SELECT ceh2.tkey FROM "catalogentryhistory" AS ceh2, "product" AS p
		WHERE ceh2.modtype = 'DELETE'
		AND p.tkey = ceh2.productobjkey
		AND p.type = 'TEMPLATE'
		AND p.status = 'DELETED');
		

UPDATE "hibernate_sequences" SET "sequence_next_hi_value" = (select COALESCE((MAX(tkey)/1000),0)+10 FROM "catalogentry") where "sequence_name" = 'CatalogEntry';
