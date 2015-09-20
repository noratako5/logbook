/// <reference path="logbook.d.ts" />

module combat {

    load('script/combat/lodash.js');

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

    export class DayPhaseRow {

        static header() {
            var row = PhaseRow.header();
            row.push.apply(row, [
                '自索敵'
                , '敵索敵'
                , '制空権'
                , '会敵'
                , '自昼触接'
                , '敵昼触接'
            ]);
            return row;
        }

        static body(battleExDto: BattleExDto, phaseDto: BattleExDto.Phase, phaseApi: DayPhaseApi, itemInfos: ItemInfos) {
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
        }
    }

    export class NightPhaseRow {

        static header() {
            var row = PhaseRow.header();
            row.push.apply(row, [
                '自夜触接'
                , '敵夜触接'
                , '自照明弾'
                , '敵照明弾'
            ]);
            return row;
        }

        static body(battleExDto: BattleExDto, phaseDto: BattleExDto.Phase, phaseApi: NightPhaseApi, itemInfos: ItemInfos) {
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
        }
    }

    export class PhaseRow {

        static header() {
            return [
                '日付'
                , '海域'
                , 'マス'
                , '出撃'
                , 'ランク'
                , '敵艦隊'
                , '提督レベル'
                , '自陣形'
                , '敵陣形'
            ];
        }

        static body(battleExDto: BattleExDto) {
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
        }
    }

    export class ItemInfos {

        dtos: { [id: number]: ItemInfoDto } = {};

        getName(id: number) {
            var dto = this.dtos[id];
            if (dto != null) {
                return dto.getName();
            }
        }
    }

    export class Ships {

        itemInfos: ItemInfos;
        friendRows: any[][];
        friendCombinedShipRows: any[][];
        enemyRows: any[][];

        constructor(battleExDto: BattleExDto, body: (shipBaseDto: ShipBaseDto) => any[] = ShipRow.body) {
            this.itemInfos = new ItemInfos();
            var construct = (shipDtos: JavaList<ShipBaseDto>) => {
                var shipRows = [];
                for (var i = 0; i < 6; ++i) {
                    shipDto = null;
                    if (shipDtos != null && i < shipDtos.length) {
                        var shipDto = shipDtos[i];
                        if (shipDto != null) {
                            var itemInfoDtos = shipDto.getItem();
                            if (itemInfoDtos != null) {
                                _.forEach(itemInfoDtos, (itemInfoDto) => {
                                    if (itemInfoDto != null) {
                                        this.itemInfos.dtos[itemInfoDto.getId()] = itemInfoDto;
                                    }
                                });
                            }
                        }
                    }
                    shipRows.push(body(shipDto));
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
    }

    export class ShipRow {

        static header() {
            var row = [
                'ID'
                , '名前'
                , '種別'
                , '疲労'
                , '残り燃料'
                , '最大燃料'
                , '残り弾薬'
                , '最大弾薬'
                , 'Lv'
                , '速力'
                , '火力'
                , '雷装'
                , '対空'
                , '装甲'
                , '回避'
                , '対潜'
                , '索敵'
                , '運'
                , '射程'
            ];
            row.push.apply(row, ItemRow.header());
            return row;
        }

        static body(shipBaseDto: ShipBaseDto) {
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
                    var shipDto = <ShipDto>shipBaseDto;
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
                row.push.apply(row, ItemRow.body(shipBaseDto));
                return row;
            }
            else {
                return new Array(this.header().length);
            }
        }
    }

    export class ItemRow {

        static header() {
            var row = [];
            for (var i = 1; i <= 5; ++i) {
                row.push.apply(row, _.map([
                    '名前'
                    , '改修'
                    , '熟練度'
                    , '搭載数'
                ], (s) => ('装備' + i + '.' + s)));
            }
            return row;
        }

        static body(shipBaseDto: ShipBaseDto) {
            var construct = (itemDto: ItemDto, itemInfoDto: ItemInfoDto, onSlot: number) => {
                if (itemInfoDto != null) {
                    return [
                        itemInfoDto.getName()
                        , (itemDto != null) ? itemDto.getLevel() : null
                        , (itemDto != null) ? itemDto.getAlv() : null
                        , onSlot
                    ];
                }
                else {
                    return new Array(this.header().length);
                }
            }
            var row = [];
            if (shipBaseDto != null) {
                if (shipBaseDto instanceof ShipDto) {
                    var shipDto = <ShipDto>(shipBaseDto);
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
        }
    }

    // javascriptの配列をそのまま返すと遅いので
    // Comparable[]に変換しておく
    // undefinedはnullに変換される
    export function toComparable(sourceRows: any[][]) {
        var ComparableType = Java.type('java.lang.Comparable');
        var ComparableArrayType = <ComparableArray>Java.type('java.lang.Comparable[]');
        var ComparableArrayArrayType = <ComparableArrayArray>Java.type('java.lang.Comparable[][]');
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
}
