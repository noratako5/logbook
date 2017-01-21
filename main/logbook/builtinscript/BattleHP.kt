package logbook.builtinscript
import logbook.dto.BattleExDto
import java.util.ArrayList

class BattleHP(battle:BattleExDto){
    val dayPhase:DayPhaseHP?
    val nightPhase:NightPhaseHP?
    init {
        dayPhase = battle.phaseList.find { p -> p.isNight.not() }?.run{ DayPhaseHP(battle,this) }
        nightPhase = battle.phaseList.find { p -> p.isNight }?.run{ NightPhaseHP(battle,this) }
    }
}

class DayPhaseHP(battle:BattleExDto,dayPhase:BattleExDto.Phase){
    val dayPhaseStartHP:ArrayList<IntArray>
    val injectionAirBaseStartHP:ArrayList<IntArray>
    val injectionAirStartHP:ArrayList<IntArray>
    val airBaseStartHP:ArrayList<ArrayList<IntArray>>
    val air1StartHP:ArrayList<IntArray>
    val supportStartHP:ArrayList<IntArray>
    val openingTaisenStartHP:ArrayList<IntArray>
    val openingRaigekiStartHP:ArrayList<IntArray>
    val air2StartHP:ArrayList<IntArray>
    val hougeki1StartHP:ArrayList<IntArray>
    val hougeki2StartHP:ArrayList<IntArray>
    val hougeki3StartHP:ArrayList<IntArray>
    val raigekiStartHP:ArrayList<IntArray>
    val dayPhaseEndHP:ArrayList<IntArray>
    init {
        val zero = IntArray(6)
        val nowHPArray = arrayListOf(zero.clone(), zero.clone(), zero.clone(), zero.clone())
        battle.startFriendHp?.run{ for(i in this.indices) {  nowHPArray[HP_INDEX_FRIEND][i] = this[i]  } }
        battle.startEnemyHp?.run{for(i in this.indices) {  nowHPArray[HP_INDEX_ENEMY][i] = this[i]  }}
        battle.startFriendHpCombined?.run{for(i in this.indices) {  nowHPArray[HP_INDEX_FRIEND_COMBINED][i] = this[i]  }}
        battle.startEnemyHpCombined?.run{for(i in this.indices) {  nowHPArray[HP_INDEX_ENEMY_COMBINED][i] = this[i]  }}

        dayPhaseStartHP = nowHPArray.deepClone()
        injectionAirBaseStartHP = dayPhaseStartHP
        injectionAirStartHP = injectionAirBaseStartHP.createNextHPAir(dayPhase.airBaseInjection)
        val firstAirBaseStartHP = injectionAirStartHP.createNextHPAir(dayPhase.airInjection)
        airBaseStartHP = arrayListOf(injectionAirStartHP.createNextHPAir(dayPhase.airInjection))
        dayPhase.airBase?.run {
            val airBaseEndHP = firstAirBaseStartHP.createAirBaseHP(dayPhase.airBase)
            for (i in 0..airBaseEndHP.size - 2) {
                airBaseStartHP.add(airBaseEndHP[i])
            }
        }
        air1StartHP = firstAirBaseStartHP.createNextHPAirBase(dayPhase.airBase)
        supportStartHP = air1StartHP.createNextHPAir(dayPhase.air)
        openingTaisenStartHP = supportStartHP.createNextHP(dayPhase.support)
        openingRaigekiStartHP = openingTaisenStartHP.createNextHP(dayPhase.openingTaisen)
        air2StartHP = openingRaigekiStartHP.createNextHP(dayPhase.opening)
        hougeki1StartHP = air2StartHP.createNextHPAir(dayPhase.air2)
        //砲撃何巡目の後に雷撃挟むか
        val raigekiNumber =
                if (battle.isCombined.not()) {
                    if (battle.isEnemyCombined.not()) { 3 }
                    else { 1 }
                }
                else {
                    if (dayPhase.kind.isHougeki1Second) { 1 }
                    else if (dayPhase.kind.isHougeki2Second) { 2 }
                    else { 3 }
                }
        when (raigekiNumber) {
            1 -> {
                raigekiStartHP = hougeki1StartHP.createNextHP(dayPhase.hougeki1)
                hougeki2StartHP = raigekiStartHP.createNextHP(dayPhase.raigeki)
                hougeki3StartHP = hougeki2StartHP.createNextHP(dayPhase.hougeki2)
                dayPhaseEndHP = hougeki3StartHP.createNextHP(dayPhase.hougeki3)
            }
            2 -> {
                hougeki2StartHP = hougeki1StartHP.createNextHP(dayPhase.hougeki1)
                raigekiStartHP = hougeki2StartHP.createNextHP(dayPhase.hougeki2)
                hougeki3StartHP = raigekiStartHP.createNextHP(dayPhase.raigeki)
                dayPhaseEndHP = hougeki3StartHP.createNextHP(dayPhase.hougeki3)
            }
            else -> {
                hougeki2StartHP = hougeki1StartHP.createNextHP(dayPhase.hougeki1)
                hougeki3StartHP = hougeki2StartHP.createNextHP(dayPhase.hougeki2)
                raigekiStartHP = hougeki3StartHP.createNextHP(dayPhase.hougeki3)
                dayPhaseEndHP = raigekiStartHP.createNextHP(dayPhase.raigeki)
            }
        }
    }
}
class NightPhaseHP(battle:BattleExDto,nightPhase:BattleExDto.Phase){
    val nightPhaseStartHP:ArrayList<IntArray>
    val hougekiStartHP:ArrayList<IntArray>
    val endHP:ArrayList<IntArray>
    init{
        val zero = IntArray(6)
        val nowHPArray = arrayListOf(zero.clone(), zero.clone(), zero.clone(), zero.clone())
        if(nightPhase === battle.phase2) {
            battle.phase1.nowFriendHp?.run { for (i in this.indices) { nowHPArray[HP_INDEX_FRIEND][i] = this[i] } }
            battle.phase1.nowEnemyHp?.run { for (i in this.indices) { nowHPArray[HP_INDEX_ENEMY][i] = this[i] } }
            battle.phase1.nowFriendHpCombined?.run { for (i in this.indices) { nowHPArray[HP_INDEX_FRIEND_COMBINED][i] = this[i] } }
            battle.phase1.nowEnemyHpCombined?.run { for (i in this.indices) { nowHPArray[HP_INDEX_ENEMY_COMBINED][i] = this[i] } }
        }
        else{
            battle.startFriendHp?.run{ for(i in this.indices) {  nowHPArray[HP_INDEX_FRIEND][i] = this[i]  } }
            battle.startEnemyHp?.run{for(i in this.indices) {  nowHPArray[HP_INDEX_ENEMY][i] = this[i]  }}
            battle.startFriendHpCombined?.run{for(i in this.indices) {  nowHPArray[HP_INDEX_FRIEND_COMBINED][i] = this[i]  }}
            battle.startEnemyHpCombined?.run{for(i in this.indices) {  nowHPArray[HP_INDEX_ENEMY_COMBINED][i] = this[i]  }}
        }
        nightPhaseStartHP = nowHPArray.deepClone()
        hougekiStartHP = nightPhaseStartHP
        endHP = hougekiStartHP.createNextHP(nightPhase.hougeki)
    }
}