/* 
 *  Copyright FUJITSU LIMITED 2017
 */ 
(function($) {

  /*
   This script applies init focus for the element that cre
   to be found within update node in the response from the server
   This is crucial for elements that are becoming visible after
   some condition changes and are not rendered prior to to
   AJAX request therefore not available in the dom.

   In order to optimize things up this scripts calls
   AdmUtils.initFocus only for the elements that are being
   actually updated [i.e. update note]
  */

  $(function() {
    jsf.ajax.addOnEvent(function partialRenderingUi(data) {
      var status = data.status;

      if(!status){
        return true;
      }

      try{

        switch(status){
          case 'success':
            onSuccess(getIdsToUpdate(data.responseXML));
            break;
        }

      }catch(err){
        console.log('PartialRendering.UI caught exception while processing');
        console.log(err);
      }

    });

    function onSuccess(idsToUpdate){
      var length = idsToUpdate.length;
      while(length--){
        AdmUtils.initFocus(idsToUpdate[length], true);
      }
    }

    function getIdsToUpdate(responseXML) {
      var updates = getUpdateNodes(responseXML),
          length = updates.length,
          arr = [],
          id;

      while(length--){
        id = updates[length].getAttribute('id');
        if(id){
          if(id === 'javax.faces.ViewState'){
            continue;
          }
          arr.push(id.trim ? id.trim() : id );
        }
      }

      return arr;
    }

    function getUpdateNodes(responseXML){
      return responseXML.getElementsByTagName("update");
    }
  })
}(window.jQuery));