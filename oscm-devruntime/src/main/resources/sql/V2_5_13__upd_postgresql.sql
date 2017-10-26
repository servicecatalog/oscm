-- Bug 11083: Migrate Indonesian supportedlanguage id to in.    
UPDATE supportedlanguage SET languageisocode='in' WHERE languageisocode = 'id';
UPDATE supportedlanguagehistory SET languageisocode='in' WHERE languageisocode = 'id';