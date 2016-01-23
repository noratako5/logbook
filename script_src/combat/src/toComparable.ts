import * as _ from 'lodash';
import JavaString = Packages.java.lang.String;
import JavaInteger = Packages.java.lang.Integer;
import JavaList = Packages.java.util.List;
import DateTimeString = Packages.logbook.gui.logic.DateTimeString;
import BattleExDto = Packages.logbook.dto.BattleExDto;
import ShipBaseDto = Packages.logbook.dto.ShipBaseDto;
import ShipDto = Packages.logbook.dto.ShipDto;
import EnemyShipDto = Packages.logbook.dto.EnemyShipDto;
import ItemDto = Packages.logbook.dto.ItemDto;
import ItemInfoDto = Packages.logbook.dto.ItemInfoDto;
import BattleAtackDto = Packages.logbook.dto.BattleAtackDto;
import AirBattleDto = Packages.logbook.dto.AirBattleDto;

type ComparableArray = JavaArray<any>;
type ComparableArrayArray = JavaArray<ComparableArray>;

// javascriptの配列をそのまま返すと遅いので
// Comparable[]に変換しておく
// undefinedはnullに変換される
export default function toComparable(sourceRows: any[][]) {
    var ComparableType = Java.type('java.lang.Comparable');
    var ComparableArrayType = <ComparableArray>Java.type('java.lang.Comparable[]');
    var ComparableArrayArrayType = <ComparableArrayArray>Java.type('java.lang.Comparable[][]');
    var targetRows = new ComparableArrayArrayType(sourceRows.length);
    for (var j = 0; j < sourceRows.length; ++j) {
        var sourceRow = sourceRows[j];
        var targetRow = new ComparableArrayType(sourceRow.length);
        for (var i = 0; i < sourceRow.length; ++i) {
            var source = sourceRow[i];
            if (source == null) {
                targetRow[i] = null;
            }
            else if (source instanceof ComparableType) {
                targetRow[i] = source;
            }
            else {
                targetRow[i] = JavaString.valueOf(source);
            }
        }
        targetRows[j] = targetRow;
    }
    return targetRows;
}
