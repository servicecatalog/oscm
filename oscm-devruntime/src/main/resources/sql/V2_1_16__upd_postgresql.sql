UPDATE "localizedresource" SET "value"='利用者情報レポート' WHERE "locale"='ja' AND "objectkey"=0 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='支払い請求可能なイベント情報レポート' WHERE "locale"='ja' AND "objectkey"=1 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='サービス情報レポート' WHERE "locale"='ja' AND "objectkey"=2 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='利用部門情報レポート' WHERE "locale"='ja' AND "objectkey"=3 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='技術サービス使用レポート' WHERE "locale"='ja' AND "objectkey"=4 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='サービス提供部門情報レポート' WHERE "locale"='ja' AND "objectkey"=5 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='購入済サービスレポート' WHERE "locale"='ja' AND "objectkey"=6 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='インスタンスレポート' WHERE "locale"='ja' AND "objectkey"=7 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='請求書レポート' WHERE "locale"='ja' AND "objectkey"=8 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='支払処理ステータスレポート' WHERE "locale"='ja' AND "objectkey"=9 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='利用部門の既存請求書の明細レポート' WHERE "locale"='ja' AND "objectkey"=10 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='既存請求書の明細レポート' WHERE "locale"='ja' AND "objectkey"=11 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='月初から現在までの支払プレビュー' WHERE "locale"='ja' AND "objectkey"=12 AND "objecttype"='REPORT_DESC';

UPDATE "localizedresource" SET "value"='購入済サービスの最大同時利用者数' WHERE "locale"='ja' AND "objectkey"=1000 AND "objecttype"='PARAMETER_DEF_DESC';
UPDATE "localizedresource" SET "value"='購入済サービスの最大登録利用者数' WHERE "locale"='ja' AND "objectkey"=1001 AND "objecttype"='PARAMETER_DEF_DESC';
UPDATE "localizedresource" SET "value"='購入済サービスが無効化されるまでの日数' WHERE "locale"='ja' AND "objectkey"=1002 AND "objecttype"='PARAMETER_DEF_DESC';

UPDATE "localizedresource" SET "value"='サービスへの利用者のログイン' WHERE "locale"='ja' AND "objectkey"=1000 AND "objecttype"='EVENT_DESC';
UPDATE "localizedresource" SET "value"='サービスからの利用者のログアウト' WHERE "locale"='ja' AND "objectkey"=1001 AND "objecttype"='EVENT_DESC';

INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'ja', 13, 'REPORT_DESC', 'サービス提供部門収入レポート');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'ja', 14, 'REPORT_DESC', '外部サービスレポート');
    
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((SELECT COALESCE(MAX(tkey), 0) + 1 FROM "localizedresource"), 0, 'ja', 1, 'PAYMENT_TYPE_NAME', 'クレジットカード');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((SELECT COALESCE(MAX(tkey), 0) + 1 FROM "localizedresource"), 0, 'ja', 2, 'PAYMENT_TYPE_NAME', '口座振替');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((SELECT COALESCE(MAX(tkey), 0) + 1 FROM "localizedresource"), 0, 'ja', 3, 'PAYMENT_TYPE_NAME', '請求書');
