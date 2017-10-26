ALTER TABLE "marketplace" ADD "restricted" boolean NOT NULL DEFAULT false;
ALTER TABLE "marketplacehistory" ADD "restricted" boolean NOT NULL DEFAULT false;

CREATE TABLE "marketplaceaccess" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER DEFAULT 0 NOT NULL,
		"marketplace_tkey" BIGINT NOT NULL,
		"organization_tkey" BIGINT NOT NULL 
	)
;

CREATE UNIQUE INDEX "marketplace_access_nuidx" ON "marketplaceaccess" ("marketplace_tkey" asc, "organization_tkey" asc);

ALTER TABLE "marketplaceaccess" ADD CONSTRAINT "marketplaceaccess_pk" PRIMARY KEY ("tkey");

ALTER TABLE "marketplaceaccess" ADD CONSTRAINT "marketplaceaccess_marketplace_fk" FOREIGN KEY ("marketplace_tkey")
	REFERENCES "marketplace" ("tkey");
	
ALTER TABLE "marketplaceaccess" ADD CONSTRAINT "marketplaceaccess_organization_fk" FOREIGN KEY ("organization_tkey")
	REFERENCES "organization" ("tkey");

insert into hibernate_sequences ("sequence_name", "sequence_next_hi_value") select 'MarketplaceAccess', COALESCE((MAX(tkey)/1000),0)+10 from marketplaceaccess;