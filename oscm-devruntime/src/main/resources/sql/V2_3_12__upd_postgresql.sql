DELETE FROM "revenuesharemodelhistory" rh WHERE 
	rh.objkey NOT IN (SELECT mh.pricemodelobjkey FROM marketplacehistory mh WHERE mh.pricemodelobjkey = rh.objkey)  
	AND rh.objkey NOT IN (SELECT mh.brokerpricemodelobjkey FROM marketplacehistory mh WHERE mh.brokerpricemodelobjkey = rh.objkey) 
	AND rh.objkey NOT IN (SELECT mh.resellerpricemodelobjkey FROM marketplacehistory mh WHERE mh.resellerpricemodelobjkey = rh.objkey) 
	AND rh.objkey NOT IN (SELECT tkey FROM revenuesharemodel r WHERE r.tkey = rh.objkey);