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

BootsFacesUtils.changeSelectionIndexAtDropDown = function(event, element) {
    event = event ? event : window.event;
    var i = element.selectedIndex;
	var o = element.options[i];
	var input = document.getElementById("selectForm:selectedKey");
	var isDirty = false
	$(document).on('select2:selecting', function(event) {
           if (AdmUtils.isNotDirtyOrConfirmed()) {
                $(document).off('select2:selecting');
           } else {
               isDirty = true;
               event.preventDefault();
           }
    });
    if (!isDirty) {
        if (input != null) {
            input.value = o.value;
        }
        document.getElementById('selectForm').submit();
    }
}