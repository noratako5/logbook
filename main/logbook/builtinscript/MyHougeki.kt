package logbook.builtinscript

import com.google.gson.internal.LinkedTreeMap
import logbook.dto.BattleExDto
import logbook.dto.BattleAtackDto
import logbook.internal.Item
import logbook.scripting.BuiltinScriptFilter
import logbook.util.GsonUtil
import java.util.*

fun MyHougekiRowHeader(): ArrayList<String> {
    val header = DamageDayNightRowHeader()
    header.add("戦闘種別")
    header.add("自艦隊")
    header.add("巡目")
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
    header.add("味方第一艦隊ターゲット数")
    header.add("味方第二艦隊ターゲット数")
    return header
}

/**
 * attackListがnullのときは即returnする
 * 敵が連合のときisSecondフラグは無視される
 */
private fun MyHougekiRowBodyConstruct(
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
    if(arg.battle.isEnemyCombined){
        MyHougekiRowBodyConstructEC(arg = arg,attackList = attackList,apiName = apiName,hougekiCount = hougekiCount,startHP = startHP,body = body)
        return
    }
}

/**
 * 敵が連合の時用　勝手に分岐してこっちにくるので直接こっちを呼ぶ必要はない
 */
private fun MyHougekiRowBodyConstructEC(
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
    val hougekiHP = startHP.createAttackHP(attackList).first
    val api_at_eflag = GsonUtil.toIntArray(api_hougeki["api_at_eflag"])
    val api_at_list = GsonUtil.toIntArray(api_hougeki["api_at_list"])
    val api_at_type = GsonUtil.toIntArray(api_hougeki["api_at_type"])
    val api_df_list = GsonUtil.toIntArrayArray(api_hougeki["api_df_list"])
    val api_si_list = GsonUtil.toIntArrayArray(api_hougeki["api_si_list"])
    val api_cl_list = GsonUtil.toIntArrayArray(api_hougeki["api_cl_list"])
    val api_damage = GsonUtil.toDoubleArrayArray(api_hougeki["api_damage"])
    val dayPhaseRow =  DamageDayRowBody(arg)

    val targetCount = startHP[HP_INDEX_FRIEND].count { it > 0 }.let{ it -  (arg.battle.escaped?.copyOfRange(0,6)?.count{ it }?:0) }.toString()
    val targetCountCombined = startHP[HP_INDEX_FRIEND_COMBINED].count { it > 0 }.let{ it -  (arg.battle.escaped?.copyOfRange(6,12)?.count{ it }?:0) }.toString()
    for (i in 1..api_at_list.size - 1) {
        val eflag = api_at_eflag[i]
        val at = api_at_list[i]
        val atType = api_at_type[i]
        val dfList = api_df_list[i]
        val siList = api_si_list[i]
        val clList = api_cl_list[i]
        val damageList = api_damage[i]

        val attackFleetName = if (eflag == 0) "自軍" else "敵軍"
        val itemName = arrayOf("","","")
        for (j in itemName.indices) {
            if (j < siList.size && siList[j] > 0) {
                itemName[j] = Item.get(siList[j]).name
            }
        }

        for (j in dfList.indices) {
            val df = dfList[j]
            val damage = damageList[j].toInt()
            val kabau = damageList[j] - damage.toDouble() > 0.05
            val cl = clList[j]
            val fleetName =
                    if (arg.battle.isCombined.not()) {"通常艦隊"}
                    else if ((eflag == 1 && df > 6) || (eflag == 0 && at > 6)) {"連合第2艦隊"}
                    else {"連合第1艦隊"}
            val row = ArrayList<String>(dayPhaseRow)
            row.add("砲撃戦")
            row.add(fleetName)
            row.add(hougekiCount)
            row.add(attackFleetName)
            row.add(atType.toString())
            row.add(itemName[0])
            row.add(itemName[1])
            row.add(itemName[2])
            row.add(cl.toString())
            row.add(damage.toString())
            row.add(if (kabau) "1" else "0")
            if (eflag == 0) {
                if (at < 7) { row.addAll(arg.friendRows[at-1].updateShipRowBody(prevHP[HP_INDEX_FRIEND][at-1],arg.battle.maxFriendHp[at-1])) }
                else { row.addAll(arg.combinedRows[at-7].updateShipRowBody(prevHP[HP_INDEX_FRIEND_COMBINED][at-7], arg.battle.maxFriendHpCombined[at-7])) }
                if (df < 7) { row.addAll(arg.enemyRows[df-1].updateShipRowBody(prevHP[HP_INDEX_ENEMY][df-1],arg.battle.maxEnemyHp[df-1])) }
                else { row.addAll(arg.enemyCombinedRows[df-7].updateShipRowBody(prevHP[HP_INDEX_ENEMY_COMBINED][df-7],arg.battle.maxEnemyHpCombined[df-7])) }
            }
            else {
                if (at < 7) { row.addAll(arg.enemyRows[at-1].updateShipRowBody(prevHP[HP_INDEX_ENEMY][at-1],arg.battle.maxEnemyHp[at-1])) }
                else { row.addAll(arg.enemyCombinedRows[at-7].updateShipRowBody(prevHP[HP_INDEX_ENEMY_COMBINED][at-7],arg.battle.maxEnemyHpCombined[at-7])) }
                if (df < 7) { row.addAll(arg.friendRows[df-1].updateShipRowBody(prevHP[HP_INDEX_FRIEND][df-1],arg.battle.maxFriendHp[df-1])) }
                else { row.addAll(arg.combinedRows[df-7].updateShipRowBody(prevHP[HP_INDEX_FRIEND_COMBINED][df-7], arg.battle.maxFriendHpCombined[df-7])) }
            }
            row.add(arg.combinedFlagString)
            row.add(if (arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
            row.add(targetCount)
            row.add(targetCountCombined)
            if (arg.filter.filterHougekiAttackDefenceEC(arg.battle, at, df, eflag) && arg.filter.filterOutput(row)) {
                body.add(row)
            }
            if (i - 1 < hougekiHP.size && j < hougekiHP[i-1].size) {
                prevHP = hougekiHP[i-1][j]
            }
        }
    }
}

fun MyHougekiRowBody(arg:ScriptArg): ArrayList<ArrayList<String>> {
    val body = ArrayList<ArrayList<String>>()
    val phase = arg.dayPhaseOrNull
    if(phase == null){ return body }

    val main = arg.battle.dock?.ships
    if(main == null || main.any { s -> s.type == "潜水艦" || s.type == "潜水空母" }){ return body }

    val combined = arg.battle.dockCombined?.ships
    if(combined == null || combined.any { s -> s.type == "潜水艦" || s.type == "潜水空母" }){ return body }

//        MyHougekiRowBodyConstruct(
//                arg = arg,
//                attackList = phase.hougeki1,
//                apiName = "api_hougeki1",
//                hougekiCount = "1",
//                isSecond = phase.kind.isHougeki1Second,
//                startHP = arg.battleHP.dayPhase!!.hougeki1StartHP,
//                body = body
//        )
    MyHougekiRowBodyConstruct(
            arg = arg,
            attackList = phase.hougeki2,
            apiName = "api_hougeki2",
            hougekiCount = if(phase.kind.isHougeki1Second == phase.kind.isHougeki2Second)"2" else "1",
            isSecond = phase.kind.isHougeki2Second,
            startHP = arg.battleHP.dayPhase!!.hougeki2StartHP,
            body = body
    )
//        MyHougekiRowBodyConstruct(
//                arg = arg,
//                attackList = phase.hougeki3,
//                apiName = "api_hougeki3",
//                hougekiCount =
//                if(phase.kind.isHougeki1Second == phase.kind.isHougeki2Second  && phase.kind.isHougeki1Second == phase.kind.isHougeki3Second)"3"
//                else if(phase.kind.isHougeki1Second == phase.kind.isHougeki2Second) "1"
//                else "2",
//                isSecond = phase.kind.isHougeki3Second,
//                startHP = arg.battleHP.dayPhase!!.hougeki3StartHP,
//                body = body
//        )

    return body
}