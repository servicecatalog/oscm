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

BootsFacesUtils.changeSelectionIndexAtDropDown = function(e, dropDownID) {
        if(AdmUtils.isNotDirtyOrConfirmed()) {
        document.getElementById("selectForm:selectedKey") = dropDownID.options[dropDownID.selectedIndex].value;
        document.getElementById('selectForm').submit();
        } else {
           e.preventDefault();
        }
}