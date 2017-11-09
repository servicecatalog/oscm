/* 
 *  Copyright FUJITSU LIMITED 2017
 */ 
var Localize = {};

//copy hidden translation into field
Localize.translation2Field = function(localeElementId, hiddenElementPrefix, field) {
  var s = Localize.getElement(localeElementId);
  var id = hiddenElementPrefix + s.options[s.selectedIndex].value;
  if (field.disabled) {
    field.disabled = false;
    field.value = Localize.getElement(id).value;
    field.disabled = true;
  } else {
    field.value = Localize.getElement(id).value;
  }
}

//store the field value in the hidden translation 
Localize.field2Translation = function(localeElementId, hiddenElementPrefix, field) {
  var s = Localize.getElement(localeElementId);
  var id = hiddenElementPrefix + s.options[s.selectedIndex].value;
  Localize.getElement(id).value = field.value;
  setDirty(true);
}

//copy hidden translation into editor
Localize.translation2Editor = function(localeElementId, hiddenElementPrefix, editor) {
  var s = Localize.getElement(localeElementId);
  var id = hiddenElementPrefix + s.options[s.selectedIndex].value;
  if (editor) {
    editor.setData(Localize.getElement(id).value);
  }
}

//store the editor content in the hidden translation 
Localize.editor2Translation = function(localeElementId, hiddenElementPrefix, editor) {
  var s = Localize.getElement(localeElementId);
  var id = hiddenElementPrefix + s.options[s.selectedIndex].value;
  if (editor) {
    Localize.getElement(id).value = editor.getData();
  }
  setDirty(true);
}

Localize.getEditor = function(editorId) {
  var f = document.getElementById('localizeForm');
  return CKEDITOR.instances[f.id + ":" + editorId + ":inp"];
}

Localize.getElement = function(id) {
  var f = document.getElementById('localizeForm');
  var eId = f.id + ":" + id;
  return document.getElementById(eId);
}

// initialize the locale select boxes of a localize form 
Localize.init = function(locale) {
  var e=Localize.getElement('srcLocale');
  for(var i=0; i<e.options.length; i++) {
    if(e.options[i].value==locale) {
      e.selectedIndex=i;
    }
  }
  var e=Localize.getElement('trgLocale');
  for(var i=0; i<e.options.length; i++) {
    if(e.options[i].value==locale) {
      if (e.options.length==1) {
        e.selectedIndex=i;
      } else if (e.options.length>i+1) {
        e.selectedIndex=i+1;
      } else {
        e.selectedIndex=i-1;
      }
    }
  }
}