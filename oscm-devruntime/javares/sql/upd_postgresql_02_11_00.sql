CREATE TABLE displaysettings (
    tkey bigint NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL,
    userTKey BIGINT NOT NULL,
    data character varying(32735) NOT NULL );  
    
ALTER TABLE displaysettings ADD CONSTRAINT displaysettings_pk PRIMARY KEY (tkey);

insert into "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") select 'DisplaySettings', COALESCE((MAX(tkey)/1000),0)+10 from displaysettings;
