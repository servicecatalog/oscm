//  Copyright FUJITSU LIMITED 2020

var BootsFacesUtils = {
	storedForm: []
};

BootsFacesUtils.preventChangeSelectionForDropDown = function() {
    $(document).on('select2:selecting',function (e) {
       if (AdmUtils.isNotDirtyOrConfirmed()) {
           AdmUtils.storeValue(this);
           $(document).off('select2:selecting');
       } else {
           e.preventDefault();
       }
    });
}

BootsFacesUtils.changeSelectionIndexAtDropDown = function(dropDownID) {
alert("Wszedem do funckji change");
    $(document).on('select2:selecting', function (e) {
        if(AdmUtils.isNotDirtyOrConfirmed()) {
           alert("Wszedem do ifa");
           document.getElementById("selectedKey").value = document.getElementById(dropDownID).value;
//         document.element('selectedKey')}.value = dropDownID.options[dropDownID.selectedIndex].value;
           document.forms["selectForm"].submit();
//         #{rich:element('selectForm')}.submit();
           $(document).off('select2:selecting');
        } else {
           alert("Wszedem do else");
           e.preventDefault();
        }
    });
}