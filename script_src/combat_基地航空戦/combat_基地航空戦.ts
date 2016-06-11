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

    export class BaseAirTable {

        static header() {
            return BaseAirRow.header();
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
                                if (phaseApi.api_air_base_attack != null) {
                                    for (var i= 0; i < phaseApi.api_air_base_attack.length; i+=1){
                                        rows.push.apply(rows, BaseAirRow.body(battleExDto, phaseStatus, phaseDto, phaseApi, i));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return toComparable(rows);
        }
    }

    export class BaseShipsSummary extends ShipsBase {

        constructor(battleExDto: BattleExDto, phaseStatus: PhaseStatus, fleetsStatus: FleetsStatus) {
            super(battleExDto, phaseStatus, fleetsStatus);
        }

        protected createShipRow(shipBaseDto: ShipBaseDto, hp: number) {
            return BaseShipSummaryRow.body(shipBaseDto, hp);
        }
    }

    export class BaseShipSummaryRow {

        static header() {
            var row: string[] = [];
            row.push.apply(row, [
                'ID'
                , '名前'
                , 'Lv'
            ]);
            //row.push.apply(row, ItemRow.header());
            return row;
        }

        static body(shipBaseDto: ShipBaseDto, hp: number) {
            var row: any[] = [];
            if (shipBaseDto != null) {
                var shipInfoDto = shipBaseDto.getShipInfo();
                if (shipInfoDto != null) {
                    var shipId = shipInfoDto.getShipId();
                    var fullName = shipInfoDto.getFullName();
                }
                var lv = shipBaseDto.getLv();
            }
            row.push(shipId);
            row.push(fullName);
            row.push(lv);
            //row.push.apply(row, ItemRow.body(shipBaseDto));
            return row;
        }
    }

    export class BaseAirRow {

        static header() {
            var row = _.clone(DayPhaseRow.header());
            row.push.apply(row, ['航空隊', '攻撃順', '基地自触接','基地敵触接']);
            for (var i = 1; i <= 4; ++i) {
                row.push.apply(row, ['第' + i + '中隊', '第' + i + '機数']);
            }
            row.push.apply(row, [
                'ステージ1.自艦載機総数'
                , 'ステージ1.自艦載機喪失数'
                , 'ステージ1.敵艦載機総数'
                , 'ステージ1.敵艦載機喪失数'
                , 'ステージ2.自艦載機総数'
                , 'ステージ2.自艦載機喪失数'
                , 'ステージ2.敵艦載機総数'
                , 'ステージ2.敵艦載機喪失数'
            ]);
            for (var i = 1; i <= 6; ++i) {
                var shipRow: any[] = [];
                shipRow.push(...BaseShipSummaryRow.header());
                row.push.apply(row, _.map(shipRow, (y) => ('攻撃艦' + i + '.' + y)));
            }
            row.push(...[
                '雷撃'
                , '爆撃'
                , 'クリティカル'
                , 'ダメージ'
                , 'かばう'
            ]);
            row.push(..._.map(ShipRow.header(), y => ('防御艦.' + y)));
            return row;
        }

        static body(battleExDto: BattleExDto, phaseStatus: PhaseStatus, phaseDto: BattleExDto.Phase, phaseApi: DayPhaseApi, airIndex: number) {
            var rows: any[][] = [];
            var ships = new Ships(battleExDto, phaseStatus, phaseStatus.baseAirStatus[airIndex]);
            var shipsSummary = new BaseShipsSummary(battleExDto, phaseStatus, phaseStatus.baseAirStatus[airIndex]);
            var api_kouku = phaseApi.api_air_base_attack[airIndex];
            if (api_kouku != null && api_kouku.api_stage3 != null) {
                var api_plane_from = api_kouku.api_plane_from;
                if (api_plane_from != null) {
                    var f_plane_from = _.map(_.range(0, 6), () => 0);
                    var e_plane_from = _.map(_.range(0, 6), () => 0);
                    _.forEach(api_plane_from[0], (i) => {
                        if (i >= 1) {
                            e_plane_from[i - 1] = 1;
                        }
                    });
                }
                var row = _.clone(DayPhaseRow.body(battleExDto, phaseDto, phaseApi, shipsSummary.itemInfos));
                row.push(api_kouku.api_base_id);
                var index = ([1, 2, 3, 4, 5, 6, 7, 8])[airIndex];//なぜかこうしないと浮動小数になってしまう
                row.push(index);
                row.push(shipsSummary.itemInfos.getName(api_kouku.api_stage1.api_touch_plane[0]));
                row.push(shipsSummary.itemInfos.getName(api_kouku.api_stage1.api_touch_plane[1]));
                for (var i = 0; i < 4; ++i) {
                    if (api_kouku.api_squadron_plane != null && i < api_kouku.api_squadron_plane.length) {
                        var baseAir = phaseDto.getAirBase()[airIndex];
                        row.push(baseAir.getBasePlane(api_kouku.api_squadron_plane[i].api_mst_id));
                        row.push(api_kouku.api_squadron_plane[i].api_count);
                    }
                    else {
                        row.push(null);
                        row.push(null);
                    }
                }
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
                }
                row.push(stage2_f_count);
                row.push(stage2_f_lostcount);
                row.push(stage2_e_count);
                row.push(stage2_e_lostcount);
                var api_stage3 = api_kouku.api_stage3;
                var frai_flag = [-1,0,0,0,0,0,0];
                var erai_flag = api_stage3.api_erai_flag;
                var fbak_flag = [-1, 0, 0, 0, 0, 0, 0];
                var ebak_flag = api_stage3.api_ebak_flag;
                var fcl_flag = [-1, 0, 0, 0, 0, 0, 0];
                var ecl_flag = api_stage3.api_ecl_flag;
                var fdam = [-1, 0, 0, 0, 0, 0, 0];
                var edam = api_stage3.api_edam;
                var construct = (row: any[], atShipRows: any[][], dfShipRows: any[][], plane_from: number[], rai_flag: number[], bak_flag: number[], cl_flag: number[], dam: number[]) => {
                    var rows: any[][] = [];
                    row = row.concat(...atShipRows);
                    for (var i = 0; i < 6; ++i) {
                        var innerRow: any[] = _.clone(row);
                        if (rai_flag != null) {
                            var rai = JavaInteger.valueOf(rai_flag[i + 1]);
                        }
                        innerRow.push(rai);
                        if (bak_flag != null) {
                            var bak = JavaInteger.valueOf(bak_flag[i + 1]);
                        }
                        innerRow.push(bak);
                        if (cl_flag != null) {
                            var cl = JavaInteger.valueOf(cl_flag[i + 1]);
                        }
                        innerRow.push(cl);
                        if (dam != null) {
                            var d = JavaInteger.valueOf(dam[i + 1]);
                            var protects = d != dam[i + 1] ? 1 : 0;
                        }
                        innerRow.push(d);
                        innerRow.push(protects);
                        rows.push(innerRow.concat(dfShipRows[i]));
                    }
                    return rows;
                };
                rows.push(...construct(row, shipsSummary.friendRows, ships.enemyRows, f_plane_from, erai_flag, ebak_flag, ecl_flag, edam));
                //rows.push(...construct(row, shipsSummary.enemyRows, ships.friendRows, e_plane_from, frai_flag, fbak_flag, fcl_flag, fdam));
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
    return combat.BaseAirTable.header();
}

function body(battleExDto: Packages.logbook.dto.BattleExDto) {
     return combat.BaseAirTable.body(battleExDto);
}
