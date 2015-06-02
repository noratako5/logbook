load("script/util/underscore.js");


function header() {
	return [
		"日付"
		, "提督名"
		, "提督ID"
		, "支援"
	];
}

function begin() { }

function body(battle) {
	return toComparable([
		battle.getBattleDate()
		, battle.getNickname()
		, battle.getMemberId()
		, battle.getDockSupport()
	]);
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
		return Java.to(_.map(raw, function (r) {
			if (_.isUndefined(r)) {
				return "undefined"
			}
			else if (_.isNull(r)) {
				return "null"
			}
			else {
				return r.toString()
			}
		}), ComparableArrayType);
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
