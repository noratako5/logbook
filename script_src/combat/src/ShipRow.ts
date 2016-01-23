import * as _ from 'lodash';
import ItemRow from './ItemRow';
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

export default class ShipRow {

    static header() {
        var row = [
            '編成順'
            , 'ID'
            , '名前'
            , '種別'
            , '疲労'
            , '残耐久'
            , '最大耐久'
            , '損傷'
            , '残燃料'
            , '最大燃料'
            , '残弾薬'
            , '最大弾薬'
            , 'Lv'
            , '速力'
            , '火力'
            , '雷装'
            , '対空'
            , '装甲'
            , '回避'
            , '対潜'
            , '索敵'
            , '運'
            , '射程'
        ];
        row.push.apply(row, ItemRow.header());
        return row;
    }

    static body(shipBaseDto: ShipBaseDto, hp: number, maxHp: number, index: number) {
        if (shipBaseDto != null) {
            var row: any[] = [];
            var shipInfoDto = shipBaseDto.getShipInfo();
            if (shipInfoDto != null) {
                var shipId = shipInfoDto.getShipId();
                var fullName = shipInfoDto.getFullName();
                var type = shipInfoDto.getType();
                var maxFuel = shipInfoDto.getMaxFuel();
                var maxBull = shipInfoDto.getMaxBull();
            }
            if (shipBaseDto instanceof ShipDto) {
                var shipDto = <ShipDto>shipBaseDto;
                var cond = shipDto.getCond();
                var fuel = shipDto.getFuel();
                var bull = shipDto.getBull();
            }
            var shipParamDto = shipBaseDto.getParam();
            if (shipParamDto != null) {
                switch (shipParamDto.getSoku()) {
                    case 0:
                        var soku = '陸上';
                        break;
                    case 5:
                        var soku = '低速';
                        break;
                    case 10:
                        var soku = '高速';
                        break;
                }
                var houg = shipParamDto.getHoug();
                var raig = shipParamDto.getRaig();
                var taik = shipParamDto.getTaik();
                var souk = shipParamDto.getSouk();
                var kaih = shipParamDto.getKaih();
                var tais = shipParamDto.getTais();
                var saku = shipParamDto.getSaku();
                var luck = shipParamDto.getLuck();
                switch (shipParamDto.getLeng()) {
                    case 0:
                        var leng = '超短';
                        break;
                    case 1:
                        var leng = '短';
                        break;
                    case 2:
                        var leng = '中';
                        break;
                    case 3:
                        var leng = '長';
                        break;
                    case 4:
                        var leng = '超長';
                        break;
                }
            }
            var lv = shipBaseDto.getLv();
            var hpRate = 4 * hp / maxHp;
            if (hpRate > 3) {
                var hpText = '小破未満';
            }
            else if (hpRate > 2) {
                var hpText = '小破';
            }
            else if (hpRate > 1) {
                var hpText = '中破';
            }
            else if (hpRate > 0) {
                var hpText = '大破';
            }
            else {
                var hpText = '轟沈';
            }
            row.push(JavaInteger.valueOf(index));
            row.push(shipId);
            row.push(fullName);
            row.push(type);
            row.push(cond);
            row.push(hp);
            row.push(maxHp);
            row.push(hpText);
            row.push(fuel);
            row.push(maxFuel);
            row.push(bull);
            row.push(maxBull);
            row.push(lv);
            row.push(soku);
            row.push(houg);
            row.push(raig);
            row.push(taik);
            row.push(souk);
            row.push(kaih);
            row.push(tais);
            row.push(saku);
            row.push(luck);
            row.push(leng);
            row.push.apply(row, ItemRow.body(shipBaseDto));
            return row;
        }
        else {
            return new Array(this.header().length);
        }
    }
}
