function begin() {
}
function end() {
}
function header() {
    return ['日時'];
}
function body(battleExDto) {
    return toComparable([
        [new Packages.logbook.gui.logic.DateTimeString(battleExDto.getBattleDate())]
    ]);
}
var ComparableArrayArrayType = Java.type("java.lang.Comparable[][]");
var ComparableArrayType = Java.type("java.lang.Comparable[]");
// javascriptの配列をそのまま返すと遅いので
// Comparable[]に変換しておく
// undefinedはnullに変換される
function toComparable(sourceRows) {
    var targetRows = new ComparableArrayArrayType(sourceRows.length);
    for (var j = 0; j < sourceRows.length; ++j) {
        var sourceRow = sourceRows[j];
        var targetRow = new ComparableArrayType(sourceRow.length);
        for (var i = 0; i < sourceRow.length; ++i) {
            if (sourceRow[i] == null) {
                targetRow[i] = null;
            }
            else {
                targetRow[i] = sourceRow[i];
            }
        }
        targetRows[j] = targetRow;
    }
    return targetRows;
}
