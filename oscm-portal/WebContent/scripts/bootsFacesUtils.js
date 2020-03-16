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
        var getValue = document.getElementById(dropDownID);
        alert (getValue);
        var value = getValue.options[getValue.selectedIndex].value;
        alert (value);
           document.getElementById("selectedKey").value = value;
           document.forms["selectForm"].submit();
           $(document).off('select2:selecting');
        } else {
           e.preventDefault();
        }
    });
}