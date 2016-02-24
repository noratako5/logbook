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
            return RaigekiRow.header();
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
                                var phaseStatus = new PhaseStatus(battleExDto, phaseDto);
                                rows.push.apply(rows, RaigekiRow.body(battleExDto, phaseStatus, phaseDto, phaseApi, 1));
                                rows.push.apply(rows, RaigekiRow.body(battleExDto, phaseStatus, phaseDto, phaseApi, 2));
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
            var row = _.clone(DayPhaseRow.header());
            row.push.apply(row, [
                '戦闘種別'
                , '自艦隊'
                , '開幕/閉幕'
                , '攻撃艦'
                , '種別'
                , '表示装備1'
                , '表示装備2'
                , '表示装備3'
                , 'クリティカル'
                , 'ダメージ'
                , 'かばう'
            ]);
            row.push.apply(row, _.map(ShipRow.header(), (s) => ('攻撃艦.' + s)));
            row.push.apply(row, _.map(ShipRow.header(), (s) => ('防御艦.' + s)));
            return row;
        }

        static body(battleExDto: BattleExDto, phaseStatus: PhaseStatus, phaseDto: BattleExDto.Phase, phaseApi: DayPhaseApi, raigekiIndex: number) {
            if (raigekiIndex === 1) {
                var ships = new Ships(battleExDto, phaseStatus, phaseStatus.openingFleetsStatus);
                var api_raigeki = phaseApi.api_opening_atack;
                var isSecond = phaseDto.getKind().isOpeningSecond();
                var stage = '開幕';
            }
            else if (raigekiIndex === 2) {
                var ships = new Ships(battleExDto, phaseStatus, phaseStatus.raigekiFleetsStatus);
                var api_raigeki = phaseApi.api_raigeki;
                var isSecond = phaseDto.getKind().isRaigekiSecond();
                var stage = '閉幕';
            }
            var phaseRow = DayPhaseRow.body(battleExDto, phaseDto, phaseApi, ships.itemInfos);
            if (isSecond) {
                var friendShips = battleExDto.getDockCombined().getShips();
                var friendShipRows = ships.friendCombinedShipRows;
            }
            else {
                var friendShips = battleExDto.getDock().getShips();
                var friendShipRows = ships.friendRows;
            }
            var enemyShips = battleExDto.getEnemy();
            var enemyShipRows = ships.enemyRows;
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
                var construct = (atShipRows: any[][], dfShipRows: any[][], api_rai: number[], api_ydam: number[], api_cl: number[], atackFleetName: string) => {
                    var rows = [];
                    for (var i = 1; i <= 6; ++i) {
                        var row = _.clone(phaseRow);
                        var cl = JavaInteger.valueOf(api_cl[i]);
                        var ydam = JavaInteger.valueOf(api_ydam[i]);
                        if (cl >= 0) {
                            row.push('雷撃戦');
                            row.push(fleetName);
                            row.push(stage);
                            row.push(atackFleetName);
                            row.push(null);
                            row.push(null);
                            row.push(null);
                            row.push(null);
                            row.push(cl);
                            row.push(ydam);
                            row.push(ydam != api_ydam[i] ? 1 : 0);
                            row.push.apply(row, atShipRows[i - 1]);
                            row.push.apply(row, dfShipRows[api_rai[i] - 1]);
                            rows.push(row);
                        }
                    }
                    return rows;
                };
                rows.push.apply(rows, construct(friendShipRows, enemyShipRows, api_raigeki.api_frai, api_raigeki.api_fydam, api_raigeki.api_fcl, '自軍'));
                rows.push.apply(rows, construct(enemyShipRows, friendShipRows, api_raigeki.api_erai, api_raigeki.api_eydam, api_raigeki.api_ecl, '敵軍'));
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
