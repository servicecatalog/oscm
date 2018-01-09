/* 
 *  Copyright FUJITSU LIMITED 2018
 */ 
var isVisible = isVisible;

function toTop() {
  if (isVisible) {
    // the window was already visible, the only way to bring
    // it to the top is to close it and open it again
    opener.setTimeout('window.open("'+location.href+'", "help", "")', 100);
    window.close();
  }
}

function initHelpPage() {
  var url = location.href;
  var idx = url.indexOf("#");
  if (idx >= 0) {
    var page = "./BES/tasks/" + url.substring(idx+1).replace(/\./g, "_")+".htm";
    contentwin.location.href = page;
  }
  isVisible = true;
}