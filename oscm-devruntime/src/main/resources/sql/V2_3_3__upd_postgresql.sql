-- Bug 9875
UPDATE localizedresource SET value='Kostenvorschau'         WHERE locale='de' AND objectkey=12 AND objecttype='REPORT_DESC';
UPDATE localizedresource SET value='Payment preview report' WHERE locale='en' AND objectkey=12 AND objecttype='REPORT_DESC';
UPDATE localizedresource SET value='支払いプレビュー'                       WHERE locale='ja' AND objectkey=12 AND objecttype='REPORT_DESC';