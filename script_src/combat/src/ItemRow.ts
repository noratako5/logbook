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

export default class ItemRow {

    static header() {
        var row: string[] = [];
        for (var i = 1; i <= 5; ++i) {
            row.push.apply(row, _.map([
                '名前'
                , '改修'
                , '熟練度'
                , '搭載数'
            ], (s) => ('装備' + i + '.' + s)));
        }
        return row;
    }

    static body(shipBaseDto: ShipBaseDto) {
        var construct = (itemDto: ItemDto, itemInfoDto: ItemInfoDto, onSlot: number) => {
            if (itemInfoDto != null) {
                var name = itemInfoDto.getName();
                var level = (itemDto != null) ? itemDto.getLevel() : null;
                var alv = (itemDto != null) ? itemDto.getAlv() : null;
            }
            return [
                name
                , level
                , alv
                , onSlot
            ];
        }
        var row: any[] = [];
        if (shipBaseDto != null) {
            if (shipBaseDto instanceof ShipDto) {
                var shipDto = <ShipDto>(shipBaseDto);
                var itemDtos = shipDto.getItem2();
                var itemExDto = shipDto.getSlotExItem();
                if (itemExDto != null) {
                    var itemInfoExDto = itemExDto.getInfo();
                }
            }
            var itemInfoDtos = shipBaseDto.getItem();
            var onSlots = shipBaseDto.getOnSlot();
        }
        for (var i = 0; i < 5; ++i) {
            if (i === 4 && itemExDto != null && itemInfoExDto != null) {
                var itemRow = construct(itemExDto, itemInfoExDto, null);
            }
            else if (itemInfoDtos != null && i < itemInfoDtos.length) {
                if (onSlots != null && i < onSlots.length) {
                    var onSlot = onSlots[i];
                }
                if (itemDtos != null && i < itemDtos.length) {
                    var itemRow = construct(itemDtos[i], itemInfoDtos[i], onSlot);
                }
                else {
                    var itemRow = construct(null, itemInfoDtos[i], onSlot);
                }
            }
            else {
                var itemRow = construct(null, null, null);
            }
            row.push.apply(row, itemRow);
        }
        return row;
    }
}
