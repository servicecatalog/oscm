---------------------------------------------------------------
-- RQ: GPaaS - Flexible billing cut-off day 
---------------------------------------------------------------

ALTER TABLE organization ADD COLUMN cutoffday INT;
ALTER TABLE organizationhistory ADD COLUMN cutoffday INT;
ALTER TABLE subscription ADD COLUMN cutoffday INT;
ALTER TABLE subscriptionhistory ADD COLUMN cutoffday INT;

UPDATE organization SET cutoffday = 1;
UPDATE organizationhistory SET cutoffday = 1;
UPDATE subscription SET cutoffday = 1;
UPDATE subscriptionhistory SET cutoffday = 1;

ALTER TABLE organization ALTER COLUMN cutoffday SET NOT NULL;
ALTER TABLE organizationhistory ALTER COLUMN cutoffday SET NOT NULL;
ALTER TABLE subscription ALTER COLUMN cutoffday SET NOT NULL;
ALTER TABLE subscriptionhistory ALTER COLUMN cutoffday SET NOT NULL;
ALTER TABLE organization ADD CONSTRAINT organization_cutoffday CHECK (cutoffday>0 AND cutoffday<29);
ALTER TABLE organizationhistory ADD CONSTRAINT organizationhistory_cutoffday CHECK (cutoffday>0 AND cutoffday<29);
ALTER TABLE subscription ADD CONSTRAINT subscription_cutoffday CHECK (cutoffday>0 AND cutoffday<29);
ALTER TABLE subscriptionhistory ADD CONSTRAINT subscriptionhistory_cutoffday CHECK (cutoffday>0 AND cutoffday<29);

