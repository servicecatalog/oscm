-- Delete all sessions from database (node SingleNode not present in Cluster)

DELETE FROM session;

-- Create new table for Reporting result cache (needed for cluster)

CREATE TABLE "reportresultcache" (
		"tkey" BIGINT NOT NULL,
		"cachekey" VARCHAR(255) NOT NULL UNIQUE,
		"version" INTEGER NOT NULL,
		"timestamp" TIMESTAMP NOT NULL,
		"report" bytea,
		 CONSTRAINT "reportresultcache_pk" PRIMARY KEY ("tkey")
	);
	
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('ReportResultCache', 10);
