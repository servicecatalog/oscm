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
alert ("change value");
    $(document).on('select2:selecting', function (e) {
    var nonnsa = "#" + dropDownID;
    var selValue = $(nonnsa).val();
                    alert ("Value to: " + selValue + " a dropDOwn to: " + dropDownID)
                        document.getElementById("selectedKey").value=selValue;
                       document.forms["selectForm"].submit();
        if(AdmUtils.isNotDirtyOrConfirmed()) {
alert ("w ifie");
           $(document).off('select2:selecting');
        } else {
        alert ("w elsie");
        AdmUtils.restoreValue(dropDownID);
           e.preventDefault();
        }
    });
}