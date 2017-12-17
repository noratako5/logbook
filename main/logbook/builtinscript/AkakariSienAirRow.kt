package logbook.builtinscript

import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import logbook.builtinscript.akakariLog.AkakariSyutsugekiLogReader
import logbook.dto.BattleExDto
import logbook.dto.BattleAtackDto
import logbook.dto.ShipDto
import logbook.internal.Item
import logbook.scripting.BuiltinScriptFilter
import logbook.util.GsonUtil
import java.util.*

fun AkakariSienAirRowHeader(): ArrayList<String> {
    val header = DamageDayNightRowHeader()
    header.add("支援種別")
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
    for (i in 1..6) {
        val index = i.toString()
        AkakariSienShipAirSummaryRowHeader()
                .forEach { s -> header.add("攻撃艦$index.$s") }
    }
    header.add("雷撃")
    header.add("爆撃")
    header.add("クリティカル")
    header.add("ダメージ")
    header.add("かばう")
    ShipRowHeader()
            .forEach { s -> header.add("防御艦." + s) }
    header.add("艦隊種類")
    header.add("敵艦隊種類")
    return header
}

private fun AkakariSienAirRowBodyConstruct(
        arg:ScriptArg,
        apiName: String,
        phase:BattleExDto.Phase,
        startHP: ArrayList<IntArray>,
        body: ArrayList<ArrayList<String>>
) {
    val prevHP = startHP
    val api_support_info = phase.tree?.get(apiName) as? LinkedTreeMap<*, *>
    if (api_support_info == null) {
        return
    }
    val api_support_airatack = api_support_info.get("api_support_airatack") as? LinkedTreeMap<*, *>
    if (api_support_airatack == null) {
        return
    }
    val rowHead = DamageDayRowBodySienAir(arg)
    val type = GsonUtil.toInt(phase.tree.get("api_support_flag"))
    rowHead.add(if(type==1) "航空" else if (type == 4) "対潜" else "不明")

    val api_kouku = api_support_airatack
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

    val shipIds = GsonUtil.toIntArray(api_support_airatack.get("api_ship_id"))
    val shipList = ArrayList<ShipDto>()
    if(arg.hasAkakariInfo){
        val log = AkakariSyutsugekiLogReader.battleDateToLog(arg.battle.battleDate)
        for(id in shipIds){
            if(id < 0){
                continue
            }
            val ship = log?.shipIdToStartShip(id.toString())
            ship?.run { shipList.add(this) }
        }
    }

    val api_stage3 = api_kouku["api_stage3"] as? LinkedTreeMap<*, *>
    api_stage3?.run {
        val erai_flag = GsonUtil.toIntArray(this["api_erai_flag"])
        val ebak_flag = GsonUtil.toIntArray(this["api_ebak_flag"])
        val ecl_flag = GsonUtil.toIntArray(this["api_ecl_flag"])
        val edam = GsonUtil.toDoubleArray(this["api_edam"])
        val offset = if((edam?.get(0)?.toInt()?:-1) < 0) 1 else 0
        for (df in offset..12) {
            if(!arg.battle.isEnemyCombined  && df == 6+offset){
                break
            }
            if(df == 12+offset){
                break
            }
            val row = ArrayList<String>(rowHead)
            for(i in 0..5){
                val ship = shipList.tryGet(i)
                row.addAll(AkakariSienShipAirSummaryRow(ship,ship?.maxhp?: -1, arg.battle.battleDate))
            }
            row.add(erai_flag?.tryGet(df)?.toString() ?: "")
            row.add(ebak_flag?.tryGet(df)?.toString() ?: "")
            row.add(ecl_flag?.tryGet(df)?.toString() ?: "")
            row.add(edam?.tryGet(df)?.toInt()?.toString() ?: "")
            row.add(edam?.tryGet(df)?.toKabauString() ?: "")
            if(df >= 6+offset){
                row.addAll(arg.enemyCombinedRows[df - offset - 6].updateShipRowBody(prevHP[HP_INDEX_ENEMY][df - offset - 6], arg.battle.maxEnemyHp?.tryGet(df - offset - 6) ?: -1))
            }
            else {
                row.addAll(arg.enemyRows[df - offset].updateShipRowBody(prevHP[HP_INDEX_ENEMY][df - offset], arg.battle.maxEnemyHp?.tryGet(df - offset) ?: -1))
            }
            row.add(if(arg.battle.isCombined) "連合艦隊" else "通常艦隊")
            row.add(if(arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
            if (arg.filter.filterOutput(row)) {
                body.add(row)
            }
        }
    }
}

fun AkakariSienAirRowBody(arg:ScriptArg): ArrayList<ArrayList<String>> {
    val body = ArrayList<ArrayList<String>>()
    arg.dayPhaseOrNull?.run {
        val phase = this
        AkakariSienAirRowBodyConstruct(
                arg = arg,
                apiName = "api_support_info",
                phase = phase,
                startHP = arg.battleHP.dayPhase!!.supportStartHP,
                body = body
        )
    }
    return body
}