/* 
 *  Copyright FUJITSU LIMITED 2020
 */
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