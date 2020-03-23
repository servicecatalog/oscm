document.addEventListener("DOMContentLoaded", function(){
            var elements = document.getElementsByClassName("select2-selection__rendered");
            var elementClicked = false;
            $(document).on('select2:opening', function(e) {
                elementClicked = true;
                alert("select");
                for (var i = 0; i < elements.length; i++) {
                    AdmUtils.setFocus(elements[i], false);
                }
		        AdmUtils.setFocus(e.target, true);
		    });
            document.addEventListener('click', function() {
                            alert("click");
		         if(!elementClicked) {
                    for (var i = 0; i < elements.length; i++) {
                        AdmUtils.setFocus(elements[i], false);
                    }}
		    });
		 });