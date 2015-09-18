/// <reference path="../combat/combat.d.ts" />
function begin() {
}
function end() {
}
function header() {
    return combat.CombatTable.header();
}
function body(battleExDto) {
    return combat.CombatTable.body(battleExDto);
}
var combat;
(function (combat) {
    load('script/combat/lodash.js');
    var JavaString = Packages.java.lang.String;
    var JavaInteger = Packages.java.lang.Integer;
    var DateTimeString = Packages.logbook.gui.logic.DateTimeString;
    var ShipDto = Packages.logbook.dto.ShipDto;
    var CombatTable = (function () {
        function CombatTable() {
        }
        CombatTable.header = function () {
            var row = BattleRow.header();
            row.push.apply(row, HougekiRow.header());
            return row;
        };
        CombatTable.body = function (battleExDto) {
            var battleRow = BattleRow.body(battleExDto);
            var dockDto = battleExDto.getDock();
            if (dockDto != null) {
                var shipDtos = dockDto.getShips();
                if (shipDtos != null) {
                    var friendShipRows = _.map(shipDtos, function (shipDto) { return ShipRow.body(shipDto); });
                }
            }
            var dockCombinedDto = battleExDto.getDockCombined();
            if (dockCombinedDto != null) {
                var dockCombinedShipDtos = dockCombinedDto.getShips();
                if (dockCombinedShipDtos != null) {
                    var friendCombinedShipRows = _.map(dockCombinedShipDtos, function (shipDto) { return ShipRow.body(shipDto); });
                }
            }
            var enemyDtos = battleExDto.getEnemy();
            if (enemyDtos != null) {
                var enemyShipRows = _.map(enemyDtos, function (shipDto) { return ShipRow.body(shipDto); });
            }
            var shipRows = {
                friends: friendShipRows,
                friendsCombined: friendCombinedShipRows,
                enemies: enemyShipRows
            };
            var hougekiRows = [];
            var phaseDto = battleExDto.getPhase1();
            if (phaseDto != null) {
                var phaseKind = phaseDto.getKind();
                if (!phaseKind.isNight()) {
                    var phaseJson = phaseDto.getJson();
                    if (phaseJson != null) {
                        var phaseApi = JSON.parse(phaseJson.toString());
                        if (phaseApi != null) {
                            hougekiRows.push.apply(hougekiRows, HougekiRow.body(battleExDto, shipRows, phaseDto.getHougeki1(), phaseApi.api_hougeki1));
                            hougekiRows.push.apply(hougekiRows, HougekiRow.body(battleExDto, shipRows, phaseDto.getHougeki2(), phaseApi.api_hougeki2));
                            hougekiRows.push.apply(hougekiRows, HougekiRow.body(battleExDto, shipRows, phaseDto.getHougeki3(), phaseApi.api_hougeki3));
                            _.forEach(hougekiRows, function (hougekiRow) { return (hougekiRow.unshift.apply(hougekiRow, battleRow)); });
                        }
                    }
                }
            }
            return toComparable(hougekiRows);
        };
        return CombatTable;
    })();
    combat.CombatTable = CombatTable;
    var BattleRow = (function () {
        function BattleRow() {
        }
        BattleRow.header = function () {
            return [
                '日付',
                '海域',
                'マス',
                '出撃',
                'ランク',
                '敵艦隊',
                '提督レベル',
                '自陣形',
                '敵陣形',
                '自索敵',
                '敵索敵',
                '制空権',
                '会敵'
            ];
        };
        BattleRow.body = function (battleExDto) {
            var row = [];
            var battleDate = battleExDto.getBattleDate();
            if (battleDate != null) {
                var battleDateTimeString = new DateTimeString(battleDate);
            }
            row.push(battleDateTimeString);
            row.push(battleExDto.getQuestName());
            var mapCellDto = battleExDto.getMapCellDto();
            if (mapCellDto != null) {
                var reportString = mapCellDto.getReportString();
                var bossTexts = [];
                if (mapCellDto.isStart()) {
                    bossTexts.push('出撃');
                }
                if (mapCellDto.isBoss()) {
                    bossTexts.push('ボス');
                }
                var bossText = bossTexts.join('&');
            }
            row.push(reportString);
            row.push(bossText);
            row.push(battleExDto.getRank());
            row.push(battleExDto.getEnemyName());
            row.push(battleExDto.getHqLv());
            var formation = battleExDto.getFormation();
            if (formation != null) {
                var formation0 = formation[0];
                var formation1 = formation[1];
            }
            row.push(formation0);
            row.push(formation1);
            var sakuteki = battleExDto.getSakuteki();
            if (sakuteki != null) {
                var sakuteki0 = sakuteki[0];
                var sakuteki1 = sakuteki[1];
            }
            row.push(sakuteki0);
            row.push(sakuteki1);
            var phaseDto = battleExDto.getPhase1();
            if (phaseDto != null) {
                var airBattleDto = phaseDto.getAir();
                if (airBattleDto != null) {
                    var seiku = airBattleDto.seiku;
                }
            }
            row.push(seiku);
            row.push(battleExDto.getFormationMatch());
            return row;
        };
        return BattleRow;
    })();
    var ShipRow = (function () {
        function ShipRow() {
        }
        ShipRow.header = function () {
            var row = [
                'ID',
                '名前',
                '種別',
                '疲労',
                '残り燃料',
                '最大燃料',
                '残り弾薬',
                '最大弾薬',
                'Lv',
                '速力',
                '火力',
                '雷装',
                '対空',
                '装甲',
                '回避',
                '対潜',
                '索敵',
                '運',
                '射程'
            ];
            for (var i = 1; i <= 5; ++i) {
                row.push.apply(row, _.map(ItemRow.header(), function (s) { return ('装備' + i + '.' + s); }));
            }
            return row;
        };
        ShipRow.body = function (shipBaseDto) {
            if (shipBaseDto != null) {
                if (shipBaseDto instanceof ShipDto) {
                    var shipDto = shipBaseDto;
                }
                var shipParamDto = shipBaseDto.getParam();
                var shipParamMaxDto = shipBaseDto.getMax();
                if (shipDto != null) {
                    var shipApi = JSON.parse(shipDto.getJson().toString());
                }
                var shipInfoDto = shipBaseDto.getShipInfo();
                var shipInfoApi = JSON.parse(shipInfoDto.getJson().toString());
                if (shipDto != null) {
                    var cond = shipDto.getCond();
                }
                if (shipDto != null) {
                    var fuel = shipDto.getFuel();
                    var fuelMax = shipBaseDto.getFuelMax();
                    var bull = shipDto.getBull();
                    var bullMax = shipBaseDto.getBullMax();
                }
                var soku = shipParamDto.getSoku();
                if (soku === 0) {
                    var sokuText = '陸上';
                }
                else if (soku === 5) {
                    var sokuText = '低速';
                }
                else if (soku === 10) {
                    var sokuText = '高速';
                }
                var leng = shipParamDto.getLeng();
                if (leng == 0) {
                    var lengText = '超短';
                }
                else if (leng === 1) {
                    var lengText = '短';
                }
                else if (leng === 2) {
                    var lengText = '中';
                }
                else if (leng === 3) {
                    var lengText = '長';
                }
                else if (leng === 4) {
                    var lengText = '超長';
                }
                var row = [
                    shipInfoDto.getShipId(),
                    shipInfoDto.getFullName(),
                    shipInfoDto.getType(),
                    cond,
                    fuel,
                    fuelMax,
                    bull,
                    bullMax,
                    shipBaseDto.getLv(),
                    sokuText,
                    shipParamDto.getHoug(),
                    shipParamDto.getRaig(),
                    shipParamDto.getTaik(),
                    shipParamDto.getSouk(),
                    shipParamDto.getKaih(),
                    shipParamDto.getTais(),
                    shipParamDto.getSaku(),
                    shipParamDto.getLuck(),
                    lengText
                ];
                if (shipDto != null) {
                    var itemDtoList = shipDto.getItem2();
                    var itemInfoDtoList = shipDto.getItem();
                    for (var i = 0; i < itemInfoDtoList.length; ++i) {
                        row.push.apply(row, ItemRow.body(itemDtoList[i], itemInfoDtoList[i]));
                    }
                    var itemExDto = shipDto.getSlotExItem();
                    if (itemExDto != null) {
                        var itemInfoExDto = itemExDto.getInfo();
                        for (; i < 4; ++i) {
                            row.push.apply(row, ItemRow.body(null, null));
                        }
                        row.push.apply(row, ItemRow.body(itemExDto, itemInfoExDto));
                    }
                    else {
                        for (; i < 5; ++i) {
                            row.push.apply(row, ItemRow.body(null, null));
                        }
                    }
                }
                else {
                    var itemInfoDtoList = shipBaseDto.getItem();
                    for (var i = 0; i < itemInfoDtoList.length; ++i) {
                        row.push.apply(row, ItemRow.body(null, itemInfoDtoList[i]));
                    }
                    for (var i = 0; i < 5; ++i) {
                        row.push.apply(row, ItemRow.body(null, null));
                    }
                }
                return row;
            }
            else {
                return new Array(this.header().length);
            }
        };
        return ShipRow;
    })();
    var ItemRow = (function () {
        function ItemRow() {
        }
        ItemRow.header = function () {
            return [
                '名前',
                '改修',
                '熟練度'
            ];
        };
        ItemRow.body = function (itemDto, itemInfoDto) {
            if (itemInfoDto != null) {
                return [
                    itemInfoDto.getName(),
                    (itemDto != null) ? itemDto.getLevel() : null,
                    (itemDto != null) ? itemDto.getAlv() : null
                ];
            }
            else {
                return new Array(this.header().length);
            }
        };
        return ItemRow;
    })();
    var HougekiRow = (function () {
        function HougekiRow() {
        }
        HougekiRow.header = function () {
            var row = [
                '砲撃種別',
                '表示装備1',
                '表示装備2',
                '表示装備3',
                'クリティカル',
                'ダメージ',
                'かばう'
            ];
            row.push.apply(row, _.map(ShipRow.header(), function (s) { return ('攻撃艦.' + s); }));
            row.push.apply(row, _.map(ShipRow.header(), function (s) { return ('防御艦.' + s); }));
            return row;
        };
        HougekiRow.body = function (battleExDto, shipRows, battleAtackDtoList, hougekiBattleApi) {
            var rows = [];
            if (hougekiBattleApi != null) {
                for (var i = 1; i < hougekiBattleApi.api_at_list.length; ++i) {
                    var api_at = hougekiBattleApi.api_at_list[i];
                    var api_at_type = hougekiBattleApi.api_at_type[i];
                    var api_df_list = hougekiBattleApi.api_df_list[i];
                    var api_si_list = hougekiBattleApi.api_si_list[i];
                    var api_cl_list = hougekiBattleApi.api_cl_list[i];
                    var api_damage = hougekiBattleApi.api_damage[i];
                    if (api_at < 7) {
                        var itemInfoDtos = battleExDto.getDock().getShips()[api_at - 1].getItem();
                    }
                    else {
                        var itemInfoDtos = battleExDto.getEnemy()[api_at - 7].getItem();
                    }
                    var itemNames = _.map(api_si_list, function (api_si) {
                        var itemDto = _.find(itemInfoDtos, function (itemInfoDto) { return itemInfoDto != null ? itemInfoDto.getId() == api_si : false; });
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
                            api_at_type,
                            itemNames[0],
                            itemNames[1],
                            itemNames[2],
                            JavaInteger.valueOf(api_cl_list[j]),
                            damage,
                            damage != api_damage[j]
                        ];
                        if (api_at < 7) {
                            row.push.apply(row, shipRows.friends[api_at - 1]);
                        }
                        else {
                            row.push.apply(row, shipRows.enemies[api_at - 7]);
                        }
                        if (api_df < 7) {
                            row.push.apply(row, shipRows.friends[api_df - 1]);
                        }
                        else {
                            row.push.apply(row, shipRows.enemies[api_df - 7]);
                        }
                        rows.push(row);
                    }
                }
            }
            return rows;
        };
        return HougekiRow;
    })();
    // javascriptの配列をそのまま返すと遅いので
    // Comparable[]に変換しておく
    // undefinedはnullに変換される
    function toComparable(sourceRows) {
        var ComparableType = Java.type('java.lang.Comparable');
        var ComparableArrayType = Java.type('java.lang.Comparable[]');
        var ComparableArrayArrayType = Java.type('java.lang.Comparable[][]');
        var targetRows = new ComparableArrayArrayType(sourceRows.length);
        for (var j = 0; j < sourceRows.length; ++j) {
            var sourceRow = sourceRows[j];
            var targetRow = new ComparableArrayType(sourceRow.length);
            for (var i = 0; i < sourceRow.length; ++i) {
                var source = sourceRow[i];
                if (source == null) {
                    targetRow[i] = null;
                }
                else if (source instanceof ComparableType) {
                    targetRow[i] = source;
                }
                else {
                    targetRow[i] = JavaString.valueOf(source);
                }
            }
            targetRows[j] = targetRow;
        }
        return targetRows;
    }
})(combat || (combat = {}));
