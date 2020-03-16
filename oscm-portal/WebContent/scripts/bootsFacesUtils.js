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
        alert ("change value");
        if(AdmUtils.isNotDirtyOrConfirmed()) {
        var selObj = document.getElementById(dropDownID);
        var selValue = selObj.options[selObj.selectedIndex].value;
        alert ("Value to: " + selValue + " a dropDOwn to: " + dropDownID)
        $("#selectedKey").val(selValue);
        document.forms["selectForm"].submit();
        alert ("w ifie");
        $(document).off('select2:selecting');
        } else {
        alert ("w elsie");
        AdmUtils.restoreValue(dropDownID);
           e.preventDefault();
        }
}