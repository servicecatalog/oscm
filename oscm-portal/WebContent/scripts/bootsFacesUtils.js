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
    var idDD = "#" + dropDownID;
    $(idDD).on("change", function (e) {
        if(AdmUtils.isNotDirtyOrConfirmed()) {
        var getValue = $(idDD).val();
        alert (getValue);
        $("#selectedKey").val(getValue);
           document.forms["selectForm"].submit();
           $(document).off('select2:selecting');
        } else {
           e.preventDefault();
        }
    });
}