update pricemodel as pm 
  set priceperuserassignment=0 
  where exists (
    select * from steppedprice 
    where pricemodel_tkey=pm.tkey and pricedevent_tkey is null and pricedparameter_tkey is null);
update pricedevent as pe 
  set eventprice=0 
  where exists (
    select * from steppedprice 
    where pricedevent_tkey=pe.tkey and pricedparameter_tkey is null);
update pricedparameter as pp 
  set pricepersubscription=0 
  where exists (
    select * from steppedprice 
    where pricedevent_tkey is null and pricedparameter_tkey=pp.tkey);

update pricemodelhistory as pmh set priceperuserassignment=0 
  where exists (
    select tkey from pricemodel as pm 
    where pm.tkey=pmh.objkey and pm.version=pmh.objversion and pm.priceperuserassignment=0);
update pricedeventhistory as peh set eventprice=0 
  where exists (
    select tkey from pricedevent as pe 
    where pe.tkey=peh.objkey and pe.version=peh.objversion and pe.eventprice=0);
update pricedparameterhistory as pph set pricepersubscription=0
  where exists (
    select tkey from pricedparameter as pp 
    where pp.tkey=pph.objkey and pp.version=pph.objversion and pp.pricepersubscription=0);