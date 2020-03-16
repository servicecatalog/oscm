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
        var selValue = $(document).val();
                alert ("Value to: " + selValue + " a dropDOwn to: " + dropDownID)
                    document.getElementById("selectedKey").value=selValue;
                   document.forms["selectForm"].submit();
           $(document).off('select2:selecting');
        } else {
           e.preventDefault();
        }
    });
}