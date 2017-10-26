CREATE TABLE unitroleassignmenttmp (
                                "tkey" SERIAL,
                                "version" INTEGER NOT NULL,
                                "usergrouptouser_tkey" BIGINT NOT NULL,
                                "unituserrole_tkey" BIGINT NOT NULL
);
insert into unitroleassignmenttmp (version, usergrouptouser_tkey, unituserrole_tkey) select version, usergrouptouser_tkey, unituserrole_tkey from unitroleassignment ura order by ura.tkey;
insert into unitroleassignmenttmp (version, usergrouptouser_tkey, unituserrole_tkey)
    select 0, ugtu.tkey, 2 from usergrouptouser as ugtu where ugtu.tkey
    not in (select ura.usergrouptouser_tkey from unitroleassignment ura);
delete from unitroleassignment;
insert into unitroleassignment select tkey, version, usergrouptouser_tkey, unituserrole_tkey from unitroleassignmenttmp ura order by ura.tkey;
update hibernate_sequences set sequence_next_hi_value=nextVal('unitroleassignmenttmp_tkey_seq') where sequence_name='UnitRoleAssignment';
drop table unitroleassignmenttmp;
