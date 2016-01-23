import * as _ from 'lodash';
import HougekiTable from './HougekiTable';
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

type ComparableArray = JavaArray<any>;
type ComparableArrayArray = JavaArray<ComparableArray>;

(function () {

    this.begin = function begin() {
    }

    this.end = function end() {
    }

    this.header = function header() {
        return HougekiTable.header();
    }

    this.body = function body(battleExDto: BattleExDto) {
        return HougekiTable.body(battleExDto);
    }
})();