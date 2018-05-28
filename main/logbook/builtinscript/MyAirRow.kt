package logbook.builtinscript

import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import logbook.dto.AirBattleDto
import logbook.internal.Item
import logbook.scripting.BuiltinScriptFilter
import logbook.util.GsonUtil
import java.util.*
import logbook.gui.logic.CalcTaiku

fun MyAirRowHeader(): ArrayList<String> {
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
    for (i in 1..7) {
        val index = i.toString()
        ShipSummaryRowHeader()
                .forEach { s -> header.add("攻撃艦$index.$s") }
    }
    header.add("雷撃")
    header.add("爆撃")
    header.add("クリティカル")
    header.add("ダメージ")
    header.add("かばう")
    ShipRowHeader()
            .forEach { s -> header.add("防御艦." + s) }
    header.add("防御艦.加重対空")
    return header
}

private fun MyAirRowBodyConstruct(
        arg:ScriptArg,
        air: AirBattleDto?,
        apiName: String,
        startHP: ArrayList<IntArray>,
        body: ArrayList<ArrayList<String>>
)
{
    val calc = CalcTaiku()
    if(air == null){ return }
    val prevHP = startHP
    val api_kouku = arg.dayPhaseOrNull?.tree?.get(apiName) as? LinkedTreeMap<*,*>
    if(api_kouku == null){ return }

    val rowHead = DamageDayRowBodyAir(arg, air)
    val api_stage1 = api_kouku["api_stage1"] as? LinkedTreeMap<*, *>
    rowHead.add(GsonUtil.toIntString(api_stage1?.get("api_f_count")) ?: "")
    rowHead.add(GsonUtil.toIntString(api_stage1?.get("api_f_lostcount")) ?: "")
    rowHead.add(GsonUtil.toIntString(api_stage1?.get("api_e_count")) ?: "")
    rowHead.add(GsonUtil.toIntString(api_stage1?.get("api_e_lostcount")) ?: "")
    val api_stage2 = api_kouku["api_stage2"] as? LinkedTreeMap<*, *>
    rowHead.add(GsonUtil.toIntString(api_stage2?.get("api_f_count")) ?: "")
    rowHead.add(GsonUtil.toIntString(api_stage2?.get("api_f_lostcount")) ?: "")
    rowHead.add(GsonUtil.toIntString(api_stage2?.get("api_e_count")) ?: "")
    rowHead.add(GsonUtil.toIntString(api_stage2?.get("api_e_lostcount")) ?: "")
    val api_air_fire = api_stage2?.get("api_air_fire") as? LinkedTreeMap<*, *>
    rowHead.add(GsonUtil.toIntPlusOneString(api_air_fire?.get("api_idx")) ?: "")
    rowHead.add(GsonUtil.toIntString(api_air_fire?.get("api_kind")) ?: "")
    val useItems = GsonUtil.toIntArray(api_air_fire?.get("api_use_items"))
    rowHead.add(useItems?.tryGet(0)?.toItemInfo()?.name ?: "")
    rowHead.add(useItems?.tryGet(1)?.toItemInfo()?.name ?: "")
    rowHead.add(useItems?.tryGet(2)?.toItemInfo()?.name ?: "")

    val api_stage3 = api_kouku["api_stage3"] as? LinkedTreeMap<*, *>
    api_stage3?.run {
        val frai_flag = GsonUtil.toIntArray(this["api_frai_flag"])
        val erai_flag = GsonUtil.toIntArray(this["api_erai_flag"])
        val fbak_flag = GsonUtil.toIntArray(this["api_fbak_flag"])
        val ebak_flag = GsonUtil.toIntArray(this["api_ebak_flag"])
        val fcl_flag = GsonUtil.toIntArray(this["api_fcl_flag"])
        val ecl_flag = GsonUtil.toIntArray(this["api_ecl_flag"])
        val fdam = GsonUtil.toDoubleArray(this["api_fdam"])
        val edam = GsonUtil.toDoubleArray(this["api_edam"])
        if(arg.isSplitHp){
            for (df in 0..6) {
                if (arg.battle.dock.ships.size <= df) {
                    continue
                }
                val row = ArrayList<String>(rowHead)
                arg.enemySummaryRows.forEach { b -> row.addAll(b) }
                row.add(frai_flag?.tryGet(df)?.toString() ?: "")
                row.add(fbak_flag?.tryGet(df)?.toString() ?: "")
                row.add(fcl_flag?.tryGet(df)?.toString() ?: "")
                row.add(fdam?.tryGet(df)?.toInt()?.toString() ?: "")
                row.add(fdam?.tryGet(df)?.toKabauString() ?: "")
                row.addAll(arg.friendRows[df].updateShipRowBody(prevHP[HP_INDEX_FRIEND][df], arg.battle.maxFriendHp?.tryGet(df) ?: -1))
                row.add(calc.getFriendKajuuValue(arg.battle.dock.ships[df]).toString())
                if (arg.filter.filterDefenceCountItem(arg.battle.dock.ships.tryGet(df)) && arg.filter.filterOutput(row)) {
                    body.add(row)
                }
            }
        }
    }
}

fun MyAirRowBody(arg:ScriptArg): ArrayList<ArrayList<String>> {
    val body = ArrayList<ArrayList<String>>()
    arg.dayPhaseOrNull?.run {
        val phase = this
        MyAirRowBodyConstruct(
                arg = arg,
                air = phase.airInjection,
                apiName = "api_injection_kouku",
                startHP = arg.battleHP.dayPhase!!.injectionAirStartHP,
                body = body
        )
        MyAirRowBodyConstruct(
                arg = arg,
                air = phase.air,
                apiName = "api_kouku",
                startHP = arg.battleHP.dayPhase!!.air1StartHP,
                body = body
        )
        MyAirRowBodyConstruct(
                arg = arg,
                air = phase.air2,
                apiName = "api_kouku2",
                startHP = arg.battleHP.dayPhase!!.air2StartHP,
                body = body
        )
    }
    return body
}