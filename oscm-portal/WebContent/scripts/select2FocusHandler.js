/* 
 *  Copyright FUJITSU LIMITED 2020
 */ 
 $(document).ready(function() {
    AdmUtils.initFocus();
    AdmUtils.setUIElements()
    });

  $(document).ready(function(){
    var element = document.getElementById('#{elementId}');
    try { element.focus();
           document.getElementById('contentPanelDiv').scrollIntoView(true);
           } catch (e) {/* if the element cannot get the focus, ignore it */}
   })

  $(document).ready(function () {
	     
      $(document).on('select2:open', '.select2style', function (e) {
		  AdmUtils.setFocus(e.target,true);
		});
      
      $(document).on('select2:select', '.select2style', function (e) {
		  AdmUtils.setFocus(e.target,true);
		});
      
      $(document).on('blur', '.select2-selection--single', function (e) {
		  AdmUtils.setFocus(e.target,false);
		});

  });