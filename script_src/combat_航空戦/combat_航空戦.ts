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
            var row = DayPhaseRow.header();
            row.push.apply(row, AirRow.header());
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
                                rows.push.apply(rows, AirRow.body(battleExDto, ships, phaseDto.getAir(), phaseApi.api_kouku));
                                rows.push.apply(rows, AirRow.body(battleExDto, ships, phaseDto.getAir2(), phaseApi.api_kouku2));
                                _.forEach(rows, (row) => (row.unshift.apply(row, phaseRow)));
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
            var row = [
                'ステージ1.自艦載機総数'
                , 'ステージ1.自艦載機喪失数'
                , 'ステージ1.敵艦載機総数'
                , 'ステージ1.敵艦載機喪失数'
                , 'ステージ2.自艦載機総数'
                , 'ステージ2.自艦載機喪失数'
                , 'ステージ2.敵艦載機総数'
                , 'ステージ2.敵艦載機喪失数'
                , '対空カットイン.インデックス'
                , '対空カットイン.種別'
                , '対空カットイン.表示装備1'
                , '対空カットイン.表示装備2'
                , '対空カットイン.表示装備3'
            ];
            _.forEach(['自艦', '敵艦'], (x) => {
                for (var i = 1; i <= 6; ++i) {
                    var r = [
                        '被雷撃'
                        , '被爆撃'
                        , '被クリティカル'
                        , '被ダメージ'
                        , 'かばう'
                    ];
                    //r.push.apply(r, ShipRow.header());
                    row.push.apply(row, _.map(r, (y) => (x + i + '.' + y)));
                }
            });
            return row;
        }

        static body(battleExDto: BattleExDto, ships: Ships, battleAtackDtoList: AirBattleDto, api_kouku: AirBattleApi) {
            var rows = [];
            if (api_kouku != null) {
                var row = [];
                var api_stage1 = api_kouku.api_stage1;
                if (api_stage1 != null) {
                    var stage1_f_count = api_stage1.api_f_count;
                    var stage1_f_lostcount = api_stage1.api_f_lostcount;
                    var stage1_e_count = api_stage1.api_e_count;
                    var stage1_e_lostcount = api_stage1.api_e_lostcount;
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
                var construct = (shipRows: any[][], api_rai_flag: number[], api_bak_flag: number[], api_cl_flag: number[], api_dam: number[]) => {
                    var row = [];
                    for (var i = 1; i <= 6; ++i) {
                        if (api_rai_flag != null) {
                            var rai = JavaInteger.valueOf(api_rai_flag[i]);
                        }
                        row.push(rai);
                        if (api_bak_flag != null) {
                            var bak = JavaInteger.valueOf(api_bak_flag[i]);
                        }
                         row.push(bak);
                       if (api_cl_flag != null) {
                            var cl = JavaInteger.valueOf(api_cl_flag[i]);
                        }
                        row.push(cl);
                        if (api_dam != null) {
                            var dam = JavaInteger.valueOf(api_dam[i]);
                            var protects = dam != api_dam[i] ? 1 : 0;
                        }
                        row.push(dam);
                        row.push(protects);
                        //row.push.apply(row, shipRows[i - 1]);
                    }
                    return row;
                };
                var api_stage3 = api_kouku.api_stage3;
                if (api_stage3 != null) {
                    row.push.apply(row, construct(ships.friendRows, api_kouku.api_stage3.api_frai_flag, api_kouku.api_stage3.api_fbak_flag, api_kouku.api_stage3.api_fcl_flag, api_kouku.api_stage3.api_fdam));
                    row.push.apply(row, construct(ships.enemyRows, api_kouku.api_stage3.api_erai_flag, api_kouku.api_stage3.api_ebak_flag, api_kouku.api_stage3.api_ecl_flag, api_kouku.api_stage3.api_edam));
                }
                else {
                    row.push.apply(row, construct(ships.friendRows, null, null, null, null));
                    row.push.apply(row, construct(ships.enemyRows, null, null, null, null));
                }
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
