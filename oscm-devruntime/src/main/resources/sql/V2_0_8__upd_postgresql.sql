DROP INDEX organizationreference_tp_nuidx;

CREATE INDEX billingresult_orgkey_date_nuidx ON billingresult (organizationtkey, periodstarttime, periodendtime);
CREATE INDEX subscriptionhistory_orgobjkey_nuidx ON subscriptionhistory (organizationobjkey);
CREATE INDEX pricemodelhistory_objkey_nuidx ON pricemodelhistory (objkey);
CREATE INDEX producthistory_objkey_nuidx ON producthistory (objkey);
CREATE INDEX paymentinfo_orgkey_nuidx ON paymentinfo (organizationkey);
CREATE INDEX orghistory_nuidx ON organizationhistory (objkey);


