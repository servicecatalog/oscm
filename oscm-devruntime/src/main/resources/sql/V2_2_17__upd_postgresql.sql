UPDATE "localizedresource" SET "value"='利用者情報' WHERE "locale"='ja' AND "objectkey"=0 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='支払い請求可能イベント情報' WHERE "locale"='ja' AND "objectkey"=1 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='サービス情報' WHERE "locale"='ja' AND "objectkey"=2 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='利用部門情報' WHERE "locale"='ja' AND "objectkey"=3 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='技術サービス使用情報' WHERE "locale"='ja' AND "objectkey"=4 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='サービス提供部門情報' WHERE "locale"='ja' AND "objectkey"=5 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='購入済サービス情報' WHERE "locale"='ja' AND "objectkey"=6 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='インスタンス情報' WHERE "locale"='ja' AND "objectkey"=7 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='請求書情報' WHERE "locale"='ja' AND "objectkey"=8 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='支払処理ステータス' WHERE "locale"='ja' AND "objectkey"=9 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='利用部門の請求書明細' WHERE "locale"='ja' AND "objectkey"=10 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='請求書明細' WHERE "locale"='ja' AND "objectkey"=11 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='現在の支払プレビュー' WHERE "locale"='ja' AND "objectkey"=12 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='サービス提供部門収入情報' WHERE "locale"='ja' AND "objectkey"=13 AND "objecttype"='REPORT_DESC';
UPDATE "localizedresource" SET "value"='外部サービス情報' WHERE "locale"='ja' AND "objectkey"=14 AND "objecttype"='REPORT_DESC';

INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'ja', 15, 'REPORT_DESC', 'サービス仲介部門の料金配分明細');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'ja', 16, 'REPORT_DESC', 'サービス再提供部門の料金配分明細');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'ja', 17, 'REPORT_DESC', 'サービス仲介部門/サービス再提供部門の料金配分明細');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'ja', 18, 'REPORT_DESC', '料金配分明細');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((select COALESCE(MAX(tkey), 0) + 1 from "localizedresource"), 0, 'ja', 19, 'REPORT_DESC', 'サービス提供部門の料金配分明細');

