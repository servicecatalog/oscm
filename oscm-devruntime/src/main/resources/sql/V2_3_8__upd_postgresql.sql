----------------------------------------------------
-- Bug 10000
----------------------------------------------------
create index usagelicensehistory_moddate on usagelicensehistory (moddate desc);
create index usagelicensehistory_objkey on usagelicensehistory (objkey asc);
analyze usagelicensehistory;