load("script/util/underscore.js");


function header() {
	return [
		"1"
		, "2"
		, "3"
		, "4"
		, "5"
		, "6"
		, "7"
		, "8"
		, "9"
		, "10"
		, "11"
		, "12"
		, "13"
		, "14"
		, "15"
		, "16"
		, "17"
		, "18"
		
	];
}

function begin() { }

function body(battle) {
	var basicJson = battle.getBasicJsonString();
	if (basicJson != null) {
		var nickname = JSON.parse(basicJson).api_nickname;
	}
	return toComparable([
		type(nickname)
		, type(battle.resultJson)
		, type(battle.getResultJsonString())
		, type(battle.basicJson)
		, type(battle.getBasicJsonString())
		, type(battle.support)
		, type(battle.getDockSupport())
	]);
}

function type(val) {
	if (val === null) {
		return "Null";
	}
	else {
		return Object.prototype.toString.call(val).match(/^\[object (.*)\]$/)[1];
	}
}

function toString(val) {
	if (_.isUndefined(val)) {
		return "Undefined";
	}
	else if (_.isNull(val)) {
		return "Null";
	}
	else {
		return val.toString();
	}
}

function end() { }



// javascriptの配列をそのまま返すと遅いので
// Comparable[]に変換しておく
// undefinedはnullに変換される
function toComparable(raw) {
	if (_.isArray(raw)) {
		var ComparableArrayType = Java.type("java.lang.Comparable[]");
		if (_.isArray(raw[0])) {
			var ComparableArrayArrayType = Java.type("java.lang.Comparable[][]");
			return Java.to(_.map(raw, toComparableArray), ComparableArrayArrayType);
		}
		else {
			return toComparableArray(raw);
		}
	}
	else {
		return raw;
	}
	
	function toComparableArray(raw) {
		return Java.to(raw, ComparableArrayType);
	}
}

// メッセージボックス表示
function alert(str) {
	SWT = Java.type("org.eclipse.swt.SWT");
	MessageBox = Java.type("org.eclipse.swt.widgets.MessageBox");
	Display = Java.type("org.eclipse.swt.widgets.Display");
	var shell = Display.getDefault().getActiveShell();
	var box = new MessageBox(shell,SWT.OK);
	box.setMessage(String(str));
	box.open();
}
