CREATE TABLE auditlog
(
  tkey BIGINT NOT NULL,
  operationid CHARACTER VARYING(5) NOT NULL,
  operationname CHARACTER VARYING(255) NOT NULL,
  userid CHARACTER VARYING(255) NOT NULL,
  organizationid CHARACTER VARYING(255) NOT NULL,
  organizationname CHARACTER VARYING(255) NOT NULL,
  log TEXT NOT NULL,
  creationtime BIGINT NOT NULL,
  CONSTRAINT auditlog_pk PRIMARY KEY (tkey)
);
CREATE INDEX auditlog_creationdate_opname_idx ON auditlog (creationtime, operationname);