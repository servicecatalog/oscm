UPDATE hibernate_sequences
SET sequence_next_hi_value=(SELECT COALESCE((MAX(tkey)/1000),0)+10
                            FROM usergrouphistory)
WHERE sequence_name='UserGroupHistory';