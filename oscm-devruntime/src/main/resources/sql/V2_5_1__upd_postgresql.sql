CREATE TABLE "supportedlanguage" (
"tkey" BIGINT NOT NULL,
"version" INTEGER NOT NULL,
"languageisocode" VARCHAR(255) NOT NULL,
"activestatus" BOOLEAN,
"defaultstatus" BOOLEAN
);

ALTER TABLE "supportedlanguage" ADD CONSTRAINT "supportedlanguage_pk" PRIMARY KEY ("tkey");

CREATE TABLE "supportedlanguagehistory" (
"tkey" BIGINT NOT NULL,
"moddate" TIMESTAMP NOT NULL,
"modtype" VARCHAR(255) NOT NULL,
"moduser" VARCHAR(255) NOT NULL,
"objkey" BIGINT NOT NULL,
"objversion" BIGINT NOT NULL,
"invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00',
"languageisocode" VARCHAR(255) NOT NULL,
"activestatus" BOOLEAN,
"defaultstatus" BOOLEAN
);

ALTER TABLE "supportedlanguagehistory" ADD CONSTRAINT "supportedlanguagehistory_pk" PRIMARY KEY ("tkey");

insert into "supportedlanguage" ("tkey", "version", "languageisocode", "activestatus", "defaultstatus") 
     values ('1','0','en',TRUE,TRUE);
insert into "supportedlanguage" ("tkey", "version", "languageisocode", "activestatus", "defaultstatus") 
     values ('2','0','de',TRUE,FALSE);
insert into "supportedlanguage" ("tkey", "version", "languageisocode", "activestatus", "defaultstatus") 
     values ('3','0','ja',TRUE,FALSE);
     
insert into "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") select 'SupportedLanguage', COALESCE((MAX(tkey)/1000),0)+10 from supportedlanguage;
insert into "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") select 'SupportedLanguageHistory', COALESCE((MAX(tkey)/1000),0)+10 from supportedlanguagehistory;
