/// <reference path="../combat/combat.ts" />

module combat {
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

    export class HenseiTable {

        static header() {
            return HenseiRow.header();
        }

        static body(battleExDto: BattleExDto) {
            var rows: any[][] = [];
            var phaseDto = battleExDto.getPhase1();
            if (phaseDto != null) {
                var phaseKindDto = phaseDto.getKind();
                if (phaseKindDto != null) {
                    if (!phaseKindDto.isNight()) {
                        var phaseJson = phaseDto.getJson();
                        if (phaseJson != null) {
                            var phaseApi = <DayPhaseApi>JSON.parse(phaseJson.toString());
                            if (phaseApi != null) {
                                var phaseStatus = new PhaseStatus(battleExDto, phaseDto);
                                rows.push.apply(rows, HenseiRow.body(battleExDto, phaseStatus, phaseDto, phaseApi));
                            }
                        }
                    }
                }
            }
            return toComparable(rows);
        }
    }

    export class HenseiRow {

        static header() {
            var row = _.clone(DayPhaseRow.header());
            for (var i = 1; i <= 6; ++i) {
                row.push(..._.map(ShipRow.header(), s => '自軍' + i + '.' + s));
            }
            //for (var i = 1; i <= 6; ++i) {
            //    row.push(..._.map(ShipRow.header(), s => '敵軍' + i + '.' + s));
            //}
            return row;
        }

        static body(battleExDto: BattleExDto, phaseStatus: PhaseStatus, phaseDto: BattleExDto.Phase, phaseApi: DayPhaseApi) {
            var ships = new Ships(battleExDto, phaseStatus, phaseStatus.firstFleetsStatus);
            var row = DayPhaseRow.body(battleExDto, phaseDto, phaseApi, ships.itemInfos);
            row = row.concat(...ships.friendRows);
            //row = row.concat(...ships.enemyRows);
            return [ row ];
        }
    }
}

function begin() {
}

function end() {
}

function header() {
    return combat.HenseiTable.header();
}

function body(battleExDto: Packages.logbook.dto.BattleExDto) {
    return combat.HenseiTable.body(battleExDto);
}
