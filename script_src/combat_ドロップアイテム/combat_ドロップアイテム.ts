/// <reference path="../combat/combat.ts" />

module combat.DropItem {
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

    export class Table {

        static header() {
            return FleetRow.header();
        }

        static body(battleExDto: BattleExDto) {
            var rows: any[][] = [];
            var phaseStatus = new PhaseStatus(battleExDto, battleExDto.getLastPhase());
            rows.push.apply(rows, FleetRow.body(battleExDto, phaseStatus));
            return toComparable(rows);
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
                , '夜戦'
                , 'ドロップアイテム'
            ];
        }

        static body(battleExDto: BattleExDto) {
            var row: any[] = [];
            var battleDate = battleExDto.getBattleDate();
            if (battleDate != null) {
                var battleDateTimeString = new DateTimeString(battleDate);
            }
            row.push(battleDateTimeString);
            row.push(battleExDto.getQuestName());
            var mapCellDto = battleExDto.getMapCellDto();
            if (mapCellDto != null) {
                var reportString = mapCellDto.getReportString();
                var bossTexts: string[] = [];
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
            row.push(_.any(_.filter(battleExDto.getPhaseList(), phaseDto => phaseDto.getKind().isNight())));
            row.push(battleExDto.getDropItemName());
            return row;
        }
    }

    export class ItemRow {

        static header() {
            var row: string[] = [];
            for (var i = 1; i <= 5; ++i) {
                row.push.apply(row, _.map([
                    '名前'
                ], (s) => ('装備' + i + '.' + s)));
            }
            return row;
        }

        static body(shipBaseDto: ShipBaseDto) {
            var construct = (itemDto: ItemDto, itemInfoDto: ItemInfoDto, onSlot: number) => {
                if (itemInfoDto != null) {
                    var name = itemInfoDto.getName();
                }
                return [
                    name
                ];
            }
            var row: any[] = [];
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
                    var itemRow = construct(itemExDto, itemInfoExDto, null);
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

    export class ShipRow {

        static header() {
            var row = [
                'ID'
                , '名前'
                , 'Lv'
            ];
            row.push.apply(row, ItemRow.header());
            return row;
        }

        static body(shipBaseDto: ShipBaseDto) {
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
            row.push.apply(row, ItemRow.body(shipBaseDto));
            return row;
        }
    }

    export class FleetRow {

        static header() {
            var row = _.clone(PhaseRow.header());
            _.forEach(['自艦', '敵艦'], (x) => {
                for (var i = 1; i <= 6; ++i) {
                    var shipRow: any[] = [];
                    shipRow.push.apply(shipRow, ShipRow.header());
                    row.push.apply(row, _.map(shipRow, (y) => (x + i + '.' + y)));
                }
            });
            return row;
        }

        static body(battleExDto: BattleExDto, phaseStatus: PhaseStatus) {
            var ships = new Ships(battleExDto, phaseStatus, phaseStatus.lastFleetsStatus);
            var rows: any[][] = [];
            var row: any[] = [];
            var construct = (shipRows: any[][]) => {
                var row: any[] = [];
                for (var i = 1; i <= 6; ++i) {
                    row.push.apply(row, shipRows[i - 1]);
                }
                return row;
            };
            row.push.apply(row, construct(ships.friendRows));
            row.push.apply(row, construct(ships.enemyRows));
            rows.push(row);
            return rows;
        }
    }
}

function begin() {
}

function end() {
}

function header() {
    return combat.DropItem.Table.header();
}

function body(battleExDto: Packages.logbook.dto.BattleExDto) {
    return combat.DropItem.Table.body(battleExDto);
}
