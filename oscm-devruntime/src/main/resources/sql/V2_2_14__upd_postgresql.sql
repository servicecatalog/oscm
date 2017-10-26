---------------------------------------------------------------
-- RQ: Partner-Model - Billing
---------------------------------------------------------------
ALTER TABLE billingresult ADD COLUMN vendorkey BIGINT;
UPDATE billingresult br
SET vendorkey = (
	SELECT DISTINCT ph.vendorobjkey
    FROM billingresult br2, subscriptionhistory sh, producthistory ph
    WHERE br2.tkey = br.tkey AND br2.subscriptionkey = sh.objkey AND sh.productobjkey = ph.objkey
);

-- migrate billingresults which do not have any subscriptions listed in the billing result, set vendor key to charging organization tkey 
update billingresult br set vendorkey=br.chargingorgkey;      
      
ALTER TABLE billingresult ALTER COLUMN vendorkey SET NOT NULL;