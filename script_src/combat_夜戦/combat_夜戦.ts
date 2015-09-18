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

    export class NightTable {

        static header() {
            var row = NightPhaseRow.header();
            row.push.apply(row, NightRow.header());
            return row;
        }

        static body(battleExDto: BattleExDto) {
            var rows = <any[][]>[];
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
                var ships = new Ships(battleExDto);
                var phaseJson = phaseDto.getJson();
                if (phaseJson != null) {
                    var phaseApi = <NightPhaseApi>JSON.parse(phaseJson.toString());
                    if (phaseApi != null) {
                        var battleRow = NightPhaseRow.body(battleExDto, phaseDto, phaseApi, ships.itemInfos);
                        rows.push.apply(rows, NightRow.body(battleExDto, ships, phaseDto.getHougeki(), phaseApi.api_hougeki));
                        _.forEach(rows, (hougekiRow) => (hougekiRow.unshift.apply(hougekiRow, battleRow)));
                    }
                }
            }
            return toComparable(rows);
        }
    }

    export class NightRow {

        static header() {
            var row = [
                '砲撃種別'
                , '表示装備1'
                , '表示装備2'
                , '表示装備3'
                , 'クリティカル'
                , 'ダメージ'
                , 'かばう'
            ];
            row.push.apply(row, _.map(ShipRow.header(), (s) => ('攻撃艦.' + s)));
            row.push.apply(row, _.map(ShipRow.header(), (s) => ('防御艦.' + s)));
            return row;
        }

        static body(battleExDto: BattleExDto, ships: Ships, battleAtackDtoList: JavaList<BattleAtackDto>, api_hougeki: NightHougekiBattleApi) {
            var rows = <any[][]>[];
            if (api_hougeki != null) {
                for (var i = 1; i < api_hougeki.api_at_list.length; ++i) {
                    var api_at = api_hougeki.api_at_list[i];
                    var api_sp = api_hougeki.api_sp_list[i];
                    var api_df_list = api_hougeki.api_df_list[i];
                    var api_si_list = api_hougeki.api_si_list[i];
                    var api_cl_list = api_hougeki.api_cl_list[i];
                    var api_damage = api_hougeki.api_damage[i];
                    if (api_at < 7) {
                        var itemInfoDtos = battleExDto.getDock().getShips()[api_at - 1].getItem();
                    }
                    else {
                        var itemInfoDtos = battleExDto.getEnemy()[api_at - 7].getItem();
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
                    for (var j = 0; j < api_df_list.length; ++j) {
                        var api_df = api_df_list[j];
                        var damage = JavaInteger.valueOf(api_damage[j]);
                        var row = [
                            api_sp
                            , itemNames[0]
                            , itemNames[1]
                            , itemNames[2]
                            , JavaInteger.valueOf(api_cl_list[j])
                            , damage
                            , damage != api_damage[j]
                        ];
                        if (api_at < 7) {
                            row.push.apply(row, ships.friendRows[api_at - 1]);
                        }
                        else {
                            row.push.apply(row, ships.enemyRows[api_at - 7]);
                        }
                        if (api_df < 7) {
                            row.push.apply(row, ships.friendRows[api_df - 1]);
                        }
                        else {
                            row.push.apply(row, ships.enemyRows[api_df - 7]);
                        }
                        rows.push(row);
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
