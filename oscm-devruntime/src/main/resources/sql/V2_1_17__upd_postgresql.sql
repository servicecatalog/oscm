-- indices for billing performance (Bug 9386)
drop index IF EXISTS parameterdefinitionhistory_datekeyversion;
drop index IF EXISTS parameterhistory_dateversion;
drop index IF EXISTS pricedparameterhistory_datepmkeyversion;

create index parameterdefinitionhistory_datekeyversion on
parameterdefinitionhistory (moddate, objkey, objversion);
analyze parameterdefinitionhistory;

create index parameterhistory_dateversion on parameterhistory (moddate, objkey);
analyze parameterhistory;

create index pricedparameterhistory_datepmkeyversion on pricedparameterhistory
(moddate, pricemodelobjkey, objversion);
analyze pricedparameterhistory;

-- remove duplicate entries in hibernate_sequences (Bug 9399)
create table temp_hs (tkey serial, name varchar(255), ord bigint);

insert into temp_hs (name, ord) select sequence_name, sequence_next_hi_value from hibernate_sequences;
delete from temp_hs hs where hs.ord < (select max(hsi.ord) from temp_hs hsi where hsi.name=hs.name);
delete from temp_hs hs where hs.tkey < (select max(hsi.tkey) from temp_hs hsi where hs.name=hsi.name and hs.ord = hsi.ord);

delete from  hibernate_sequences;
insert into hibernate_sequences(sequence_name, sequence_next_hi_value) select name, ord from temp_hs;

drop table temp_hs;