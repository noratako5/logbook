import * as _ from 'lodash';
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

export default class FleetsStatus {

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
