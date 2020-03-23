$(document).ready(function () {
  var elements = document.getElementsByClassName("select2-selection__rendered");
  $(document).on('select2:opening', function(e) {
	AdmUtils.setFocus(e.target, true);
  });
    document.addEventListener('click', function() {
        for (var i = 0; i < elements.length; i++) {
           AdmUtils.setFocus(elements[i], false);
        }
	});
});