----------------------------------------------------
-- Bug 10631 
----------------------------------------------------

CREATE TEMPORARY TABLE temp_product AS (SELECT * FROM product AS prd 
	WHERE prd.type IN ('SUBSCRIPTION','PARTNER_SUBSCRIPTION','CUSTOMER_SUBSCRIPTION') AND prd.status='ACTIVE' AND
	EXISTS (
	SELECT * FROM SubscriptionHistory sub
	WHERE sub.status='INVALID' AND 
		sub.productobjkey=prd.tkey AND 
		sub.objversion=(SELECT max(tmp.objversion) FROM subscriptionhistory tmp WHERE tmp.objkey=sub.objkey)));

-- UPDATE PRODUCT
UPDATE product AS prd SET status='DELETED' WHERE prd.tkey IN (SELECT tkey FROM temp_product);

-- UPDATE PRODUCT HISTORY
CREATE TEMPORARY TABLE temp_producthistory AS (
	SELECT * FROM producthistory prdhist 
	WHERE prdhist.objkey IN (SELECT tkey FROM temp_product) AND 
	prdhist.objversion=(SELECT max(tmp.objversion) FROM producthistory tmp WHERE tmp.objkey=prdhist.objkey)
);
CREATE TEMPORARY TABLE temp_subscriptionhistory AS (
	SELECT * FROM subscriptionhistory subhist
	WHERE subhist.productobjkey IN (SELECT prodhist.objkey FROM temp_producthistory prodhist) AND 
		subhist.objversion=(SELECT max(tmp.objversion) FROM subscriptionhistory tmp WHERE tmp.objkey=subhist.objkey)
); 
UPDATE temp_producthistory prdhist SET
	objversion=objversion+1,
	moduser=(SELECT subhist.moduser FROM temp_subscriptionhistory subhist WHERE prdhist.objkey=subhist.productobjkey),
	modtype='MODIFY',
	moddate=(SELECT subhist.moddate FROM temp_subscriptionhistory subhist WHERE prdhist.objkey=subhist.productobjkey),
	status='DELETED';
INSERT INTO producthistory (tkey,deprovisioningdate,productid, provisioningdate, status, moddate, modtype,moduser,objkey,objversion,parametersetobjkey, pricemodelobjkey,vendorobjkey,targetcustomerobjkey,technicalproductobjkey,templateobjkey, invocationdate, "type", autoassignuserenabled)
	SELECT tkey+(SELECT COALESCE(MAX(tkey), 0) FROM producthistory),deprovisioningdate,productid, provisioningdate, status, moddate, modtype,moduser,objkey,objversion,parametersetobjkey, pricemodelobjkey,vendorobjkey,targetcustomerobjkey,technicalproductobjkey,templateobjkey, invocationdate, "type", autoassignuserenabled FROM temp_producthistory;
	
UPDATE "hibernate_sequences" SET "sequence_next_hi_value" = (select COALESCE((MAX(tkey)/1000),0)+10 FROM "producthistory") where "sequence_name" = 'ProductHistory';	