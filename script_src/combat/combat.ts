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
    import AirBattleDto = Packages.logbook.dto.AirBattleDto;

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
                , '自触接'
                , '敵触接'
                , '自照明弾'
                , '敵照明弾'
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
            row.push(null);
            row.push(null);
            return row;
        }
    }

    export class NightPhaseRow {

        static header() {
            var row = PhaseRow.header();
            row.push.apply(row, [
                '自索敵'
                , '敵索敵'
                , '制空権'
                , '会敵'
                , '自触接'
                , '敵触接'
                , '自照明弾'
                , '敵照明弾'
            ]);
            return row;
        }

        static body(battleExDto: BattleExDto, phaseDto: BattleExDto.Phase, phaseApi: NightPhaseApi, itemInfos: ItemInfos) {
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

    export abstract class ShipsBase {

        itemInfos: ItemInfos;
        friendRows: any[][];
        friendCombinedShipRows: any[][];
        enemyRows: any[][];

        constructor(battleExDto: BattleExDto, phaseStatus: PhaseStatus, fleetsStatus: FleetsStatus) {
            this.itemInfos = new ItemInfos();
            var construct = (shipDtoList: _.List<ShipBaseDto>, shipHps: number[], shipMaxHps: number[]) => {
                var shipRows = [];
                for (var i = 0; i < 6; ++i) {
                    shipDto = null;
                    if (shipDtoList != null && i < shipDtoList.length) {
                        var shipDto = shipDtoList[i];
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
                    shipRows.push(this.createShipRow(shipDto, shipHps[i], shipMaxHps[i], i + 1));
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

        protected abstract createShipRow(shipBaseDto: ShipBaseDto, hp: number, maxHp: number, index: number);
    }

    export class Ships extends ShipsBase {

        constructor(battleExDto: BattleExDto, phaseStatus: PhaseStatus, fleetsStatus: FleetsStatus) {
            super(battleExDto, phaseStatus, fleetsStatus);
        }

        protected createShipRow(shipBaseDto: ShipBaseDto, hp: number, maxHp: number, index: number) {
            return ShipRow.body(shipBaseDto, hp, maxHp, index);
        }
    }

    export class ShipRow {

        static header() {
            var row = [
                '編成順'
                , 'ID'
                , '名前'
                , '種別'
                , '疲労'
                , '残耐久'
                , '最大耐久'
                , '損傷'
                , '残燃料'
                , '最大燃料'
                , '残弾薬'
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

        static body(shipBaseDto: ShipBaseDto, hp: number, maxHp: number, index: number) {
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
                    var name = itemInfoDto.getName();
                    var level = (itemDto != null) ? itemDto.getLevel() : null;
                    var alv = (itemDto != null) ? itemDto.getAlv() : null;
                }
                return [
                    name
                    , level
                    , alv
                    , onSlot
                ];
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

    export class PhaseStatus {

        public constructor(battleExDto: BattleExDto, phaseDto: BattleExDto.Phase) {
            this.maxFleetsStatus = new FleetsStatus(
                battleExDto.getMaxFriendHp()
                , battleExDto.getMaxFriendHpCombined()
                , battleExDto.getMaxEnemyHp()
            );
            var phase1Dto = battleExDto.getPhase1();
            var phase2Dto = battleExDto.getPhase2();
            if (phaseDto === phase1Dto) {
                var fleetsStatus = new FleetsStatus(
                    battleExDto.getStartFriendHp()
                    , battleExDto.getStartFriendHpCombined()
                    , battleExDto.getStartEnemyHp()
                );
            }
            else if (phaseDto === phase2Dto) {
                var fleetsStatus = new FleetsStatus(
                    phase1Dto.getNowFriendHp()
                    , phase1Dto.getNowFriendHpCombined()
                    , phase1Dto.getNowEnemyHp()
                );
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

        public maxFleetsStatus: FleetsStatus;
        public airFleetsStatus: FleetsStatus;
        public supportFleetsStatus: FleetsStatus;
        public openingFleetsStatus: FleetsStatus;
        public air2FleetsStatus: FleetsStatus;
        public hougeki1FleetsStatusList: FleetsStatus[];
        public hougeki2FleetsStatusList: FleetsStatus[];
        public hougeki3FleetsStatusList: FleetsStatus[];
        public raigekiFleetsStatus: FleetsStatus;
        public hougekiFleetsStatusList: FleetsStatus[];
        public lastFleetsStatus: FleetsStatus;
    }

    export class FleetsStatus {

        public constructor(friendHps: _.List<number>, friendCombinedHps: _.List<number>, enemyHps: _.List<number>) {
            if (friendHps != null) {
                this.friendHps = _.map(friendHps, hp => hp);
            }
            else {
                this.friendHps = [];
            }
            if (friendCombinedHps != null) {
                this.friendCombinedHps = _.map(friendCombinedHps, hp => hp);
            }
            else {
                this.friendCombinedHps = [];
            }
            if (enemyHps != null) {
                this.enemyHps = _.map(enemyHps, hp => hp);
            }
            else {
                this.enemyHps = [];
            }
        }

        public clone() {
            return new FleetsStatus(this.friendHps, this.friendCombinedHps, this.enemyHps);
        }

        public update(battleAtackDtoList: _.List<BattleAtackDto>) {
            var previous = this.clone();
            if (battleAtackDtoList != null) {
                _.forEach(battleAtackDtoList, battleAtackDto => {
                    this.updateEach(battleAtackDto);
                });
            }
            return previous;
        }

        public updateAir(airBattleDto: AirBattleDto) {
            if (airBattleDto != null) {
                return this.update(airBattleDto.atacks);
            }
            else {
                return this.update(null);
            }
        }

        public updateHougeki(battleAtackDtoList: _.List<BattleAtackDto>) {
            if (battleAtackDtoList != null) {
                return _.map(battleAtackDtoList, battleAtackDto => {
                    var previous = this.clone();
                    this.updateEach(battleAtackDto);
                    return previous;
                });
            }
        }

        private updateEach(battleAtackDto: BattleAtackDto) {
            if (battleAtackDto.friendAtack) {
                _.forEach(battleAtackDto.target, (t, i) => {
                    this.enemyHps[t] = Math.max(0, this.enemyHps[t] - battleAtackDto.damage[i]);
                });
            }
            else {
                _.forEach(battleAtackDto.target, (t, i) => {
                    if (t < 6) {
                        this.friendHps[t] = Math.max(0, this.friendHps[t] - battleAtackDto.damage[i]);
                    }
                    else {
                        this.friendCombinedHps[t - 6] = Math.max(0, this.friendCombinedHps[t - 6] - battleAtackDto.damage[i])
                    }
                });
            }
        }

        public friendHps: number[];
        public friendCombinedHps: number[];
        public enemyHps: number[];
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
