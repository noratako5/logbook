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

export default class ItemInfos {

    dtos: { [id: number]: ItemInfoDto } = {};

    getName(id: number) {
        var dto = this.dtos[id];
        if (dto != null) {
            return dto.getName();
        }
    }
}
