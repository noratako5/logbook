package logbook.builtinscript

import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import logbook.dto.AirBattleDto
import logbook.internal.Item
import logbook.scripting.BuiltinScriptFilter
import logbook.util.GsonUtil
import java.util.*


fun AkakariAirRowEXHeader(): ArrayList<String> {
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
    for (i in 1..13) {
        val index = i.toString()
        AkakariShipRowHeader()
                .forEach { s -> header.add("攻撃艦$index.$s") }
    }
    header.add("雷撃")
    header.add("爆撃")
    header.add("クリティカル")
    header.add("ダメージ")
    header.add("かばう")
    AkakariShipRowHeader()
            .forEach { s -> header.add("防御艦." + s) }
    return header
}

private fun AkakariAirRowEXBodyConstruct(
        arg:ScriptArg,
        air: AirBattleDto?,
        apiName: String,
        startHP: ArrayList<IntArray>,
        body: ArrayList<ArrayList<String>>
)
{
    if(air == null){ return }
    AkakariShipRowInitHP(arg,startHP)
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
                if (arg.battle.enemy.size <= df) {
                    continue
                }
                val row = ArrayList<String>(rowHead)
                arg.friendAkakariRows.forEach { b -> row.addAll(b) }
                arg.combinedAkakariRows.forEachIndexed {i, b -> if(i<6)row.addAll(b) }
                row.add(erai_flag?.tryGet(df)?.toString() ?: "")
                row.add(ebak_flag?.tryGet(df)?.toString() ?: "")
                row.add(ecl_flag?.tryGet(df)?.toString() ?: "")
                row.add(edam?.tryGet(df)?.toInt()?.toString() ?: "")
                row.add(edam?.tryGet(df)?.toKabauString() ?: "")
                row.addAll(arg.enemyAkakariRows[df].updateAkakariShipRowBody(prevHP[HP_INDEX_ENEMY][df], arg.battle.maxEnemyHp?.tryGet(df) ?: -1))
                if (arg.filter.filterDefenceCountItem(arg.battle.enemy.tryGet(df)) && arg.filter.filterOutput(row)) {
                    body.add(row)
                }
            }
            for (df in 0..6) {
                if (arg.battle.dock.ships.size <= df) {
                    continue
                }
                val row = ArrayList<String>(rowHead)
                arg.enemyAkakariRows.forEach { b -> row.addAll(b) }
                arg.enemyCombinedAkakariRows.forEachIndexed {i, b -> if(i<6)row.addAll(b) }
                row.add(frai_flag?.tryGet(df)?.toString() ?: "")
                row.add(fbak_flag?.tryGet(df)?.toString() ?: "")
                row.add(fcl_flag?.tryGet(df)?.toString() ?: "")
                row.add(fdam?.tryGet(df)?.toInt()?.toString() ?: "")
                row.add(fdam?.tryGet(df)?.toKabauString() ?: "")
                row.addAll(arg.friendAkakariRows[df].updateAkakariShipRowBody(prevHP[HP_INDEX_FRIEND][df], arg.battle.maxFriendHp?.tryGet(df) ?: -1))
                if (arg.filter.filterDefenceCountItem(arg.battle.dock.ships.tryGet(df)) && arg.filter.filterOutput(row)) {
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
                arg.friendAkakariRows.forEach { b -> row.addAll(b) }
                arg.combinedAkakariRows.forEachIndexed {i, b -> if(i<6)row.addAll(b) }
                row.add(erai_flag?.tryGet(df)?.toString() ?: "")
                row.add(ebak_flag?.tryGet(df)?.toString() ?: "")
                row.add(ecl_flag?.tryGet(df)?.toString() ?: "")
                row.add(edam?.tryGet(df)?.toInt()?.toString() ?: "")
                row.add(edam?.tryGet(df)?.toKabauString() ?: "")
                row.addAll(arg.enemyAkakariRows[df - 1].updateAkakariShipRowBody(prevHP[HP_INDEX_ENEMY][df - 1], arg.battle.maxEnemyHp?.tryGet(df - 1) ?: -1))
                if (arg.filter.filterDefenceCountItem(arg.battle.enemy.tryGet(df - 1)) && arg.filter.filterOutput(row)) {
                    body.add(row)
                }
            }
            for (df in 1..6) {
                if (arg.battle.dock.ships.size <= df - 1) {
                    continue
                }
                val row = ArrayList<String>(rowHead)
                arg.enemyAkakariRows.forEach { b -> row.addAll(b) }
                arg.enemyCombinedAkakariRows.forEachIndexed {i, b -> if(i<6)row.addAll(b) }
                row.add(frai_flag?.tryGet(df)?.toString() ?: "")
                row.add(fbak_flag?.tryGet(df)?.toString() ?: "")
                row.add(fcl_flag?.tryGet(df)?.toString() ?: "")
                row.add(fdam?.tryGet(df)?.toInt()?.toString() ?: "")
                row.add(fdam?.tryGet(df)?.toKabauString() ?: "")
                row.addAll(arg.friendAkakariRows[df - 1].updateAkakariShipRowBody(prevHP[HP_INDEX_FRIEND][df - 1], arg.battle.maxFriendHp?.tryGet(df - 1) ?: -1))
                if (arg.filter.filterDefenceCountItem(arg.battle.dock.ships.tryGet(df - 1)) && arg.filter.filterOutput(row)) {
                    body.add(row)
                }
            }
        }
    }
    val combined = api_kouku["api_stage3_combined"] as? LinkedTreeMap<*, *>
    combined?.run {
        val frai_flag = GsonUtil.toIntArray(this["api_frai_flag"])
        val erai_flag = GsonUtil.toIntArray(this["api_erai_flag"])
        val fbak_flag = GsonUtil.toIntArray(this["api_fbak_flag"])
        val ebak_flag = GsonUtil.toIntArray(this["api_ebak_flag"])
        val fcl_flag = GsonUtil.toIntArray(this["api_fcl_flag"])
        val ecl_flag = GsonUtil.toIntArray(this["api_ecl_flag"])
        val fdam = GsonUtil.toDoubleArray(this["api_fdam"])
        val edam = GsonUtil.toDoubleArray(this["api_edam"])
        if (arg.battle.isCombined && frai_flag != null) {
            if(arg.isSplitHp){
                for (df in 0..6) {
                    if (arg.battle.dockCombined.ships.size <= df) {
                        continue
                    }
                    val row = ArrayList<String>(rowHead)
                    arg.enemyAkakariRows.forEach { b -> row.addAll(b) }
                    arg.enemyCombinedAkakariRows.forEachIndexed {i, b -> if(i<6)row.addAll(b) }
                    row.add(frai_flag.tryGet(df)?.toString() ?: "")
                    row.add(fbak_flag?.tryGet(df)?.toString() ?: "")
                    row.add(fcl_flag?.tryGet(df)?.toString() ?: "")
                    row.add(fdam?.tryGet(df)?.toInt()?.toString() ?: "")
                    row.add(fdam?.tryGet(df)?.toKabauString() ?: "")
                    row.addAll(arg.combinedAkakariRows[df].updateAkakariShipRowBody(prevHP[HP_INDEX_FRIEND_COMBINED][df], arg.battle.maxFriendHpCombined.tryGet(df) ?: -1))
                    if (arg.filter.filterDefenceCountItem(arg.battle.dockCombined.ships.get(df)) && arg.filter.filterOutput(row)) {
                        body.add(row)
                    }
                }
            }
            else {
                for (df in 1..6) {
                    if (arg.battle.dockCombined.ships.size <= df - 1) {
                        continue
                    }
                    val row = ArrayList<String>(rowHead)
                    arg.enemyAkakariRows.forEach { b -> row.addAll(b) }
                    arg.enemyCombinedAkakariRows.forEachIndexed {i, b -> if(i<6)row.addAll(b) }
                    row.add(frai_flag.tryGet(df)?.toString() ?: "")
                    row.add(fbak_flag?.tryGet(df)?.toString() ?: "")
                    row.add(fcl_flag?.tryGet(df)?.toString() ?: "")
                    row.add(fdam?.tryGet(df)?.toInt()?.toString() ?: "")
                    row.add(fdam?.tryGet(df)?.toKabauString() ?: "")
                    row.addAll(arg.combinedAkakariRows[df - 1].updateAkakariShipRowBody(prevHP[HP_INDEX_FRIEND_COMBINED][df - 1], arg.battle.maxFriendHpCombined.tryGet(df - 1) ?: -1))
                    if (arg.filter.filterDefenceCountItem(arg.battle.dockCombined.ships.get(df - 1)) && arg.filter.filterOutput(row)) {
                        body.add(row)
                    }
                }
            }
        }
        if (arg.battle.isEnemyCombined && erai_flag != null) {
            if(arg.isSplitHp){
                for (df in 0..6) {
                    if (arg.battle.enemyCombined.size <= df) {
                        continue
                    }
                    val row = ArrayList<String>(rowHead)
                    arg.friendAkakariRows.forEach { b -> row.addAll(b) }
                    arg.combinedAkakariRows.forEachIndexed {i, b -> if(i<6)row.addAll(b) }
                    row.add(erai_flag.tryGet(df)?.toString() ?: "")
                    row.add(ebak_flag?.tryGet(df)?.toString() ?: "")
                    row.add(ecl_flag?.tryGet(df)?.toString() ?: "")
                    row.add(edam?.tryGet(df)?.toInt()?.toString() ?: "")
                    row.add(edam?.tryGet(df)?.toKabauString() ?: "")
                    row.addAll(arg.enemyCombinedAkakariRows[df].updateAkakariShipRowBody(prevHP[HP_INDEX_ENEMY_COMBINED][df], arg.battle.maxEnemyHpCombined.tryGet(df) ?: -1))
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
                    arg.friendAkakariRows.forEach { b -> row.addAll(b) }
                    arg.combinedAkakariRows.forEachIndexed {i, b -> if(i<6)row.addAll(b) }
                    row.add(erai_flag.tryGet(df)?.toString() ?: "")
                    row.add(ebak_flag?.tryGet(df)?.toString() ?: "")
                    row.add(ecl_flag?.tryGet(df)?.toString() ?: "")
                    row.add(edam?.tryGet(df)?.toInt()?.toString() ?: "")
                    row.add(edam?.tryGet(df)?.toKabauString() ?: "")
                    row.addAll(arg.enemyCombinedAkakariRows[df - 1].updateAkakariShipRowBody(prevHP[HP_INDEX_ENEMY_COMBINED][df - 1], arg.battle.maxEnemyHpCombined.tryGet(df - 1) ?: -1))
                    if (arg.filter.filterDefenceCountItem(arg.battle.enemyCombined[df - 1]) && arg.filter.filterOutput(row)) {
                        body.add(row)
                    }
                }
            }
        }
    }
}
//攻撃側の装備構成などを全部出力する　列多すぎてテーブル表示は無理
fun AkakariAirRowEXBody(arg:ScriptArg): ArrayList<ArrayList<String>> {
    val body = ArrayList<ArrayList<String>>()
    if(arg.hasAkakariInfo) {
        arg.dayPhaseOrNull?.run {
            val phase = this
            AkakariAirRowEXBodyConstruct(
                    arg = arg,
                    air = phase.airInjection,
                    apiName = "api_injection_kouku",
                    startHP = arg.battleHP.dayPhase!!.injectionAirStartHP,
                    body = body
            )
            AkakariAirRowEXBodyConstruct(
                    arg = arg,
                    air = phase.air,
                    apiName = "api_kouku",
                    startHP = arg.battleHP.dayPhase!!.air1StartHP,
                    body = body
            )
            AkakariAirRowEXBodyConstruct(
                    arg = arg,
                    air = phase.air2,
                    apiName = "api_kouku2",
                    startHP = arg.battleHP.dayPhase!!.air2StartHP,
                    body = body
            )
        }
    }
    return body
}