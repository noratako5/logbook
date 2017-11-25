package logbook.builtinscript

import com.google.gson.internal.LinkedTreeMap
import logbook.dto.AirBattleDto
import logbook.internal.Item
import logbook.scripting.BuiltinScriptFilter
import logbook.util.GsonUtil
import java.util.*

fun AirLostRowHeader(): ArrayList<String> {
    val header = DamageDayNightRowHeader()
    header.add("ステージ1.自艦載機総数")
    header.add("ステージ1.自艦載機喪失数")
    header.add("ステージ1.敵艦載機総数")
    header.add("ステージ1.敵艦載機喪失数")
    header.add("ステージ2.自艦載機総数")
    header.add("ステージ2.自艦載機喪失数")
    header.add("ステージ2.敵艦載機総数")
    header.add("ステージ2.敵艦載機喪失数")
    header.add("対空カットイン.発動艦")
    header.add("対空カットイン.種別")
    header.add("対空カットイン.表示装備1")
    header.add("対空カットイン.表示装備2")
    header.add("対空カットイン.表示装備3")
    header.add("味方雷撃被タゲ数")
    header.add("味方爆撃被タゲ数")
    header.add("敵雷撃被タゲ数")
    header.add("敵爆撃被タゲ数")
    for (i in 1..6) {
        val index = i.toString()
        ShipRowHeader()
                .forEach { s -> header.add("敵艦$index.$s") }
    }
    for (i in 1..7) {
        val index = i.toString()
        ShipRowHeader()
                .forEach { s -> header.add("味方艦$index.$s") }
    }
    for (i in 1..6) {
        val index = i.toString()
        ShipRowHeader()
                .forEach { s -> header.add("敵連合第二艦隊$index.$s") }
    }
    for (i in 1..6) {
        val index = i.toString()
        ShipRowHeader()
                .forEach { s -> header.add("味方連合第二艦隊$index.$s") }
    }
    header.add("艦隊種類")
    header.add("敵艦隊種類")
    return header
}

private fun AirLostRowBodyConstruct(
        arg:ScriptArg,
        air: AirBattleDto?,
        apiName:String,
        startHP: ArrayList<IntArray>,
        body: ArrayList<ArrayList<String>>
)
{
    if(air == null){
        return
    }
    val prevHP = startHP
    val row = DamageDayRowBodyAir(arg,air)
    val api_kouku = arg.dayPhaseOrNull?.tree?.get(apiName) as? LinkedTreeMap<*,*>
    val api_stage1 = api_kouku?.get("api_stage1") as? LinkedTreeMap<*, *>
    if(GsonUtil.toInt(api_stage1?.get("api_f_count")) <= 0 && GsonUtil.toInt(api_stage1?.get("api_e_count")) <= 0){
        return
    }
    row.add(GsonUtil.toIntString(api_stage1?.get("api_f_count"))?:"")
    row.add(GsonUtil.toIntString(api_stage1?.get("api_f_lostcount"))?:"")
    row.add(GsonUtil.toIntString(api_stage1?.get("api_e_count"))?:"")
    row.add(GsonUtil.toIntString(api_stage1?.get("api_e_lostcount"))?:"")
    val api_stage2 = api_kouku?.get("api_stage2") as? LinkedTreeMap<*, *>
    row.add(GsonUtil.toIntString(api_stage2?.get("api_f_count"))?:"")
    row.add(GsonUtil.toIntString(api_stage2?.get("api_f_lostcount"))?:"")
    row.add(GsonUtil.toIntString(api_stage2?.get("api_e_count"))?:"")
    row.add(GsonUtil.toIntString(api_stage2?.get("api_e_lostcount"))?:"")
    val api_air_fire = api_stage2?.get("api_air_fire") as? LinkedTreeMap<*, *>
    row.add(GsonUtil.toIntPlusOneString(api_air_fire?.get("api_idx"))?:"")
    row.add(GsonUtil.toIntString(api_air_fire?.get("api_kind"))?:"")
    val useItems = GsonUtil.toIntArray(api_air_fire?.get("api_use_items"))
    row.add(useItems?.tryGet(0)?.toItemInfo()?.name?:"")
    row.add(useItems?.tryGet(1)?.toItemInfo()?.name?:"")
    row.add(useItems?.tryGet(2)?.toItemInfo()?.name?:"")
    var frai_count = 0
    var erai_count = 0
    var fbak_count = 0
    var ebak_count = 0
    val api_stage3 = api_kouku?.get("api_stage3") as? LinkedTreeMap<*, *>
    api_stage3?.run {
        val frai_flag = GsonUtil.toIntArray(this["api_frai_flag"])
        val erai_flag = GsonUtil.toIntArray(this["api_erai_flag"])
        val fbak_flag = GsonUtil.toIntArray(this["api_fbak_flag"])
        val ebak_flag = GsonUtil.toIntArray(this["api_ebak_flag"])
        for (i in frai_flag.indices) {
            if (frai_flag.tryGet(i) ?: -1 == 1) { frai_count++ }
            if (erai_flag.tryGet(i) ?: -1 == 1) { erai_count++ }
            if (fbak_flag.tryGet(i) ?: -1 == 1) { fbak_count++ }
            if (ebak_flag.tryGet(i) ?: -1 == 1) { ebak_count++ }
        }
    }
    val combined = api_kouku?.get("api_stage3_combined") as? LinkedTreeMap<*, *>
    combined?.run{
        val frai_flag = GsonUtil.toIntArray(this["api_frai_flag"])
        val fbak_flag = GsonUtil.toIntArray(this["api_fbak_flag"])
        val erai_flag = GsonUtil.toIntArray(this["api_erai_flag"])
        val ebak_flag = GsonUtil.toIntArray(this["api_ebak_flag"])
        if(frai_flag != null) {
            for (i in frai_flag.indices) {
                if (frai_flag.tryGet(i) ?: -1 == 1) {
                    frai_count++
                }
                if (fbak_flag.tryGet(i) ?: -1 == 1) {
                    fbak_count++
                }
            }
        }
        if (erai_flag != null) {
            for (i in erai_flag.indices) {
                if (erai_flag.tryGet(i) ?: -1 == 1) {
                    erai_count++
                }
                if (ebak_flag.tryGet(i) ?: -1 == 1) {
                    ebak_count++
                }
            }
        }
    }
    row.add(frai_count.toString())
    row.add(fbak_count.toString())
    row.add(erai_count.toString())
    row.add(ebak_count.toString())
    for (i in 0..5) { row.addAll(arg.enemyRows[i].updateShipRowBody(prevHP[HP_INDEX_ENEMY][i], arg.battle.maxEnemyHp?.tryGet(i)?:-1)) }
    for (i in 0..6) { row.addAll(arg.friendRows[i].updateShipRowBody(prevHP[HP_INDEX_FRIEND][i], arg.battle.maxFriendHp?.tryGet(i)?:-1)) }
    for (i in 0..5) { row.addAll(arg.enemyCombinedRows[i].updateShipRowBody(prevHP[HP_INDEX_ENEMY_COMBINED][i], arg.battle.maxEnemyHpCombined?.tryGet(i)?:-1)) }
    for (i in 0..5) { row.addAll(arg.combinedRows[i].updateShipRowBody(prevHP[HP_INDEX_FRIEND_COMBINED][i],arg.battle.maxFriendHpCombined?.tryGet(i)?:-1)) }
    row.add(arg.combinedFlagString)
    row.add(if (arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
    if (arg.filter.filterOutput(row)) {
        body.add(row)
    }
}

fun AirLostRowBody(arg:ScriptArg): ArrayList<ArrayList<String>> {
    val body = ArrayList<ArrayList<String>>()
    arg.dayPhaseOrNull?.run {
        val phase = this
        AirLostRowBodyConstruct(
                arg = arg,
                air = phase.airInjection,
                apiName = "api_injection_kouku",
                startHP = arg.battleHP.dayPhase!!.injectionAirStartHP,
                body = body
        )
        AirLostRowBodyConstruct(
                arg = arg,
                air = phase.air,
                apiName = "api_kouku",
                startHP = arg.battleHP.dayPhase!!.air1StartHP,
                body = body
        )
        AirLostRowBodyConstruct(
                arg = arg,
                air = phase.air2,
                apiName = "api_kouku2",
                startHP = arg.battleHP.dayPhase!!.air2StartHP,
                body = body
        )
    }
    return body
}
