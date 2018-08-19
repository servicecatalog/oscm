/* 
 *  Copyright FUJITSU LIMITED 2018
 **
 * open close heading data 4.

 * @param {Object} element element of heading data 4
 * @param {Object} target_id open/close、div id
 */
function accordion(element, target_id) {
	currentName = document.getElementById(element.id).className;
	if (currentName == "heading_data_4_open_mark_class") {
		document.getElementById(target_id).style.display = "none";
		document.getElementById(element.id).className='heading_data_4_close_mark_class';
	} else {
		document.getElementById(target_id).style.display = "block";
		document.getElementById(element.id).className='heading_data_4_open_mark_class';
	}
	// アコーディオンの開閉によって、親Divのサイズが変更する場合の処理
	updateLayout(document.getElementById('right_top_scroll_area_id'));
}