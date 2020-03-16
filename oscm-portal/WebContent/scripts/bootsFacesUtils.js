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

BootsFacesUtils.changeSelectionIndexAtDropDown = function(element) {
    var i = element.selectedIndex;
	var o = element.options[i];
	var input = document.getElementById("selectForm:selectedKey");
	if (AdmUtils.isNotDirtyOrConfirmed()) {
	if (input != null) {
		input.value = o.value;
    }
        alert("key " + input + "menu" + o);
   document.getElementById('selectForm').submit();
        } else {
           e.preventDefault();
        }
}