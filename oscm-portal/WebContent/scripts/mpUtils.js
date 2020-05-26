//  Copyright FUJITSU LIMITED 2020

var MPUtils = {
  storedForm: []
};

MPUtils.showTooltips = function() {
 $(document).ready(function(){
    $('[data-toggle="popover"]').popover({container: 'body'});
 });
}

MPUtils.sessionTab = function(tab) {
 var session = tab ?? "";
  $("#nav-tab").ready(function(){
    if(session.length > 2)
     $(session).tab('show');
  });
}