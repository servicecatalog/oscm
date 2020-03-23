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

BootsFacesUtils.focusLabelNextToDropDown = function() {
  var elementClicked = false;
  alert("working");
  $(document).on('select2:opening', function(e) {
    BootsFacesUtils.blurAllDropDown();
	AdmUtils.setFocus(e.target, true);
    elementClicked = true;
  });
  document.addEventListener('click', function() {
	if(!elementClicked) {
      BootsFacesUtils.blurAllDropDown();
    }
    elementClicked = false;
  });
}

BootsFacesUtils.blurAllDropDown = function() {
  var elements = document.getElementsByClassName("select2-selection__rendered");
  for (var i = 0; i < elements.length; i++) {
    AdmUtils.setFocus(elements[i], false);
  }
}