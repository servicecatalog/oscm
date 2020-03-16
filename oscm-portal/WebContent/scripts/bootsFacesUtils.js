//  Copyright FUJITSU LIMITED 2020

var BootsFacesUtils = {
	storedForm: []
};

BootsFacesUtils.preventChangeSelectionForDropDown = function() {
    $(document).on('select2:selecting',function (e) {
       if (AdmUtils.isNotDirtyOrConfirmed()) {
           AdmUtils.storeValue(this);
           $(document).off('select2:selecting');
       } else {
           e.preventDefault();
       }
    });
}

BootsFacesUtils.changeSelectionIndexAtDropDown = function(dropDownID) {
    $(document).on('select2:selecting', function (e) {
    if(AdmUtils.isNotDirtyOrConfirmed())
        {#{rich:element('selectedKey')}.value = dropDownID.options[this.selectedIndex].value;
         #{rich:element('selectForm')}.submit();}
         $(document).off('select2:selecting');
    else {
        e.preventDefault();
    }
    });
}