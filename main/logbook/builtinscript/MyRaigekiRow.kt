package logbook.builtinscript

import com.google.gson.internal.LinkedTreeMap
import logbook.dto.BattleAtackDto
import logbook.dto.BattleExDto
import logbook.scripting.BuiltinScriptFilter
import logbook.util.GsonUtil
import java.util.*

fun MyRaigekiRowHeader(): ArrayList<String> {
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
    header.add("味方第一艦隊ターゲット数")
    header.add("味方第二艦隊ターゲット数")
    return header
}

private fun MyRaigekiRowBodyConstruct(
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
        MyRaigekiRowBodyConstructEC(
                arg = arg,
                attackList = attackList,
                apiName = apiName,
                stage = stage,
                startHP = startHP,
                body = body
        )
        return
    }
}

private fun MyRaigekiRowBodyConstructEC(
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
    val api_erai = GsonUtil.toIntArray(api_raigeki["api_erai"])
    val api_fdam = GsonUtil.toDoubleArray(api_raigeki["api_fdam"])
    val api_eydam = GsonUtil.toIntArray(api_raigeki["api_eydam"])
    val api_ecl = GsonUtil.toIntArray(api_raigeki["api_ecl"])
    val dayPhaseRow = DamageDayRowBody(arg)

    val targetCount = startHP[HP_INDEX_FRIEND].count { it > 0 }.let{ it -  (arg.battle.escaped?.copyOfRange(0,6)?.count{ it }?:0) }.toString()
    val targetCountCombined = startHP[HP_INDEX_FRIEND_COMBINED].count { it > 0 }.let{ it -  (arg.battle.escaped?.copyOfRange(6,12)?.count{ it }?:0) }.toString()
    for (i in 1..12) {
        val at = i
        val df = api_erai[i]
        val isSecond = df >= 7
        val enemyIsSecond = at >= 7
        val fleetName =
                if (arg.battle.isCombined.not()) {"通常艦隊"}
                else if (isSecond) {"連合第2艦隊"}
                else {"連合第1艦隊"}
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
        if (enemyIsSecond) { row.addAll(arg.enemyCombinedRows[at-7].updateShipRowBody(prevHP[HP_INDEX_ENEMY_COMBINED][at-7],arg.battle.maxEnemyHpCombined[at-7])) }
        else { row.addAll(arg.enemyRows[at-1].updateShipRowBody(prevHP[HP_INDEX_ENEMY][at-1],arg.battle.maxEnemyHp[at-1])) }
        if (isSecond) { row.addAll(arg.combinedRows[df-7].updateShipRowBody(prevHP[HP_INDEX_FRIEND_COMBINED][df-7], arg.battle.maxFriendHpCombined[df-7])) }
        else { row.addAll(arg.friendRows[df-1].updateShipRowBody(prevHP[HP_INDEX_FRIEND][df-1], arg.battle.maxFriendHp[df-1])) }
        row.add(arg.combinedFlagString)
        row.add(if (arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
        row.add(targetCount)
        row.add(targetCountCombined)
        if (arg.filter.filterRaigekiAttackDefenceEC(arg.battle, at, df, false) && arg.filter.filterOutput(row)) {
            body.add(row)
        }
    }
}

fun MyRaigekiRowBody(arg:ScriptArg): ArrayList<ArrayList<String>> {
    val body = ArrayList<ArrayList<String>>()
    val phase = arg.dayPhaseOrNull
    if(phase == null){ return body }

    val main = arg.battle.dock?.ships
    if(main == null || main.any { s -> s.type == "潜水艦" || s.type == "潜水空母" }){ return body }

    val combined = arg.battle.dockCombined?.ships
    if(combined == null || combined.any { s -> s.type == "潜水艦" || s.type == "潜水空母" }){ return body }


    val startHPCombined = arg.battleHP.dayPhase!!.raigekiStartHP[HP_INDEX_FRIEND_COMBINED]
    val startHP = arg.battleHP.dayPhase!!.raigekiStartHP[HP_INDEX_FRIEND]
    //if(startHP.all { it > 0 } && startHPCombined.all { it > 0 }) {
        MyRaigekiRowBodyConstruct(
                arg = arg,
                attackList = phase.raigeki,
                apiName = "api_raigeki",
                stage = "閉幕",
                startHP = arg.battleHP.dayPhase!!.raigekiStartHP,
                body = body
        )
    //}


    return body
}