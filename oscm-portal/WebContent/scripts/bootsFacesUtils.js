//  Copyright FUJITSU LIMITED 2020

var BootsFacesUtils = {
	storedForm: []
};

BootsFacesUtils.preventChangeSelectionForDropDown = function() {
    $(document).on('select2:selecting', function(e) {
       if (AdmUtils.isNotDirtyOrConfirmed()) {
           $(document).off('select2:selecting');
       } else {
           e.preventDefault();
       }
    });
}

BootsFacesUtils.changeSelectionIndexAtDropDown = function(element) {
    var i = element.selectedIndex;
	var o = element.options[i];
	var input = document.getElementById("selectForm:selectedKey");
	var isNotDirty = true;

    document.getElementById(element).on('select2:selecting', function(e) {
       if (AdmUtils.isNotDirtyOrConfirmed()) {
           AdmUtils.storeValue(this);
           document.getElementById(element).off('select2:selecting');
       } else {
           isNotDirty = false;
           e.preventDefault();
       }
    });

    if (isNotDirty) {
        if (input != null) {
           input.value = o.value;
        }
        document.getElementById('selectForm').submit();
    }
}