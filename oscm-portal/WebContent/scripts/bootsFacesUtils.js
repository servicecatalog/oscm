//  Copyright FUJITSU LIMITED 2020

var BootsFacesUtils = {
	storedForm: []
};

BootsFacesUtils.preventChangeSelectionForDropDown = function() {
alert("Wszedem do funckji prevent");
    $(document).on('select2:selecting',function (e) {
       if (AdmUtils.isNotDirtyOrConfirmed()) {
       alert("Wszedem do ifa");
           AdmUtils.storeValue(this);
           $(document).off('select2:selecting');
       } else {
       alert("Wszedem do else");
           e.preventDefault();
       }
    });
}

BootsFacesUtils.changeSelectionIndexAtDropDown = function(dropDownID) {
alert("Wszedem do funckji prevent");
    $(document).on('select2:selecting', function (e) {
    if(AdmUtils.isNotDirtyOrConfirmed()) {
           alert("Wszedem do ifa");
           document.element['selectedKey'].value = $(dropDownID).find(":selected").text();
//         document.element('selectedKey')}.value = dropDownID.options[dropDownID.selectedIndex].value;
       document.forms['selectForm'].submit();
//         #{rich:element('selectForm')}.submit();
         $(document).off('select2:selecting');}
    else {
           alert("Wszedem do else");
        e.preventDefault();
    }
    });
}