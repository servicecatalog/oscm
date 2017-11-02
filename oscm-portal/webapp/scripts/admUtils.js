/* 
 *  Copyright FUJITSU LIMITED 2017
 */ 
var AdmUtils = {
	storedForm: []
};

var storedValue = "";
var selectedRow = null;
var savedSelectClassName = null;
var savedOverClassName = null;

var resizeCallback = null;
var focusButtonDivId = null;
var shownHint = null;

var loginPanelDirty = false;
var loginRedirectTarget = null;
var loginContextPath = null;

if (typeof Richfaces !== "undefined" && typeof ModalPanel !== "undefined"
		&& document.URL.indexOf('/marketplace/') === -1) {
	// animate opening of modal panels using jquery
	jQuery.easing.fxSpring = function(p) {
		return -3 * (p * p * p * p) + 2 * (p * p * p) + 2 * (p * p);
	}

	jQuery.easing.easeInOut = function(p) {
		return Math.sin(1.57079633 * p);
	}

	var mpShow = ModalPanel.prototype.show;

	// override the ModalPanel.show prototype and inject animation
	ModalPanel.prototype.show = function() {
		var panel = jQuery(this.getSizedElement());
		mpShow.apply(this, Array.prototype.slice.call(arguments, [ 1 ]));
		var w = panel.width(), h = panel.height(), l = panel.position().left, t = panel
				.position().top, x = l + (w / 2), y = t + (h / 2);
		panel.parent().css('opacity', 0);
		panel.parent().animate({
			opacity : 1
		}, 800, 'easeInOut');

		panel.css({
			width : "10px",
			height : "25px",
			overflow : 'hidden',
			left : x - 5,
			top : y
		});

		panel.animate({
			width : w,
			left : x - (w / 2)
		}, {
			duration : 250,
			easing : 'easeInOut',
			queue : false
		});

		panel.animate({
			height : h,
			top : y - (h / 2)
		}, {
			duration : 250,
			easing : 'easeInOut',
			queue : false
		});
	};
}

AdmUtils.blurButton = function(buttonId) {
	var cfgButton = document.getElementById(buttonId);
	cfgButton.style.cursor='wait';
	cfgButton.style.opacity='0.4';
	cfgButton.style.filter='alpha(opacity=40)';
}

AdmUtils.storeValue = function(select) {
	storedValue = select.value;
}

AdmUtils.restoreValue = function(select) {
	select.value = storedValue;
}

AdmUtils.isNotDirtyOrConfirmed = function() {
	if (window.onbeforeunload != null && !confirm(getUnloadMessage())) {
		return false;
	}
	setDirty(false);
	return true;
}

AdmUtils.userIdClicked = function(event) {
	if (event.ctrlKey) {
		window.resizeTo(1024, 738);
	}
	return false;
}

AdmUtils.showProgress = function(flag) {
	var e = document.getElementById("progressDiv");
	if (e) {
		if (flag) {
			e.style.display = "";
			document.body.style.cursor = "wait";
		} else {
			e.style.display = "none";
			document.body.style.cursor = "auto";
		}
	}
	return false;
}

AdmUtils.updateOrganizationIdIfNeeded = function(orgIdField, storedOrgId) {
	if (storedOrgId && storedOrgId.length > 0
			&& storedOrgId != orgIdField.value) {
		var form = orgIdField.form;
		form.submit();
		return true;
	}
	return false;
}

AdmUtils.selectRowNum = function(tableId, idx) {
	if (idx < 0) {
		return;
	}
	var e = document.getElementById(tableId);
	if (e == null || e.childNodes == null) {
		return;
	}
	AdmUtils.selectRow(e.childNodes[2].rows[idx]);
}

AdmUtils.selectRow = function(row) {
	if (selectedRow) {
		jQuery(selectedRow).removeClass("rowSelected");
	}
	selectedRow = row;
	jQuery(selectedRow).addClass("rowSelected");
}

AdmUtils.deselectRow = function() {
	if (selectedRow) {
		jQuery(selectedRow).removeClass("rowSelected");
	}
	selectedRow = null;
}

AdmUtils.mouseOverRow = function(row) {
	if (row.className.indexOf("rowSelected") < 0) {
		jQuery(row).addClass("rowOver");
	}
}

AdmUtils.mouseOutRow = function(row) {
	jQuery(row).removeClass("rowOver");
}

AdmUtils.mouseOverRowCell = function(row) {
	if (row.className.indexOf("rowSelected") < 0) {
		jQuery(row).addClass("rowCellOver");
	}
}

AdmUtils.mouseOutRowCell = function(row) {
	jQuery(row).removeClass("rowCellOver");
}

AdmUtils.mouseOverCell = function(cell) {
	var e = cell;
	while (e && e.nodeName.toUpperCase() != "TD") {
		e = e.parentNode;
	}
	cell = e;
	if (cell.className.indexOf("rowSelected") < 0) {
		savedCellOverClassName = cell.className;
		cell.className = savedCellOverClassName + " rowOver";
	}
}

AdmUtils.mouseOutCell = function(cell) {
	var e = cell;
	while (e && e.nodeName.toUpperCase() != "TD") {
		e = e.parentNode;
	}
	cell = e;
	if (cell.className.indexOf("rowSelected") < 0) {
		cell.className = savedCellOverClassName;
	}
}

AdmUtils.initResize = function(eNavPanel, callback) {
	AdmUtils.eNavPanel = eNavPanel;
	AdmUtils.resizeCallback = callback;
}

AdmUtils.resizeDelayed = function() {
	if (AdmUtils.timeout) {
		clearTimeout(AdmUtils.timeout);
		AdmUtils.timeout = null;
	}
	AdmUtils.timeout = setTimeout(AdmUtils.resize, 1000);
}

AdmUtils.resize = function() {
	var h = AdmUtils.eNavPanel.offsetHeight;
	if (h < 10) {
		h = 10; // min height
	}
	if (AdmUtils.resizeCallback != null) {
		AdmUtils.resizeCallback(h);
	}
}

AdmUtils.ie7UploadValidation = function(field) {
	if (AdmUtils.isIE() && AdmUtils.getIEVersion() == 7.0 && field != null
			&& field.value.length > 0) {
		var idx = field.value.indexOf("\\");
		if (idx != 0 && !(field.value.charAt(1) == ":" && idx == 2)) {
			alert(getIe7UploadValidationMessage());
			field.focus();
			AdmUtils.showProgress(false);
			setOnSubmit(false);
			return false;
		}
	}
	return true;
}

AdmUtils.getIEVersion = function() {
	var version = -1;
	if (navigator.appVersion.indexOf("MSIE") != -1) {
		version = parseFloat(navigator.appVersion.split("MSIE")[1]);
		if (typeof (document.documentMode) != 'undefined' && version < 8) {
			version = 8;
		}
	}
	return version;
}

AdmUtils.isIE = function() {
	return document.all != null;
}

AdmUtils.isFirefoxOrChrome = function() {
	return navigator.userAgent.indexOf("Firefox") != -1
			|| navigator.userAgent.indexOf("Chrome") != -1;
}

AdmUtils.isFirefox11 = function() {
	var version = -1;
	if (navigator.userAgent.indexOf("Firefox") != -1) {
		version = parseFloat(navigator.userAgent.split("Firefox/")[1]);
		if (version == 11) {
			return true;
		}
	}
	return false;
}

AdmUtils.observe = function(element, eventName, handler) {
  element = $(element);
  if(!(element.length && handler && (eventName && eventName.length > 0))){
    return;
  }
  return element.bind(eventName, handler);
}

AdmUtils.stopObserving = function(element, eventName, handler) {
  element = $(element);
  if(!(element.length && handler && (eventName && eventName.length > 0))){
    return;
  }
  return element.unbind(eventName, handler);
}

AdmUtils.stopEvent = function(event) {
	if (typeof (Event) != 'undefined' && typeof (Event.stop) == 'function') {
		Event.stop(event);
	} else {
		if (event.preventDefault) {
			event.preventDefault();
		}
		if (event.stopPropagation) {
			event.stopPropagation();
		}
		event.stopped = true;
	}
}

AdmUtils.preventDefaultSubmitOnEnter = function() {
	if (document.addEventListener) {
		document.addEventListener('keydown', AdmUtils.preventDefaultSubmit,
				false);
	} else if (document.attachEvent) {
		document.attachEvent('onkeydown', AdmUtils.preventDefaultSubmit);
	}
}

AdmUtils.preventDefaultSubmit = function(event) {
	event = event ? event : window.event;
	var k = event.keyCode ? event.keyCode : event.which ? event.which : null;
	if (k == 13) {
		var eventTarget = false;
		if (event.target) {
			eventTarget = event.target.type;
		} else if (event.srcElement) {
			eventTarget = event.srcElement.type;
		}

		if (eventTarget) {
			if (eventTarget == 'text' || eventTarget == 'password'
					|| eventTarget == 'checkbox' || eventTarget == 'radio') {
				event.preventDefault ? event.preventDefault()
						: event.returnValue = false;
			}
		}
	}
	return true;
}

AdmUtils.makeNavResizeable = function(dragElement, navPanel, resizePanel,
		contentPanel, callback) {
	AdmUtils.observe(dragElement, "mousedown",
			function(event) {
				AdmUtils.resizeNav(event, navPanel, resizePanel, contentPanel,
						callback);
			});
}

AdmUtils.pageTo = function(target, value) {
	var pageValue = parseInt(jQuery(value).val());
	if (isNaN(pageValue)) {
		pageValue = 0;
	}
	RichFaces.component(target).switchToPage(pageValue);
}

AdmUtils.resizeNav = function(event, navPanel, resizePanel, contentPanel,
		callback) {
	startX = event.clientX;
	origWidth = parseInt(navPanel.style.width);
	AdmUtils.observe(document, "mousemove", mouseMove);
	AdmUtils.observe(document, "mouseup", mouseUp);
	AdmUtils.stopEvent(event);

	function mouseMove(event) {
		var w = origWidth + event.clientX - startX;
		if (w > 0) {
			navPanel.style.width = w + "px";
		}
		AdmUtils.stopEvent(event);
	}

	function mouseUp(event) {
		AdmUtils.stopObserving(document, "mousemove", mouseMove);
		AdmUtils.stopObserving(document, "mouseup", mouseUp);
		if (callback) {
			callback(origWidth + event.clientX - startX);
		}
		AdmUtils.stopEvent(event);
	}
}

AdmUtils.setFrameHeight = function() {
	var $main =  jQuery("#mainContainer");
	
	if($main.length === 0) return;

	var mainOff = jQuery("#mainContainer").position().top;

	var docHeight = jQuery(window).height() - 3;
	var boxDeltaY = jQuery("#body_boxMain").offset().top - mainOff;
	var boxHeight = jQuery("#body_boxMain").height();
	jQuery("#body_boxMain").height(docHeight - boxDeltaY);

	boxDeltaY = jQuery("#navPanel #menu").offset().top;
	boxHeight = jQuery("#navPanel  #menu").height();
	jQuery("#navPanel #menu").height(docHeight - boxDeltaY);

	boxDeltaY = jQuery("#resizePanel").offset().top;
	boxHeight = jQuery("#resizePanel").height();
	jQuery("#resizePanel").height(docHeight - boxDeltaY);

	jQuery().ready(
			function() {
				try{
					if (document.activeElement) {
						// jump to element with focus, if available
						var $active = jQuery(document.activeElement);
						var $boxMain = jQuery("#body_boxMain");
						if ($active && $boxMain) {
							var toFocus = $active.offset().top - $boxMain.offset().top;
							$boxMain.scrollTop(toFocus);
						}
					}
				} catch(e){
					//
				}
			});
}

AdmUtils.makeDraggable = function(dragElement, dragWindow) {
	AdmUtils.observe(dragElement, "mousedown", function(event) {
		AdmUtils.drag(event, dragWindow);
	});
}

AdmUtils.setFirstStepHeader = function() {
	jQuery('.body_box1 .step:first').css('margin-top', '0px');
	var $body_box2 = jQuery("#body_box2");
	if($body_box2) jQuery('.step:first',$body_box2).css('margin-top', '0px');
}

AdmUtils.initUploadPreview = function() {

    jQuery('.fileChooser').bind('change', (function(File, FileReader){
        if(!File && !FileReader){
            return noopOnChange;
        }
        return onChange;
    }(window.File, window.FileReader)));

    function noopOnChange(){
    }

    function onChange(w, h) {
        var reader = new FileReader(),
            ix = $('.fileChooser').index($(this)),
            $preview = $(".previewImage:eq(" + ix + ")");

        reader.onload = function onFileLoaded(evt) {

            var img = new Image(),
                previewTarget = evt.target.result;

            img.onload = function onImageLoaded(evt) {
                var width = this.width,
                    height = this.height,
                    $preContainer = $(".imagePreview:eq(" + ix + ")"),
                    preview = eval($preContainer.attr("data-size"));

                // drop out if preview image sizing
                // function is undefined
                if (typeof preview === "undefined"){
                    return;
                }

                // check the height of the image
                if (preview.apply(this, [ width, height ])) {

                    $preContainer
                            .css("width", width + "px")
                            .css("height", height + "px");

                    $preview.attr("src", previewTarget)
                            .css("position", "absolute")
                            .fadeIn(600);

                    // remove the no image defined text, if available
                    var $noImage = $(".noImage", $preContainer);
                    if ($noImage){
                        $noImage.css("display", "none");
                    }

                    // expand the container to match the  image size
                    $preContainer.animate({
                        width : width,
                        height : height
                    });
                } else {
                    // reject image if incorrect size
                    alert($(".imagePreview:eq(" + ix + ")").attr("data-error"));
                }
            };
            img.src = evt.target.result;
        }

        reader.readAsDataURL(this.files[0]);
    }
}

AdmUtils.setUIElements = function(browseFieldOnly) {
	// browseFieldOnly toggle used for marketplace
	//	where the other elements are not affected
	if(browseFieldOnly){
		// set image upload previews
		jQuery('.fileChooser').unbind('change');
		AdmUtils.setUploadBoxes();
	} else{
		// script to set form highlights
		AdmUtils.setFormRollovers();
		// skinning of drop down selections
		AdmUtils.setSelectBoxes();

		jQuery('.fileChooser').unbind('change');
		// skinning of upload boxes
		
		AdmUtils.setUploadBoxes();
		// set image upload previews
		AdmUtils.initUploadPreview();

		// align first step header to top of box
		AdmUtils.setFirstStepHeader();
		// set table header highlight on selection
		AdmUtils.highlightTableHeader();

		AdmUtils.centerTableCheckboxes();
		
		AdmUtils.setMenuPosition();
	}

};

AdmUtils.setInfoPanel = function(panelId) {

	var infoPanel = document.getElementById(panelId);
	if (!infoPanel) {
		return;
	}
	if (infoPanel.childNodes.length > 0) {
		infoPanel.style.display = "block";
	} else {
		infoPanel.style.display = "none";
	}
	return;
};

AdmUtils.highlightNavigation = function(nav) {
	jQuery("#" + nav).parents("li").addClass("selected");
}

AdmUtils.centerTableCheckboxes = function() {
	var $rows = jQuery(".rich-table th:first-child");
	var td;
	$rows.each(function() {
		td = jQuery('input[type="checkbox"]', jQuery(this));
		if (td.length > 0) {
			jQuery(this).css('text-align', 'center');
		}
	});
}

AdmUtils.setCheckBoxClicks = function() {
	var $rows = jQuery(".rich-table tr");
	$rows.each(function() {
		jQuery(this).unbind("click");
		jQuery(this).click(function(evt) {
			var $boxes, $tar;
			$tar = jQuery(evt.target);
			if ($tar.is('input')) return;
			
			$boxes = jQuery("input[type='checkbox']", jQuery(this));
			if($boxes.length > 1) return;
			$boxes.each(function(ix, val) {
				this.click();
			});
		});
	});
}

AdmUtils.setFormRollovers = function() {
	var $col = jQuery(".labelAndValuePanel .labelAndValuePanelCol2");
	var $parent;
	$col.each(function() {
		if (jQuery("input,select,textarea", this).attr("disabled"))
			return;
		if (jQuery("*", this).hasClass("disabled"))
			return;
		jQuery(this).unbind('mouseenter mouseleave').hover(
				function() {
					$parent = jQuery(this).parents(".labelAndValuePanel tr");
					jQuery(".labelAndValuePanelCol1 .label", $parent).addClass(
							"labelRoll");
				},
				function() {
					$parent = jQuery(this).parents(".labelAndValuePanel tr");
					jQuery(".labelAndValuePanelCol1 .label", $parent)
							.removeClass("labelRoll");
				});
	});
}

AdmUtils.setUploadBoxes = function() {
	var t;
	var $sel = jQuery('input[type="file"]');
	$sel.each(function() {
		var dis = jQuery(this).attr("disabled");
		var t = jQuery(this).parents(".jqUpload");
		var w = jQuery('.fileChooser', t).width()
		/*
		 * t.css("width",w+"px"); t.css("max-width",w+"px");
		 */
		if (dis) {
			t = jQuery(this).parent();
			jQuery('span.option', t).addClass('disabled');
			jQuery('span.upload', t).addClass('disabled');
		} else {
			jQuery(this).unbind('change', 'mouseover', 'mouseleave');
			jQuery(this).bind('change', function() {
				var val = jQuery(this).val();
				t = jQuery(this).parent();
				jQuery('.output', t).text(val);
			}).hover(function() {
				t = jQuery(this).parent();
				jQuery('span.option', t).addClass('hover');
				jQuery('span.upload', t).addClass('hover');
			}, function() {
				t = jQuery(this).parent();
				jQuery('span.option', t).removeClass('hover');
				jQuery('span.upload', t).removeClass('hover');
			})
		}
	});
}

AdmUtils.setSelectBoxes = function() {
	var t;
	var $sel = jQuery('select').not('.pager select');
	$sel.each(function() {
		var title = jQuery(this).attr('title');
		title = jQuery('option:selected', this).text();

		t = jQuery(this).parent();
		jQuery('span.option', t).text(title);

		var dis = jQuery(this).attr("disabled");
		if (dis) {
			t = jQuery(this).parent();
			jQuery('span.option', t).addClass('disabled');
			jQuery('span.select', t).addClass('disabled');
		} else {
			jQuery(this).unbind('change', 'mouseover', 'mouseleave');
			jQuery(this).bind('change', function() {
				var val = jQuery('option:selected', this).text();
				t = jQuery(this).parent();
				jQuery('span.option', t).text(val);
			}).hover(function() {
				t = jQuery(this).parent();
				jQuery('span.option', t).addClass('hover');
				jQuery('span.select', t).addClass('hover');
			}, function() {
				t = jQuery(this).parent();
				jQuery('span.option', t).removeClass('hover');
				jQuery('span.select', t).removeClass('hover');
			})
		}
	});

}

AdmUtils.highlightTableHeader = function() {
	var $s;
	// clear all backgrounds
	jQuery('.rowTitle th').removeClass('tableColumnSelected');
	$s = jQuery('.rowTitle th img[src$="sortAscending"]');
	// set ascending backgrounds
	if ($s.length > 0)
		jQuery.each($s, function(i) {
			jQuery(this).parents(".rowTitle th")
					.addClass('tableColumnSelected');
		});
	// set descending backgrounds
	$s = jQuery('.rowTitle th img[src$="sortDescending"]');
	if ($s.length > 0)
		jQuery.each($s, function(i) {
			jQuery(this).parents(".rowTitle th")
					.addClass('tableColumnSelected');
		})
};

AdmUtils.drag = function(event, dragWindow) {
	startX = event.clientX;
	startY = event.clientY;
	origLeft = dragWindow.offsetLeft;
	origTop = dragWindow.offsetTop;
	AdmUtils.observe(document, "mousemove", mouseMove);
	AdmUtils.observe(document, "mouseup", mouseUp);
	AdmUtils.stopEvent(event);

	function mouseMove(event) {
		dragWindow.style.left = (event.clientX - startX + origLeft) + "px";
		dragWindow.style.top = (event.clientY - startY + origTop) + "px";
		AdmUtils.stopEvent(event);
	}

	function mouseUp(event) {
		AdmUtils.stopObserving(document, "mousemove", mouseMove);
		AdmUtils.stopObserving(document, "mouseup", mouseUp);
		AdmUtils.stopEvent(event);
	}
}

AdmUtils.getWindowWidth = function() {
	if (window.innerWidth) {
		return window.innerWidth;
	} else if (document.documentElement && document.documentElement.clientWidth) {
		return document.documentElement.clientWidth;
	} else if (document.body && document.body.clientWidth) {
		return document.body.clientWidth;
	}
	return 0;
}

AdmUtils.getWindowHeight = function() {
	if (window.innerHeight) {
		return window.innerHeight;
	} else if (document.documentElement
			&& document.documentElement.clientHeight) {
		return document.documentElement.clientHeight;
	} else if (document.body && document.body.clientHeight) {
		return document.body.clientHeight;
	}
	return 0;
}

AdmUtils.getDocumentHeight = function() {
	var D = document;
	return Math.max(Math.max(D.body.scrollHeight,
			D.documentElement.scrollHeight), Math.max(D.body.offsetHeight,
			D.documentElement.offsetHeight), Math.max(D.body.clientHeight,
			D.documentElement.clientHeight));
}

AdmUtils.showErrorPanel = function(flag) {
	AdmUtils.showPanel(flag, "errorPanel");
}

AdmUtils.showPanel = function(flag, panelId) {
	var errorPanel = document.getElementById(panelId);
	if (!errorPanel) {
		return;
	}
	if (flag) {
		errorPanel.style.display = "block";
	} else {
		errorPanel.style.display = "none";
	}
	return;

	/* use the following statement to make the error panel draggable
	var px = "px";
	var left = 0;
	var top = 0;
	var width = 400;
	if (AdmUtils.getWindowWidth() > width) {
		left = (AdmUtils.getWindowWidth() - width) / 2
	} else {
		width = AdmUtils.getWindowWidth();
	}
	top = 2;
	errorPanel.style.left = left + px;
	errorPanel.style.top = top + px;
	errorPanel.style.width = width + px;
	AdmUtils.makeDraggable(document.getElementById("errorPanel_header"),
			errorPanel);
			*/
}

AdmUtils.showModalErrorPanel = function(dialogId, width) {
	var errorPanel = document.getElementById(dialogId + "modalErrorPanel");
	if (!errorPanel) {
		return;
	}
	var newWidth = width + 50;
	Richfaces.hideModalPanel(dialogId);
	RichFaces.$(dialogId, {
		width : newWidth
	}).show();
	return;
}


AdmUtils.clearModalErrorPanel = function(errorPanelId) {
    var errorPanel = document.getElementById(errorPanelId);
    if (errorPanel) {
        errorPanel.style.display = "none";
        errorPanel.innerHTML = "";
    }
}
AdmUtils.clearMsgrPanel = function() {
    var infoPanel = document.getElementById("infoMessages");
    if (infoPanel) {
    	infoPanel.style.display = "none";
    	infoPanel.innerHTML = "";
    }
    var errorPanel = document.getElementById("errorPanel");
    if (errorPanel) {
        errorPanel.style.display = "none";
        errorPanel.innerHTML = "";
    }
}
AdmUtils.clearHiddenErrorPanel = function() {
    var errorPanel = document.getElementById("hiddenErrorPanel");
    if (errorPanel) {
        errorPanel.style.display = "none";
        errorPanel.innerHTML = "";
    }
}
AdmUtils.moveChildren = function(src, trg) {
	if (src == null || trg == null || trg.childNodes.length > 0) {
		return;
	}
	for ( var i = src.childNodes.length - 1; i >= 0; i--) {
		trg.appendChild(src.childNodes[i]);
	}
	if (src.style) {
		src.style.display = "";
	}
}

AdmUtils.moveChildrenForDifferentBrowser = function(src, trg) {
	if (src == null || trg == null ||AdmUtils.isErrorMsgAdded(trg)) {
		return;
	}
	for ( var i = src.childNodes.length - 1; i >= 0; i--) {
		trg.appendChild(src.childNodes[i]);
	}
	if (src.style) {
		src.style.display = "";
	}
}

AdmUtils.moveChildrenForDifferentBrowserWithRefresh = function(src, trg, info, flag) {
    if (src == null || trg == null) {
        return;
	}
	
    if (flag){
        AdmUtils.removeChildren(trg);
	    AdmUtils.removeChildren(info);
	    for ( var i = src.childNodes.length - 1; i >= 0; i--) {
    		trg.appendChild(src.childNodes[i]);
    	}
    }
    
	if (src.style) {
		src.style.display = "";
	}
}

AdmUtils.isErrorMsgAdded = function(trg){
	if(trg.childNodes.length > 0){
		for ( var i = trg.childNodes.length - 1; i >= 0; i--) {
			if(trg.childNodes[i].nodeName=="DL"){
				return true;
			}
		}
	}
}

AdmUtils.removeChildren = function(parent) {
	if (parent == null) {
		return;
	}
	while (parent.childNodes.length > 0) {
		parent.removeChild(parent.lastChild);
	}
}

AdmUtils.removePanelChildren = function(parent,flag) {
	if (parent == null) {
		return;
	}
	while (parent.childNodes.length > 0 && flag) {
		parent.removeChild(parent.lastChild);
	}
}

AdmUtils.findRow = function(prefix, colId, colText, size, callback) {
		for ( var i = 0; i < size;  i++){
			var e = document.getElementById(prefix + i + ":" + colId);	
			if(e != null && e.innerHTML == colText){
				while (e && e.nodeName.toUpperCase() != "TR"){
					e = e.parentNode;
				}
				if (e){
					callback(e);
				}
			}
		}
}

AdmUtils.findRowForDataList = function(prefix, col1Id, col1Text, col2Id, col2Text, size, callback) {
	for ( var i = 0; i < size ; i++) {
		var e1 = document.getElementById(prefix + i + ":" + col1Id);
		var e2 = document.getElementById(prefix + i + ":" + col2Id);
		if(e1 != null || e2 != null){
			if (e1.innerHTML == col1Text && e2.innerHTML == col2Text) {
				while ((e1 && e1.nodeName.toUpperCase() != "TR")&&(e2 && e2.nodeName.toUpperCase() != "TR")) {
					e1 = e1.parentNode;
					e2 = e2.parentNode;
				}
				if (e1 || e2) {
					callback(e1);
					callback(e2);
				}
			}
		}
	}
}

AdmUtils.showHelp = function(baseUrl, locale, contextId) {
	var page = baseUrl + "-help/help/" + locale + "/help/tasks/"
			+ contextId.replace(/\./g, "_") + ".htm";
	
	var defaulePage = baseUrl + "-help/help/en/help/tasks/"
			+ contextId.replace(/\./g, "_") + ".htm";

	if(AdmUtils.testHttpConnection(page)){
		AdmUtils.openWindow(page);
		return true;
	}else if(AdmUtils.testHttpConnection(defaulePage)){
		AdmUtils.openWindow(defaulePage);
		return true
	}
	return false;
}

AdmUtils.openWindow = function(url){
	var help = window.open("", "help", "");
	if (help) {
		try {
			if (help.isVisible) {
				// the window was already visible, the only way to bring
				// it to the top is to close it and open it again
				help.close();
				help = window.open(url, "help", "");
			} else {
				help.location.href = url;
			}
		} catch (e) {
			help.close();
			help = window.open(url, "help", "");
		}
		help.isVisible = true;
	}
	return false;
}

AdmUtils.showFaq = function(baseUrl, locale) {
	var faq = baseUrl + "-help/faq/" + locale + "/faq.html";
	var defaultfaq = baseUrl + "-help/faq/en/faq.html";
	if(AdmUtils.testHttpConnection(faq)){
		AdmUtils.openWindow(faq);
		return true;
	}else if(AdmUtils.testHttpConnection(defaultfaq)){
		AdmUtils.openWindow(defaultfaq);
		return true
	}
	return false;
}

AdmUtils.submitOnReturn = function(e, f) {
	var evt = e || window.event;
	if (!evt) {
		return;
	}
	if (evt.keyCode == 13) {
		f.submit();
	}
}

AdmUtils.clickOnReturn = function(e, id) {
	var evt = e || window.event;
	if (!evt) {
		return;
	}
	if (evt.keyCode == 13) {
		AdmUtils.executeOnClick(id);
	}
}

AdmUtils.executeOnClick = function(id) {
	var element = document.getElementById(id);
	if (element) {
		if (typeof element.onclick == "function") {
			element["onclick"]();
		}
	}
}

AdmUtils.selectElement = function(e) {
	e.focus();
	setTimeout("document.getElementById('" + e.id + "').select();", 300);
}

AdmUtils.focusElementById = function(id) {
	if (id)
	{
		var elm = document.getElementById(id);
		if (elm) {
			try {
				elm.focus();
			} catch (e) {/* if the element cannot get the focus, ignore it */
			}
		}
	}
}

// submit only hidden fields, submit fields and the given field of the form
AdmUtils.submitElement = function(form, element) {
	for ( var i = 0; i < form.elements.length; i++) {
		var type = form.elements[i].type;
		if (form.elements[i] != element && type != 'hidden' && type != 'submit') {
			form.elements[i].disabled = true;
		}
	}
	form.submit();
	return false;
}

AdmUtils.resetSelect = function(element) {
	for ( var i = 0; i < element.length; i++) {
		if (element.options[i].defaultSelected) {
			element.options[i].selected = true;
			break;
		}
	}
}

AdmUtils.suggestUserId = function(e1, e2) {
	if (e2.value == null || e2.value.length == 0) {
		e2.value = e1.value;
	}
}

AdmUtils.setHighlights = function(str) {
	if (str.length < 3) {
		return;
	}
	str = str.substring(1, str.length - 1);
	var ids = str.split(",");
	for ( var i = 0; i < ids.length; i++) {
		AdmUtils.setHighlight(AdmUtils.trim(ids[i]));
	}
	var element = document.getElementById(ids[0]);
	if (element != null) {
		element.focus();
	}
}

AdmUtils.setHighlight = function(id) {
	var element = document.getElementById(id);
	if (element != null) {
		if (element.type == 'checkbox') {
			element.style.outline = '2px solid #A00000';
		} else {
			element.style.border = '2px solid #A00000';
		}
	}
}

AdmUtils.removeHighlight = function(element) {
	if (element) {
		if (element.type == 'checkbox') {
			element.style.outline = '';
		} else {
			element.style.border = '';
		}
	}
}

// cut leading and trailing whitespace
AdmUtils.trim = function(str) {
	return str.replace(/^\s+|\s+$/g, "");
}

AdmUtils.hideMessages = function() {
	// remove error panel if present
	var el = document.getElementById("errorPanel");
	if (el != null) {
		el.style.display = 'none';
	}
	// remove the success message if present
	el = document.getElementById("infoPanel");
	if (el != null) {
		el.style.display = 'none';
	}
	AdmUtils.setFrameHeight();
}

AdmUtils.updateButton = function(table, buttonEnabled, buttonDisabled) {
	var result = AdmUtils.isTableRowChecked(table);
	if (result) {
		buttonEnabled.style.display = 'block';
		buttonDisabled.style.display = 'none';
	} else {
		buttonEnabled.style.display = 'none';
		buttonDisabled.style.display = 'block';
	}
}

AdmUtils.isTableRowChecked = function(table) {
	var result = false;
	if (table != null) {
		var body = table.tBodies[0];
		if (body) {
			var rows = body.rows;
			for ( var i = 0; i < rows.length && !result; i++) {
				result = rows[i].cells[0].firstChild.checked;
			}
		}
	}
	return result;
}

AdmUtils.setAllCheckboxes = function(prefix, id, value, dirty) {
	setDirty(dirty);
	var num = 0;
	var field;
	while ((field = document.getElementById(prefix + ":" + num + ":" + id)) != null) {
		field.checked = value;
		num++;
	}
}

AdmUtils.setFocusBackgroundColor = function(focusBackgroundColor) {
	AdmUtils.focusBackgroundColor = focusBackgroundColor;
}

AdmUtils.isNoDymanicHighlighting = function(e) {
	if (jQuery(e).hasClass('ndh')) {
		return true;
	}
	// Search for the first parent element with the
	// marker class "ndh" (No Dynamic Highlighting)
	return (jQuery(e).parents('.ndh:first').length) ? true : false;
}

AdmUtils.setFocus = function(e, flag) {
	if (AdmUtils.isNoDymanicHighlighting(e)) {
		return;
	}
	e = AdmUtils.getParentElementWithTagName(e, "tr");
	if (e && typeof (e.style) != 'undefined') {
		if (flag) {
			// e.style.backgroundColor = AdmUtils.focusBackgroundColor;
			jQuery(e).addClass('formHighlight');
			if (focusButtonDivId) {
				var div = document.getElementById(focusButtonDivId);
				if (div) {
					div.style.display = 'none'
				}
			}
			focusButtonDivId = null;
			if (e.lastChild && e.lastChild.firstChild
					&& e.lastChild.firstChild.id
					&& e.lastChild.firstChild.id.indexOf(':focus') >= 0) {
				// display special focus buttons
				e.lastChild.firstChild.style.display = 'block';
			}
		} else {
			// e.style.backgroundColor = '';
			jQuery(e).removeClass('formHighlight');
			if (e.lastChild && e.lastChild.firstChild
					&& e.lastChild.firstChild.id
					&& e.lastChild.firstChild.id.indexOf(':focus') >= 0) {
				// hide special focus buttons with a short delay (so that a
				// click is
				// possible)
				focusButtonDivId = e.lastChild.firstChild.id;
				setTimeout(
						"if (focusButtonDivId != null ) { var div = document.getElementById(focusButtonDivId); if (div) div.style.display = 'none'; }",
						300);
			}
		}
	}
}

AdmUtils.getParentElementWithTagName = function(e, tagName) {
	while (e && typeof (e.tagName) != 'undefined'
			&& e.tagName.toLowerCase() != tagName) {
		e = e.parentNode;
	}
	return e;
}

AdmUtils.initFocusAfterDomUpdate = function(req, event, data) {
	var idsFromResponse = req.getResponseHeader("Ajax-Update-Ids");
	if (!idsFromResponse || idsFromResponse.length < 1) {
		return;
	}

	var idsFromResponseArray = idsFromResponse.split(',');
	var idCount = idsFromResponseArray.length;
	if (idCount < 1) {
		return;
	}

	for ( var i = 0; i < idCount; i++) {
		var id = idsFromResponseArray[i];
		AdmUtils.initFocus(id);
	}

	// Refresh the selection to make sure the newly added highlighting takes
	// effect
	if (!AdmUtils.isIE()) {
		var focusElement = jQuery(':focus');
		if (focusElement.length > 0) {
			AdmUtils.setFocus(focusElement.get(0), true);
		}
	}
}

AdmUtils.initFocus = function(parentId) {
  var parent,
      partial = (function(args){
        return args.length === 2 && args[1] === true;
      }(arguments)),
      elsToCheck = ['input','select','textarea'],
      elsToCheckLength = elsToCheck.length;

  if (!parentId || typeof (parentId) != 'string') {
    parent = $(document);
  } else {
    parent = $('[id="' + parentId + '"]');
  }

  if(!parent.length){
    return;
  }

  while(elsToCheckLength--){
    parent.find(elsToCheck[elsToCheckLength])
      .focus(function() {
        AdmUtils.setFocus(this, true);
      })
      .blur(function() {
        AdmUtils.setFocus(this, false);
      })
  }

  parent = partial ?
    parent.find('div[id$="focusButtons"] > span > a') :
    $('div[id$="focusButtons"] > span > a');
  parent
    .focus(function() {
      AdmUtils.setFocus(this, true);
    })
    .blur(function() {
      AdmUtils.setFocus(this, false);
    });

  $(".pager select").unbind('focus');
  $(".pager input").unbind('focus');
}

AdmUtils.resizeIframe = function(iframe) {
	// resize the iframe by increasing its height by 1 pixel.
	var frameHeight = iframe.contentWindow.document.body.scrollHeight + 1
			+ 'px';
	iframe.style.height = frameHeight;
}

AdmUtils.selectDropDownOption = function(element, formID, inputElementID,
		submitButtonID) {
	var i = element.selectedIndex;
	var o = element.options[i];
	var input = document.getElementById(formID + ":" + inputElementID);
	if (input != null) {
		input.value = o.value;
		var submit = document.getElementById(formID + ":" + submitButtonID);
		if (submit != null) {
			submit.click();
		}
		return true;
	}
	return false;
}

AdmUtils.setEditorContent = function(value, formID, editorID) {
	var editor = tinyMCE.getInstanceById(formID + ":" + editorID + "TextArea");
	if (editor != null && typeof (editor) != "undefined") {
		editor.setContent(value);
	}
}

AdmUtils.setLoginPanelDirty = function() {
	loginPanelDirty = true;
}

AdmUtils.showMplLoginPanelAfterRefresh = function() {
	loginPanelDirty = false;
	showLoginPanel(loginRedirectTarget, false, loginContextPath);
}

AdmUtils.showMplLoginPanel = function(redirectTarget, directForward,
		contextPath) {
	if (redirectTarget && directForward) {
		window.location.href = contextPath + redirectTarget;
		return;
	}
	if (loginPanelDirty) {
		loginRedirectTarget = redirectTarget;
		loginContextPath = contextPath;
		refreshLogin();
		return;
	}
	var hField = document.getElementById('loginForm:loginRedirectTarget');
	if (hField) {
		hField.value = redirectTarget;
	} else {
		hField = document.getElementById('passwordForm:loginRedirectTarget');
		if (hField) {
			hField.value = redirectTarget;
		}
	}
	var panel = document.getElementById('loginPanel');
	if (panel && panel.rf.component) {
		AdmUtils.clearModalErrorPanel('modalErrorPanel');
		panel.rf.component.show();
		var userField = document.getElementById('loginForm:loginUserId');
		if (userField) {
			userField.focus();
		}
		var pwdField = document.getElementById('passwordForm:currentPassword');
		if (pwdField) {
			pwdField.focus();
		}
	}
}

AdmUtils.setPageOverflow = function(type) {
	document.getElementsByTagName("html")[0].style.overflow = type;
}

AdmUtils.adjustMplModalDialogPosition = function(dialogId) {
	var selectorId = dialogId + 'CDiv';
	var divElement = jQuery("div[id$=" + selectorId + "]");

	divElement.css("width", divElement.width());
	divElement.css("position", "relative");

	var windowHeight = jQuery($(window)).height() / 2;

	var heightSetting = divElement.height() / 2;

	if (windowHeight > heightSetting) {
		divElement.css("margin-top", -heightSetting);
	} else {
		divElement.css("margin-top", -windowHeight);
	}
}

AdmUtils.showRating = function(rating) {
	if (document.getElementById('ratingText')) {
		if (rating == 0) {
			document.getElementById('ratingText').className = 'ratingTextWarning';
		} else {
			document.getElementById('ratingText').className = 'ratingTextNormal';
		}
		document.getElementById('ratingText').innerHTML = document
				.getElementById('commentForm:star' + rating).innerHTML;
	}

	if (document.getElementById('starParent')) {
		document.getElementById('starParent').className = 'ratingStars rating'
				+ rating + '_0';
	}
}

AdmUtils.changeMarketplace = function(mId) {
	var url = window.location.href;
	if (url.indexOf('?') > 0) {
		var idx = url.indexOf('mId=');
		if (idx > 0) {
			var rest = url.substr(idx + 4);
			var nIdx = rest.indexOf('&');
			var oldMid = (nIdx > 0) ? rest.substr(0, nIdx) : rest;
			var oldPart = "mId=" + oldMid;
			var newPart = "mId=" + mId;
			url = url.replace(oldPart, newPart);
		} else
			url = url + "&mId=" + mId;
	} else
		url = url + "?mId=" + mId;
	window.location.href = url;
}

AdmUtils.copyElementValues = function(srcId, destId) {
	var o = document.getElementById(srcId);
	document.getElementById(destId).value = o.value;
}

AdmUtils.copySelectedOption = function(name, destId) {
	var elm = document.getElementById(destId);
	var elms = document.getElementsByName(name);
	if (elms) {
		for ( var i = 0; i < elms.length; i++) {
			if (elms[i].checked) {
				elm.value = elms[i].value;
				break;
			}
		}
	}
	return elm.value;
}

AdmUtils.copyFormContent = function(srcId, destId) {
	var sFormParts = srcId.split(':');
	var dFormParts = destId.split(':');
	var s = document.getElementById(sFormParts[0]);
	var d = document.getElementById(dFormParts[0]);
	if (s && d) {
		for ( var i = 0; i < s.elements.length; i++) {
			var es = s.elements[i];
			if (es && es.type != 'hidden') {
				var ed = document.getElementById(es.id.replace(srcId, destId));
				if (ed) {
					if (es.type == 'radio' || es.type == 'checkbox') {
						ed.checked = es.checked;
					} else {
						ed.value = es.value;
					}
				}
			}
		}
	}
}

AdmUtils.handlePaymentTypeSelection = function(standardSaveId, ajaxSaveId,
		paymentTypeSelection, selectedPaymentTypeId) {
	var selectedType = AdmUtils.copySelectedOption(paymentTypeSelection,
			selectedPaymentTypeId);
	var buttonId = ajaxSaveId;
	if (selectedType == 'INVOICE') {
		buttonId = standardSaveId;
	}
	document.getElementById(buttonId).click();
}

AdmUtils.cutServiceTags = function(elements) {
	if (elements) {
		for ( var i = 0; i < elements.length; i++) {
			var elem = elements[i];
			var checkSize = true;
			var aIndex = -1;
			var aCnt = 0;
			while (elem.scrollWidth > elem.offsetWidth && checkSize) {
				checkSize = false;
				aIndex = -1;
				aCnt = 0;
				for ( var x = 0; x < elem.childNodes.length; x++) {
					if (typeof (elem.childNodes[x].tagName) != 'undefined'
							&& elem.childNodes[x].tagName.toLowerCase() == 'a') {
						aCnt++;
						aIndex = x;
					}
				}
				if (aCnt > 1) {
					while (elem.childNodes.length > aIndex) {
						elem
								.removeChild(elem.childNodes[elem.childNodes.length - 1]);
					}
					elem.appendChild(document.createTextNode('...'));
					checkSize = aCnt > 2;
				}
			}
		}
	}
}

AdmUtils.showHint = function(hint) {
	if (hint == null) {
		return;
	}
	if (shownHint != null) {
		shownHint.style.display = "none";
		if (shownHint == hint) {
			shownHint = null;
			return;
		}
	}
	shownHint = hint;
	hint.style.display = "";
}

AdmUtils.testHttpConnection = function(url) {
	var xmlhttp = null;
	if (window.XMLHttpRequest) {
		xmlhttp = new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	} else {
		return false;
	}
	try {
		xmlhttp.open('GET', url, false);
		xmlhttp.send(null);
		if (xmlhttp.readyState === 4 && xmlhttp.status === 200) {
			return true;
		}
	} catch (e) {
		// do noting - false will be returned afterwards
	}
	return false;
}

AdmUtils.SuggestionBox = function(suggestionElement, nothingLabel, target) {
	this.$box = null;
	this.suggestionElement = suggestionElement;
	this.$box = jQuery(target);
	this.$dropDown = jQuery('.suggestionDrop', this.$box);
	this.hasFilter = false;
	this.colText = [],

	this.nothingLabel = nothingLabel;
	this.colText[0] = 'Organization ID';
	this.colText[1] = 'Organization Name';
	this.suggestTitle = 'Suggested Organizations';
}

AdmUtils.SuggestionBox.prototype.buildSuggestionBox = function(suggestionBox) {

	var $tab = jQuery("<table style='width:100%'></table>"), $cell;
	var data = suggestionBox.fetchValues;
	var numEntriesMult = 6; // testing
	var len = data.length * numEntriesMult;// testing
	var $input = jQuery(".suggestionInput", this.$box);
	var $marker = jQuery(".suggestion-box-marker", this.$box);
	var that = this;

	this.addSuggestionTitle(len, $input, $marker);

	for ( var kk = 0; kk < data.length; ++kk) {
		for ( var jj = 0; jj < numEntriesMult; ++jj) { // testing
			$cell = this.createCell(data[kk].VOOrganization);
			$tab.append($cell);
		}
	}
	this.destroyList();
	jQuery(".organizationResults", this.$box).text("");
	jQuery(".organizationResults", this.$box).append($tab);
}

AdmUtils.SuggestionBox.prototype.createCell = function(obj) {
	var $cell, $subTab;
	var that = this;
	$cell = jQuery("<tr class='suggestionBoxItem'><td class='id'></td><td class='name'></td>/tr>");

	$subTab = jQuery(".id", $cell);
	$subTab.html(obj.organizationId);
	$subTab = jQuery(".name", $cell);
	$subTab.html(obj.name);
	$subTab.parent().attr("data-suggestion-id", obj.organizationId);
	$subTab.parent().attr("data-suggestion-name", obj.name);
	$cell.click(function() {
		var $element;
		$element = jQuery(".suggestionInput", this.$box);
		$element.val(jQuery(this).attr("data-suggestion-id"));
		$element = jQuery(".select .option", this.$box);
		$element.html(jQuery(this).attr("data-suggestion-id"));
		$element = jQuery(".suggestionDrop", this.$box);
		that.openCloseSuggestion();
	});
	$cell.hover(function() {
		jQuery(this).addClass('formHighlight');
	}, function() {
		jQuery(this).removeClass('formHighlight');
	})
	return $cell;
}

AdmUtils.SuggestionBox.prototype.destroyList = function() {
	var len, $cells;
	$cells = jQuery("td", this.$dropDown);
	len = $cells.length;
	$cells.each(function() {
		jQuery(this).unbind();
	});
}

AdmUtils.SuggestionBox.prototype.addSuggestionTitle = function(len, $input,
		$marker) {
	var title, titleText;
	if (len > 10) {
		title = {
			col1 : this.colText[0],
			col2 : this.colText[1],
			title : this.suggestTitle
		}
		$input.css("display", "");
		$marker.css("display", "");
		this.hasFilter = true;
	} else if (len > 0) {
		titleText = (this.hasFilter) ? this.suggestTitle : ' '
		title = {
			col1 : this.colText[0],
			col2 : this.colText[1],
			title : this.suggestTitle
		}

		if (!this.hasFilter) {
			$input.css("display", "none");
			$marker.css("display", "none");
		}
	} else {
		title = {
			col1 : '',
			col2 : '',
			title : this.nothingLabel
		}
	}
	var $tab = jQuery("<table class='suggestionListTitle'>"
			+ "<tr><td colspan='2' class='title'>" + title.title + "</td></tr>"
			+ "<tr><td style='width:50%;'class='subTitle'>" + title.col1
			+ "</td><td style='width:50%;' class='subTitle'>" + title.col2
			+ "</td></tr></table>");
	jQuery(".titleAnchor", this.$box).text("");
	jQuery(".titleAnchor", this.$box).append($tab);
}

AdmUtils.SuggestionBox.prototype.openCloseSuggestion = function() {
	var that = this;
	if (this.$dropDown.attr("data-open") === "true") {
		this.$dropDown.animate({
			height : 0,
			opacity : 0
		}, 200, function() {
			that.$dropDown.attr("data-open", "false");
		});
		jQuery("body").unbind("click");
	} else {
		this.$dropDown.css("height", "auto");
		this.$dropDown.animate({
			opacity : 1
		});
		this.$dropDown.attr("data-open", "true");
		if (this.$dropDown.attr("data-empty") === "true") {
			this.$dropDown.attr("data-empty", "false");
			this.suggestionElement.callSuggestion(true);
			var $input = jQuery(".suggestionInput", this.$box);
			var $marker = jQuery(".suggestion-box-marker", this.$box);
			if (!this.hasFilter) {
				$input.css("display", "none");
				$marker.css("display", "none");
			}
		}

		this.$box.unbind();
		this.$box.click(function(evt) {
			evt.stopPropagation();
		});
		jQuery("body").click(function() {
			that.openCloseSuggestion();
		});
	}
}


//this function could be removed once all the contentTitles of every page not using contentTitles have been removed.
AdmUtils.contentsTitle = function (){
	var title = jQuery(".contentstitle").html();
	if(!title) return;
    title = title.replace(/(\r\n|\n|\r|\s)/gm, "");
    if ((title=='<br>')||(title=='<BR>')) jQuery(".contentstitle").css('display', 'none');
}

// Selects the specified options of the specified radio button groups. If the selection differs than the
// one on page rendering time, the page is set dirty.
// Parameter: selectedRadioOptions - an Object holding the names of the radio button groups 
//				to set the selections for as properties. The value of each such property 
//				is the value of the radio input field to be selected for that radio button group. 
AdmUtils.selectRadioButtons = function(selectedRadioOptions) {
	for(var name in selectedRadioOptions) {
        if(selectedRadioOptions.hasOwnProperty(name)) {
        	var radioGroup = document.getElementsByName(name);
			for (var i = 0; i < radioGroup.length; i++) {
				if(radioGroup[i].value == selectedRadioOptions[name]) {
					if(!radioGroup[i].checked) {
						radioGroup[i].checked = true;
						setDirty(true);
						break;
					}
				}
    	    }
        }
    }
}


AdmUtils.unhideMenuWithWidth = function (navWidth) {
    jQuery('#navPanel2').hide();
    jQuery('#navPanel').show();
    jQuery('#navPanel').animate({ width: navWidth, minWidth:'200px'}, 300 );
}

AdmUtils.hideMenu = function (navWidth) {
	jQuery('#navPanel').animate({ width: '13px', minWidth:'13px'}, 300,
		function(){jQuery('#navPanel').hide();jQuery('#navPanel2').show();} );
}

AdmUtils.setFrameHeight = function(targetHeight){
	
	var $main =  jQuery("#mainContainer");
	
	if($main.length === 0) return;
	
	targetHeight = targetHeight || parseInt(AdmUtils.getCookie("targetHeight"),10);
	targetHeight = targetHeight || 300;

	var docHeight = jQuery(window).height() - 3;
	var boxDeltaY = jQuery("#body_boxMain").offset().top;
	var boxHeight = jQuery("#navPanel").height();
	var boxMainMinHeight= parseInt(jQuery("#body_boxMain").css("min-height"), 10);
	
	var splitContent = ( jQuery("#body_box2").length > 0 ) ? jQuery("#body_box2").html() : null;
	
	if(splitContent){
		splitContent = splitContent.replace(/(\r\n|\n|\r)/gm, "")
		splitContent = splitContent.replace(/\s+/,"");
	}
	if(splitContent !== null && splitContent.length){
		if(targetHeight + boxDeltaY + 11 > docHeight){
			targetHeight = docHeight - boxDeltaY -11;
			jQuery("#body_boxMain").height(targetHeight);
			jQuery("#body_boxSub").height(0);	    
		} else {	
			if(targetHeight<boxMainMinHeight){	
				targetHeight = boxMainMinHeight;
			}
			jQuery("#body_boxMain").height(targetHeight);
			jQuery("#body_boxSub").height(docHeight-boxDeltaY-targetHeight-11);		    
		}
		document.cookie = "targetHeight="+targetHeight+"; path=/";
	} else {
		jQuery("#contentPanelSub").remove();
		jQuery("#panelSplitResize").remove();
		jQuery("#body_boxMain").height(docHeight-boxDeltaY);
	}
	boxDeltaY = jQuery(".resizePanel").offset().top;
	jQuery(".resizePanel").height(docHeight-boxDeltaY);

	boxDeltaY = jQuery("#navPanel #menu").offset().top;
	jQuery("#navPanel #menu").height(docHeight-boxDeltaY);
}

AdmUtils.resizeSplit = function(event, navPanel, resizePanel, contentPanel,
		callback) {
	startY = event.clientY;
	origHeight = parseInt(navPanel.style.height);
	AdmUtils.observe(document, "mousemove", mouseMove);
	AdmUtils.observe(document, "mouseup", mouseUp);
	AdmUtils.stopEvent(event);

	function mouseMove(event) {
		var h = origHeight + event.clientY - startY;
		if (h > 0) {
			AdmUtils.setFrameHeight(h);
		}
		AdmUtils.stopEvent(event);
	}

	function mouseUp(event) {
		AdmUtils.stopObserving(document, "mousemove", mouseMove);
		AdmUtils.stopObserving(document, "mouseup", mouseUp);
		if (callback) {
			callback(origHeight + event.clientY - startY);
		}
		AdmUtils.stopEvent(event);
	}
}

AdmUtils.makeSplitResizeable = function(dragElement, navPanel, resizePanel,
		contentPanel, callback) {
	AdmUtils.observe(dragElement, "mousedown",
			function(event) {
				AdmUtils.resizeSplit(event, navPanel, resizePanel, contentPanel,
						callback);
			});
}

AdmUtils.getCookie = function(n) {
 var r = document.cookie.match("(^|;) ?" + n + "=([^;]*)(;|$)");
 return (r) ? unescape(r[2]) : null;
}


AdmUtils.dataTableSelectOneRadio = function(radio) {
	var id = radio.name.substring(radio.name.lastIndexOf(':'));
	var e = radio.form.elements;
	var length = e.length;
	for ( var i = 0; i < length; i++) {
		if (e[i].name.substring(e[i].name.lastIndexOf(':')) == id) {
			e[i].checked = false;
		}
	}
	radio.checked = true;
}

AdmUtils.dataTableSelectCheckboxes = function(prefix, id, value, size) {
	var field;
	for ( var i = 0 ; i <= size ; i++) {
		if((field = document.getElementById(prefix + ":" + i + ":" + id)) != null){
			if(field.checked != value && !field.disabled){
				field.checked = value;
			}
		}
	}
}

AdmUtils.updateSelectAllCheckbox = function(prefix, id, size, checkbox) {
	var isEverythingSelected = true;
	var isListEmpty = true;
	for (var i = 0; i <= size; i++) {
		if ((field = document.getElementById(prefix + ":" + i + ":" + id)) != null) {
			isListEmpty = false;
			if (field.checked != true) {
				isEverythingSelected = false;
				break;
			}
		}
	}
	var element = document.getElementById(checkbox);
	if (element != null) {
		if (isListEmpty == true) {
			element.checked = false;
		} else if (isEverythingSelected == true) {
			element.checked = true;
		}
	}
};

AdmUtils.setAllServiceCheckbox = function(prefix, id, size, allSelectId) {
	var field;
	var servicesIsNull = true;
	var unCheckAllservices = false;
	for (var i = 0; i <= size; i++) {
		if ((field = document.getElementById(prefix + ":" + i + ":" + id)) != null) {
			servicesIsNull = false;
			if (field.checked == false) {
				unCheckAllservices = true;
				break;
			}
		}
	}
	if(document.getElementById(prefix + ":" + allSelectId) == null){
		return;
	}
	if ((servicesIsNull == true) || (unCheckAllservices == true)) {
        document.getElementById(prefix + ":" + allSelectId).checked = false;
		return;
	}
	if (unCheckAllservices == false){
		document.getElementById(prefix + ":" + allSelectId).checked = true;
	}
}

AdmUtils.cancelBubble = function(event) {
	event=event?event:window.event;
	if(typeof (event) == 'undefined'){
		return;
	}
	if (typeof (event.cancelBubble) != 'undefined'){
		event.cancelBubble=true;
	}else if(event.stopPropagation) {
		event.stopPropagation();
	}
}

AdmUtils.saveMenuPosition = function() {
	if (typeof localStorage != 'undefined') {
		localStorage.setItem('menupos', jQuery('#menu').scrollTop());
	}
}

AdmUtils.setMenuPosition = function() {
	if (typeof localStorage != 'undefined') {
		jQuery('#menu').scrollTop(localStorage.getItem('menupos'));
	}

}

AdmUtils.storeFormState = function (id) {
	AdmUtils.storedForm = $(id).serializeArray();
};

AdmUtils.restoreFormState = function () {
	var element = null;
	$.each(AdmUtils.storedForm, function (key, item) {
		if (item.name.indexOf(':') > -1) {
			element = $('#' + item.name.replace(':', '\\:'));
			element.val(item.value);
			if (element.is('select')) {
				$('#' + item.name.replace(':', '\\:') + 'Selected').text(
					element.find(':selected').text()
				);
			}
		}
	});
};

// Function is used to force autocomplete action by putting non-printed chars
// in rich:autocomplete component
AdmUtils.autocompleteSearch = function(autocompleteComponent) {
	var autocompleteVal = autocompleteComponent.getValue();
    if (autocompleteVal.length < 3) {
    	autocompleteComponent.setValue(autocompleteVal + '\u200C\u200C\u200C');
    }
};

// TODO label style should be moved to proper css file
AdmUtils.addNothingLabel = function(autocompleteComponent, popupClass,
		nothingLabelText) {
	var popupClassSelector = "." + popupClass;
	$(popupClassSelector + " #NothingLabel").remove();
	if ($(popupClassSelector + " .rf-au-itm.rf-au-opt.rf-au-fnt.rf-au-inp").length == 0) {
		var labelContainer = $(popupClassSelector + " .rf-au-lst-scrl")
				.attr("style", "position: relative;");
		$("<span/>").html(nothingLabelText).attr("id", "NothingLabel")
				.appendTo(labelContainer).attr("style", "position: absolute;");
	}
	autocompleteComponent.showPopup();
};

AdmUtils.removeNonPrintedChars = function(autocompleteComponent) {
	var fieldValue = autocompleteComponent.getValue();
	if (fieldValue.indexOf('\u200C') != -1) {
		autocompleteComponent.setValue(fieldValue.replace(/\u200C/g, ''));
	}
};

AdmUtils.managePageSelection = function(pager) {
	var p = RichFaces.component(pager);
	if (document.getElementById(pager + "Panel") == null) {
		return;
	}
	if (typeof(p) != "undefined") {
		document.getElementById(pager + "Panel").style.display = "";
	} else {
		document.getElementById(pager + "Panel").style.display = "none";
	}
};

AdmUtils.markSortColumnHeader = function(sortSelector) {
    $(sortSelector).each(function() {
    	$(this).parent().addClass("clicked-column-header").removeClass("hovered-column-header");
    	var columnid = $(this).data("columnid");
    	$(this).parent().parent().parent().find(".rf-dt-flt .rf-dt-c-" + columnid)
    		.removeClass("hovered-column-header").addClass("clicked-column-header");
    });
};

AdmUtils.handleSortAction = function() {
	AdmUtils.markSortColumnHeader("span.rf-dt-srt-asc");
	AdmUtils.markSortColumnHeader("span.rf-dt-srt-des");
}

AdmUtils.setFirstPage =  function(pagerName) {
var pager = RichFaces.component(pagerName);
    if (typeof pager != 'undefined') {
        pager.switchToPage(1);
    }
}

AdmUtils.noRoleSelected = function(){
    var rows = document.getElementById('editForm:userRolesTable:tb').rows.length;
    for (var i=0; i<rows; i++){
      if(document.getElementById("editForm:userRolesTable:"+i+":rolesCheckbox").checked){
        return false;
      }
    }
    return true;
}

AdmUtils.callKeyup = function(filterInputId) {
	var filterElement = $('input[id*="' + filterInputId + '"]');
    if (document.documentMode == 8) {
          $(filterElement).bind("propertychange", function() {
                $(this).keyup();
          });
    } else if (document.documentMode == 9) {
          window.addEventListener("contextmenu", function() {
                $(filterElement).bind("mouseout", function(event) {
                      $(this).keyup();
                      $(".rich-filter-input").unbind("mouseout");
                });
          });
    } else {
          $(filterElement).on("input", function() {
                $(this).keyup();
          });
    }
}

AdmUtils.getTopOffset = function (element) {
	var offset = 0;
	while (element && !isNaN(element.offsetTop)) {
		offset += element.offsetTop;
		element = element.offsetParent;
	}
	return offset;
}

AdmUtils.resizeTrackingCodeForm = function () {
	var textarea = document.getElementById('trackingCodeForm:trackingCode');
	textarea.style.height = (document.documentElement.clientHeight - AdmUtils.getTopOffset(textarea) - 56) + "px";
}

AdmUtils.registerTrackingCodeEvents = function () {
	if(window.attachEvent) {
		window.attachEvent('onresize', AdmUtils.resizeTrackingCodeForm);
		window.attachEvent('onload', AdmUtils.resizeTrackingCodeForm);
	} else if(window.addEventListener) {
		window.addEventListener('resize', AdmUtils.resizeTrackingCodeForm, true);
		window.addEventListener('load', AdmUtils.resizeTrackingCodeForm, true);
	}
}

AdmUtils.emitResizeEvent = function() {
	if(!AdmUtils.isIE() || AdmUtils.getIEVersion() > 8) {
		var resizeEvent = document.createEvent("HTMLEvents");
		resizeEvent.initEvent("resize", false, false);
		window.dispatchEvent(resizeEvent);
	}
	AdmUtils.resizeTrackingCodeForm();
}

AdmUtils.updateRoleComboBox = function(event, roleId) {
	var elements = $("[id$='{0}']".f(roleId));
	var id = event.target.id.match(/\d+/)[0];

	for(var i = 0; i < elements.length; i++) {
		var elemId = elements[i].id.match(/\d+/)[0];
		if(elemId === id) {
			if (!event.target.checked) {
				elements[i].style.visibility = 'hidden';
			} else {
				elements[i].style.visibility = 'visible';
			}
		}
	}
}

AdmUtils.initRoleFieldSetup = function (roleId, checkboxId) {
	var unitRoleElems = $("[id$='{0}']".f(roleId));
	var groupCheckboxElems = $("[id*='{0}']".f(checkboxId));

	for (var i = 0; i < groupCheckboxElems.length; i++) {
		var groupElemId = groupCheckboxElems[i].id.match(/\d+/)[0];
		for(var j = 0; j < unitRoleElems.length; j++) {
			var unitElemId = unitRoleElems[j].id.match(/\d+/)[0];
			
			if(groupElemId === unitElemId && !groupCheckboxElems[i].checked) {
				unitRoleElems[j].style.visibility = 'hidden';
			}
		}
	}
}

AdmUtils.updateSubRoleComboBox = function(event, roleId) {

	var elements = $("[id$='{0}']".f(roleId));
	var id = event.target.id.replace('assignCheckbox',roleId);
	
	for(var i = 0; i < elements.length; i++) {	
		if(elements[i].id==id){
		
			var roleSelector = document.getElementById(id);
			
			if (!event.target.checked) {
				roleSelector.style.visibility = 'hidden';
			} else {
				roleSelector.style.visibility = 'visible';
			}
			
		}		
	}
}

AdmUtils.initSubRoleFieldSetup = function (roleId, checkboxId) {
	
	
	var roleElems = $("[id$='{0}']".f(roleId));
	var checkboxElems = $("[id*='{0}']".f(checkboxId));

	for (var i = 0; i < roleElems.length; i++) {
		
		var roleElement = roleElems[i];
		var roleElementId = roleElems[i].id;

		var checkElementId = roleElementId.replace(':role',':assignCheckbox');
		var checkElement = document.getElementById(checkElementId);

		if(checkElement.checked){
			roleElement.style.visibility = 'visible';
		} else{
			roleElement.style.visibility = 'hidden';
		}		
	}
}



String.prototype.format = String.prototype.f = function() {
	var s = this,
		i = arguments.length;

	while (i--) {
		s = s.replace(new RegExp('\\{' + i + '\\}', 'gm'), arguments[i]);
	}
	return s;
};

AdmUtils.handleSelectAllWithPaging = function(panelClass) {
	var checkBoxes = $('.' + panelClass + ' .user-group-select-user');
	var selectAllCheckbox = $('.' + panelClass + ' .select-all-checkbox')[0];

	selectAllCheckbox.checked = checkBoxes.length != 0;

	$.each(checkBoxes, function() {
		if ($(this)[0].checked == false) {
			selectAllCheckbox.checked = false;
			return;
		}
	})
}

AdmUtils.handlePagerActionsWithPopup = function(panelClass, pagerComponent, pagerButtonId) {
	$.each($('.' + panelClass + ' a.rf-ds-nmb-btn'), function() {
    	var pageNr = $(this).html();
    	$(this).unbind('click');
    	$(this).bind('click', function() {
    		if (handleOnKeyUp() == true) {
    			pagerComponent.switchToPage(pageNr);
    			
    		}
    	});
    });
    
    var nextButton = $('.' + panelClass + ' a.rf-ds-btn.rf-ds-btn-next');
    nextButton.unbind('click');
    nextButton.bind('click', function() {
    	if (handleOnKeyUp() == true) {
    		pagerComponent.next();
    	}
    });
    
    var lastButton = $('.' + panelClass + ' a.rf-ds-btn.rf-ds-btn-last');
    lastButton.unbind('click');
    lastButton.bind('click', function() {
    	if (handleOnKeyUp() == true) {
    		pagerComponent.last();
    	}
    });
    
    var prevButton = $('.' + panelClass + ' a.rf-ds-btn.rf-ds-btn-prev');
    prevButton.unbind('click');
    prevButton.bind('click', function() {
    	if (handleOnKeyUp() == true) {
    		pagerComponent.previous();
    	}
    });
    
    var firstButton = $('.' + panelClass + ' a.rf-ds-btn.rf-ds-btn-first');
    firstButton.unbind('click');
    firstButton.bind('click', function() {
    	if (handleOnKeyUp() == true) {
    		pagerComponent.first();
    	}
    });
    
    var pageButton = document.getElementById(pagerButtonId);
    $(pageButton).unbind('click');
    $(pageButton)[0].onclick = null;
    $(pageButton).bind('click', function(event) {
    	event.preventDefault();
    	if (handleOnKeyUp() == true) {
    		var pageNr = $('.' + panelClass + ' .pageInput').val();
    		pagerComponent.switchToPage(pageNr);
    	}
    });
}

AdmUtils.adjustDialogHeight = function(dialogId) {
	try {
		var topPos = $("#" + dialogId + "_container").position().top;
		var dialH = $("#" + dialogId + "_container").height();
		var dialogBottomYposition = topPos + dialH;

		if (dialogBottomYposition > $(window).height()) {
			var dialogHeight = $(window).height() - topPos - 20;
			$("#" + dialogId + "Grid").height(dialogHeight);
			var tablePanelHeight = dialogHeight - 50;
			$("#" + dialogId + "Form .responsive-table-panel").height(
					tablePanelHeight);
			$("#" + dialogId + "_content").height(dialogHeight + 100);
		} else {
			$("#" + dialogId + "Grid").height($(this)[0].height);
			$("#" + dialogId + "Form .responsive-table-panel").height(
					$(this)[0].height);
			$("#" + dialogId + "_content").height($(this)[0].height);
		}
	} catch (err) {
		// catch only for webtests
	}
}

AdmUtils.adjustDialogHeightOnResize = function(dialogId) {
	var topPos = $("#" + dialogId + "_container").position().top;
	var dialH = $("#" + dialogId + "_container").height();
	var dialogBottomYposition = topPos + dialH;
	var dialogHeight = $(window).height() - topPos - 150;
	$("#" + dialogId + "Grid").height(dialogHeight);
	var tablePanelHeight = dialogHeight - 50;
	$("#" + dialogId + "Form .responsive-table-panel").height(
			tablePanelHeight);
	$("#" + dialogId + "_content").height(dialogHeight + 100);
}

AdmUtils.IE9PlaceHolderFix = function (searchPhraseProperty, inputId){
     function hasPlaceholderSupport() {
      var input = document.createElement('input');
      return ('placeholder' in input);
     }

     if(!hasPlaceholderSupport()){
        var input = document.getElementById(inputId);
            if(input.getAttribute('placeholder')){
                input.style.cssText = "color:#939393;font-style:italic;"
                if (searchPhraseProperty != ''){
                    this.value = searchPhraseProperty;
                } else {
                    input.value = input.getAttribute('placeholder');
                }
                input.onblur = function(){
                    if (this.value == ''){
                        this.value = this.getAttribute("placeholder");
                    }
                }
            }
     }
}

AdmUtils.IE9OnClick = function (id){
     function hasPlaceholderSupport() {
       var input = document.createElement('input');
       return ('placeholder' in input);
     }
     if(!hasPlaceholderSupport()){

        var input = document.getElementById(id);
            if (input.value == input.getAttribute("placeholder")){
                input.value = '';
            }
     }
}

AdmUtils.IE9AfterClick = function (id){
     function hasPlaceholderSupport() {
       var input = document.createElement('input');
       return ('placeholder' in input);
     }
     if(!hasPlaceholderSupport()){

        var input = document.getElementById(id);
            if (input.value == ''){
                input.value = input.getAttribute("placeholder");
            }
     }
}

AdmUtils.sortSelect = function() {
	// Applies the function to each select on the page
	$("select").each(function(index, select) {
		var $select = $(select);

		// First we find out if one of the options is selected
		var selectedIndex = select.options.selectedIndex;
		if (typeof selectedIndex != 'undefined') {
			selectedValue = select.options[selectedIndex].value;
		}

		// We sort the options
		$select.find("option").sort(function(left, right) {
			return left.text == right.text ? 0 : left.text < right.text ? -1 : 1;
		}).appendTo($select);

		// If an option was selected, we need to reselected it. This is necessary because
		// after sorting, the item in position x won't be there anymore, and therefore the
		// wrong option will be selected
		if (typeof selectedValue != 'undefined') {
			for (var i = 0; i < $select.find("option").length; i++) {
				var option = $select.find("option")[i];
				if (option.value == selectedValue) {
					option.selected = true;
					$select.find("option").selectedIndex = i;
					break;
				}
			}
		}
	});
}
