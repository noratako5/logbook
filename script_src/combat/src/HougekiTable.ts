import * as _ from 'lodash';
import HougekiRow from './HougekiRow';
import PhaseStatus from './PhaseStatus';
import toComparable from './toComparable';
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
import PhaseApi = combat.PhaseApi;

type ComparableArray = JavaArray<any>;
type ComparableArrayArray = JavaArray<ComparableArray>;

export default class HougekiTable {

    static header() {
        return HougekiRow.header();
    }

    static body(battleExDto: BattleExDto) {
        var rows: any[] = [];
        var phaseDto = battleExDto.getPhase1();
        if (phaseDto != null) {
            var phaseKindDto = phaseDto.getKind();
            if (phaseKindDto != null) {
                if (!phaseKindDto.isNight()) {
                    var phaseJson = phaseDto.getJson();
                    if (phaseJson != null) {
                        var phaseApi = <PhaseApi>JSON.parse(phaseJson.toString());
                        if (phaseApi != null) {
                            var phaseStatus = new PhaseStatus(battleExDto, phaseDto);
                            rows.push.apply(rows, HougekiRow.body(battleExDto, phaseStatus, phaseDto, phaseApi, 1));
                            rows.push.apply(rows, HougekiRow.body(battleExDto, phaseStatus, phaseDto, phaseApi, 2));
                            rows.push.apply(rows, HougekiRow.body(battleExDto, phaseStatus, phaseDto, phaseApi, 3));
                        }
                    }
                }
            }
        }
        return toComparable(rows);
    }
}
