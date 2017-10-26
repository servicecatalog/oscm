INSERT INTO "userrole" ("tkey", "version", "rolename" ) VALUES (9, 0, 'UNIT_ADMINISTRATOR');

CREATE TABLE "unituserrole" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"rolename" VARCHAR(255) NOT NULL
);

CREATE TABLE "unitroleassignment" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"usergrouptouser_tkey" BIGINT NOT NULL,
		"unituserrole_tkey" BIGINT NOT NULL
);

ALTER TABLE "unituserrole" ADD CONSTRAINT "unituserrole_pk" PRIMARY KEY ("tkey");
ALTER TABLE "unitroleassignment" ADD CONSTRAINT "unitroleassignment_pk" PRIMARY KEY ("tkey");

INSERT INTO "unituserrole" ("tkey", "version", "rolename" ) VALUES (1, 0, 'ADMINISTRATOR');
INSERT INTO "unituserrole" ("tkey", "version", "rolename" ) VALUES (2, 0, 'USER');

ALTER TABLE "unitroleassignment" ADD CONSTRAINT "unitroleassignment_to_unituserrole_fk" FOREIGN KEY ("unituserrole_tkey")
    REFERENCES "unituserrole" ("tkey") ON DELETE SET NULL;
ALTER TABLE "unitroleassignment" ADD CONSTRAINT "unitroleassignment_to_usergrouptouser_fk" FOREIGN KEY ("usergrouptouser_tkey")
    REFERENCES "usergrouptouser" ("tkey") ON DELETE SET NULL;
    
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('UnitRoleAssignment', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('UnitUserRole', 10);
