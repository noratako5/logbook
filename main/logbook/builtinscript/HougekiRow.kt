package logbook.builtinscript

import com.google.gson.internal.LinkedTreeMap
import logbook.dto.BattleExDto
import logbook.dto.BattleAtackDto
import logbook.internal.Item
import logbook.scripting.BuiltinScriptFilter
import logbook.util.GsonUtil
import java.util.*

fun HougekiRowHeader(): ArrayList<String> {
    val header = DamageDayNightRowHeader()
    header.add("戦闘種別")
    header.add("艦名1")
    header.add("艦名2")
    header.add("艦名3")
    header.add("艦名4")
    header.add("艦名5")
    header.add("艦名6")
    header.add("自艦隊")
    header.add("巡目")
    header.add("攻撃艦")
    header.add("砲撃種別")
    header.add("砲撃回数")
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

/**
 * attackListがnullのときは即returnする
 * 敵が連合のときisSecondフラグは無視される
 */
private fun HougekiRowBodyConstruct(
        arg:ScriptArg,
        attackList: List<BattleAtackDto>?,
        apiName:String,
        isSecond: Boolean,
        hougekiCount: String,
        startHP: ArrayList<IntArray>,
        body: ArrayList<ArrayList<String>>
)
{
    if(attackList == null){ return }
    if(arg.battle.isEnemyCombined || arg.isSplitHp){
        HougekiRowBodyConstructEC(arg = arg,attackList = attackList,apiName = apiName,hougekiCount = hougekiCount,startHP = startHP,body = body)
        return
    }
    val api_hougeki = arg.dayPhaseOrNull?.tree?.get(apiName) as? LinkedTreeMap<*,*>
    if(api_hougeki == null){ return }
    var prevHP = startHP
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

    val hougekiHP = startHP.createAttackHP(attackList,arg.battle).first

    val api_at_list = GsonUtil.toIntArray(api_hougeki["api_at_list"])
    val api_at_type = GsonUtil.toIntArray(api_hougeki["api_at_type"])
    val api_df_list = GsonUtil.toIntArrayArray(api_hougeki["api_df_list"])
    val api_si_list = GsonUtil.toIntArrayArray(api_hougeki["api_si_list"])
    val api_cl_list = GsonUtil.toIntArrayArray(api_hougeki["api_cl_list"])
    val api_damage = GsonUtil.toDoubleArrayArray(api_hougeki["api_damage"])
    val dayPhaseRow = DamageDayRowBody(arg)
    for (i in 1..api_at_list.size - 1) {
        val at = api_at_list[i]
        val atType = api_at_type[i]
        val dfList = api_df_list[i]
        val siList = api_si_list[i]
        val clList = api_cl_list[i]
        val damageList = api_damage[i]

        val attackFleetName = if (at < 7) "自軍" else "敵軍"
        val itemName = arrayOf("","","")
        for (j in itemName.indices) {
            if (j < siList.size && siList[j] > 0) {
                itemName[j] = Item.get(siList[j]).name
            }
        }

        for (j in dfList.indices) {
            val at = NelsonTouchEffect(at,atType,j)
            val df = dfList[j]
            val damage = damageList[j].toInt()
            val kabau = damageList[j] - damage.toDouble() > 0.05
            val cl = clList[j]
            val row = ArrayList<String>(dayPhaseRow)
            row.add("砲撃戦")
            for(k in 0..5) {
                if (at < 7) {
                    //自軍
                    if (isSecond) {
                        row.add(arg.battle.dockCombined?.ships?.tryGet(k)?.name ?: "")
                    }
                    else{
                        row.add(arg.battle.dock?.ships?.tryGet(k)?.name ?: "")
                    }
                }
                else{
                    row.add(arg.battle.enemy?.tryGet(k)?.name?:"")
                }
            }
            row.add(fleetName)
            row.add(hougekiCount)
            row.add(attackFleetName)
            row.add(atType.toString())
            row.add(j.toString())
            row.add(itemName[0])
            row.add(itemName[1])
            row.add(itemName[2])
            row.add(cl.toString())
            row.add(damage.toString())
            row.add(if (kabau) "1" else "0")
            if (at < 7) { row.addAll(friendRows[at-1].updateShipRowBody(prevHP[friendHPIndex][at-1],friendMaxHP[at-1])) }
            else { row.addAll(enemyRows[at-7].updateShipRowBody(prevHP[enemyHPIndex][at-7],enemyMaxHP[at-7])) }
            if (df < 7) { row.addAll(friendRows[df-1].updateShipRowBody(prevHP[friendHPIndex][df-1],friendMaxHP[df-1])) }
            else { row.addAll(enemyRows[df-7].updateShipRowBody(prevHP[enemyHPIndex][df-7],enemyMaxHP[df-7])) }
            row.add(arg.combinedFlagString)
            row.add(if (arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
            if (arg.filter.filterHougekiAttackDefence(arg.battle, at, df, isSecond) && arg.filter.filterOutput(row)) {
                body.add(row)
            }
            if (i-1 < hougekiHP.size && j < hougekiHP[i-1].size) {
                prevHP = hougekiHP[i-1][j]
            }
        }
    }
}

/**
 * 敵が連合の時用　勝手に分岐してこっちにくるので直接こっちを呼ぶ必要はない
 */
private fun HougekiRowBodyConstructEC(
        arg:ScriptArg,
        attackList: List<BattleAtackDto>?,
        apiName:String,
        hougekiCount: String,
        startHP: ArrayList<IntArray>,
        body: ArrayList<ArrayList<String>>
)
{
    if(attackList == null){ return }
    val api_hougeki = arg.dayPhaseOrNull?.tree?.get(apiName) as? LinkedTreeMap<*,*>
    if(api_hougeki == null){ return }
    var prevHP = startHP
    val hougekiHP = startHP.createAttackHP(attackList,arg.battle).first
    val api_at_eflag = GsonUtil.toIntArray(api_hougeki["api_at_eflag"])
    val api_at_list = GsonUtil.toIntArray(api_hougeki["api_at_list"])
    val api_at_type = GsonUtil.toIntArray(api_hougeki["api_at_type"])
    val api_df_list = GsonUtil.toIntArrayArray(api_hougeki["api_df_list"])
    val api_si_list = GsonUtil.toIntArrayArray(api_hougeki["api_si_list"])
    val api_cl_list = GsonUtil.toIntArrayArray(api_hougeki["api_cl_list"])
    val api_damage = GsonUtil.toDoubleArrayArray(api_hougeki["api_damage"])
    val dayPhaseRow =  DamageDayRowBody(arg)
    if(arg.isSplitHp){
        for (i in 0..api_at_list.size - 1) {
            val eflag = api_at_eflag[i]
            val at = api_at_list[i]
            val atType = api_at_type[i]
            val dfList = api_df_list[i]
            val siList = api_si_list[i]
            val clList = api_cl_list[i]
            val damageList = api_damage[i]

            val attackFleetName = if (eflag == 0) "自軍" else "敵軍"
            val itemName = arrayOf("", "", "")
            for (j in itemName.indices) {
                if (j < siList.size && siList[j] > 0) {
                    itemName[j] = Item.get(siList[j]).name
                }
            }

            for (j in dfList.indices) {
                val at = NelsonTouchEffect(at,atType,j)
                val df = dfList[j]
                val damage = damageList[j].toInt()
                val kabau = damageList[j] - damage.toDouble() > 0.05
                val cl = clList[j]
                val fleetName =
                        if (arg.battle.isCombined.not()) {
                            "通常艦隊"
                        }
                        else if ((eflag == 1 && df >= 6) || (eflag == 0 && at >= 6)) {
                            "連合第2艦隊"
                        }
                        else {
                            "連合第1艦隊"
                        }
                val row = ArrayList<String>(dayPhaseRow)
                row.add("砲撃戦")
                for(k in 0..5) {
                    if (eflag == 0 ) {
                        //自軍
                        if (at < arg.battle.dock.ships.size) {
                            row.add(arg.battle.dock?.ships?.tryGet(k)?.name ?: "")
                        }
                        else{
                            row.add(arg.battle.dockCombined?.ships?.tryGet(k)?.name ?: "")
                        }
                    }
                    else{
                        if(at < 6) {
                            row.add(arg.battle.enemy?.tryGet(k)?.name ?: "")
                        }
                        else{
                            row.add(arg.battle.enemyCombined?.tryGet(k)?.name ?: "")
                        }
                    }
                }
                row.add(fleetName)
                row.add(hougekiCount)
                row.add(attackFleetName)
                row.add(atType.toString())
                row.add(j.toString())
                row.add(itemName[0])
                row.add(itemName[1])
                row.add(itemName[2])
                row.add(cl.toString())
                row.add(damage.toString())
                row.add(if (kabau) "1" else "0")
                if (eflag == 0) {
                    if (at < arg.battle.dock.ships.size) {
                        row.addAll(arg.friendRows[at].updateShipRowBody(prevHP[HP_INDEX_FRIEND][at], arg.battle.maxFriendHp[at]))
                    }
                    else {
                        row.addAll(arg.combinedRows[at - 6].updateShipRowBody(prevHP[HP_INDEX_FRIEND_COMBINED][at - 6], arg.battle.maxFriendHpCombined.tryGet(at - 6)?:-1))
                    }
                    if (df < 6) {
                        row.addAll(arg.enemyRows[df].updateShipRowBody(prevHP[HP_INDEX_ENEMY][df], arg.battle.maxEnemyHp[df]))
                    }
                    else {
                        row.addAll(arg.enemyCombinedRows[df - 6].updateShipRowBody(prevHP[HP_INDEX_ENEMY_COMBINED][df - 6], arg.battle.maxEnemyHpCombined.tryGet(df-6)?:-1))
                    }
                }
                else {
                    if (at < 6) {
                        row.addAll(arg.enemyRows[at].updateShipRowBody(prevHP[HP_INDEX_ENEMY][at], arg.battle.maxEnemyHp[at]))
                    }
                    else {
                        row.addAll(arg.enemyCombinedRows[at - 6].updateShipRowBody(prevHP[HP_INDEX_ENEMY_COMBINED][at - 6], arg.battle.maxEnemyHpCombined?.tryGet(at-6)?:-1))
                    }
                    if (df < arg.battle.dock.ships.size) {
                        row.addAll(arg.friendRows[df].updateShipRowBody(prevHP[HP_INDEX_FRIEND][df], arg.battle.maxFriendHp[df]))
                    }
                    else {
                        row.addAll(arg.combinedRows[df - 6].updateShipRowBody(prevHP[HP_INDEX_FRIEND_COMBINED][df - 6], arg.battle.maxFriendHpCombined?.tryGet(df-6)?:-1))
                    }
                }
                row.add(arg.combinedFlagString)
                row.add(if (arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
                if (arg.filter.filterHougekiAttackDefenceEC(arg.battle, at, df, eflag) && arg.filter.filterOutput(row)) {
                    body.add(row)
                }
                if (i < hougekiHP.size && j < hougekiHP[i].size) {
                    prevHP = hougekiHP[i][j]
                }
            }
        }
    }
    else {
        for (i in 1..api_at_list.size - 1) {
            val eflag = api_at_eflag[i]
            val at = api_at_list[i]
            val atType = api_at_type[i]
            val dfList = api_df_list[i]
            val siList = api_si_list[i]
            val clList = api_cl_list[i]
            val damageList = api_damage[i]

            val attackFleetName = if (eflag == 0) "自軍" else "敵軍"
            val itemName = arrayOf("", "", "")
            for (j in itemName.indices) {
                if (j < siList.size && siList[j] > 0) {
                    itemName[j] = Item.get(siList[j]).name
                }
            }

            for (j in dfList.indices) {
                val at = NelsonTouchEffect(at,atType,j)
                val df = dfList[j]
                val damage = damageList[j].toInt()
                val kabau = damageList[j] - damage.toDouble() > 0.05
                val cl = clList[j]
                val fleetName =
                        if (arg.battle.isCombined.not()) {
                            "通常艦隊"
                        }
                        else if ((eflag == 1 && df > 6) || (eflag == 0 && at > 6)) {
                            "連合第2艦隊"
                        }
                        else {
                            "連合第1艦隊"
                        }
                val row = ArrayList<String>(dayPhaseRow)
                row.add("砲撃戦")
                for(k in 0..5) {
                    if (eflag == 0 ) {
                        //自軍
                        if (at < 7) {
                            row.add(arg.battle.dock?.ships?.tryGet(k)?.name ?: "")
                        }
                        else{
                            row.add(arg.battle.dockCombined?.ships?.tryGet(k)?.name ?: "")
                        }
                    }
                    else{
                        if(at < 7) {
                            row.add(arg.battle.enemy?.tryGet(k)?.name ?: "")
                        }
                        else{
                            row.add(arg.battle.enemyCombined?.tryGet(k)?.name ?: "")
                        }
                    }
                }
                row.add(fleetName)
                row.add(hougekiCount)
                row.add(attackFleetName)
                row.add(atType.toString())
                row.add(j.toString())
                row.add(itemName[0])
                row.add(itemName[1])
                row.add(itemName[2])
                row.add(cl.toString())
                row.add(damage.toString())
                row.add(if (kabau) "1" else "0")
                if (eflag == 0) {
                    if (at < 7) {
                        row.addAll(arg.friendRows[at - 1].updateShipRowBody(prevHP[HP_INDEX_FRIEND][at - 1], arg.battle.maxFriendHp[at - 1]))
                    }
                    else {
                        row.addAll(arg.combinedRows[at - 7].updateShipRowBody(prevHP[HP_INDEX_FRIEND_COMBINED][at - 7], arg.battle.maxFriendHpCombined[at - 7]))
                    }
                    if (df < 7) {
                        row.addAll(arg.enemyRows[df - 1].updateShipRowBody(prevHP[HP_INDEX_ENEMY][df - 1], arg.battle.maxEnemyHp[df - 1]))
                    }
                    else {
                        row.addAll(arg.enemyCombinedRows[df - 7].updateShipRowBody(prevHP[HP_INDEX_ENEMY_COMBINED][df - 7], arg.battle.maxEnemyHpCombined[df - 7]))
                    }
                }
                else {
                    if (at < 7) {
                        row.addAll(arg.enemyRows[at - 1].updateShipRowBody(prevHP[HP_INDEX_ENEMY][at - 1], arg.battle.maxEnemyHp[at - 1]))
                    }
                    else {
                        row.addAll(arg.enemyCombinedRows[at - 7].updateShipRowBody(prevHP[HP_INDEX_ENEMY_COMBINED][at - 7], arg.battle.maxEnemyHpCombined[at - 7]))
                    }
                    if (df < 7) {
                        row.addAll(arg.friendRows[df - 1].updateShipRowBody(prevHP[HP_INDEX_FRIEND][df - 1], arg.battle.maxFriendHp[df - 1]))
                    }
                    else {
                        row.addAll(arg.combinedRows[df - 7].updateShipRowBody(prevHP[HP_INDEX_FRIEND_COMBINED][df - 7], arg.battle.maxFriendHpCombined[df - 7]))
                    }
                }
                row.add(arg.combinedFlagString)
                row.add(if (arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
                if (arg.filter.filterHougekiAttackDefenceEC(arg.battle, at, df, eflag) && arg.filter.filterOutput(row)) {
                    body.add(row)
                }
                if (i - 1 < hougekiHP.size && j < hougekiHP[i - 1].size) {
                    prevHP = hougekiHP[i - 1][j]
                }
            }
        }
    }
}

fun HougekiRowBody(arg:ScriptArg): ArrayList<ArrayList<String>> {
    val body = ArrayList<ArrayList<String>>()
    arg.dayPhaseOrNull?.run {
        val phase = this
        HougekiRowBodyConstruct(
                arg = arg,
                attackList = phase.openingTaisen,
                apiName = "api_opening_taisen",
                hougekiCount = "先制対潜",
                isSecond = arg.battle.isCombined,
                startHP = arg.battleHP.dayPhase!!.openingTaisenStartHP,
                body = body
        )
        HougekiRowBodyConstruct(
                arg = arg,
                attackList = phase.hougeki1,
                apiName = "api_hougeki1",
                hougekiCount = "1",
                isSecond = phase.kind.isHougeki1Second,
                startHP = arg.battleHP.dayPhase!!.hougeki1StartHP,
                body = body
        )
        HougekiRowBodyConstruct(
                arg = arg,
                attackList = phase.hougeki2,
                apiName = "api_hougeki2",
                hougekiCount = if(phase.kind.isHougeki1Second == phase.kind.isHougeki2Second)"2" else "1",
                isSecond = phase.kind.isHougeki2Second,
                startHP = arg.battleHP.dayPhase!!.hougeki2StartHP,
                body = body
        )
        HougekiRowBodyConstruct(
                arg = arg,
                attackList = phase.hougeki3,
                apiName = "api_hougeki3",
                hougekiCount =
                    if(phase.kind.isHougeki1Second == phase.kind.isHougeki2Second  && phase.kind.isHougeki1Second == phase.kind.isHougeki3Second)"3"
                    else if(phase.kind.isHougeki1Second == phase.kind.isHougeki2Second) "1"
                    else "2",
                isSecond = phase.kind.isHougeki3Second,
                startHP = arg.battleHP.dayPhase!!.hougeki3StartHP,
                body = body
        )
    }
    return body
}