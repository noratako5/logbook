/// <reference path="logbook.d.ts" />
var combat;
(function (combat) {
    load('script/combat/lodash.js');
    var JavaString = Packages.java.lang.String;
    var DateTimeString = Packages.logbook.gui.logic.DateTimeString;
    var ShipDto = Packages.logbook.dto.ShipDto;
    var DayPhaseRow = (function () {
        function DayPhaseRow() {
        }
        DayPhaseRow.header = function () {
            var row = PhaseRow.header();
            row.push.apply(row, [
                '自索敵',
                '敵索敵',
                '制空権',
                '会敵',
                '自昼触接',
                '敵昼触接'
            ]);
            return row;
        };
        DayPhaseRow.body = function (battleExDto, phaseDto, phaseApi, itemInfos) {
            var row = PhaseRow.body(battleExDto);
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
            var touchPlane = phaseDto.getTouchPlane();
            if (touchPlane != null) {
                var touchPlane0 = itemInfos.getName(touchPlane[0]);
                var touchPlane1 = itemInfos.getName(touchPlane[1]);
            }
            row.push(touchPlane0);
            row.push(touchPlane1);
            return row;
        };
        return DayPhaseRow;
    })();
    combat.DayPhaseRow = DayPhaseRow;
    var NightPhaseRow = (function () {
        function NightPhaseRow() {
        }
        NightPhaseRow.header = function () {
            var row = PhaseRow.header();
            row.push.apply(row, [
                '自夜触接',
                '敵夜触接',
                '自照明弾',
                '敵照明弾'
            ]);
            return row;
        };
        NightPhaseRow.body = function (battleExDto, phaseDto, phaseApi, itemInfos) {
            var row = PhaseRow.body(battleExDto);
            var touchPlane = phaseDto.getTouchPlane();
            if (touchPlane != null) {
                var touchPlane0 = itemInfos.getName(touchPlane[0]);
                var touchPlane1 = itemInfos.getName(touchPlane[1]);
            }
            row.push(touchPlane0);
            row.push(touchPlane1);
            var api_flare_pos = phaseApi.api_flare_pos;
            if (api_flare_pos != null) {
                var api_flare_pos0 = api_flare_pos[0];
                if (api_flare_pos0 >= 0) {
                    var flarePos0 = api_flare_pos0;
                }
                var api_flare_pos1 = api_flare_pos[1];
                if (api_flare_pos1 >= 0) {
                    var flarePos1 = api_flare_pos1;
                }
            }
            row.push(flarePos0);
            row.push(flarePos1);
            return row;
        };
        return NightPhaseRow;
    })();
    combat.NightPhaseRow = NightPhaseRow;
    var PhaseRow = (function () {
        function PhaseRow() {
        }
        PhaseRow.header = function () {
            return [
                '日付',
                '海域',
                'マス',
                '出撃',
                'ランク',
                '敵艦隊',
                '提督レベル',
                '自陣形',
                '敵陣形'
            ];
        };
        PhaseRow.body = function (battleExDto) {
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
            return row;
        };
        return PhaseRow;
    })();
    combat.PhaseRow = PhaseRow;
    var ItemInfos = (function () {
        function ItemInfos() {
            this.dtos = {};
        }
        ItemInfos.prototype.getName = function (id) {
            var dto = this.dtos[id];
            if (dto != null) {
                return dto.getName();
            }
        };
        return ItemInfos;
    })();
    combat.ItemInfos = ItemInfos;
    var Ships = (function () {
        function Ships(battleExDto) {
            var _this = this;
            this.itemInfos = new ItemInfos();
            var construct = function (shipDtos) {
                var shipRows = [];
                for (var i = 0; i < 6; ++i) {
                    if (shipDtos != null && i < shipDtos.length) {
                        var shipDto = shipDtos[i];
                        if (shipDto != null) {
                            var itemInfoDtos = shipDto.getItem();
                            if (itemInfoDtos != null) {
                                _.forEach(itemInfoDtos, function (itemInfoDto) {
                                    if (itemInfoDto != null) {
                                        _this.itemInfos.dtos[itemInfoDto.getId()] = itemInfoDto;
                                    }
                                });
                            }
                        }
                    }
                    shipRows.push(ShipRow.body(shipDto));
                }
                return shipRows;
            };
            var dockDto = battleExDto.getDock();
            if (dockDto != null) {
                this.friendRows = construct(dockDto.getShips());
            }
            var dockCombinedDto = battleExDto.getDockCombined();
            if (dockCombinedDto != null) {
                this.friendCombinedShipRows = construct(dockCombinedDto.getShips());
            }
            this.enemyRows = construct(battleExDto.getEnemy());
        }
        return Ships;
    })();
    combat.Ships = Ships;
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
                var row = [];
                var shipInfoDto = shipBaseDto.getShipInfo();
                if (shipInfoDto != null) {
                    var shipId = shipInfoDto.getShipId();
                    var fullName = shipInfoDto.getFullName();
                    var type = shipInfoDto.getType();
                    var maxFuel = shipInfoDto.getMaxFuel();
                    var maxBull = shipInfoDto.getMaxBull();
                }
                if (shipBaseDto instanceof ShipDto) {
                    var shipDto = shipBaseDto;
                    var cond = shipDto.getCond();
                    var fuel = shipDto.getFuel();
                    var bull = shipDto.getBull();
                }
                var shipParamDto = shipBaseDto.getParam();
                if (shipParamDto != null) {
                    switch (shipParamDto.getSoku()) {
                        case 0:
                            var soku = '陸上';
                            break;
                        case 5:
                            var soku = '低速';
                            break;
                        case 10:
                            var soku = '高速';
                            break;
                    }
                    var houg = shipParamDto.getHoug();
                    var raig = shipParamDto.getRaig();
                    var taik = shipParamDto.getTaik();
                    var souk = shipParamDto.getSouk();
                    var kaih = shipParamDto.getKaih();
                    var tais = shipParamDto.getTais();
                    var saku = shipParamDto.getSaku();
                    var luck = shipParamDto.getLuck();
                    switch (shipParamDto.getLeng()) {
                        case 0:
                            var leng = '超短';
                            break;
                        case 1:
                            var leng = '短';
                            break;
                        case 2:
                            var leng = '中';
                            break;
                        case 3:
                            var leng = '長';
                            break;
                        case 4:
                            var leng = '超長';
                            break;
                    }
                }
                row.push(shipId);
                row.push(fullName);
                row.push(type);
                row.push(cond);
                row.push(fuel);
                row.push(maxFuel);
                row.push(bull);
                row.push(maxBull);
                row.push(shipBaseDto.getLv());
                row.push(soku);
                row.push(houg);
                row.push(raig);
                row.push(taik);
                row.push(souk);
                row.push(kaih);
                row.push(tais);
                row.push(saku);
                row.push(luck);
                row.push(leng);
                if (shipDto != null) {
                    var itemDtos = shipDto.getItem2();
                    var itemExDto = shipDto.getSlotExItem();
                    if (itemExDto != null) {
                        var itemInfoExDto = itemExDto.getInfo();
                    }
                }
                var itemInfoDtos = shipBaseDto.getItem();
                var onSlots = shipBaseDto.getOnSlot();
                for (var i = 0; i < 5; ++i) {
                    if (i === 4 && itemExDto != null && itemInfoExDto != null) {
                        var itemRow = ItemRow.body(itemExDto[i], itemInfoExDto[i], null);
                    }
                    else if (itemInfoDtos != null && i < itemInfoDtos.length) {
                        if (onSlots != null && i < onSlots.length) {
                            var onSlot = onSlots[i];
                        }
                        if (itemDtos != null && i < itemDtos.length) {
                            var itemRow = ItemRow.body(itemDtos[i], itemInfoDtos[i], onSlot);
                        }
                        else {
                            var itemRow = ItemRow.body(null, itemInfoDtos[i], onSlot);
                        }
                    }
                    else {
                        var itemRow = ItemRow.body(null, null, null);
                    }
                    row.push.apply(row, itemRow);
                }
                return row;
            }
            else {
                return new Array(this.header().length);
            }
        };
        return ShipRow;
    })();
    combat.ShipRow = ShipRow;
    var ItemRow = (function () {
        function ItemRow() {
        }
        ItemRow.header = function () {
            return [
                '名前',
                '改修',
                '熟練度',
                '搭載数'
            ];
        };
        ItemRow.body = function (itemDto, itemInfoDto, onslot) {
            if (itemInfoDto != null) {
                return [
                    itemInfoDto.getName(),
                    (itemDto != null) ? itemDto.getLevel() : null,
                    (itemDto != null) ? itemDto.getAlv() : null,
                    onslot
                ];
            }
            else {
                return new Array(this.header().length);
            }
        };
        return ItemRow;
    })();
    combat.ItemRow = ItemRow;
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
    combat.toComparable = toComparable;
})(combat || (combat = {}));
/// <reference path="../combat/combat.ts" />
var combat;
(function (combat) {
    var JavaInteger = Packages.java.lang.Integer;
    var AirTable = (function () {
        function AirTable() {
        }
        AirTable.header = function () {
            var row = combat.DayPhaseRow.header();
            row.push.apply(row, AirRow.header());
            return row;
        };
        AirTable.body = function (battleExDto) {
            var rows = [];
            var phaseDto = battleExDto.getPhase1();
            if (phaseDto != null) {
                var phaseKindDto = phaseDto.getKind();
                if (phaseKindDto != null) {
                    if (!phaseKindDto.isNight()) {
                        var phaseJson = phaseDto.getJson();
                        if (phaseJson != null) {
                            var phaseApi = JSON.parse(phaseJson.toString());
                            if (phaseApi != null) {
                                var ships = new combat.Ships(battleExDto);
                                var phaseRow = combat.DayPhaseRow.body(battleExDto, phaseDto, phaseApi, ships.itemInfos);
                                rows.push.apply(rows, AirRow.body(battleExDto, ships, phaseDto.getAir(), phaseApi.api_kouku));
                                rows.push.apply(rows, AirRow.body(battleExDto, ships, phaseDto.getAir2(), phaseApi.api_kouku2));
                                _.forEach(rows, function (row) { return (row.unshift.apply(row, phaseRow)); });
                            }
                        }
                    }
                }
            }
            return combat.toComparable(rows);
        };
        return AirTable;
    })();
    combat.AirTable = AirTable;
    var AirRow = (function () {
        function AirRow() {
        }
        AirRow.header = function () {
            var row = [
                'ステージ1.自艦載機総数',
                'ステージ1.自艦載機喪失数',
                'ステージ1.敵艦載機総数',
                'ステージ1.敵艦載機喪失数',
                'ステージ2.自艦載機総数',
                'ステージ2.自艦載機喪失数',
                'ステージ2.敵艦載機総数',
                'ステージ2.敵艦載機喪失数',
                '対空カットイン.インデックス',
                '対空カットイン.種別',
                '対空カットイン.表示装備1',
                '対空カットイン.表示装備2',
                '対空カットイン.表示装備3'
            ];
            _.forEach(['自艦', '敵艦'], function (x) {
                for (var i = 1; i <= 6; ++i) {
                    var r = [
                        '被雷撃',
                        '被爆撃',
                        '被クリティカル',
                        '被ダメージ',
                        'かばう'
                    ];
                    //r.push.apply(r, ShipRow.header());
                    row.push.apply(row, _.map(r, function (y) { return (x + i + '.' + y); }));
                }
            });
            return row;
        };
        AirRow.body = function (battleExDto, ships, battleAtackDtoList, api_kouku) {
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
                var construct = function (shipRows, api_rai_flag, api_bak_flag, api_cl_flag, api_dam) {
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
        };
        return AirRow;
    })();
    combat.AirRow = AirRow;
})(combat || (combat = {}));
function begin() {
}
function end() {
}
function header() {
    return combat.AirTable.header();
}
function body(battleExDto) {
    return combat.AirTable.body(battleExDto);
}
