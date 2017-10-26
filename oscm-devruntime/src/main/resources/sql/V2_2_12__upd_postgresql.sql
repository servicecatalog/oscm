-- SCHEMA UPDATE (1)
ALTER TABLE "product" ADD COLUMN "type" VARCHAR(255);
ALTER TABLE "producthistory" ADD COLUMN "type" VARCHAR(255);

-- MIGRATION
-- product template
UPDATE product AS p SET "type" = 'TEMPLATE' 
WHERE p.template_tkey IS NULL AND 
	p.targetcustomer_tkey IS NULL AND
	(SELECT COUNT(*) FROM subscription WHERE product_tkey=p.tkey)=0;

-- subscription copy
UPDATE product AS p SET "type" = 'SUBSCRIPTION' 
WHERE p.template_tkey IS NOT NULL AND 
    p.targetcustomer_tkey IS NULL AND 
    (SELECT COUNT(*) FROM subscription WHERE product_tkey=p.tkey)=1;
    
-- customer specific copy
UPDATE product AS p SET "type" = 'CUSTOMER_TEMPLATE' 
WHERE p.template_tkey IS NOT NULL AND 
    p.targetcustomer_tkey IS NOT NULL AND
    (SELECT COUNT(*) FROM subscription WHERE product_tkey=p.tkey)=0;
    
-- customer specific copy for subscription
UPDATE product AS p SET "type" = 'CUSTOMER_SUBSCRIPTION' 
WHERE p.template_tkey IS NOT NULL AND 
    p.targetcustomer_tkey IS NOT NULL AND
    (SELECT COUNT(*) FROM subscription WHERE product_tkey=p.tkey)=1;

-- product histories
UPDATE producthistory AS ph SET "type" = (SELECT p.type FROM product p WHERE p.tkey = ph.objkey);

-- SCHEMA UPDATE (2)
ALTER TABLE "product" ALTER COLUMN "type" SET NOT NULL;
--ALTER TABLE "producthistory" ALTER COLUMN "type" SET NOT NULL;