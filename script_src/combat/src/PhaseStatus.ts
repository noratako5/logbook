import * as _ from 'lodash';
import FleetsStatus from './FleetsStatus';
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

export default class PhaseStatus {

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
