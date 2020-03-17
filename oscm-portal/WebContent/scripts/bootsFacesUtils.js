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

BootsFacesUtils.preventChangeSelectionForDropDown = function(element) {
    var elementID = "[id='" + element + "']";
            alert("Bedzie dobrze");
    $(document).ready(function () {
                alert("Jest dobrze");
     $(elementID).select2().on('select2:selecting', function(e) {
       if (!AdmUtils.isNotDirtyOrConfirmed()) {
           e.preventDefault();
       }
    });
});
}

BootsFacesUtils.changeSelectionIndexAtDropDown = function(element) {
       var i = element.selectedIndex;
       var o = element.options[i];
       var input = document.getElementById("selectForm:selectedKey");
       if (input != null) {
           input.value = o.value;
       }
       document.getElementById('selectForm').submit();
}

BootsFacesUtils.textshowingforme = function(element) {
alert(element);
}