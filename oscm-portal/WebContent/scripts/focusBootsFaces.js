$(document).ready(function () {
  var elements = document.getElementsByClassName("select2-selection__rendered");
  var elementClicked = false;
  $(document).on('select2:opening', function(e) {
    for (var i = 0; i < elements.length; i++) {
      AdmUtils.setFocus(elements[i], false);
    }
    alert("opening");
	AdmUtils.setFocus(e.target, true);
    elementClicked = true;
  });
    document.addEventListener('click', function() {
	  if(!elementClicked) {
        for (var i = 0; i < elements.length; i++) {
           AdmUtils.setFocus(elements[i], false);
        }
      }
      elementClicked = false;
	});
});