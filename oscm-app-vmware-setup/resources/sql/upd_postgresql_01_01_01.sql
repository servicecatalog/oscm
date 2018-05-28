SET search_path TO vmware;

CREATE TABLE IF NOT EXISTS vcenter (
	tkey serial primary key,
	name character varying(255) NOT NULL,
	identifier character varying(255) NOT NULL,
	url character varying(255),
	userid character varying(255) NOT NULL,
	password character varying(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS datacenter (
	tkey serial primary key,
	name character varying(255) NOT NULL,
	identifier character varying(255) NOT NULL,
	vcenter_tkey int NOT NULL
);

CREATE TABLE IF NOT EXISTS cluster (
	tkey serial primary key,
	name character varying(255) NOT NULL,
    load_balancer character varying(4000),
	datacenter_tkey int NOT NULL
);

CREATE TABLE IF NOT EXISTS vlan (
    tkey serial primary key,
    name character varying(255) NOT NULL,
	subnet_mask character varying(50) NOT NULL,
    gateway character varying(50) NOT NULL,
    dnsserver character varying(500) NOT NULL,
    dnssuffix character varying(2000) NOT NULL,
    enabled boolean NOT NULL default true,
    cluster_tkey int NOT NULL
);

CREATE TABLE IF NOT EXISTS ippool (
    tkey serial primary key,
    ip_address character varying(50) NOT NULL,
    in_use boolean NOT NULL default false,
    vlan_tkey int NOT NULL
);

CREATE TABLE IF NOT EXISTS version (
		"productmajorversion" INTEGER NOT NULL, 
		"productminorversion" INTEGER NOT NULL, 
		"schemaversion" INTEGER NOT NULL, 
		"migrationdate" TIMESTAMP
	);
	
CREATE INDEX IF NOT EXISTS "ippool_vlan_idx" ON ippool ("vlan_tkey");
CREATE INDEX IF NOT EXISTS "vlan_cluster_idx" ON vlan ("cluster_tkey");

ALTER TABLE cluster ADD CONSTRAINT "cluster_datacenter_fk" FOREIGN KEY ("datacenter_tkey") REFERENCES datacenter ("tkey");
ALTER TABLE datacenter ADD CONSTRAINT "datacenter_vcenter_fk" FOREIGN KEY ("vcenter_tkey") REFERENCES vcenter ("tkey");
ALTER TABLE ippool ADD CONSTRAINT "ippool_vlan_fk" FOREIGN KEY ("vlan_tkey") REFERENCES vlan ("tkey");
ALTER TABLE vlan ADD CONSTRAINT "vlan_cluster_fk" FOREIGN KEY ("cluster_tkey") REFERENCES cluster ("tkey");
