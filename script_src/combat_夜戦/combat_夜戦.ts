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
    import BattlePhaseKind = Packages.logbook.dto.BattlePhaseKind;

    type ComparableArray = JavaArray<any>;
    type ComparableArrayArray = JavaArray<ComparableArray>;

    export class NightTable {

        static header() {
            return NightRow.header();
        }

        static body(battleExDto: BattleExDto) {
            var rows: any[][] = [];
            var phase1Dto = battleExDto.getPhase1();
            if (phase1Dto != null) {
                var phase1KindDto = phase1Dto.getKind();
                if (phase1KindDto != null && phase1KindDto.isNight()) {
                    var phaseDto = phase1Dto;
                }
                else {
                    var phase2Dto = battleExDto.getPhase2();
                    if (phase2Dto != null) {
                        var phase2KindDto = phase2Dto.getKind();
                        if (phase2KindDto != null && phase2KindDto.isNight()) {
                            var phaseDto = phase2Dto;
                        }
                    }
                }
            }
            if (phaseDto != null) {
                var phaseJson = phaseDto.getJson();
                if (phaseJson != null) {
                    var phaseApi = <NightPhaseApi>JSON.parse(phaseJson.toString());
                    if (phaseApi != null) {
                        var phaseStatus = new PhaseStatus(battleExDto, phaseDto);
                        rows.push.apply(rows, NightRow.body(battleExDto, phaseStatus, phaseDto, phaseApi));
                    }
                }
            }
            return toComparable(rows);
        }
    }

    export class NightRow {

        static header() {
            var row = _.clone(NightPhaseRow.header());
            row.push.apply(row, [
                '戦闘種別'
                , '自艦隊'
                , '開始'
                , '攻撃艦'
                , '砲撃種別'
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

        static body(battleExDto: BattleExDto, phaseStatus: PhaseStatus, phaseDto: BattleExDto.Phase, phaseApi: NightPhaseApi) {
            var api_hougeki = phaseApi.api_hougeki;
            var isSecond = phaseDto.getKind().isHougekiSecond();
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
            var rows = <any[][]>[];
            if (api_hougeki != null) {
                for (var i = 1; i < api_hougeki.api_at_list.length; ++i) {
                    var api_at = api_hougeki.api_at_list[i];
                    var api_sp = api_hougeki.api_sp_list[i];
                    var api_df_list = api_hougeki.api_df_list[i];
                    var api_si_list = api_hougeki.api_si_list[i];
                    var api_cl_list = api_hougeki.api_cl_list[i];
                    var api_damage = api_hougeki.api_damage[i];
                    for (var j = 0; j < api_df_list.length; ++j) {
                        var ships = new Ships(battleExDto, phaseStatus, phaseStatus.hougekiFleetsStatusList[i - 1][j]);
                        var phaseRow = NightPhaseRow.body(battleExDto, phaseDto, phaseApi, ships.itemInfos);
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
                        if (api_at < 7) {
                            var itemInfoDtos = friendShips[api_at - 1].getItem();
                            var atackFleetName = '自軍';
                        }
                        else {
                            var itemInfoDtos = enemyShips[api_at - 7].getItem();
                            var atackFleetName = '敵軍';
                        }
                        var itemNames = _.map(api_si_list, (api_si) => {
                            var itemDto = _.find(itemInfoDtos, (itemInfoDto) => itemInfoDto != null ? itemInfoDto.getId() == api_si : false);
                            if (itemDto != null) {
                                return itemDto.getName();
                            }
                            else {
                                return null;
                            }
                        });
                        var api_df = api_df_list[j];
                        var cl = JavaInteger.valueOf(api_cl_list[j]);
                        if (cl >= 0) {
                            var damage = JavaInteger.valueOf(api_damage[j]);
                            var row = _.clone(phaseRow);
                            row.push.apply(row, [
                                '夜戦'
                                , fleetName
                                , phaseDto.getKind() == BattlePhaseKind.SP_MIDNIGHT ? '夜戦開始' : '昼戦開始'
                                , atackFleetName
                                , api_sp
                                , itemNames[0]
                                , itemNames[1]
                                , itemNames[2]
                                , cl
                                , damage
                                , damage != api_damage[j] ? 1 : 0
                            ]);
                            if (api_at < 7) {
                                row.push.apply(row, friendShipRows[api_at - 1]);
                            }
                            else {
                                row.push.apply(row, enemyShipRows[api_at - 7]);
                            }
                            if (api_df < 7) {
                                row.push.apply(row, friendShipRows[api_df - 1]);
                            }
                            else {
                                row.push.apply(row, enemyShipRows[api_df - 7]);
                            }
                            rows.push(row);
                        }
                    }
                }
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
    return combat.NightTable.header();
}

function body(battleExDto: Packages.logbook.dto.BattleExDto) {
    return combat.NightTable.body(battleExDto);
}
