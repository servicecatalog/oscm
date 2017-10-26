ALTER TABLE subscription ADD COLUMN usergroup_tkey BIGINT DEFAULT NULL;

ALTER TABLE subscription ADD CONSTRAINT subscription_to_usergroup_fk FOREIGN KEY (usergroup_tkey)
	REFERENCES usergroup (tkey);
	
ALTER TABLE subscriptionhistory ADD COLUMN usergroupobjkey BIGINT DEFAULT NULL;

CREATE TABLE usergrouphistory (
	tkey BIGINT NOT NULL,
	name VARCHAR(256) NOT NULL,
    description VARCHAR(255),
    isdefault BOOLEAN NOT NULL,
    invocationdate TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00',
	moddate TIMESTAMP NOT NULL,
	modtype VARCHAR(255) NOT NULL,
	moduser VARCHAR(255) NOT NULL,
	objkey BIGINT NOT NULL,
	objversion BIGINT NOT NULL,
    organizationobjkey BIGINT NOT NULL,
    CONSTRAINT usergrouphistory_pk PRIMARY KEY (tkey)
	);

INSERT INTO usergrouphistory (tkey, name, description, isdefault, invocationdate, moddate, modtype, moduser, objkey, objversion, organizationobjkey)
SELECT tkey, name, description, isdefault, NOW(), NOW(), 'ADD', '1000', tkey, version, organization_tkey
FROM usergroup;

INSERT INTO hibernate_sequences (sequence_name, sequence_next_hi_value) select 'UserGroupHistory', COALESCE((MAX(tkey)/1000),0)+10 from usergrouphistory;