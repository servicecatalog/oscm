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

BootsFacesUtils.preventChangeSelectionForSpecificDropDown = function(item) {
    var itemID = "[id='" + item + "']";
      $(itemID).on('select2:selecting', function (e) {
        if (AdmUtils.isNotDirtyOrConfirmed()) {
           setDirty(false);
        } else {
            e.preventDefault();
        }
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


BootsFacesUtils.focusLabelNextToDropDown = function(tagName) {
    var element = document.getElementsByTagName(tagName);
    AdmUtils.initFocus(element);
});