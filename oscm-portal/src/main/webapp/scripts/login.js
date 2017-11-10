/* 
 *  Copyright FUJITSU LIMITED 2017
 */ 
onload = init;

function init() {
	document.getElementById("loginButton").style.display = "inline";
}

function OverMouseBt(element)
{
	document.getElementById(element.id).className='login_button_over_class';
}

function OutMouseBt(element)
{
	document.getElementById(element.id).className='login_button_class';
}

function DownMouseBt(element)
{
	document.getElementById(element.id).className='login_button_down_class';
}