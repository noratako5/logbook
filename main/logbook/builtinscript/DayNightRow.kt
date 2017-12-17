package logbook.builtinscript

import logbook.dto.AirBattleDto
import java.util.*
import logbook.dto.BattleExDto

/**
 * 昼と夜で共通の列を用いるダメージ検証系用
 * 命中検証などでは昼夜合算しないので昼夜別の列を使うべき
 */
fun DamageDayNightRowHeader(): ArrayList<String> {
    val header = PhaseRowHeader()
    header.add("自索敵")
    header.add("敵索敵")
    header.add("制空権")
    header.add("会敵")
    header.add("自触接")
    header.add("敵触接")
    header.add("自照明弾")
    header.add("敵照明弾")
    return header
}

/**
 * 制空、触接が1回目を参照するので空襲マスとか基地航空の航空出力に使ってはいけない
 */
fun DamageDayRowBody(arg: ScriptArg): ArrayList<String> {
    val battle = arg.battle
    val body = PhaseRowBody(arg)
    body.add(battle.sakuteki?.get(0)?:"")
    body.add(battle.sakuteki?.get(1)?:"")
    val phase1 = battle.getPhase1()
    body.add(phase1?.air?.seiku?:"")
    body.add(battle.formationMatch?:"")
    body.add(phase1?.air?.getTouchPlane()?.get(0)?.run{if(this == "なし") "" else this}?:"")
    body.add(phase1?.air?.getTouchPlane()?.get(1)?.run{if(this == "なし") "" else this}?:"")
    body.add("")
    body.add("")
    return body
}

fun DamageDayRowBodyAir(arg:ScriptArg,air: AirBattleDto?): ArrayList<String> {
    val battle = arg.battle
    val body = PhaseRowBody(arg)
    body.add(battle.sakuteki?.get(0)?:"")
    body.add(battle.sakuteki?.get(1)?:"")
    body.add(air?.seiku?:"")
    body.add(battle.formationMatch?:"")
    body.add(air?.getTouchPlane()?.get(0)?.run{if(this == "なし") "" else this}?:"")
    body.add(air?.getTouchPlane()?.get(1)?.run{if(this == "なし") "" else this}?:"")
    body.add("")
    body.add("")
    return body
}
fun DamageDayRowBodySienAir(arg:ScriptArg): ArrayList<String> {
    val battle = arg.battle
    val body = PhaseRowBody(arg)
    body.add(battle.sakuteki?.get(0)?:"")
    body.add(battle.sakuteki?.get(1)?:"")
    body.add("")
    body.add(battle.formationMatch?:"")
    body.add("")
    body.add("")
    body.add("")
    body.add("")
    return body
}
fun DamageNightRowBody(arg:ScriptArg): ArrayList<String> {
    val battle = arg.battle
    val phaseOrNull =
        if(battle.phase1?.isNight?:false){
            battle.phase1
        }else if(battle.phase2?.isNight?:false){
            battle.phase2
        }else{
            null
        }
    if(phaseOrNull == null){
        val body = PhaseRowBody(arg)
        val length = DamageDayNightRowHeader().size
        for (i in body.size..length - 1) {
            body.add("")
        }
        return body
    }
    val phase = phaseOrNull!!
    val body = PhaseRowBody(arg)
    body.add("")
    body.add("")
    body.add("")
    body.add(battle.formationMatch)
    val touchPlaneNames = AirBattleDto.toTouchPlaneString(phase.touchPlane)
    body.add(if (touchPlaneNames[0] == "なし") "" else touchPlaneNames[0])
    body.add(if (touchPlaneNames[1] == "なし") "" else touchPlaneNames[1])
    body.add(phase.flarePos?.get(0)?.toString()?.run{if(this == "-1") "" else this}?:"")
    body.add(phase.flarePos?.get(1)?.toString()?.run{if(this == "-1") "" else this}?:"")
    return body
}
