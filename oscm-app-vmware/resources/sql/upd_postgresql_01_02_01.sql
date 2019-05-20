CREATE TABLE IF NOT EXISTS distributedvirtualswitch (
    tkey serial primary key,
    uuid character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
	cluster_tkey int NOT NULL
);

CREATE TABLE IF NOT EXISTS portgroup (
    tkey serial primary key,
    uuid character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
	distributedvirtualswitch_cluster_fk int NOT NULL
);

CREATE TABLE IF NOT EXISTS portgroup_ippool (
    tkey serial primary key,
    ip_address character varying(50) NOT NULL,
    in_use boolean NOT NULL default false,
    portgroup_fk int NOT NULL
);


ALTER TABLE distributedvirtualswitch ADD CONSTRAINT "distributedvirtualswitch_cluster_fk" FOREIGN KEY ("cluster_tkey") REFERENCES cluster ("tkey");
ALTER TABLE portgroup ADD CONSTRAINT "distributedvirtualswitch_cluster_fk" FOREIGN KEY ("distributedvirtualswitch_cluster_fk") REFERENCES distributedvirtualswitch ("tkey");
ALTER TABLE portgroup_ippool ADD CONSTRAINT "pool_tkey" FOREIGN KEY ("portgroup_fk") REFERENCES portgroup ("tkey");