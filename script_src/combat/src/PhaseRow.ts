import * as _ from 'lodash';
import ItemInfos from './ItemInfos';
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
import PhaseApi = combat.PhaseApi;

type ComparableArray = JavaArray<any>;
type ComparableArrayArray = JavaArray<ComparableArray>;

export default class PhaseRow {

    static header() {
        return [
            '日付'
            , '海域'
            , 'マス'
            , '出撃'
            , 'ランク'
            , '敵艦隊'
            , '提督レベル'
            , '自陣形'
            , '敵陣形'
            , '自索敵'
            , '敵索敵'
            , '制空権'
            , '会敵'
            , '自触接'
            , '敵触接'
            , '自照明弾'
            , '敵照明弾'
        ];
    }

    static body(battleExDto: BattleExDto, phaseDto: BattleExDto.Phase, phaseApi: PhaseApi, itemInfos: ItemInfos) {
        var row: any[] = [];
        var battleDate = battleExDto.getBattleDate();
        if (battleDate != null) {
            var battleDateTimeString = new DateTimeString(battleDate);
        }
        row.push(battleDateTimeString);
        row.push(battleExDto.getQuestName());
        var mapCellDto = battleExDto.getMapCellDto();
        if (mapCellDto != null) {
            var reportString = mapCellDto.getReportString();
            var bossTexts: string[] = [];
            if (mapCellDto.isStart()) {
                bossTexts.push('出撃');
            }
            if (mapCellDto.isBoss()) {
                bossTexts.push('ボス');
            }
            var bossText = bossTexts.join('&');
        }
        row.push(reportString);
        row.push(bossText);
        row.push(battleExDto.getRank());
        row.push(battleExDto.getEnemyName());
        row.push(battleExDto.getHqLv());
        var formation = battleExDto.getFormation();
        if (formation != null) {
            var formation0 = formation[0];
            var formation1 = formation[1];
        }
        row.push(formation0);
        row.push(formation1);
        var sakuteki = battleExDto.getSakuteki();
        if (sakuteki != null) {
            var sakuteki0 = sakuteki[0];
            var sakuteki1 = sakuteki[1];
        }
        row.push(sakuteki0);
        row.push(sakuteki1);
        var phaseDto = battleExDto.getPhase1();
        if (phaseDto != null) {
            var airBattleDto = phaseDto.getAir();
            if (airBattleDto != null) {
                var seiku = airBattleDto.seiku;
            }
        }
        row.push(seiku);
        row.push(battleExDto.getFormationMatch());
        var touchPlane = phaseDto.getTouchPlane();
        if (touchPlane != null) {
            var touchPlane0 = itemInfos.getName(touchPlane[0]);
            var touchPlane1 = itemInfos.getName(touchPlane[1]);
        }
        row.push(touchPlane0);
        row.push(touchPlane1);
        var api_flare_pos = phaseApi.api_flare_pos;
        if (api_flare_pos != null) {
            var api_flare_pos0 = api_flare_pos[0];
            if (api_flare_pos0 >= 0) {
                var flarePos0 = api_flare_pos0;
            }
            var api_flare_pos1 = api_flare_pos[1];
            if (api_flare_pos1 >= 0) {
                var flarePos1 = api_flare_pos1;
            }
        }
        row.push(flarePos0);
        row.push(flarePos1);
        return row;
    }
}
