package logbook.builtinscript

import com.google.gson.internal.LinkedTreeMap
import logbook.builtinscript.akakariLog.AkakariSyutsugekiLogReader
import logbook.dto.AirBattleDto
import logbook.internal.Item
import logbook.scripting.BuiltinScriptFilter
import logbook.util.GsonUtil
import java.util.*


fun AkakariBaseAirRowHeader(): ArrayList<String> {
    val header = DamageDayNightRowHeader()
    header.add("航空隊")
    header.add("攻撃順")
    header.add("基地自触接")
    header.add("基地敵触接")
    for (i in 1..4) {
        val index = i.toString()
        header.add("第" + index + "中隊")
        header.add("第" + index + "機数")
        header.add("第" + index + "出撃時改修")
        header.add("第" + index + "出撃時熟練")
        header.add("第" + index + "出撃時cond")
        header.add("第" + index + "帰投時機数")
        header.add("第" + index + "帰投時改修")
        header.add("第" + index + "帰投時熟練")
        header.add("第" + index + "帰投時cond")
    }
    header.add("ステージ1.自艦載機総数")
    header.add("ステージ1.自艦載機喪失数")
    header.add("ステージ1.敵艦載機総数")
    header.add("ステージ1.敵艦載機喪失数")
    header.add("ステージ2.自艦載機総数")
    header.add("ステージ2.自艦載機喪失数")
    header.add("ステージ2.敵艦載機総数")
    header.add("ステージ2.敵艦載機喪失数")

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
    return header
}

private fun AkakariBaseAirRowBodyConstruct(
        arg: ScriptArg,
        air: AirBattleDto,
        apiName:String,
        airIndex: Int,
        startHP: ArrayList<IntArray>,
        body: ArrayList<ArrayList<String>>
)
{
    val date = arg.battle.battleDate
    val startAirBase = AkakariSyutsugekiLogReader.battleDateToStartAirBaseData(date)
    if(startAirBase == null){
        return
    }
    val endAirBase = AkakariSyutsugekiLogReader.battleDateToEndAirBaseData(date)
    if(endAirBase == null){
        return
    }
    val areaId = AkakariSyutsugekiLogReader.battleDateToAreaId(date)
    if(areaId <= 0){
        return
    }
    val api_kouku = (arg.dayPhaseOrNull?.tree?.get(apiName) as? List<*>)?.get(airIndex) as? LinkedTreeMap<*,*>
    if(api_kouku == null){ return }

    val baseId = GsonUtil.toInt(api_kouku["api_base_id"]);
    val prevHP = startHP
    val rowHead = DamageDayRowBodyAir(arg,air)
    rowHead.add(GsonUtil.toIntString(api_kouku["api_base_id"])?:"")
    rowHead.add((airIndex+1).toString())
    val api_stage1 = api_kouku["api_stage1"] as? LinkedTreeMap<*, *>
    val touch_plane = GsonUtil.toIntArray(api_stage1?.get("api_touch_plane"))
    rowHead.add(touch_plane?.tryGet(0)?.toItemInfo()?.name?:"")
    rowHead.add(touch_plane?.tryGet(1)?.toItemInfo()?.name?:"")

    val squadron_plane = api_kouku["api_squadron_plane"] as? List<*>
    for (i in 0..3) {
        val plane = squadron_plane?.tryGet(i) as? LinkedTreeMap<*, *>
        rowHead.add(GsonUtil.toInt(plane?.get("api_mst_id")).toItemInfo()?.name?:"")
        rowHead.add(GsonUtil.toIntString(plane?.get("api_count"))?:"")
        val startPlane = startAirBase.getPlaneInfo(areaId,baseId,i+1)
        val startItem = startAirBase.getItem(areaId,baseId,i+1)
        val endPlane = endAirBase.getPlaneInfo(areaId,baseId,i+1)
        val endItem = endAirBase.getItem(areaId,baseId,i+1)
        rowHead.add(startItem?.get("api_level")?.toString()?:"")
        rowHead.add(startItem?.get("api_alv")?.toString()?:"")
        rowHead.add(startPlane?.get("api_cond")?.toString()?:"")
        rowHead.add(endPlane?.get("api_count")?.toString()?:"")
        rowHead.add(endItem?.get("api_level")?.toString()?:"")
        rowHead.add(endItem?.get("api_alv")?.toString()?:"")
        rowHead.add(endPlane?.get("api_cond")?.toString()?:"")
    }
    rowHead.add(GsonUtil.toIntString(api_stage1?.get("api_f_count"))?:"")
    rowHead.add(GsonUtil.toIntString(api_stage1?.get("api_f_lostcount"))?:"")
    rowHead.add(GsonUtil.toIntString(api_stage1?.get("api_e_count"))?:"")
    rowHead.add(GsonUtil.toIntString(api_stage1?.get("api_e_lostcount"))?:"")
    val api_stage2 = api_kouku["api_stage2"] as? LinkedTreeMap<*, *>
    rowHead.add(GsonUtil.toIntString(api_stage2?.get("api_f_count"))?:"")
    rowHead.add(GsonUtil.toIntString(api_stage2?.get("api_f_lostcount"))?:"")
    rowHead.add(GsonUtil.toIntString(api_stage2?.get("api_e_count"))?:"")
    rowHead.add(GsonUtil.toIntString(api_stage2?.get("api_e_lostcount"))?:"")
    val api_stage3 = api_kouku["api_stage3"] as? LinkedTreeMap<*, *>
    api_stage3?.run{
        val erai_flag = GsonUtil.toIntArray(this["api_erai_flag"])
        val ebak_flag = GsonUtil.toIntArray(this["api_ebak_flag"])
        val ecl_flag = GsonUtil.toIntArray(this["api_ecl_flag"])
        val edam = GsonUtil.toDoubleArray(this["api_edam"])
        if(arg.isSplitHp){
            for (df in 0..6) {
                if (arg.battle.enemy.size <= df) {
                    continue
                }
                val row = ArrayList<String>(rowHead)
                arg.friendSummaryRows.forEach { b -> row.addAll(b) }
                row.add(erai_flag?.tryGet(df)?.toString() ?: "")
                row.add(ebak_flag?.tryGet(df)?.toString() ?: "")
                row.add(ecl_flag?.tryGet(df)?.toString() ?: "")
                row.add(edam?.tryGet(df)?.toInt()?.toString() ?: "")
                row.add(edam?.tryGet(df)?.toKabauString() ?: "")
                row.addAll(arg.enemyRows[df].updateShipRowBody(prevHP[HP_INDEX_ENEMY][df], arg.battle.maxEnemyHp?.tryGet(df) ?: -1))
                if (arg.filter.filterDefenceCountItem(arg.battle.enemy.tryGet(df)) && arg.filter.filterOutput(row)) {
                    body.add(row)
                }
            }
        }
        else {
            for (df in 1..6) {
                if (arg.battle.enemy.size <= df - 1) {
                    continue
                }
                val row = ArrayList<String>(rowHead)
                arg.friendSummaryRows.forEach { b -> row.addAll(b) }
                row.add(erai_flag?.tryGet(df)?.toString() ?: "")
                row.add(ebak_flag?.tryGet(df)?.toString() ?: "")
                row.add(ecl_flag?.tryGet(df)?.toString() ?: "")
                row.add(edam?.tryGet(df)?.toInt()?.toString() ?: "")
                row.add(edam?.tryGet(df)?.toKabauString() ?: "")
                row.addAll(arg.enemyRows[df - 1].updateShipRowBody(prevHP[HP_INDEX_ENEMY][df - 1], arg.battle.maxEnemyHp?.tryGet(df - 1) ?: -1))
                if (arg.filter.filterDefenceCountItem(arg.battle.enemy.tryGet(df - 1)) && arg.filter.filterOutput(row)) {
                    body.add(row)
                }
            }
        }
    }
    val combined = api_kouku["api_stage3_combined"] as? LinkedTreeMap<*, *>
    combined?.run {
        val erai_flag = GsonUtil.toIntArray(this["api_erai_flag"])
        val ebak_flag = GsonUtil.toIntArray(this["api_ebak_flag"])
        val ecl_flag = GsonUtil.toIntArray(this["api_ecl_flag"])
        val edam = GsonUtil.toDoubleArray(this["api_edam"])
        if(arg.isSplitHp){
            for (df in 0..6) {
                if (arg.battle.enemyCombined.size <= df) {
                    continue
                }
                val row = ArrayList<String>(rowHead)
                arg.friendSummaryRows.forEach { b -> row.addAll(b) }
                row.add(erai_flag.tryGet(df)?.toString() ?: "")
                row.add(ebak_flag?.tryGet(df)?.toString() ?: "")
                row.add(ecl_flag?.tryGet(df)?.toString() ?: "")
                row.add(edam?.tryGet(df)?.toInt()?.toString() ?: "")
                row.add(edam?.tryGet(df)?.toKabauString() ?: "")
                row.addAll(arg.enemyCombinedRows[df].updateShipRowBody(prevHP[HP_INDEX_ENEMY_COMBINED][df], arg.battle.maxEnemyHpCombined.tryGet(df) ?: -1))
                if (arg.filter.filterDefenceCountItem(arg.battle.enemyCombined[df]) && arg.filter.filterOutput(row)) {
                    body.add(row)
                }
            }
        }
        else {
            for (df in 1..6) {
                if (arg.battle.enemyCombined.size <= df - 1) {
                    continue
                }
                val row = ArrayList<String>(rowHead)
                arg.friendSummaryRows.forEach { b -> row.addAll(b) }
                row.add(erai_flag.tryGet(df)?.toString() ?: "")
                row.add(ebak_flag?.tryGet(df)?.toString() ?: "")
                row.add(ecl_flag?.tryGet(df)?.toString() ?: "")
                row.add(edam?.tryGet(df)?.toInt()?.toString() ?: "")
                row.add(edam?.tryGet(df)?.toKabauString() ?: "")
                row.addAll(arg.enemyCombinedRows[df - 1].updateShipRowBody(prevHP[HP_INDEX_ENEMY_COMBINED][df - 1], arg.battle.maxEnemyHpCombined.tryGet(df - 1) ?: -1))
                if (arg.filter.filterDefenceCountItem(arg.battle.enemyCombined[df - 1]) && arg.filter.filterOutput(row)) {
                    body.add(row)
                }
            }
        }
    }
}

fun AkakariBaseAirRowBody(arg:ScriptArg): ArrayList<ArrayList<String>> {
    val body = ArrayList<ArrayList<String>>()
    if(arg.hasAkakariInfo) {
        arg.dayPhaseOrNull?.run {
            val phase = this
            phase.airBase?.run {
                for (i in this.indices) {
                    AkakariBaseAirRowBodyConstruct(
                            arg = arg,
                            air = this[i],
                            airIndex = i,
                            apiName = "api_air_base_attack",
                            startHP = arg.battleHP.dayPhase!!.airBaseStartHP[i],
                            body = body
                    )
                }
            }
        }
    }
    return body
}