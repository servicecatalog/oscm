ALTER TABLE "triggerdefinition" ADD COLUMN "name" varchar (255);

update triggerdefinition set name=type where name is null;
commit;

ALTER TABLE "triggerdefinition" ALTER COLUMN "name" set not null; 

ALTER TABLE "triggerdefinitionhistory" ADD COLUMN "name" varchar (255);

update triggerdefinitionhistory set name=type where name is null;
commit;

ALTER TABLE "triggerdefinitionhistory" ALTER COLUMN "name" set not null; 