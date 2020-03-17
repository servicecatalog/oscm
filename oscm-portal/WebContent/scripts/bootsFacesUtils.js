//  Copyright FUJITSU LIMITED 2020

var BootsFacesUtils = {
	storedForm: []
};

BootsFacesUtils.preventChangeSelectionForDropDown = function() {
    $(document).on('select2:selecting', 'select2:select', function(e) {
       if (AdmUtils.isNotDirtyOrConfirmed()) {
           $(document).off('select2:selecting', 'select2:select');
       } else {
           e.preventDefault();
       }
    });
}

BootsFacesUtils.changeSelectionIndexAtDropDown = function(element) {
    var i = element.selectedIndex;
	var o = element.options[i];
	var input = document.getElementById("selectForm:selectedKey");
	BootsFacesUtils.preventChangeSelectionForDropDown();

    if (AdmUtils.isNotDirtyOrConfirmed()) {
        if (input != null) {
           input.value = o.value;
        }
        document.getElementById('selectForm').submit();
    }
}