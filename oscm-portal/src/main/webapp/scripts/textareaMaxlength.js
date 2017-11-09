/* 
 *  Copyright FUJITSU LIMITED 2017
 */ 
jQuery.fn.maxlength = function(elementId, maxlength){
	// escape : which denotes a pseudo-class in jQuery
	elementId = elementId.replace(':', '\\:');
	jQuery('#'+elementId).keypress(function(event){
        var key = event.which;
        if(key >= 32 || key == 13) {//all keys including return and space(32)
            var length = event.target.value.length;
            if(length >= maxlength) {
                event.preventDefault();
            }
        }
    });
}