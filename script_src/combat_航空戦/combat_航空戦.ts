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

    export class ShipsSummary extends ShipsBase {

        constructor(battleExDto: BattleExDto, phaseStatus: PhaseStatus, fleetsStatus: FleetsStatus) {
            super(battleExDto, phaseStatus, fleetsStatus);
        }

        protected createShipRow(shipBaseDto: ShipBaseDto, hp: number) {
            return ShipSummaryRow.body(shipBaseDto, hp);
        }
    }

    export class ShipSummaryRow {

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
                , '発艦'
                , '被雷撃'
                , '被爆撃'
                , '被クリティカル'
                , '被ダメージ'
                , 'かばう'
            ]);
            for (var i = 1; i <= 6; ++i) {
                var shipRow: any[] = [];
                shipRow.push(...ShipSummaryRow.header());
                row.push.apply(row, _.map(shipRow, (y) => ('攻撃艦' + i + '.' + y)));
            }
            row.push(..._.map(ShipRow.header(), y => ('防御艦.' + y)));
            return row;
        }

        static body(battleExDto: BattleExDto, phaseStatus: PhaseStatus, phaseDto: BattleExDto.Phase, phaseApi: DayPhaseApi, airIndex: number) {
            if (airIndex === 1) {
                var ships = new Ships(battleExDto, phaseStatus, phaseStatus.airFleetsStatus);
                var shipsSummary = new ShipsSummary(battleExDto, phaseStatus, phaseStatus.airFleetsStatus);
                var api_kouku = phaseApi.api_kouku;
            }
            else if (airIndex === 2) {
                var ships = new Ships(battleExDto, phaseStatus, phaseStatus.air2FleetsStatus);
                var shipsSummary = new ShipsSummary(battleExDto, phaseStatus, phaseStatus.air2FleetsStatus);
                var api_kouku = phaseApi.api_kouku2;
            }
            var rows: any[][] = [];
            if (api_kouku != null) {
                var row = _.clone(DayPhaseRow.body(battleExDto, phaseDto, phaseApi, shipsSummary.itemInfos));
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
                        var use_item0 = shipsSummary.itemInfos.getName(api_air_fire.api_use_items[0]);
                        var use_item1 = shipsSummary.itemInfos.getName(api_air_fire.api_use_items[1]);
                        var use_item2 = shipsSummary.itemInfos.getName(api_air_fire.api_use_items[2]);
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
                if (api_stage3 != null) {
                    var frai_flag = api_stage3.api_frai_flag;
                    var erai_flag = api_stage3.api_erai_flag;
                    var fbak_flag = api_stage3.api_fbak_flag;
                    var ebak_flag = api_stage3.api_ebak_flag;
                    var fcl_flag = api_stage3.api_fcl_flag;
                    var ecl_flag = api_stage3.api_ecl_flag;
                    var fdam = api_stage3.api_fdam;
                    var edam = api_stage3.api_edam;
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
                var construct = (row: any[], atShipRows: any[][], dfShipRows: any[][], plane_from: number[], rai_flag: number[], bak_flag: number[], cl_flag: number[], dam: number[]) => {
                    var rows: any[][] = [];
                    for (var i = 1; i <= 6; ++i) {
                        var innerRow = _.clone(row);
                        if (plane_from != null) {
                            var pf = plane_from[i];
                        }
                        innerRow.push(pf);
                        if (rai_flag != null) {
                            var rai = JavaInteger.valueOf(rai_flag[i]);
                        }
                        innerRow.push(rai);
                        if (bak_flag != null) {
                            var bak = JavaInteger.valueOf(bak_flag[i]);
                        }
                        innerRow.push(bak);
                        if (cl_flag != null) {
                            var cl = JavaInteger.valueOf(cl_flag[i]);
                        }
                        innerRow.push(cl);
                        if (dam != null) {
                            var d = JavaInteger.valueOf(dam[i]);
                            var protects = d != dam[i] ? 1 : 0;
                        }
                        innerRow.push(d);
                        innerRow.push(protects);
                        innerRow.push(...([] as any[]).concat(...atShipRows));
                        for (var i = 0; i < 6; ++i) {
                            rows.push(innerRow.concat(dfShipRows[i]));
                        }
                    }
                    return rows;
                };
                rows.push(...construct(row, shipsSummary.friendRows, ships.enemyRows, f_plane_from, frai_flag, fbak_flag, fcl_flag, fdam));
                rows.push(...construct(row, shipsSummary.enemyRows, ships.friendRows, e_plane_from, erai_flag, ebak_flag, ecl_flag, edam));
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
