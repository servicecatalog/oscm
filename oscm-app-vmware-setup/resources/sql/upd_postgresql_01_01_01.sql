DO $$
BEGIN
  IF NOT EXISTS (
    SELECT
    FROM   bssapp.pg_catalog.pg_roles
    WHERE  rolname = 'vmwareuser') THEN
    CREATE ROLE vmwareuser LOGIN PASSWORD 'vmwareuser';
  END IF;
END
$$;

DO $$
BEGIN
  IF NOT EXISTS(
    SELECT schema_name
    FROM bssapp.information_schema.schemata
    WHERE schema_name = 'vmwareuser') THEN
      CREATE SCHEMA vmwareuser;
    END IF;
END
$$;

GRANT CONNECT ON DATABASE bssapp TO vmwareuser;
GRANT ALL PRIVILEGES ON SCHEMA vmwareuser TO vmwareuser;

CREATE TABLE IF NOT EXISTS bssapp.vmwareuser.vcenter (
	tkey serial primary key,
	name character varying(255) NOT NULL,
	identifier character varying(255) NOT NULL,
	url character varying(255),
	userid character varying(255) NOT NULL,
	password character varying(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS bssapp.vmwareuser.datacenter (
	tkey serial primary key,
	name character varying(255) NOT NULL,
	identifier character varying(255) NOT NULL,
	vcenter_tkey int NOT NULL
);

CREATE TABLE IF NOT EXISTS bssapp.vmwareuser.cluster (
	tkey serial primary key,
	name character varying(255) NOT NULL,
    load_balancer character varying(4000),
	datacenter_tkey int NOT NULL
);

CREATE TABLE IF NOT EXISTS bssapp.vmwareuser.vlan (
    tkey serial primary key,
    name character varying(255) NOT NULL,
	subnet_mask character varying(50) NOT NULL,
    gateway character varying(50) NOT NULL,
    dnsserver character varying(500) NOT NULL,
    dnssuffix character varying(2000) NOT NULL,
    enabled boolean NOT NULL default true,
    cluster_tkey int NOT NULL
);

CREATE TABLE IF NOT EXISTS bssapp.vmwareuser.ippool (
    tkey serial primary key,
    ip_address character varying(50) NOT NULL,
    in_use boolean NOT NULL default false,
    vlan_tkey int NOT NULL
);

CREATE TABLE IF NOT EXISTS bssapp.vmwareuser.version (
		"productmajorversion" INTEGER NOT NULL, 
		"productminorversion" INTEGER NOT NULL, 
		"schemaversion" INTEGER NOT NULL, 
		"migrationdate" TIMESTAMP
	);
	
CREATE INDEX IF NOT EXISTS "ippool_vlan_idx" ON bssapp.vmwareuser.ippool ("vlan_tkey");
CREATE INDEX IF NOT EXISTS "vlan_cluster_idx" ON bssapp.vmwareuser.vlan ("cluster_tkey");

DO $$
BEGIN
  IF NOT EXISTS(
      SELECT
      FROM bssapp.pg_catalog.pg_constraint
      WHERE conname = 'cluster_datacenter_fk'
  ) THEN
      ALTER TABLE bssapp.vmwareuser.cluster ADD CONSTRAINT "cluster_datacenter_fk" FOREIGN KEY ("datacenter_tkey") REFERENCES bssapp.vmwareuser.datacenter ("tkey");
  END IF;
END$$;

DO $$
BEGIN
  IF NOT EXISTS(
      SELECT
      FROM bssapp.pg_catalog.pg_constraint
      WHERE conname = 'datacenter_vcenter_fk'
  ) THEN
    ALTER TABLE bssapp.vmwareuser.datacenter ADD CONSTRAINT "datacenter_vcenter_fk" FOREIGN KEY ("vcenter_tkey") REFERENCES bssapp.vmwareuser.vcenter ("tkey");
  END IF;
END$$;

DO $$
BEGIN
  IF NOT EXISTS(
      SELECT
      FROM bssapp.pg_catalog.pg_constraint
      WHERE conname = 'ippool_vlan_fk'
  ) THEN
    ALTER TABLE bssapp.vmwareuser.ippool ADD CONSTRAINT "ippool_vlan_fk" FOREIGN KEY ("vlan_tkey") REFERENCES bssapp.vmwareuser.vlan ("tkey");
  END IF;
END$$;

DO $$
BEGIN
  IF NOT EXISTS(
      SELECT
      FROM bssapp.pg_catalog.pg_constraint
      WHERE conname = 'vlan_cluster_fk'
  ) THEN
    ALTER TABLE bssapp.vmwareuser.vlan ADD CONSTRAINT "vlan_cluster_fk" FOREIGN KEY ("cluster_tkey") REFERENCES bssapp.vmwareuser.cluster ("tkey");
  END IF;
END$$;

GRANT ALL ON ALL TABLES IN SCHEMA vmwareuser TO vmwareuser;
