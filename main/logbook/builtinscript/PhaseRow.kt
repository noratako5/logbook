package logbook.builtinscript

import java.util.*
import logbook.dto.BattleExDto

fun PhaseRowHeader(): ArrayList<String> {
    val header = ArrayList<String>()
    header.add("日付")
    header.add("海域")
    header.add("マス")
    header.add("出撃")
    header.add("ランク")
    header.add("敵艦隊")
    header.add("提督レベル")
    header.add("自陣形")
    header.add("敵陣形")
    return header
}

fun PhaseRowBody(arg:ScriptArg): ArrayList<String> {
    val battle = arg.battle
    val body = ArrayList<String>()
    body.add(arg.dateString)
    body.add(battle.questName)
    body.add(battle.mapCellDto?.reportString ?: "")
    val reportType =
            if((battle.mapCellDto?.isStart()?:false)&&(battle.mapCellDto?.isBoss()?:false)){ "出撃&ボス" }
            else if(battle.mapCellDto?.isStart()?:false){ "出撃" }
            else if(battle.mapCellDto?.isBoss()?:false){ "ボス" }
            else{ "" }
    body.add(reportType)
    body.add(battle.rank.toString())
    body.add(battle.enemyName)
    body.add(battle.hqLv.toString())
    body.add(battle.formation?.get(0)?:"")
    body.add(battle.formation?.get(1)?:"")
    return body
}
