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
        var keyID = document.getElementById("selectedKey");
        var menuID = dropDownID.options[dropDownID.selectedIndex].value;
        alert("key " + keyID + "menu" + menuID);
        keyID = menuID
            document.getElementById('selectForm').submit();
        } else {
           e.preventDefault();
        }
}