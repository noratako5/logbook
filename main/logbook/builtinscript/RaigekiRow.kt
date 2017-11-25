package logbook.builtinscript

import com.google.gson.internal.LinkedTreeMap
import logbook.dto.BattleAtackDto
import logbook.dto.BattleExDto
import logbook.scripting.BuiltinScriptFilter
import logbook.util.GsonUtil
import java.util.*

fun RaigekiRowHeader(): ArrayList<String> {
    val header = DamageDayNightRowHeader()
    header.add("戦闘種別")
    header.add("自艦隊")
    header.add("開幕|閉幕")
    header.add("攻撃艦")
    header.add("種別")
    header.add("表示装備1")
    header.add("表示装備2")
    header.add("表示装備3")
    header.add("クリティカル")
    header.add("ダメージ")
    header.add("かばう")
    val shipHeader = ShipRowHeader()
    val length = shipHeader.size
    for (i in 0..length - 1) {
        header.add(String.format("攻撃艦.%s", shipHeader[i]))
    }
    for (i in 0..length - 1) {
        header.add(String.format("防御艦.%s", shipHeader[i]))
    }
    header.add("艦隊種類")
    header.add("敵艦隊種類")
    return header
}

private fun RaigekiRowBodyConstruct(
        arg:ScriptArg,
        attackList: List<BattleAtackDto>?,
        apiName: String,
        stage: String,
        startHP: ArrayList<IntArray>,
        body: ArrayList<ArrayList<String>>
)
{
    if(attackList == null){
        return
    }
    if(arg.battle.isEnemyCombined){
        RaigekiRowBodyConstructEC(
                arg = arg,
                attackList = attackList,
                apiName = apiName,
                stage = stage,
                startHP = startHP,
                body = body
        )
        return
    }
    val api_raigeki = arg.dayPhaseOrNull?.tree?.get(apiName) as? LinkedTreeMap<*,*>
    if(api_raigeki == null){return}

    val prevHP = startHP
    val isSecond = arg.battle.isCombined

    val friendRows = if(isSecond) arg.combinedRows else arg.friendRows
    val enemyRows = arg.enemyRows
    val friendHPIndex = if(isSecond) HP_INDEX_FRIEND_COMBINED else HP_INDEX_FRIEND
    val enemyHPIndex = HP_INDEX_ENEMY
    val friendMaxHP = if(isSecond) arg.battle.maxFriendHpCombined else arg.battle.maxFriendHp
    val enemyMaxHP = arg.battle.maxEnemyHp

    val fleetName =
        if (arg.battle.isCombined.not()) {"通常艦隊"}
        else if (isSecond) {"連合第2艦隊"}
        else {"連合第1艦隊"}

    val api_frai = GsonUtil.toIntArray(api_raigeki["api_frai"])
    val api_erai = GsonUtil.toIntArray(api_raigeki["api_erai"])
    val api_fdam = GsonUtil.toDoubleArray(api_raigeki["api_fdam"])
    val api_edam = GsonUtil.toDoubleArray(api_raigeki["api_edam"])
    val api_fydam = GsonUtil.toIntArray(api_raigeki["api_fydam"])
    val api_eydam = GsonUtil.toIntArray(api_raigeki["api_eydam"])
    val api_fcl = GsonUtil.toIntArray(api_raigeki["api_fcl"])
    val api_ecl = GsonUtil.toIntArray(api_raigeki["api_ecl"])

    val dayPhaseRow = DamageDayRowBody(arg)
    if(arg.isSplitHp){
        for (i in 0..6) {
            val at = i
            val df = api_frai.tryGet(i) ?: -1
            if (df < 0) {
                continue
            }
            val cl = api_fcl[i]
            val ydam = api_fydam[i]
            val kabau = (api_edam[df] * 100).toInt() % 100 > 5
            val row = ArrayList<String>(dayPhaseRow)
            row.add("雷撃戦")
            row.add(fleetName)
            row.add(stage)
            row.add("自軍")
            row.add("")
            row.add("")
            row.add("")
            row.add("")
            row.add(cl.toString())
            row.add(ydam.toString())
            row.add(if (kabau) "1" else "0")
            row.addAll(friendRows[at].updateShipRowBody(prevHP[friendHPIndex][at], friendMaxHP[at]))
            row.addAll(enemyRows[df].updateShipRowBody(prevHP[enemyHPIndex][df], enemyMaxHP[df]))
            row.add(arg.combinedFlagString)
            row.add(if (arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
            if (arg.filter.filterRaigekiAttackDefence(arg.battle, at, df, isSecond, true) && arg.filter.filterOutput(row)) {
                body.add(row)
            }
        }
        for (i in 0..6) {
            val at = i
            val df = api_erai.tryGet(i) ?: -1
            if (df < 0) {
                continue
            }
            val cl = api_ecl[i]
            val ydam = api_eydam[i]
            val kabau = (api_fdam[df] * 100).toInt() % 100 > 5
            val row = ArrayList<String>(dayPhaseRow)
            row.add("雷撃戦")
            row.add(fleetName)
            row.add(stage)
            row.add("敵軍")
            row.add("")
            row.add("")
            row.add("")
            row.add("")
            row.add(cl.toString())
            row.add(ydam.toString())
            row.add(if (kabau) "1" else "0")
            row.addAll(enemyRows[at].updateShipRowBody(prevHP[enemyHPIndex][at], enemyMaxHP[at]))
            row.addAll(friendRows[df].updateShipRowBody(prevHP[friendHPIndex][df], friendMaxHP[df]))
            row.add(arg.combinedFlagString)
            row.add(if (arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
            if (arg.filter.filterRaigekiAttackDefence(arg.battle, at, df, isSecond, false) && arg.filter.filterOutput(row)) {
                body.add(row)
            }
        }    }
    else {
        for (i in 1..6) {
            val at = i
            val df = api_frai[i]
            if (df <= 0) {
                continue
            }
            val cl = api_fcl[i]
            val ydam = api_fydam[i]
            val kabau = (api_edam[df] * 100).toInt() % 100 > 5
            val row = ArrayList<String>(dayPhaseRow)
            row.add("雷撃戦")
            row.add(fleetName)
            row.add(stage)
            row.add("自軍")
            row.add("")
            row.add("")
            row.add("")
            row.add("")
            row.add(cl.toString())
            row.add(ydam.toString())
            row.add(if (kabau) "1" else "0")
            row.addAll(friendRows[at - 1].updateShipRowBody(prevHP[friendHPIndex][at - 1], friendMaxHP[at - 1]))
            row.addAll(enemyRows[df - 1].updateShipRowBody(prevHP[enemyHPIndex][df - 1], enemyMaxHP[df - 1]))
            row.add(arg.combinedFlagString)
            row.add(if (arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
            if (arg.filter.filterRaigekiAttackDefence(arg.battle, at, df, isSecond, true) && arg.filter.filterOutput(row)) {
                body.add(row)
            }
        }
        for (i in 1..6) {
            val at = i
            val df = api_erai[i]
            if (df <= 0) {
                continue
            }
            val cl = api_ecl[i]
            val ydam = api_eydam[i]
            val kabau = (api_fdam[df] * 100).toInt() % 100 > 5
            val row = ArrayList<String>(dayPhaseRow)
            row.add("雷撃戦")
            row.add(fleetName)
            row.add(stage)
            row.add("敵軍")
            row.add("")
            row.add("")
            row.add("")
            row.add("")
            row.add(cl.toString())
            row.add(ydam.toString())
            row.add(if (kabau) "1" else "0")
            row.addAll(enemyRows[at - 1].updateShipRowBody(prevHP[enemyHPIndex][at - 1], enemyMaxHP[at - 1]))
            row.addAll(friendRows[df - 1].updateShipRowBody(prevHP[friendHPIndex][df - 1], friendMaxHP[df - 1]))
            row.add(arg.combinedFlagString)
            row.add(if (arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
            if (arg.filter.filterRaigekiAttackDefence(arg.battle, at, df, isSecond, false) && arg.filter.filterOutput(row)) {
                body.add(row)
            }
        }
    }
}

private fun RaigekiRowBodyConstructEC(
        arg:ScriptArg,
        attackList: List<BattleAtackDto>?,
        apiName:String,
        stage: String,
        startHP: ArrayList<IntArray>,
        body: ArrayList<ArrayList<String>>
)
{
    if(attackList == null){ return }
    val api_raigeki = arg.dayPhaseOrNull?.tree?.get(apiName) as? LinkedTreeMap<*,*>
    if(api_raigeki == null){ return }
    val prevHP = startHP
    val api_frai = GsonUtil.toIntArray(api_raigeki["api_frai"])
    val api_erai = GsonUtil.toIntArray(api_raigeki["api_erai"])
    val api_fdam = GsonUtil.toDoubleArray(api_raigeki["api_fdam"])
    val api_edam = GsonUtil.toDoubleArray(api_raigeki["api_edam"])
    val api_fydam = GsonUtil.toIntArray(api_raigeki["api_fydam"])
    val api_eydam = GsonUtil.toIntArray(api_raigeki["api_eydam"])
    val api_fcl = GsonUtil.toIntArray(api_raigeki["api_fcl"])
    val api_ecl = GsonUtil.toIntArray(api_raigeki["api_ecl"])
    val dayPhaseRow = DamageDayRowBody(arg)
    if(arg.isSplitHp){
        for (i in 0..12) {
            val at = i
            val df = api_frai.tryGet(i) ?: -1
            val isSecond = arg.battle.isCombined && at >= 6
            val enemyIsSecond = df >= 6
            val fleetName =
                    if (arg.battle.isCombined.not()) {
                        "通常艦隊"
                    }
                    else if (isSecond) {
                        "連合第2艦隊"
                    }
                    else {
                        "連合第1艦隊"
                    }
            if (df <= 0) {
                continue
            }
            val cl = api_fcl[i]
            val ydam = api_fydam[i]
            val kabau = (api_edam[df] * 100).toInt() % 100 > 5
            val row = ArrayList<String>(dayPhaseRow)
            row.add("雷撃戦")
            row.add(fleetName)
            row.add(stage)
            row.add("自軍")
            row.add("")
            row.add("")
            row.add("")
            row.add("")
            row.add(cl.toString())
            row.add(ydam.toString())
            row.add(if (kabau) "1" else "0")
            if (isSecond) {
                row.addAll(arg.combinedRows[at - 6].updateShipRowBody(prevHP[HP_INDEX_FRIEND_COMBINED][at - 6], arg.battle.maxFriendHpCombined[at - 6]))
            }
            else {
                row.addAll(arg.friendRows[at].updateShipRowBody(prevHP[HP_INDEX_FRIEND][at], arg.battle.maxFriendHp[at]))
            }
            if (enemyIsSecond) {
                row.addAll(arg.enemyCombinedRows[df - 6].updateShipRowBody(prevHP[HP_INDEX_ENEMY_COMBINED][df - 6], arg.battle.maxEnemyHpCombined[df - 6]))
            }
            else {
                row.addAll(arg.enemyRows[df - 1].updateShipRowBody(prevHP[HP_INDEX_ENEMY][df - 1], arg.battle.maxEnemyHp[df - 1]))
            }
            row.add(arg.combinedFlagString)
            row.add(if (arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
            if (arg.filter.filterRaigekiAttackDefenceEC(arg.battle, at, df, true) && arg.filter.filterOutput(row)) {
                body.add(row)
            }
        }
        for (i in 0..12) {
            val at = i
            val df = api_erai.tryGet(i) ?: -1
            val isSecond = arg.battle.isCombined && df >= 6
            val enemyIsSecond = at >= 6
            val fleetName =
                    if (arg.battle.isCombined.not()) {
                        "通常艦隊"
                    }
                    else if (isSecond) {
                        "連合第2艦隊"
                    }
                    else {
                        "連合第1艦隊"
                    }
            if (df <= 0) {
                continue
            }
            val cl = api_ecl[i]
            val ydam = api_eydam[i]
            val kabau = (api_fdam[df] * 100).toInt() % 100 > 5
            val row = ArrayList<String>(dayPhaseRow)
            row.add("雷撃戦")
            row.add(fleetName)
            row.add(stage)
            row.add("敵軍")
            row.add("")
            row.add("")
            row.add("")
            row.add("")
            row.add(cl.toString())
            row.add(ydam.toString())
            row.add(if (kabau) "1" else "0")
            if (enemyIsSecond) {
                row.addAll(arg.enemyCombinedRows[at - 6].updateShipRowBody(prevHP[HP_INDEX_ENEMY_COMBINED][at - 6], arg.battle.maxEnemyHpCombined[at - 6]))
            }
            else {
                row.addAll(arg.enemyRows[at].updateShipRowBody(prevHP[HP_INDEX_ENEMY][at], arg.battle.maxEnemyHp[at]))
            }
            if (isSecond) {
                row.addAll(arg.combinedRows[df - 6].updateShipRowBody(prevHP[HP_INDEX_FRIEND_COMBINED][df - 6], arg.battle.maxFriendHpCombined[df - 6]))
            }
            else {
                row.addAll(arg.friendRows[df].updateShipRowBody(prevHP[HP_INDEX_FRIEND][df], arg.battle.maxFriendHp[df]))
            }
            row.add(arg.combinedFlagString)
            row.add(if (arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
            if (arg.filter.filterRaigekiAttackDefenceEC(arg.battle, at, df, false) && arg.filter.filterOutput(row)) {
                body.add(row)
            }
        }
    }
    else {
        for (i in 1..12) {
            val at = i
            val df = api_frai[i]
            val isSecond = at >= 7
            val enemyIsSecond = df >= 7
            val fleetName =
                    if (arg.battle.isCombined.not()) {
                        "通常艦隊"
                    }
                    else if (isSecond) {
                        "連合第2艦隊"
                    }
                    else {
                        "連合第1艦隊"
                    }
            if (df <= 0) {
                continue
            }
            val cl = api_fcl[i]
            val ydam = api_fydam[i]
            val kabau = (api_edam[df] * 100).toInt() % 100 > 5
            val row = ArrayList<String>(dayPhaseRow)
            row.add("雷撃戦")
            row.add(fleetName)
            row.add(stage)
            row.add("自軍")
            row.add("")
            row.add("")
            row.add("")
            row.add("")
            row.add(cl.toString())
            row.add(ydam.toString())
            row.add(if (kabau) "1" else "0")
            if (isSecond) {
                row.addAll(arg.combinedRows[at - 7].updateShipRowBody(prevHP[HP_INDEX_FRIEND_COMBINED][at - 7], arg.battle.maxFriendHpCombined[at - 7]))
            }
            else {
                row.addAll(arg.friendRows[at - 1].updateShipRowBody(prevHP[HP_INDEX_FRIEND][at - 1], arg.battle.maxFriendHp[at - 1]))
            }
            if (enemyIsSecond) {
                row.addAll(arg.enemyCombinedRows[df - 7].updateShipRowBody(prevHP[HP_INDEX_ENEMY_COMBINED][df - 7], arg.battle.maxEnemyHpCombined[df - 7]))
            }
            else {
                row.addAll(arg.enemyRows[df - 1].updateShipRowBody(prevHP[HP_INDEX_ENEMY][df - 1], arg.battle.maxEnemyHp[df - 1]))
            }
            row.add(arg.combinedFlagString)
            row.add(if (arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
            if (arg.filter.filterRaigekiAttackDefenceEC(arg.battle, at, df, true) && arg.filter.filterOutput(row)) {
                body.add(row)
            }
        }
        for (i in 1..12) {
            val at = i
            val df = api_erai[i]
            val isSecond = df >= 7
            val enemyIsSecond = at >= 7
            val fleetName =
                    if (arg.battle.isCombined.not()) {
                        "通常艦隊"
                    }
                    else if (isSecond) {
                        "連合第2艦隊"
                    }
                    else {
                        "連合第1艦隊"
                    }
            if (df <= 0) {
                continue
            }
            val cl = api_ecl[i]
            val ydam = api_eydam[i]
            val kabau = (api_fdam[df] * 100).toInt() % 100 > 5
            val row = ArrayList<String>(dayPhaseRow)
            row.add("雷撃戦")
            row.add(fleetName)
            row.add(stage)
            row.add("敵軍")
            row.add("")
            row.add("")
            row.add("")
            row.add("")
            row.add(cl.toString())
            row.add(ydam.toString())
            row.add(if (kabau) "1" else "0")
            if (enemyIsSecond) {
                row.addAll(arg.enemyCombinedRows[at - 7].updateShipRowBody(prevHP[HP_INDEX_ENEMY_COMBINED][at - 7], arg.battle.maxEnemyHpCombined[at - 7]))
            }
            else {
                row.addAll(arg.enemyRows[at - 1].updateShipRowBody(prevHP[HP_INDEX_ENEMY][at - 1], arg.battle.maxEnemyHp[at - 1]))
            }
            if (isSecond) {
                row.addAll(arg.combinedRows[df - 7].updateShipRowBody(prevHP[HP_INDEX_FRIEND_COMBINED][df - 7], arg.battle.maxFriendHpCombined[df - 7]))
            }
            else {
                row.addAll(arg.friendRows[df - 1].updateShipRowBody(prevHP[HP_INDEX_FRIEND][df - 1], arg.battle.maxFriendHp[df - 1]))
            }
            row.add(arg.combinedFlagString)
            row.add(if (arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
            if (arg.filter.filterRaigekiAttackDefenceEC(arg.battle, at, df, false) && arg.filter.filterOutput(row)) {
                body.add(row)
            }
        }
    }
}


fun RaigekiRowBody(arg:ScriptArg): ArrayList<ArrayList<String>> {
    val body = ArrayList<ArrayList<String>>()
    arg.dayPhaseOrNull?.run {
        val phase = this
        RaigekiRowBodyConstruct(
                arg = arg,
                attackList = phase.opening,
                apiName = "api_opening_atack",
                stage = "開幕",
                startHP = arg.battleHP.dayPhase!!.openingRaigekiStartHP,
                body = body
        )
        RaigekiRowBodyConstruct(
                arg = arg,
                attackList = phase.raigeki,
                apiName = "api_raigeki",
                stage = "閉幕",
                startHP = arg.battleHP.dayPhase!!.raigekiStartHP,
                body = body
        )
    }
    return body
}

