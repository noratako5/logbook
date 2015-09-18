/// <reference path="../combat/combat.ts" />

module combat {
    import JavaString = Packages.java.lang.String;
    import JavaInteger = Packages.java.lang.Integer;
    import JavaList = Packages.java.util.List;
    import DateTimeString = Packages.logbook.gui.logic.DateTimeString;
    import BattleExDto = Packages.logbook.dto.BattleExDto;
    import BasicInfoApi = combat.BasicInfoApi;
    import MapCellApi = combat.MapCellApi;
    import BattleResultApi = combat.BattleResultApi;
    import ShipBaseDto = Packages.logbook.dto.ShipBaseDto;
    import ShipDto = Packages.logbook.dto.ShipDto;
    import EnemyShipDto = Packages.logbook.dto.EnemyShipDto;
    import ShipApi = combat.ShipApi;
    import ShipInfoApi = combat.ShipInfoApi;
    import ItemDto = Packages.logbook.dto.ItemDto;
    import ItemApi = combat.ItemApi;
    import ItemInfoDto = Packages.logbook.dto.ItemInfoDto;
    import ItemInfoApi = combat.ItemInfoApi;
    import BattleAtackDto = Packages.logbook.dto.BattleAtackDto;
    import DayPhaseApi = combat.DayPhaseApi;
    import HougekiBattleApi = combat.HougekiBattleApi;

    type ComparableArray = JavaArray<any>;
    type ComparableArrayArray = JavaArray<ComparableArray>;

    export class HougekiTable {

        static header() {
            var row = DayPhaseRow.header();
            row.push.apply(row, HougekiRow.header());
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
                                rows.push.apply(rows, HougekiRow.body(battleExDto, ships, phaseDto.getHougeki1(), phaseApi.api_hougeki1));
                                rows.push.apply(rows, HougekiRow.body(battleExDto, ships, phaseDto.getHougeki2(), phaseApi.api_hougeki2));
                                rows.push.apply(rows, HougekiRow.body(battleExDto, ships, phaseDto.getHougeki3(), phaseApi.api_hougeki3));
                                _.forEach(rows, (row) => (row.unshift.apply(row, phaseRow)));
                            }
                        }
                    }
                }
            }
            return toComparable(rows);
        }
    }

    export class HougekiRow {

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

        static body(battleExDto: BattleExDto, ships: Ships, battleAtackDtoList: JavaList<BattleAtackDto>, api_hougeki: HougekiBattleApi) {
            var rows = <any[][]>[];
            if (api_hougeki != null) {
                for (var i = 1; i < api_hougeki.api_at_list.length; ++i) {
                    var api_at = api_hougeki.api_at_list[i];
                    var api_at_type = api_hougeki.api_at_type[i];
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
                            api_at_type
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
    return combat.HougekiTable.header();
}

function body(battleExDto: Packages.logbook.dto.BattleExDto) {
    return combat.HougekiTable.body(battleExDto);
}
