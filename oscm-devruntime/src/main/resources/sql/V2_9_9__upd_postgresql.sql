alter table "hibernate_sequences" alter column "sequence_next_hi_value" type BIGINT;
UPDATE "parameterdefinition" SET "parameterid" = 'SUBNET' WHERE "parameterid" = 'subnet';
UPDATE "parameterdefinition" SET "parameterid" = 'PUBLIC_IP' WHERE "parameterid" = 'publicIp';
UPDATE "parameterdefinition" SET "parameterid" = 'DISK_SIZE' WHERE "parameterid" = 'diskSize';
UPDATE "parameterdefinition" SET "parameterid" = 'INSTANCE_PLATFORM' WHERE "parameterid" = 'instancePlatform';
UPDATE "parameterdefinition" SET "parameterid" = 'EAI_INSTANCE_PUBLIC_DNS' WHERE "parameterid" = 'instancePublicDns';
UPDATE "parameterdefinition" SET "parameterid" = 'SNAPSHOT_ID' WHERE "parameterid" = 'snapshotId';

UPDATE "parameterdefinitionhistory" SET "parameterid" = 'SUBNET' WHERE "parameterid" = 'subnet';
UPDATE "parameterdefinitionhistory" SET "parameterid" = 'PUBLIC_IP' WHERE "parameterid" = 'publicIp';
UPDATE "parameterdefinitionhistory" SET "parameterid" = 'DISK_SIZE' WHERE "parameterid" = 'diskSize';
UPDATE "parameterdefinitionhistory" SET "parameterid" = 'INSTANCE_PLATFORM' WHERE "parameterid" = 'instancePlatform';
UPDATE "parameterdefinitionhistory" SET "parameterid" = 'EAI_INSTANCE_PUBLIC_DNS' WHERE "parameterid" = 'instancePublicDns';
UPDATE "parameterdefinitionhistory" SET "parameterid" = 'SNAPSHOT_ID' WHERE "parameterid" = 'snapshotId';

UPDATE "udadefinition" SET "udaid" = 'SUBNET' WHERE "udaid" = 'subnet';
UPDATE "udadefinition" SET "udaid" = 'PUBLIC_IP' WHERE "udaid" = 'publicIp';
UPDATE "udadefinition" SET "udaid" = 'DISK_SIZE' WHERE "udaid" = 'diskSize';
UPDATE "udadefinition" SET "udaid" = 'INSTANCE_PLATFORM' WHERE "udaid" = 'instancePlatform';
UPDATE "udadefinition" SET "udaid" = 'EAI_INSTANCE_PUBLIC_DNS' WHERE "udaid" = 'instancePublicDns';
UPDATE "udadefinition" SET "udaid" = 'SNAPSHOT_ID' WHERE "udaid" = 'snapshotId';

UPDATE "udadefinitionhistory" SET "udaid" = 'SUBNET' WHERE "udaid" = 'subnet';
UPDATE "udadefinitionhistory" SET "udaid" = 'PUBLIC_IP' WHERE "udaid" = 'publicIp';
UPDATE "udadefinitionhistory" SET "udaid" = 'DISK_SIZE' WHERE "udaid" = 'diskSize';
UPDATE "udadefinitionhistory" SET "udaid" = 'INSTANCE_PLATFORM' WHERE "udaid" = 'instancePlatform';
UPDATE "udadefinitionhistory" SET "udaid" = 'EAI_INSTANCE_PUBLIC_DNS' WHERE "udaid" = 'instancePublicDns';
UPDATE "udadefinitionhistory" SET "udaid" = 'SNAPSHOT_ID' WHERE "udaid" = 'snapshotId';