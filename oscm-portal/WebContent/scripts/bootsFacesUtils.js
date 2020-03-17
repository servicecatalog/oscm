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
    alert("Wchodzę do otwartego dropdowna");
    $(document).on('select2:selecting', function(e) {
    alert("wybrano!");
       var i = element.selectedIndex;
       var o = element.options[i];
       var input = document.getElementById("selectForm:selectedKey");
       if (AdmUtils.isNotDirtyOrConfirmed()) {
           alert("zmieniono i czysto!");
           if (input != null) {
                                input.value = o.value;
                             }
                             document.getElementById('selectForm').submit();
           $(document).off('select2:selecting');
       } else {
       alert("zmieniono ale brudno!");
           e.preventDefault();
       }

    });
}