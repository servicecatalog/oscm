/* 
 *  Copyright FUJITSU LIMITED 2020
 */ 

  $(document).ready(function () {
	   
	  $(document).on('select2:selecting', '.select2style', function (e) {   	 
    	  if (!AdmUtils.isNotDirtyOrConfirmed()) {
    	  e.preventDefault();
          }    
          
      });
      
      $(document).on('select2:open', '.select2style', function (e) {
		  AdmUtils.setFocus(e.target,true);
		});
      
      $(document).on('change', '.select2style', function (e) {
		  AdmUtils.setFocus(e.target,true);
		});
	  
	  $(document).on('mouseout', '.select2style', function (e) {
		  AdmUtils.setFocus(e.target,false);
		});
      
  });