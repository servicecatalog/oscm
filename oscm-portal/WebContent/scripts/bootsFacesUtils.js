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
alert("Wszedem do funckji change " + dropDownID);
    $(document).on('select2:selecting', function (e) {
        if(AdmUtils.isNotDirtyOrConfirmed()) {
           alert("Wszedem do ifa this to: ");
           document.getElementById("selectedKey").value = dropDownID.options[dropDownID.selectedIndex].text;
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