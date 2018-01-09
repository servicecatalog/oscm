/* 
 *  Copyright FUJITSU LIMITED 2018
 */ 
window.onload = frameHeight;
window.onresize = frameHeight;

/**
 * Calc each pane height

 */
function frameHeight(){
	// header_area size
	var headHeight = document.getElementById("header_area_id").offsetHeight;
	if (navigator.appName.charAt(0) == 'M') {
		// window size
		var winHeight = document.documentElement.clientHeight;
		// base size
		var baseHeight = checkSize(winHeight - headHeight);
		// right top area
		var rightTopHeight = checkSize(baseHeight * 0.7 - 6);
		// right bottom area
		var rightBottomHeight = checkSize(baseHeight * 0.3 - 5);
		// left area
		var pane1Height = checkSize(rightTopHeight + rightBottomHeight + 6);
		// left scroll area
		var leftScroll = checkSize(pane1Height - 34);
		// right top scroll area
		var rightTopScroll = checkSize(rightTopHeight - 72);
		// right bottom scroll area
		var rightBottomScroll = checkSize(rightBottomHeight - 39);
		// splitter
		var splitterPosition = checkSize(baseHeight - 5);
	} else {
		// window size
		var winHeight = window.innerHeight;
		// base size
		var baseHeight = checkSize(winHeight - headHeight);
		// left area
		var pane1Height = checkSize(baseHeight - 7) + 'px';
		// right top area
		var rightTopHeight = checkSize(baseHeight * 0.7 - 7) + 'px';
		// right bottom area
		var rightBottomHeight = checkSize(baseHeight * 0.3 - 7) + 'px';
		// left scroll area
		var leftScroll = checkSize(baseHeight - 39) + 'px';
		// right top scroll area
		var rightTopScroll = checkSize(baseHeight * 0.7 - 79) + 'px';
		// right bottom scroll area
		var rightBottomScroll = checkSize(baseHeight * 0.3 - 46) + 'px';
		// splitter
		var splitterPosition = checkSize(baseHeight - 7)  + 'px';
	}
	// set
	//document.getElementById('left_area_id').style.height = pane1Height;
	//document.getElementById('right_top_area_id').style.height = rightTopHeight;
	//document.getElementById('right_bottom_area_id').style.height = rightBottomHeight;
	//document.getElementById('left_scroll_area_id').style.height = leftScroll;
	//document.getElementById('right_top_scroll_area_id').style.height = rightTopScroll;
	//document.getElementById('right_bottom_scroll_area_id').style.height = rightBottomScroll;
	//document.getElementById('pinch_vertical_id').style.height = splitterPosition;
	//updateLayout(document.getElementById('right_top_scroll_area_id'));
	//updateLayout(document.getElementById('right_bottom_scroll_area_id'));
}

/**
 * Validate paramator
 * @param {Object} windowSize
 */
function checkSize(windowSize){
	if (windowSize > 0) {
		return windowSize;
	}
	else {
		return 0;
	}
}

/**
 * HTMLElementの描画更新を行う。
 * (IE7の場合のみ)
 * @param {Object} target HTMLElements
 */
function updateLayout(target) {
	if (checkIE7()) {
		if (checkScroll(target)){
			target.style.paddingRight = 29;
		} else {
			target.style.paddingRight = 12;
		}
	} 
}

/**
 * IE7かを判定する。
 */
function checkIE7(){
	var userAgent = window.navigator.userAgent.toLowerCase(); 
	var appVersion = window.navigator.appVersion.toLowerCase();
	if (userAgent.indexOf("msie") > -1){
		if (appVersion.indexOf("msie 7.0") > -1){
			return true;
		}
	}
	return false;
}

/**
 * 縦スクロールが表示されているか判定する。
 * @param {Object} target HTMLElements
 */
function checkScroll(target){
	return target.clientHeight < target.scrollHeight;
}