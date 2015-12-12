/// <reference path="logbook.d.ts" />
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var combat;
(function (combat) {
    load('script/combat/lodash.js');
    var JavaString = Packages.java.lang.String;
    var JavaInteger = Packages.java.lang.Integer;
    var DateTimeString = Packages.logbook.gui.logic.DateTimeString;
    var BattleExDto = Packages.logbook.dto.BattleExDto;
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
                '自触接',
                '敵触接',
                '自照明弾',
                '敵照明弾'
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
            row.push(null);
            row.push(null);
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
                '自索敵',
                '敵索敵',
                '制空権',
                '会敵',
                '自触接',
                '敵触接',
                '自照明弾',
                '敵照明弾'
            ]);
            return row;
        };
        NightPhaseRow.body = function (battleExDto, phaseDto, phaseApi, itemInfos) {
            var row = PhaseRow.body(battleExDto);
            row.push(null);
            row.push(null);
            row.push(null);
            if (phaseApi.api_formation != null) {
                var formation = BattleExDto.toMatch(JavaInteger.valueOf(phaseApi.api_formation[2]));
            }
            row.push(formation);
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
    var ShipsBase = (function () {
        function ShipsBase(battleExDto, phaseStatus, fleetsStatus) {
            var _this = this;
            this.itemInfos = new ItemInfos();
            var construct = function (shipDtoList, shipHps, shipMaxHps) {
                var shipRows = [];
                for (var i = 0; i < 6; ++i) {
                    shipDto = null;
                    if (shipDtoList != null && i < shipDtoList.length) {
                        var shipDto = shipDtoList[i];
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
                    shipRows.push(_this.createShipRow(shipDto, shipHps[i], shipMaxHps[i], i + 1));
                }
                return shipRows;
            };
            var dockDto = battleExDto.getDock();
            if (dockDto != null) {
                this.friendRows = construct(dockDto.getShips(), fleetsStatus.friendHps, phaseStatus.maxFleetsStatus.friendHps);
            }
            var dockCombinedDto = battleExDto.getDockCombined();
            if (dockCombinedDto != null) {
                this.friendCombinedShipRows = construct(dockCombinedDto.getShips(), fleetsStatus.friendCombinedHps, phaseStatus.maxFleetsStatus.friendCombinedHps);
            }
            this.enemyRows = construct(battleExDto.getEnemy(), fleetsStatus.enemyHps, phaseStatus.maxFleetsStatus.enemyHps);
        }
        return ShipsBase;
    })();
    combat.ShipsBase = ShipsBase;
    var Ships = (function (_super) {
        __extends(Ships, _super);
        function Ships(battleExDto, phaseStatus, fleetsStatus) {
            _super.call(this, battleExDto, phaseStatus, fleetsStatus);
        }
        Ships.prototype.createShipRow = function (shipBaseDto, hp, maxHp, index) {
            return ShipRow.body(shipBaseDto, hp, maxHp, index);
        };
        return Ships;
    })(ShipsBase);
    combat.Ships = Ships;
    var ShipRow = (function () {
        function ShipRow() {
        }
        ShipRow.header = function () {
            var row = [
                '編成順',
                'ID',
                '名前',
                '種別',
                '疲労',
                '残耐久',
                '最大耐久',
                '損傷',
                '残燃料',
                '最大燃料',
                '残弾薬',
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
            row.push.apply(row, ItemRow.header());
            return row;
        };
        ShipRow.body = function (shipBaseDto, hp, maxHp, index) {
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
                var lv = shipBaseDto.getLv();
                var hpRate = 4 * hp / maxHp;
                if (hpRate > 3) {
                    var hpText = '小破未満';
                }
                else if (hpRate > 2) {
                    var hpText = '小破';
                }
                else if (hpRate > 1) {
                    var hpText = '中破';
                }
                else if (hpRate > 0) {
                    var hpText = '大破';
                }
                else {
                    var hpText = '轟沈';
                }
                row.push(JavaInteger.valueOf(index));
                row.push(shipId);
                row.push(fullName);
                row.push(type);
                row.push(cond);
                row.push(hp);
                row.push(maxHp);
                row.push(hpText);
                row.push(fuel);
                row.push(maxFuel);
                row.push(bull);
                row.push(maxBull);
                row.push(lv);
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
                row.push.apply(row, ItemRow.body(shipBaseDto));
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
            var row = [];
            for (var i = 1; i <= 5; ++i) {
                row.push.apply(row, _.map([
                    '名前',
                    '改修',
                    '熟練度',
                    '搭載数'
                ], function (s) { return ('装備' + i + '.' + s); }));
            }
            return row;
        };
        ItemRow.body = function (shipBaseDto) {
            var construct = function (itemDto, itemInfoDto, onSlot) {
                if (itemInfoDto != null) {
                    var name = itemInfoDto.getName();
                    var level = (itemDto != null) ? itemDto.getLevel() : null;
                    var alv = (itemDto != null) ? itemDto.getAlv() : null;
                }
                return [
                    name,
                    level,
                    alv,
                    onSlot
                ];
            };
            var row = [];
            if (shipBaseDto != null) {
                if (shipBaseDto instanceof ShipDto) {
                    var shipDto = (shipBaseDto);
                    var itemDtos = shipDto.getItem2();
                    var itemExDto = shipDto.getSlotExItem();
                    if (itemExDto != null) {
                        var itemInfoExDto = itemExDto.getInfo();
                    }
                }
                var itemInfoDtos = shipBaseDto.getItem();
                var onSlots = shipBaseDto.getOnSlot();
            }
            for (var i = 0; i < 5; ++i) {
                if (i === 4 && itemExDto != null && itemInfoExDto != null) {
                    var itemRow = construct(itemExDto[i], itemInfoExDto[i], null);
                }
                else if (itemInfoDtos != null && i < itemInfoDtos.length) {
                    if (onSlots != null && i < onSlots.length) {
                        var onSlot = onSlots[i];
                    }
                    if (itemDtos != null && i < itemDtos.length) {
                        var itemRow = construct(itemDtos[i], itemInfoDtos[i], onSlot);
                    }
                    else {
                        var itemRow = construct(null, itemInfoDtos[i], onSlot);
                    }
                }
                else {
                    var itemRow = construct(null, null, null);
                }
                row.push.apply(row, itemRow);
            }
            return row;
        };
        return ItemRow;
    })();
    combat.ItemRow = ItemRow;
    var PhaseStatus = (function () {
        function PhaseStatus(battleExDto, phaseDto) {
            this.maxFleetsStatus = new FleetsStatus(battleExDto.getMaxFriendHp(), battleExDto.getMaxFriendHpCombined(), battleExDto.getMaxEnemyHp());
            var phase1Dto = battleExDto.getPhase1();
            var phase2Dto = battleExDto.getPhase2();
            if (phaseDto === phase1Dto) {
                var fleetsStatus = new FleetsStatus(battleExDto.getStartFriendHp(), battleExDto.getStartFriendHpCombined(), battleExDto.getStartEnemyHp());
            }
            else if (phaseDto === phase2Dto) {
                var fleetsStatus = new FleetsStatus(phase1Dto.getNowFriendHp(), phase1Dto.getNowFriendHpCombined(), phase1Dto.getNowEnemyHp());
            }
            this.airFleetsStatus = fleetsStatus.updateAir(phaseDto.getAir());
            this.supportFleetsStatus = fleetsStatus.update(phaseDto.getSupport());
            this.openingFleetsStatus = fleetsStatus.update(phaseDto.getOpening());
            this.air2FleetsStatus = fleetsStatus.updateAir(phaseDto.getAir2());
            this.hougeki1FleetsStatusList = fleetsStatus.updateHougeki(phaseDto.getHougeki1());
            if (phaseDto.getKind().isHougeki1Second()) {
                this.raigekiFleetsStatus = fleetsStatus.update(phaseDto.getRaigeki());
                this.hougeki2FleetsStatusList = fleetsStatus.updateHougeki(phaseDto.getHougeki2());
                this.hougeki3FleetsStatusList = fleetsStatus.updateHougeki(phaseDto.getHougeki3());
            }
            else {
                this.hougeki2FleetsStatusList = fleetsStatus.updateHougeki(phaseDto.getHougeki2());
                this.hougeki3FleetsStatusList = fleetsStatus.updateHougeki(phaseDto.getHougeki3());
                this.raigekiFleetsStatus = fleetsStatus.update(phaseDto.getRaigeki());
            }
            this.hougekiFleetsStatusList = fleetsStatus.updateHougeki(phaseDto.getHougeki());
            this.lastFleetsStatus = fleetsStatus;
        }
        return PhaseStatus;
    })();
    combat.PhaseStatus = PhaseStatus;
    var FleetsStatus = (function () {
        function FleetsStatus(friendHps, friendCombinedHps, enemyHps) {
            if (friendHps != null) {
                this.friendHps = _.map(friendHps, function (hp) { return hp; });
            }
            else {
                this.friendHps = [];
            }
            if (friendCombinedHps != null) {
                this.friendCombinedHps = _.map(friendCombinedHps, function (hp) { return hp; });
            }
            else {
                this.friendCombinedHps = [];
            }
            if (enemyHps != null) {
                this.enemyHps = _.map(enemyHps, function (hp) { return hp; });
            }
            else {
                this.enemyHps = [];
            }
        }
        FleetsStatus.prototype.clone = function () {
            return new FleetsStatus(this.friendHps, this.friendCombinedHps, this.enemyHps);
        };
        FleetsStatus.prototype.update = function (battleAtackDtoList) {
            var _this = this;
            var previous = this.clone();
            if (battleAtackDtoList != null) {
                _.forEach(battleAtackDtoList, function (battleAtackDto) {
                    _this.updateEach(battleAtackDto);
                });
            }
            return previous;
        };
        FleetsStatus.prototype.updateAir = function (airBattleDto) {
            if (airBattleDto != null) {
                return this.update(airBattleDto.atacks);
            }
            else {
                return this.update(null);
            }
        };
        FleetsStatus.prototype.updateHougeki = function (battleAtackDtoList) {
            var _this = this;
            if (battleAtackDtoList != null) {
                return _.map(battleAtackDtoList, function (battleAtackDto) {
                    var previous = _this.clone();
                    _this.updateEach(battleAtackDto);
                    return previous;
                });
            }
        };
        FleetsStatus.prototype.updateEach = function (battleAtackDto) {
            var _this = this;
            if (battleAtackDto.friendAtack) {
                _.forEach(battleAtackDto.target, function (t, i) {
                    _this.enemyHps[t] = Math.max(0, _this.enemyHps[t] - battleAtackDto.damage[i]);
                });
            }
            else {
                _.forEach(battleAtackDto.target, function (t, i) {
                    if (t < 6) {
                        _this.friendHps[t] = Math.max(0, _this.friendHps[t] - battleAtackDto.damage[i]);
                    }
                    else {
                        _this.friendCombinedHps[t - 6] = Math.max(0, _this.friendCombinedHps[t - 6] - battleAtackDto.damage[i]);
                    }
                });
            }
        };
        return FleetsStatus;
    })();
    combat.FleetsStatus = FleetsStatus;
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
    var NightTable = (function () {
        function NightTable() {
        }
        NightTable.header = function () {
            return NightRow.header();
        };
        NightTable.body = function (battleExDto) {
            var rows = [];
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
                    var phaseApi = JSON.parse(phaseJson.toString());
                    if (phaseApi != null) {
                        var phaseStatus = new combat.PhaseStatus(battleExDto, phaseDto);
                        rows.push.apply(rows, NightRow.body(battleExDto, phaseStatus, phaseDto, phaseApi));
                    }
                }
            }
            return combat.toComparable(rows);
        };
        return NightTable;
    })();
    combat.NightTable = NightTable;
    var NightRow = (function () {
        function NightRow() {
        }
        NightRow.header = function () {
            var row = _.clone(combat.NightPhaseRow.header());
            row.push.apply(row, [
                '戦闘種別',
                '自艦隊',
                '巡目',
                '砲撃種別',
                '表示装備1',
                '表示装備2',
                '表示装備3',
                'クリティカル',
                'ダメージ',
                'かばう'
            ]);
            row.push.apply(row, _.map(combat.ShipRow.header(), function (s) { return ('攻撃艦.' + s); }));
            row.push.apply(row, _.map(combat.ShipRow.header(), function (s) { return ('防御艦.' + s); }));
            return row;
        };
        NightRow.body = function (battleExDto, phaseStatus, phaseDto, phaseApi) {
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
            var rows = [];
            if (api_hougeki != null) {
                for (var i = 1; i < api_hougeki.api_at_list.length; ++i) {
                    var ships = new combat.Ships(battleExDto, phaseStatus, phaseStatus.hougekiFleetsStatusList[i - 1]);
                    var phaseRow = combat.NightPhaseRow.body(battleExDto, phaseDto, phaseApi, ships.itemInfos);
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
                    var api_at = api_hougeki.api_at_list[i];
                    var api_sp = api_hougeki.api_sp_list[i];
                    var api_df_list = api_hougeki.api_df_list[i];
                    var api_si_list = api_hougeki.api_si_list[i];
                    var api_cl_list = api_hougeki.api_cl_list[i];
                    var api_damage = api_hougeki.api_damage[i];
                    if (api_at < 7) {
                        var itemInfoDtos = friendShips[api_at - 1].getItem();
                    }
                    else {
                        var itemInfoDtos = enemyShips[api_at - 7].getItem();
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
                        var cl = JavaInteger.valueOf(api_cl_list[j]);
                        if (cl >= 0) {
                            var damage = JavaInteger.valueOf(api_damage[j]);
                            var row = _.clone(phaseRow);
                            row.push.apply(row, [
                                '夜戦',
                                fleetName,
                                null,
                                api_sp,
                                itemNames[0],
                                itemNames[1],
                                itemNames[2],
                                cl,
                                damage,
                                damage != api_damage[j] ? 1 : 0
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
        };
        return NightRow;
    })();
    combat.NightRow = NightRow;
})(combat || (combat = {}));
function begin() {
}
function end() {
}
function header() {
    return combat.NightTable.header();
}
function body(battleExDto) {
    return combat.NightTable.body(battleExDto);
}
