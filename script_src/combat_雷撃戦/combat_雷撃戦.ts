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

    export class RaigekiTable {

        static header() {
            var row = DayPhaseRow.header();
            row.push.apply(row, RaigekiRow.header());
            return row;
        }

        static body(battleExDto: BattleExDto) {
            var rows = [];
            var phaseDto = battleExDto.getPhase1();
            if (phaseDto != null) {
                var phaseKindDto = phaseDto.getKind();
                if (phaseKindDto != null) {
                    if (!phaseKindDto.isNight()) {
                        var phaseJson = phaseDto.getJson();
                        if (phaseJson != null) {
                            var phaseApi = <DayPhaseApi>JSON.parse(phaseJson.toString());
                            if (phaseApi != null) {
                                var ships = new Ships(battleExDto);
                                var phaseRow = DayPhaseRow.body(battleExDto, phaseDto, phaseApi, ships.itemInfos);
                                rows.push.apply(rows, RaigekiRow.body(battleExDto, ships, phaseDto, phaseApi, 1));
                                rows.push.apply(rows, RaigekiRow.body(battleExDto, ships, phaseDto, phaseApi, 2));
                                _.forEach(rows, (row) => (row.unshift.apply(row, phaseRow)));
                            }
                        }
                    }
                }
            }
            return toComparable(rows);
        }
    }

    export class RaigekiRow {

        static header() {
            var row = [
                '自艦隊'
                , 'クリティカル'
                , 'ダメージ'
                , 'かばう'
            ];
            row.push.apply(row, _.map(ShipRow.header(), (s) => ('攻撃艦.' + s)));
            row.push.apply(row, _.map(ShipRow.header(), (s) => ('防御艦.' + s)));
            return row;
        }

        static body(battleExDto: BattleExDto, ships: Ships, phaseDto: BattleExDto.Phase, phaseApi: DayPhaseApi, raigekiIndex: number) {
            if (raigekiIndex === 1) {
                var api_raigeki = phaseApi.api_opening_atack;
                var isSecond = phaseDto.getKind().isOpeningSecond();
            }
            else if (raigekiIndex === 2) {
                var api_raigeki = phaseApi.api_raigeki;
                var isSecond = phaseDto.getKind().isRaigekiSecond();
            }
            if (isSecond) {
                var friendShipRows = ships.friendCombinedShipRows;
            }
            else {
                var friendShipRows = ships.friendRows;
            }
            if (battleExDto.isCombined()) {
                if (isSecond) {
                    var fleetName = '連合第2艦隊';
                }
                else {
                    var fleetName = '連合第1艦隊';
                }
            }
            else {
                var fleetName = '通常艦隊';
            }
            var rows = [];
            if (api_raigeki != null) {
                var construct = (atShipRows: any[][], dfShipRows: any[][], api_rai: number[], api_ydam: number[], api_cl: number[]) => {
                    var rows = [];
                    for (var i = 1; i <= 6; ++i) {
                        var row = [];
                        var cl = JavaInteger.valueOf(api_cl[i]);
                        var ydam = JavaInteger.valueOf(api_ydam[i]);
                        row.push(fleetName);
                        row.push(cl);
                        row.push(ydam);
                        row.push(ydam != api_ydam[i] ? 1 : 0);
                        row.push.apply(row, atShipRows[i - 1]);
                        row.push.apply(row, dfShipRows[api_rai[i] - 1]);
                        rows.push(row);
                    }
                    return rows;
                };
                rows.push.apply(rows, construct(friendShipRows, ships.enemyRows, api_raigeki.api_frai, api_raigeki.api_fydam, api_raigeki.api_fcl));
                rows.push.apply(rows, construct(ships.enemyRows, friendShipRows, api_raigeki.api_erai, api_raigeki.api_eydam, api_raigeki.api_ecl));
            }
            return rows;
        }
    }
}

function begin() {
}

function end() {
}

function header() {
    return combat.RaigekiTable.header();
}

function body(battleExDto: Packages.logbook.dto.BattleExDto) {
    return combat.RaigekiTable.body(battleExDto);
}
