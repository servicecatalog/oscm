UPDATE marketplacehistory mph
SET objkey=mp.tkey
FROM marketplace mp
WHERE mp.marketplaceid=mph.marketplaceid
 AND (SELECT COUNT(*) FROM marketplacehistory mph2 WHERE mph2.marketplaceid=mph.marketplaceid AND mph2.modtype='DELETE')=0
 AND mph.modtype='ADD';

UPDATE marketplacehistory mph
SET objkey=(SELECT DISTINCT objkey FROM marketplacehistory mph2 WHERE mph2.modtype = 'DELETE' AND mph2.marketplaceid=mph.marketplaceid AND
 mph2.moddate=(SELECT MIN(moddate) FROM marketplacehistory mph3 WHERE mph3.modtype = 'DELETE' AND mph3.moddate >= mph.moddate AND mph3.marketplaceid = mph.marketplaceid))
WHERE (SELECT COUNT(*) FROM marketplacehistory mph2 WHERE mph2.marketplaceid=mph.marketplaceid AND mph2.modtype='DELETE')>0
 AND mph.modtype='ADD';