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
    import AirBattleDto = Packages.logbook.dto.AirBattleDto;

    type ComparableArray = JavaArray<any>;
    type ComparableArrayArray = JavaArray<ComparableArray>;

    export class AirTable {

        static header() {
            return AirRow.header();
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
                                rows.push.apply(rows, AirRow.body(battleExDto, phaseStatus, phaseDto, phaseApi, 1));
                                rows.push.apply(rows, AirRow.body(battleExDto, phaseStatus, phaseDto, phaseApi, 2));
                            }
                        }
                    }
                }
            }
            return toComparable(rows);
        }
    }
    export class AirRow {

        static header() {
            var row = _.clone(DayPhaseRow.header());
            row.push.apply(row, [
                'ステージ1.自艦載機総数'
                , 'ステージ1.自艦載機喪失数'
                , 'ステージ1.敵艦載機総数'
                , 'ステージ1.敵艦載機喪失数'
                , 'ステージ2.自艦載機総数'
                , 'ステージ2.自艦載機喪失数'
                , 'ステージ2.敵艦載機総数'
                , 'ステージ2.敵艦載機喪失数'
                , '対空カットイン.発動艦'
                , '対空カットイン.種別'
                , '対空カットイン.表示装備1'
                , '対空カットイン.表示装備2'
                , '対空カットイン.表示装備3'
                , '味方雷撃被タゲ数'
                , '味方爆撃被タゲ数'
                , '敵雷撃被タゲ数'
                , '敵爆撃被タゲ数'
            ]);
            for (var i = 1; i <= 6; ++i) {
                row.push.apply(row, _.map(ShipRow.header(), (y) => ('敵艦' + i + '.' + y)));
            }
            for (var i = 1; i <= 6; ++i) {
                row.push.apply(row, _.map(ShipRow.header(), (y) => ('味方艦' + i + '.' + y)));
            }
            for (var i = 1; i <= 6; ++i) {
                row.push.apply(row, _.map(ShipRow.header(), (y) => ('連合第二艦隊艦' + i + '.' + y)));
            }
            row.push.apply(row, ['艦隊種類']);
            return row;
        }

        static body(battleExDto: BattleExDto, phaseStatus: PhaseStatus, phaseDto: BattleExDto.Phase, phaseApi: DayPhaseApi, airIndex: number) {
            var rows: any[][] = [];
            if (airIndex === 1) {
                var ships = new Ships(battleExDto, phaseStatus, phaseStatus.airFleetsStatus);
                var api_kouku = phaseApi.api_kouku;
            }
            else if (airIndex === 2) {
                var ships = new Ships(battleExDto, phaseStatus, phaseStatus.air2FleetsStatus);
                var api_kouku = phaseApi.api_kouku2;
            }
            if (api_kouku != null) {
                var combinedFlag = battleExDto.getCombinedFlag();
                if (combinedFlag === 0) {
                    var combinedFlagString = '通常艦隊';
                }
                else if (combinedFlag === 1) {
                    var combinedFlagString = '機動部隊';
                }
                else if (combinedFlag === 2) {
                    var combinedFlagString = '水上部隊';
                }
                else if (combinedFlag === 3) {
                    var combinedFlagString = '輸送部隊';
                }
                else {
                    var combinedFlagString = '不明';
                }
                var api_plane_from = api_kouku.api_plane_from;
                if (api_plane_from != null) {
                    var f_plane_from = _.map(_.range(0, 6), () => 0);
                    _.forEach(api_plane_from[0], (i) => {
                        if (i >= 1) {
                            f_plane_from[i - 1] = 1;
                        }
                    });
                    var e_plane_from = _.map(_.range(0, 6), () => 0);
                    _.forEach(api_plane_from[1], (i) => {
                        if (i >= 1) {
                            e_plane_from[i - 1] = 1;
                        }
                    });
                }
                var row = _.clone(DayPhaseRow.body(battleExDto, phaseDto, phaseApi, ships.itemInfos));
                var api_stage1 = api_kouku.api_stage1;
                if (api_stage1 != null) {
                    var stage1_f_count = api_stage1.api_f_count;
                    var stage1_f_lostcount = api_stage1.api_f_lostcount;
                    var stage1_e_count = api_stage1.api_e_count;
                    var stage1_e_lostcount = api_stage1.api_e_lostcount;
                    if (stage1_f_count == 0 && stage1_e_count == 0) {
                        return rows;
                    }
                }
                row.push(stage1_f_count);
                row.push(stage1_f_lostcount);
                row.push(stage1_e_count);
                row.push(stage1_e_lostcount);
                var api_stage2 = api_kouku.api_stage2;
                if (api_stage2 != null) {
                    var stage2_f_count = api_stage2.api_f_count;
                    var stage2_f_lostcount = api_stage2.api_f_lostcount;
                    var stage2_e_count = api_stage2.api_e_count;
                    var stage2_e_lostcount = api_stage2.api_e_lostcount;
                    var api_air_fire = api_kouku.api_stage2.api_air_fire;
                    if (api_air_fire != null) {
                        var idx = 1 + api_air_fire.api_idx;
                        var kind = api_air_fire.api_kind;
                        var use_item0 = ships.itemInfos.getName(api_air_fire.api_use_items[0]);
                        var use_item1 = ships.itemInfos.getName(api_air_fire.api_use_items[1]);
                        var use_item2 = ships.itemInfos.getName(api_air_fire.api_use_items[2]);
                    }
                }
                row.push(stage2_f_count);
                row.push(stage2_f_lostcount);
                row.push(stage2_e_count);
                row.push(stage2_e_lostcount);
                row.push(idx);
                row.push(kind);
                row.push(use_item0);
                row.push(use_item1);
                row.push(use_item2);
                var api_stage3 = api_kouku.api_stage3;
                var frai_count = 0;
                var erai_count = 0;
                var fbak_count = 0;
                var ebak_count = 0;
                if (api_stage3 != null) {
                    var frai_flag = api_stage3.api_frai_flag;
                    var erai_flag = api_stage3.api_erai_flag;
                    var fbak_flag = api_stage3.api_fbak_flag;
                    var ebak_flag = api_stage3.api_ebak_flag;
                    if (frai_flag != null) { for (var i = 0; i < frai_flag.length; ++i) { if (frai_flag[i] == 1) { ++frai_count; } } }
                    if (erai_flag != null) { for (var i = 0; i < erai_flag.length; ++i) { if (erai_flag[i] == 1) { ++erai_count; } } }
                    if (fbak_flag != null) { for (var i = 0; i < fbak_flag.length; ++i) { if (fbak_flag[i] == 1) { ++fbak_count; } } }
                    if (ebak_flag != null) { for (var i = 0; i < ebak_flag.length; ++i) { if (ebak_flag[i] == 1) { ++ebak_count; } } }
                }
                var api_stage3_combined = api_kouku.api_stage3_combined
                if (api_stage3_combined != null) {
                    var frai_flag = api_stage3_combined.api_frai_flag;
                    var fbak_flag = api_stage3_combined.api_fbak_flag;
                    for (var i = 0; i < frai_flag.length; ++i) { if (frai_flag[i] == 1) { ++frai_count; } }
                    for (var i = 0; i < fbak_flag.length; ++i) { if (fbak_flag[i] == 1) { ++fbak_count; } }
                }
                row.push(frai_count);
                row.push(fbak_count);
                row.push(erai_count);
                row.push(ebak_count);
                for (var i = 0; i < 6; ++i) { row.push.apply(row, ships.enemyRows[i]); }
                for (var i = 0; i < 6; ++i) { row.push.apply(row, ships.friendRows[i]); }
                for (var i = 0; i < 6; ++i) { row.push.apply(row, ships.friendCombinedShipRows[i]); }
                row.push.apply(row, [combinedFlagString]);
                rows.push(row);
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
    return combat.AirTable.header();
}

function body(battleExDto: Packages.logbook.dto.BattleExDto) {
    return combat.AirTable.body(battleExDto);
}
