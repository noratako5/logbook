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
            let rows = [] as any[][];
            _.forEach(battleExDto.getPhaseList(), phaseDto => {
                if (phaseDto != null) {
                    let phaseJson = phaseDto.getJson();
                    if (phaseJson != null) {
                        if (phaseDto.isNight()) {
                            let phaseApi = JSON.parse(phaseJson.toString()) as NightPhaseApi;
                            rows.push(...HenseiRow.body(battleExDto, new PhaseStatus(battleExDto, phaseDto), phaseDto, phaseApi));
                        }
                        else {
                            let phaseApi = JSON.parse(phaseJson.toString()) as DayPhaseApi;
                            rows.push(...HenseiRow.body(battleExDto, new PhaseStatus(battleExDto, phaseDto), phaseDto, phaseApi));
                        }
                    }
                }
            });
            return toComparable(rows);
        }
    }

    export class HenseiRow {

        static header() {
            let row = _.clone(DayPhaseRow.header());
            row.push('昼戦|夜戦');
            for (var i = 1; i <= 6; ++i) {
                row.push(..._.map(ShipRow.header(), s => '自軍' + i + '.' + s));
            }
            //for (var i = 1; i <= 6; ++i) {
            //    row.push(..._.map(ShipRow.header(), s => '敵軍' + i + '.' + s));
            //}
            return row;
        }

        static body(battleExDto: BattleExDto, phaseStatus: PhaseStatus, phaseDto: BattleExDto.Phase, phaseApi: DayPhaseApi | NightPhaseApi) {
            let row: any[];
            let ships = new Ships(battleExDto, phaseStatus, phaseStatus.firstFleetsStatus);
            if (phaseDto.isNight()) {
                row = NightPhaseRow.body(battleExDto, phaseDto, phaseApi as NightPhaseApi, ships.itemInfos);
                row.push('夜戦');
            }
            else {
                row = DayPhaseRow.body(battleExDto, phaseDto, phaseApi as DayPhaseApi, ships.itemInfos);
                row.push('昼戦');
            }
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
