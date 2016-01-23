import * as _ from 'lodash';
import ItemInfos from './ItemInfos';
import PhaseStatus from './PhaseStatus';
import FleetsStatus from './FleetsStatus';
import ShipRow from './ShipRow';
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

export default class Ships {

    itemInfos: ItemInfos;
    friendRows: any[][];
    friendCombinedShipRows: any[][];
    enemyRows: any[][];

    constructor(battleExDto: BattleExDto, phaseStatus: PhaseStatus, fleetsStatus: FleetsStatus) {
        this.itemInfos = new ItemInfos();
        var construct = (shipDtoList: _.List<ShipBaseDto>, shipHps: number[], shipMaxHps: number[]) => {
            var shipRows: any[] = [];
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

    protected createShipRow(shipBaseDto: ShipBaseDto, hp: number, maxHp: number, index: number) {
        return ShipRow.body(shipBaseDto, hp, maxHp, index);
    }
}
