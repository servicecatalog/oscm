/* 
 *  Copyright FUJITSU LIMITED 2017
 */ 
var minPasswordLength = 6;
var fairPasswordLength = 8;

var STRENGTH_EMPTY = 0;
var STRENGTH_POOR = 1;
var STRENGTH_SHORT = 2;
var STRENGTH_TOOWEAK = 3;
var STRENGTH_WEAK = 4;
var STRENGTH_FAIR = 5;
var STRENGTH_RECOMENDED = 6;
var STRENGTH_STRONG = 7;
var STRENGTH_MORESTRONG = 8;
var STRENGTH_STRONGEST = 9;
var STRENGTH_EXCEPTIONAL = 10;



function getStrength(password) {
    if (isEmpty(password)) {
	return STRENGTH_EMPTY;
    } else if (isPoor(password)) {
	return STRENGTH_POOR;
    } else if (isSmall(password)) {
	return STRENGTH_SHORT;
    } else if (isTooWeak(password)) {
	return STRENGTH_TOOWEAK;
    } else if (!isFair(password)) {
	return STRENGTH_WEAK;
    } else if (isExceptional(password)) {
	return STRENGTH_EXCEPTIONAL;
    } else if (isStrongest(password)) {
	return STRENGTH_STRONGEST;
    } else if(isMoreStrong(password)){
	return STRENGTH_MORESTRONG;
    } else if (isStrong(password)) {
	return STRENGTH_STRONG;
    } else if (isRecomended(password)) {
	return STRENGTH_RECOMENDED;
    }else {
	return STRENGTH_FAIR;
    }
}

/**
 * 
 * @param password
 * @return
 */
function isEmpty(password) {
    if (password.length == 0) {
	return true;
    }
    return false;
}

/**
 * if the password is of a single character
 * 
 * @param password
 * @return
 */
function isPoor(password) {
    if (password.length == 1) {
	return true;
    }
    return false;
}

/**
 * if the password is less than the minimum length
 * 
 * @param password
 * @return
 */
function isSmall(password) {
    if (password.length < minPasswordLength) {
	return true;
    } else {
	return false;
    }
}

/**
 * if the password is equal the minimum length
 * 
 * @param password
 * @return
 */
function isTooWeak(password) {
    if (password.length == minPasswordLength) {
	return true;
    }
    return false;
}

/**
 * if the password is less the fair password length
 * 
 * @param password
 * @return
 */
function isFair(password) {
    if (password.length < fairPasswordLength) {
	return false;
    } else {
	return true;
    }
}

/**
 * The password should contain at least 
 * one number, symbol or special character.
 * 
 * @param password
 * @return
 */
function isRecomended(password){
    if (hasAplhabets(password) && (hasNum(password) && (hasSpecialChar(password)|| hasSymbol(password)))){
	return true;
    }
    return false;
}

/**
 * The password should contain at least 
 * one number, symbol or special character and 
 * capital and lower letters.
 * 
 * @param password
 * @return
 */
function isStrong(password) {
	if (isRecomended(password) &&((hasLowerLetter(password) && hasCaps(password)))){
	return true;
	}
    return false;
}

/**
 * The password should contain more than one number, capital and lower letters
 * and in addition one symbol, special character or white space. 
 * .
 * 
 * @param password
 * @return
 */
function isMoreStrong(password) {
    if (isStrong(password) && hasFewNums(password) && (hasSpecialChar(password)|| hasSymbol(password)|| hasWhiteSpace(password))) {
	return true;
    }
    return false;
}


/**
 * The password should contain more than one number, capital and lower letters
 * and in addition one combination of symbol and special character, symbol and white space 
 * or special character and white space. 
 * .
 * 
 * @param password
 * @return
 */
function isStrongest(password) {
    if (isMoreStrong(password) && ((hasSpecialChar(password) && hasSymbol(password)) || 
    		                       (hasSpecialChar(password) && hasWhiteSpace(password)) || 
    		                       (hasSymbol(password) && hasWhiteSpace(password)))) {
	return true;
    }
    return false;
}
/**
 * The password should contain more than one number, capital and lower letters
 * and in addition at least on or more symbol(s),special character(s) and white space(s). 
 * 
 * 
 * @param password
 * @return
 */
function isExceptional(password) {
    if (isStrongest(password) && hasWhiteSpace(password)&& hasSpecialChar(password) && hasSymbol(password)) {
	return true;
    }
    return false;
}

/**
 * 
 * @param password
 * @return
 */
function hasAplhabets(password) {
    var alpha = new RegExp("[A-Za-z]", "g");
    if (alpha.test(password)) {
	return true;
    }
    return false;
}

/**
 * 
 * @param password
 * @return
 */
function hasNum(password) {
    var num = new RegExp("[0-9]", "g");
    if (num.test(password)) {
	return true;
    }
    return false;
}

/**
 * 
 * @param password
 * @return
 */
function hasCaps(password) {
    var caps = new RegExp("[A-Z]", "g");
    if (caps.test(password)) {
	return true;
    }
    return false;
}


function hasLowerLetter(password) {
    var caps = new RegExp("[a-z]", "g");
    if (caps.test(password)) {
	return true;
    }
    return false;
}
/**
 * 
 * @param password
 * @return
 */
function hasWhiteSpace(password) {
    var whiteSpace = new RegExp("[\t\r\n]", "g"); // \s
    if (whiteSpace.test(password)) {
	return true;
    }
    return false;
}


/**
 * 
 * @param password
 * @return
 */
function hasSymbol(password) {
    var specialChar = new RegExp("[^A-Za-z0-9_\s]", "g");
    if (specialChar.test(password)) {
	return true;
    }
    return false;
}

function hasFewSymbols(password){
    var specialChar = new RegExp("[^A-Za-z0-9_\s]{2,}", "g");
    if (specialChar.test(password)) {
	return true;
    }
    return false;
}

function hasFewNums(password){
    var specialChar = new RegExp("[0-9]{2,}", "g");
    if (specialChar.test(password)) {
	return true;
    }
    return false;
}

function hasSpecialChars(password){
    var specialChar = new RegExp("[_@]{2,}", "g");
    if (specialChar.test(password)) {
	return true;
    }
    return false;
}

function hasSpecialChar(password){
    var specialChar = new RegExp("[_@]", "g");
    if (specialChar.test(password)) {
	return true;
    }
    return false;
}