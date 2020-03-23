$(document).ready(function () {
            var elements = document.getElementsByClassName("select2-selection__rendered");
            var elementClicked = false;
            $(document).on('select2-opening', function(e) {
                elementClicked = true;
                for (var i = 0; i < elements.length; i++) {
                    AdmUtils.setFocus(elements[i], false);
                }
		        AdmUtils.setFocus(e.target, true);
		    });
            document.addEventListener('click', function() {
		         if(!elementClicked) {
                    for (var i = 0; i < elements.length; i++) {
                        AdmUtils.setFocus(elements[i], false);
                    }}
                elementClicked = false;
		    });
		 });