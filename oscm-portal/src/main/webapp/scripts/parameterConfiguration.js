/* 
 *  Copyright FUJITSU LIMITED 2017
 **
 * Javascript prototype representing the Parconf class. Handles the
 * communication with the external parameter configuration tool and the behavior
 * of the modal panel.
 */
function Parconf(configuratorUrl, modalpanelId) {
	this.configuratorUrl = configuratorUrl;
	this.modalpanelId = modalpanelId;
	this.fallbackId = null;
	this.eventFunction = null;
	this.addMessageEventListener();
}

/**
 * Adds a 'message' event listener to the window object in order to receive
 * messages from the external parameter configuration tool. In case the user is
 * using IE8 or earlier, the event listener is registered for 'onmessage'
 * events.
 */
Parconf.prototype.addMessageEventListener = function() {
	var self = this;
	var eventType = null;
	this.eventFunction = function(e) {
		var incomingMessage = e.data;
		self.handleMessage(incomingMessage);
	};
	if (document.addEventListener) {
		eventType = "message";
		window.addEventListener(eventType, this.eventFunction);
	} else {
		// IE8 or earlier
		eventType = "onmessage";
		window.attachEvent(eventType, this.eventFunction);
	}
}

Parconf.prototype.handleMessageEvent = function(event) {
	var incomingMessage = event.data;
	this.handleMessage(incomingMessage);
}

/**
 * Removes 'message' or 'onmessage' event listeners from the window object.
 */
Parconf.prototype.removeMessageEventListener = function() {
	var eventType = null;
	if (document.addEventListener) {
		eventType = "message";
		window.removeEventListener(eventType, this.eventFunction);
	} else {
		eventType = "onmessage";
		window.detachEvent(eventType, this.eventFunction);
	}
}

/**
 * Sends messages to the external parameter configuration tool.
 */
Parconf.prototype.send = function(message) {
	var iframe = document.getElementById('configuratorFrame').contentWindow;
	if (iframe != null) {
		iframe.postMessage(message, this.configuratorUrl);
	}
}

/**
 * Activates the fallback solution after 4 seconds.
 */
Parconf.prototype.activateFallbackTimer = function() {
	var self = this;
	this.hideIframe();
	this.fallbackId = window.setTimeout(function() {
		self.fallback();
	}, 4000);
}

/**
 * Re-renders the modal panel and shows a message that the external parameter tool
 * could not be loaded. After closing the modal panel the default parameter
 * table is shown.
 */
Parconf.prototype.fallback = function() {
	this.removeMessageEventListener();
	setFallback();
	this.hideLoading();
}

/**
 * Handles incoming messages send by the external parameter configuration tool.
 */
Parconf.prototype.handleMessage = function(incomingMessage) {
	var parsedMessage = Message.parse(incomingMessage);
	if (parsedMessage.getMessageType() == Message.Type.INIT_MESSAGE) {
		var temp = this.fallbackId;
		window.clearTimeout(temp);		
		this.showIframe();
		this.adjustMplModalDialogPositionSize(this.modalpanelId, 'externalToolDialog', parsedMessage
				.getScreenHeight(), parsedMessage.getScreenWidth());
		sendConfigRequest();
	} else if (parsedMessage.getMessageType() == Message.Type.CONFIG_RESPONSE) {
		if (parsedMessage.getResponseCode() == ConfigResponse.Code.CONFIGURATION_FINISHED) {
			validateConfiguredParameters(incomingMessage);
		} else if (parsedMessage.getResponseCode() == ConfigResponse.Code.CONFIGURATION_CANCELLED) {
			var cancelButton = document.getElementById('configurationForm:btnHiddenCancel');
			cancelButton.click();
		}
	}
}

/**
 * Called when the validation of parameters is finished. Will close the modal
 * panel if no error is found, otherwise a new configuration request is send to
 * the external parameter configuration tool.
 */
Parconf.prototype.validationFinished = function(validationResult) {
	if (validationResult.validationError) {
	    if (typeof validationResult.configRequest == "string") {
		    this.send(validationResult.configRequest)
	    }
	} else {	 
		RichFaces.$('configurationForm:'+this.modalpanelId).hide();
		rerenderWarningPanel();		
	}
}

Parconf.prototype.showIframe = function() {
	var selectorIdExtToolDialog = 'externalToolDialog';
	var selectorIdIframe = 'configuratorFrame';
	var divExtToolDialog = jQuery("div[id$=" + selectorIdExtToolDialog + "]");
	var iframeExtConf = jQuery("iframe[id$=" + selectorIdIframe + "]");
	divExtToolDialog.css("display", "block");
	iframeExtConf.css("display", "block");
	this.hideLoading();
	AdmUtils.clearModalErrorPanel("configurationForm:configuratormodalErrorPanel");	
}

Parconf.prototype.hideIframe = function() {
	var selectorIdExtToolDialog = 'externalToolDialog';
	var divExtToolDialog = jQuery("div[id$=" + selectorIdExtToolDialog + "]");
	divExtToolDialog.css("display", "none");
	var selectorIdLoading = 'configurationForm:loadingPanel';
	document.getElementById(selectorIdLoading).style.display = 'block';
}

/**
 * Hides the loading indicator of the external parameter configuration tool.
 */
Parconf.prototype.hideLoading = function() {
	var selectorIdLoading = 'configurationForm:loadingPanel';
	document.getElementById(selectorIdLoading).style.display = 'none';
}

/**
 * Adapts the size of the modal panel to the iframe size.
 */
Parconf.prototype.adjustMplModalDialogPositionSize = function(dialogId, contentElement, height,
		width) {
	var windowHeight = jQuery($(window)).height();
	var windowWidth = jQuery($(window)).width();

	var defaultWidth = 600;
	var defaultHeight = 400;

	var maxDisplayHeight = windowHeight - 100;
	var maxDisplayWidth = windowWidth - 20;

	var intHeight = parseInt(height);
	var intWidth = parseInt(width);

	var selectorIdDialog = dialogId + '_content';
	var divDialogElement = jQuery("div[id$=" + selectorIdDialog + "]");

	var selectorIdRerenderPanel = 'configuratorRerenderPanel';
	var divRerenderPanelElement = jQuery("div[id$=" + selectorIdRerenderPanel + "]");

	var selectorIdContent = contentElement;
	var divContentElement = jQuery("div[id$=" + selectorIdContent + "]");

	var oldHeightDialog = parseInt(divDialogElement.height(), 10);
	var oldWidthDialog = parseInt(divDialogElement.width(), 10);

	var oldWidthRerenderPanel = parseInt(divRerenderPanelElement.width(), 10);
	var oldHeightRerenderPanel = parseInt(divRerenderPanelElement.height(), 10);

	var deltaDialogWidth = oldWidthDialog - oldWidthRerenderPanel;
	var deltaDialogHeight = oldHeightDialog - oldHeightRerenderPanel;
	
	if (defaultHeight > intHeight || isNaN(intHeight)) {
		intHeight = defaultHeight;
	}

	if (defaultWidth > intWidth || isNaN(intWidth)) {
		intWidth = defaultWidth;
	}

	var newDialogHeight = intHeight + deltaDialogHeight;
	var newDialogWidth = intWidth + deltaDialogWidth;

	var newRerenderPanelWidth = intWidth;
	var newRerenderPanelHeight= intHeight;

	if (oldHeightDialog > 0 && oldWidthDialog > 0 && oldWidthRerenderPanel > 0) {
		if (maxDisplayHeight < newDialogHeight) {
			newDialogHeight = maxDisplayHeight;
			intHeight = maxDisplayHeight - deltaDialogHeight;
			newRerenderPanelHeight=maxDisplayHeight - deltaDialogHeight;
		}

		if (maxDisplayWidth < newDialogWidth) {
			newDialogWidth = maxDisplayWidth;
			newRerenderPanelWidth = maxDisplayWidth - deltaDialogWidth;
			intWidth = maxDisplayWidth - deltaDialogWidth;
		}

		if (!isNaN(newDialogHeight) && !isNaN(newDialogWidth) && !isNaN(newRerenderPanelWidth)
				&& !isNaN(height) && !isNaN(width)) {
			divContentElement.css("height", intHeight + "px");
			divContentElement.css("width", intWidth + "px");
			jQuery("iframe[id$='configuratorFrame']").css("height", intHeight-106 + "px");

			divRerenderPanelElement.css("width", newRerenderPanelWidth + "px");
			divRerenderPanelElement.css("height", newRerenderPanelHeight+ "px");
		}
	}
}