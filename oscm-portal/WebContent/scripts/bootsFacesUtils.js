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
	var isNotDirty = false;
	$(document).on('select2:selecting', function(e) {
           if (AdmUtils.isNotDirtyOrConfirmed()) {
                isNotDirty = true;
                $(document).off('select2:selecting');
           } else {
               return false;
           }
    });
    if (isNotDirty) {
        if (input != null) {
            input.value = o.value;
        }
        document.getElementById('selectForm').submit();
    }
}