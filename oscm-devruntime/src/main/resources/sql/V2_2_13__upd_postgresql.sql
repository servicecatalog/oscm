ALTER TABLE billingsharesresult ADD CONSTRAINT billingsharesresult_org_period_uc 
UNIQUE (organizationtkey, resulttype, periodstarttime, periodendtime);