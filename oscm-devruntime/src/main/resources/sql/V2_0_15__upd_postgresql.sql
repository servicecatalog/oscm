----------------------------------------------------------------------------
-- Migrate Billinresult
----------------------------------------------------------------------------
ALTER TABLE billingresult ADD COLUMN chargingorgkey BIGINT;

UPDATE billingresult AS br SET chargingorgkey = (
	SELECT sourcekey FROM organizationreference AS orgref
	WHERE br.organizationtkey=orgref.targetkey);

ALTER TABLE billingresult ALTER COLUMN chargingorgkey SET NOT NULL;