/* 
 *  Copyright FUJITSU LIMITED 2020
 */ 

  $(document).ready(function () {
	   
	  $(document).on('select2:selecting', '.select2style', function (e) {   	 
    	  if (!AdmUtils.isNotDirtyOrConfirmed()) {
    	  e.preventDefault();
          }    
          
      });
    

  });