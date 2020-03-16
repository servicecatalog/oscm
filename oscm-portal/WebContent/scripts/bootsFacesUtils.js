//  Copyright FUJITSU LIMITED 2020

var BootsFacesUtils = {
	storedForm: []
};

BootsFacesUtils.preventChangeSelectionForDropDown = function() {
    $(document).on('select2:selecting',function (e) {
       if (AdmUtils.isNotDirtyOrConfirmed()) {
           $(document).off('select2:selecting');
       } else {
           e.preventDefault();
       }
    });
}

BootsFacesUtils.changeSelectionIndexAtDropDown = function(dropDownID) {
    $(document).on('select2:selecting', function (e) {
        if(AdmUtils.isNotDirtyOrConfirmed()) {
        var selValue = $("#techServiceId").val();
            document.getElementById("selectedKey").value=selValue.options[selValue.selectedIndex].value;
            document.forms["selectForm"].submit();
           document.forms["selectForm"].submit();
           $(document).off('select2:selecting');
        } else {
           AdmUtils.restoreValue(dropDownID);
           e.preventDefault();
        }
    });
}