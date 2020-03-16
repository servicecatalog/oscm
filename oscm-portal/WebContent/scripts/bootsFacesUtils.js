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

BootsFacesUtils.changeSelectionIndexAtDropDown = function(e, dropDownID) {
        if(AdmUtils.isNotDirtyOrConfirmed())
                                                      {#{rich:element('selectedKey')}.value=dropDownID.options[dropDownID.selectedIndex].value;
                                                       #{rich:element('selectForm')}.submit();}
                                                    else{
           e.preventDefault();
        }
}