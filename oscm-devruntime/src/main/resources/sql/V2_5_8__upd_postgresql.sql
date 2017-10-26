update supportedcountry set countryisocode='ME' where tkey='49';
INSERT INTO supportedcountry ("tkey", "version","countryisocode") VALUES ('240','0','RS');

create table temp (tkey serial, version integer,  countrykey bigint, orgkey bigint );
delete from organizationtocountry;
delete from organizationtocountryhistory;
insert into temp ("version", "countrykey", "orgkey") Select 0, s.tkey, o.tkey from "supportedcountry" s, "organization" o order by o.tkey asc, s.tkey asc;
insert into organizationtocountry ("tkey", "version", "supportedcountry_tkey", "organization_tkey") select tkey, version, countrykey, orgkey from temp;
insert into organizationtocountryhistory ("tkey", "moddate", "modtype", "moduser", "objkey", "objversion", "organizationobjkey", "supportedcountryobjkey" ) select tkey, now(), 'ADD', '1000', tkey, 0, orgkey, countrykey from temp;
drop table temp;