package logbook.builtinscript

import logbook.dto.AirBattleDto
import logbook.dto.BattleAtackDto
import logbook.dto.BattleExDto
import java.util.*

const val HP_INDEX_ENEMY = 0
const val HP_INDEX_FRIEND = 1
const val HP_INDEX_FRIEND_COMBINED = 2
const val HP_INDEX_ENEMY_COMBINED = 3

fun ArrayList<IntArray>.createFriendlyNextHP(attackList: List<BattleAtackDto>?,battle:BattleExDto): ArrayList<IntArray> {
    if (attackList != null) { return this.createFriendlyAttackHP(attackList,battle).second }
    else { return this }
}

fun ArrayList<IntArray>.createNextHP(attackList: List<BattleAtackDto>?,battle:BattleExDto): ArrayList<IntArray> {
    if (attackList != null) { return this.createAttackHP(attackList,battle).second }
    else { return this }
}

fun ArrayList<IntArray>.createNextHPAir(airBattle: AirBattleDto?,battle:BattleExDto): ArrayList<IntArray> {
    if (airBattle != null) { return this.createNextHP(airBattle.atacks,battle) }
    else { return this }
}

fun ArrayList<IntArray>.createNextHPAirBase(baseAirBattleList: List<AirBattleDto>?,battle:BattleExDto): ArrayList<IntArray> {
    if (baseAirBattleList != null) { return this.createAirBaseHP(baseAirBattleList,battle).lastOrNull()?:this }
    else { return this }
}

fun ArrayList<IntArray>.createAirBaseHP(baseAirBattleList: List<AirBattleDto>,battle:BattleExDto): ArrayList<ArrayList<IntArray>> {
    val result = ArrayList<ArrayList<IntArray>>()
    var prev = this
    for (attack in baseAirBattleList) {
        prev = prev.createNextHPAir(attack,battle)
        result.add(prev)
    }
    return result
}
fun ArrayList<IntArray>.createFriendlyAttackHP(attackList: List<BattleAtackDto>,battle:BattleExDto): Pair<ArrayList<ArrayList<ArrayList<IntArray>>>,ArrayList<IntArray>> {
    val enemy = this[HP_INDEX_ENEMY].clone()
    val friend = this[HP_INDEX_FRIEND].clone()
    val combined = this[HP_INDEX_FRIEND_COMBINED].clone()
    val enemyCombined = this[HP_INDEX_ENEMY_COMBINED].clone()
    val result = ArrayList<ArrayList<ArrayList<IntArray>>>()
    for (attack in attackList) {
        val array = ArrayList<ArrayList<IntArray>>()
        for (j in attack.target.indices) {
            val next = ArrayList<IntArray>(this)
            val t = attack.target[j]
            val damage = attack.damage[j]
            if (attack.friendAtack) {
                if (t < 6) { enemy[t] = Math.max(0, enemy[t] - damage) }
                else { enemyCombined[t - 6] = Math.max(0, enemyCombined[t - 6] - damage) }
            }
            next[HP_INDEX_ENEMY] = enemy.clone()
            next[HP_INDEX_FRIEND] = friend.clone()
            next[HP_INDEX_FRIEND_COMBINED] = combined.clone()
            next[HP_INDEX_ENEMY_COMBINED] = enemyCombined.clone()
            array.add(next)
        }
        result.add(array)
    }
    val last = ArrayList<IntArray>(this)
    last[HP_INDEX_ENEMY] = enemy
    last[HP_INDEX_FRIEND] = friend
    last[HP_INDEX_FRIEND_COMBINED] = combined
    last[HP_INDEX_ENEMY_COMBINED] = enemyCombined
    return Pair(result,last)
}
fun ArrayList<IntArray>.createAttackHP(attackList: List<BattleAtackDto>,battle:BattleExDto): Pair<ArrayList<ArrayList<ArrayList<IntArray>>>,ArrayList<IntArray>> {
    val enemy = this[HP_INDEX_ENEMY].clone()
    val friend = this[HP_INDEX_FRIEND].clone()
    val combined = this[HP_INDEX_FRIEND_COMBINED].clone()
    val enemyCombined = this[HP_INDEX_ENEMY_COMBINED].clone()
    val result = ArrayList<ArrayList<ArrayList<IntArray>>>()
    for (attack in attackList) {
        val array = ArrayList<ArrayList<IntArray>>()
        for (j in attack.target.indices) {
            val next = ArrayList<IntArray>(this)
            val t = attack.target[j]
            val damage = attack.damage[j]
            if (attack.friendAtack) {
                if (t < 6) { enemy[t] = Math.max(0, enemy[t] - damage) }
                else { enemyCombined[t - 6] = Math.max(0, enemyCombined[t - 6] - damage) }
            }
            else {
                if (t < battle.dock.ships.size) { friend[t] = Math.max(0, friend[t] - damage) }
                else { combined[t - 6] = Math.max(0, combined[t - 6] - damage) }
            }
            next[HP_INDEX_ENEMY] = enemy.clone()
            next[HP_INDEX_FRIEND] = friend.clone()
            next[HP_INDEX_FRIEND_COMBINED] = combined.clone()
            next[HP_INDEX_ENEMY_COMBINED] = enemyCombined.clone()
            array.add(next)
        }
        result.add(array)
    }
    val last = ArrayList<IntArray>(this)
    last[HP_INDEX_ENEMY] = enemy
    last[HP_INDEX_FRIEND] = friend
    last[HP_INDEX_FRIEND_COMBINED] = combined
    last[HP_INDEX_ENEMY_COMBINED] = enemyCombined
    return Pair(result,last)
}