package logbook.builtinscript

import com.google.gson.internal.LinkedTreeMap
import logbook.dto.BattleAtackDto
import logbook.internal.Item
import logbook.scripting.BuiltinScriptFilter
import logbook.util.GsonUtil
import java.util.*

fun YasenRowHeader(): ArrayList<String> {
    val header = DamageDayNightRowHeader()
    header.add("戦闘種別")
    header.add("自艦隊")
    header.add("開始")
    header.add("攻撃艦")
    header.add("砲撃種別")
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

private fun YasenRowBodyConstruct(
        arg:ScriptArg,
        attackList: List<BattleAtackDto>?,
        apiName:String,
        spMidnightString: String,
        startHP: ArrayList<IntArray>,
        body: ArrayList<ArrayList<String>>
)
{
    if(attackList == null){
        return
    }
    if(arg.battle.isEnemyCombined){
        YasenRowBodyConstructEC(
                arg = arg,
                attackList = attackList,
                apiName = apiName,
                spMidnightString = spMidnightString,
                startHP = startHP,
                body = body
        )
        return
    }
    val api_hougeki = arg.nightPhaseOrNull?.tree?.get(apiName) as? LinkedTreeMap<*,*>
    if(api_hougeki == null){ return }

    var prevHP = startHP
    val isSecond = arg.battle.isCombined//連合で第一艦隊が夜戦に行った前例が無いので判定をサボる
    val fleetName =
        if (arg.battle.isCombined.not()){"通常艦隊"}
        else if (isSecond) {"連合第2艦隊"}
        else {"連合第1艦隊"}
    val hougekiHP = startHP.createAttackHP(attackList,arg.battle).first
    val api_at_eflag = GsonUtil.toIntArray(api_hougeki["api_at_eflag"])
    val api_at_list = GsonUtil.toIntArray(api_hougeki["api_at_list"])
    val api_sp_list = GsonUtil.toIntArray(api_hougeki["api_sp_list"])
    val api_df_list = GsonUtil.toIntArrayArray(api_hougeki["api_df_list"])
    val api_si_list = GsonUtil.toIntArrayArray(api_hougeki["api_si_list"])
    val api_cl_list = GsonUtil.toIntArrayArray(api_hougeki["api_cl_list"])
    val api_damage = GsonUtil.toDoubleArrayArray(api_hougeki["api_damage"])
    val enemyList = arg.battle.enemy
    val friendList = if (isSecond) arg.battle.dockCombined.ships else arg.battle.dock.ships
    val friendRows = if(isSecond) arg.combinedRows else arg.friendRows
    val enemyRows = arg.enemyRows
    val friendHPIndex = if(isSecond) HP_INDEX_FRIEND_COMBINED else HP_INDEX_FRIEND
    val enemyHPIndex = HP_INDEX_ENEMY
    val friendMaxHP = if(isSecond) arg.battle.maxFriendHpCombined else arg.battle.maxFriendHp
    val enemyMaxHP = arg.battle.maxEnemyHp

    val nightPhaseRow = DamageNightRowBody(arg)
    if(arg.isSplitHp){
        for (i in 0..api_at_list.size - 1) {
            val eflag = api_at_eflag[i]
            val at = api_at_list[i]
            val sp = api_sp_list[i]
            val dfList = api_df_list[i]
            val siList = api_si_list[i]
            val clList = api_cl_list[i]
            val damageList = api_damage[i]

            val attackFleetName = if (eflag == 0) "自軍" else "敵軍"
            val itemName = arrayOfNulls<String>(3)
            for (j in itemName.indices) {
                if (j < siList.size && 0 < siList[j]) {
                    itemName[j] = Item.get(siList[j]).name
                }
            }

            for (j in dfList.indices) {
                val cl = clList[j]
                if (cl >= 0) {
                    val df = dfList[j]
                    val damage = damageList[j].toInt()
                    val kabau = damageList[j] - damage.toDouble() > 0.05
                    val row = ArrayList<String>(nightPhaseRow)
                    row.add("夜戦")
                    row.add(fleetName)
                    row.add(spMidnightString)
                    row.add(attackFleetName)
                    row.add(sp.toString())
                    row.add(itemName[0] ?: "")
                    row.add(itemName[1] ?: "")
                    row.add(itemName[2] ?: "")
                    row.add(cl.toString())
                    row.add(damage.toString())
                    row.add(if (kabau) "1" else "0")
                    if (eflag == 0) {
                        row.addAll(friendRows[at].updateShipRowBody(prevHP[friendHPIndex][at], friendMaxHP[at]))
                    }
                    else {
                        row.addAll(enemyRows[at].updateShipRowBody(prevHP[enemyHPIndex][at], enemyMaxHP[at]))
                    }
                    if (eflag == 1) {
                        row.addAll(friendRows[df].updateShipRowBody(prevHP[friendHPIndex][df], friendMaxHP[df]))
                    }
                    else {
                        row.addAll(enemyRows[df].updateShipRowBody(prevHP[enemyHPIndex][df], enemyMaxHP[df]))
                    }
                    row.add(arg.combinedFlagString)
                    row.add(if (arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
                    if (arg.filter.filterHougekiAttackDefenceEflag(arg.battle, at, df,eflag, isSecond) && arg.filter.filterOutput(row)) {
                        body.add(row)
                    }
                    if (i < hougekiHP.size && j < hougekiHP[i].size) {
                        prevHP = hougekiHP[i][j]
                    }
                }
            }
        }

    }
    else {
        for (i in 1..api_at_list.size - 1) {
            val at = api_at_list[i]
            val sp = api_sp_list[i]
            val dfList = api_df_list[i]
            val siList = api_si_list[i]
            val clList = api_cl_list[i]
            val damageList = api_damage[i]

            val attackFleetName = if (at < 7) "自軍" else "敵軍"
            val itemName = arrayOfNulls<String>(3)
            for (j in itemName.indices) {
                if (j < siList.size && siList[j] > 0) {
                    itemName[j] = Item.get(siList[j]).name
                }
            }

            val itemInfoList = if (at < 7) friendList[at - 1].getItem() else enemyList[at - 7].getItem()
            for (j in dfList.indices) {
                val cl = clList[j]
                if (cl >= 0) {
                    val df = dfList[j]
                    val damage = damageList[j].toInt()
                    val kabau = damageList[j] - damage.toDouble() > 0.05
                    val row = ArrayList<String>(nightPhaseRow)
                    row.add("夜戦")
                    row.add(fleetName)
                    row.add(spMidnightString)
                    row.add(attackFleetName)
                    row.add(sp.toString())
                    row.add(itemName[0] ?: "")
                    row.add(itemName[1] ?: "")
                    row.add(itemName[2] ?: "")
                    row.add(cl.toString())
                    row.add(damage.toString())
                    row.add(if (kabau) "1" else "0")
                    if (at < 7) {
                        row.addAll(friendRows[at - 1].updateShipRowBody(prevHP[friendHPIndex][at - 1], friendMaxHP[at - 1]))
                    }
                    else {
                        row.addAll(enemyRows[at - 7].updateShipRowBody(prevHP[enemyHPIndex][at - 7], enemyMaxHP[at - 7]))
                    }
                    if (df < 7) {
                        row.addAll(friendRows[df - 1].updateShipRowBody(prevHP[friendHPIndex][df - 1], friendMaxHP[df - 1]))
                    }
                    else {
                        row.addAll(enemyRows[df - 7].updateShipRowBody(prevHP[enemyHPIndex][df - 7], enemyMaxHP[df - 7]))
                    }
                    row.add(arg.combinedFlagString)
                    row.add(if (arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
                    if (arg.filter.filterHougekiAttackDefence(arg.battle, at, df, isSecond) && arg.filter.filterOutput(row)) {
                        body.add(row)
                    }
                    if (i - 1 < hougekiHP.size && j < hougekiHP[i - 1].size) {
                        prevHP = hougekiHP[i - 1][j]
                    }
                }
            }
        }
    }
}


private fun YasenRowBodyConstructEC(
        arg:ScriptArg,
        attackList: List<BattleAtackDto>?,
        apiName:String,
        spMidnightString: String,
        startHP: ArrayList<IntArray>,
        body: ArrayList<ArrayList<String>>
)
{
    if(attackList == null){
        return
    }
    val api_hougeki = arg.nightPhaseOrNull?.tree?.get(apiName) as? LinkedTreeMap<*,*>
    if(api_hougeki == null){ return }
    var prevHP = startHP
    val isSecond = arg.battle.isCombined//確か旧6-5の挙動が怪しかったので味方側はactiveDeck見ない
    val enemyIsSecond = ((GsonUtil.toIntArray(arg.nightPhaseOrNull?.tree?.get("api_active_deck")))?.get(1) ?: -1) == 2
    val fleetName =
            if (arg.battle.isCombined.not()) {"通常艦隊"}
            else if (isSecond) {"連合第2艦隊"}
            else {"連合第1艦隊"}

    val hougekiHP = startHP.createAttackHP(attackList,arg.battle).first
    val friendRows = if(isSecond) arg.combinedRows else arg.friendRows
    val enemyRows = if(enemyIsSecond) arg.enemyCombinedRows else arg.enemyRows
    val friendHPIndex = if(isSecond) HP_INDEX_FRIEND_COMBINED else HP_INDEX_FRIEND
    val enemyHPIndex = if(enemyIsSecond) HP_INDEX_ENEMY_COMBINED else HP_INDEX_ENEMY
    val friendMaxHP = if(isSecond) arg.battle.maxFriendHpCombined else arg.battle.maxFriendHp
    val enemyMaxHP = if(enemyIsSecond) arg.battle.maxEnemyHpCombined else arg.battle.maxEnemyHp
    val api_at_eflag = GsonUtil.toIntArray(api_hougeki["api_at_eflag"])
    val api_at_list = GsonUtil.toIntArray(api_hougeki["api_at_list"])
    val api_sp_list = GsonUtil.toIntArray(api_hougeki["api_sp_list"])
    val api_df_list = GsonUtil.toIntArrayArray(api_hougeki["api_df_list"])
    val api_si_list = GsonUtil.toIntArrayArray(api_hougeki["api_si_list"])
    val api_cl_list = GsonUtil.toIntArrayArray(api_hougeki["api_cl_list"])
    val api_damage = GsonUtil.toDoubleArrayArray(api_hougeki["api_damage"])
    val nightPhaseRow = DamageNightRowBody(arg)
    if(arg.isSplitHp){
        for (i in 0..api_at_list.size - 1) {
            val eflag = api_at_eflag[i]
            val at = api_at_list[i]
            val sp = api_sp_list[i]
            val dfList = api_df_list[i]
            val siList = api_si_list[i]
            val clList = api_cl_list[i]
            val damageList = api_damage[i]

            val attackFleetName = if (eflag == 0) "自軍" else "敵軍"
            val itemName = arrayOfNulls<String>(3)
            for (j in itemName.indices) {
                if (j < siList.size && 0 < siList[j]) {
                    itemName[j] = Item.get(siList[j]).name
                }
            }
            for (j in dfList.indices) {
                val cl = clList[j]
                if (cl >= 0) {
                    val df = dfList[j]
                    val damage = damageList[j].toInt()
                    val kabau = damageList[j] - damage.toDouble() > 0.05
                    val row = ArrayList<String>(nightPhaseRow)
                    row.add("夜戦")
                    row.add(fleetName)
                    row.add(spMidnightString)
                    row.add(attackFleetName)
                    row.add(sp.toString())
                    row.add(itemName[0] ?: "")
                    row.add(itemName[1] ?: "")
                    row.add(itemName[2] ?: "")
                    row.add(cl.toString())
                    row.add(damage.toString())
                    row.add(if (kabau) "1" else "0")
                    if (eflag == 0) {
                        row.addAll(friendRows[at].updateShipRowBody(prevHP[friendHPIndex][at], friendMaxHP[at]))
                    }
                    else if(enemyIsSecond){
                        row.addAll(arg.enemyCombinedRows[at].updateShipRowBody(prevHP[HP_INDEX_ENEMY_COMBINED][at], arg.battle.maxEnemyHpCombined[at]))
                    }
                    else if(at < 6){
                        row.addAll(arg.enemyRows[at].updateShipRowBody(prevHP[HP_INDEX_ENEMY][at], arg.battle.maxEnemyHp[at]))
                    }
                    else{
                        row.addAll(arg.enemyCombinedRows[at - 6].updateShipRowBody(prevHP[HP_INDEX_ENEMY_COMBINED][at - 6], arg.battle.maxEnemyHpCombined[at - 6]))
                    }
                    if(eflag == 1){
                        row.addAll(friendRows[df].updateShipRowBody(prevHP[friendHPIndex][df], friendMaxHP[df]))
                    }
                    else if(enemyIsSecond){
                        row.addAll(arg.enemyCombinedRows[df].updateShipRowBody(prevHP[HP_INDEX_ENEMY_COMBINED][df], arg.battle.maxEnemyHpCombined[df]))
                    }
                    else if (df < 6) {
                        row.addAll(arg.enemyRows[df].updateShipRowBody(prevHP[HP_INDEX_ENEMY][df], arg.battle.maxEnemyHp[df]))
                    }
                    else {
                        row.addAll(arg.enemyCombinedRows[df - 6].updateShipRowBody(prevHP[HP_INDEX_ENEMY_COMBINED][df - 6], arg.battle.maxEnemyHpCombined[df - 6]))
                    }
                    row.add(arg.combinedFlagString)
                    row.add(if (arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
                    if (arg.filter.filterHougekiAttackDefenceECNightEflag(arg.battle, at, df,eflag, isSecond, enemyIsSecond) && arg.filter.filterOutput(row)) {
                        body.add(row)
                    }
                    if (i < hougekiHP.size && j < hougekiHP[i].size) {
                        prevHP = hougekiHP[i][j]
                    }
                }
            }
        }
    }
    else {
        for (i in 1..api_at_list.size - 1) {
            val at = api_at_list[i]
            val sp = api_sp_list[i]
            val dfList = api_df_list[i]
            val siList = api_si_list[i]
            val clList = api_cl_list[i]
            val damageList = api_damage[i]

            val attackFleetName = if (at < 7) "自軍" else "敵軍"
            val itemName = arrayOfNulls<String>(3)
            for (j in itemName.indices) {
                if (j < siList.size && siList[j] > 0) {
                    itemName[j] = Item.get(siList[j]).name
                }
            }
            for (j in dfList.indices) {
                val cl = clList[j]
                if (cl >= 0) {
                    val df = dfList[j]
                    val damage = damageList[j].toInt()
                    val kabau = damageList[j] - damage.toDouble() > 0.05
                    val row = ArrayList<String>(nightPhaseRow)
                    row.add("夜戦")
                    row.add(fleetName)
                    row.add(spMidnightString)
                    row.add(attackFleetName)
                    row.add(sp.toString())
                    row.add(itemName[0] ?: "")
                    row.add(itemName[1] ?: "")
                    row.add(itemName[2] ?: "")
                    row.add(cl.toString())
                    row.add(damage.toString())
                    row.add(if (kabau) "1" else "0")
                    if (at < 7) {
                        row.addAll(friendRows[at - 1].updateShipRowBody(prevHP[friendHPIndex][at - 1], friendMaxHP[at - 1]))
                    }
                    else {
                        row.addAll(enemyRows[at - 7].updateShipRowBody(prevHP[enemyHPIndex][at - 7], enemyMaxHP[at - 7]))
                    }
                    if (df < 7) {
                        row.addAll(friendRows[df - 1].updateShipRowBody(prevHP[friendHPIndex][df - 1], friendMaxHP[df - 1]))
                    }
                    else {
                        row.addAll(enemyRows[df - 7].updateShipRowBody(prevHP[enemyHPIndex][df - 7], enemyMaxHP[df - 7]))
                    }
                    row.add(arg.combinedFlagString)
                    row.add(if (arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
                    if (arg.filter.filterHougekiAttackDefenceECNight(arg.battle, at, df, isSecond, enemyIsSecond) && arg.filter.filterOutput(row)) {
                        body.add(row)
                    }
                    if (i - 1 < hougekiHP.size && j < hougekiHP[i - 1].size) {
                        prevHP = hougekiHP[i - 1][j]
                    }
                }
            }
        }
    }
}

fun YasenRowBody(arg:ScriptArg): ArrayList<ArrayList<String>> {
    val body = ArrayList<ArrayList<String>>()
    arg.nightPhaseOrNull?.run {
        val phase = this
        YasenRowBodyConstruct(
                arg = arg,
                attackList = phase.hougeki,
                apiName = "api_hougeki",
                spMidnightString = if (phase === arg.battle.phase1) "夜戦開始" else "昼戦開始",
                startHP = arg.battleHP.nightPhase!!.hougekiStartHP,
                body = body
        )
        YasenRowBodyConstruct(
                arg = arg,
                attackList = phase.hougeki1,
                apiName = "api_n_hougeki1",
                spMidnightString = "1",
                startHP = arg.battleHP.nightPhase!!.hougeki1StartHP,
                body = body
        )
        YasenRowBodyConstruct(
                arg = arg,
                attackList = phase.hougeki2,
                apiName = "api_n_hougeki2",
                spMidnightString = "2",
                startHP = arg.battleHP.nightPhase!!.hougeki2StartHP,
                body = body
        )
    }
    return body
}