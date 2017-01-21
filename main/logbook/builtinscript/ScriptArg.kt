package logbook.builtinscript

import com.google.gson.internal.LinkedTreeMap
import logbook.config.AppConfig
import logbook.dto.BattleExDto
import logbook.gui.logic.DateTimeString
import logbook.scripting.BuiltinScriptFilter
import org.apache.commons.lang3.time.FastDateFormat
import java.util.*

/**
 * 組み込みスクリプト出力の共通引数
 * BattleCacheの艦船列HP欄がスレッドセーフでないので同一戦闘を複数スレッドで回す時はnullで消してスレッドごとに別キャッシュ使うこと
 */
data class ScriptArg(
        val battle:BattleExDto,
        var filterOrNull:BuiltinScriptFilter? = null,
        private var battleCacheOrNull:BattleCache? = null
)
{
    init{
        if(battleCacheOrNull?.run{this.battle !== battle}?:false){
            //別バトルのキャッシュをねじ込まれた場合は捨てる
            battleCacheOrNull = null
        }
    }
    val filter:BuiltinScriptFilter
        get(){
            if(filterOrNull == null){
                filterOrNull = BuiltinScriptFilter.createTrueFilter()
            }
            return filterOrNull!!
        }
    val battleCache:BattleCache
        get(){
            if(battleCacheOrNull == null){
                battleCacheOrNull = BattleCache(battle = battle)
            }
            return battleCacheOrNull!!
        }
    val combinedFlagString:String
        get() = battleCache.combinedFlagString
    val enemyRows:ArrayList<ArrayList<String>>
        get() = battleCache.enemyRows
    val friendRows:ArrayList<ArrayList<String>>
        get() = battleCache.friendRows
    val enemyCombinedRows:ArrayList<ArrayList<String>>
        get() = battleCache.enemyCombinedRows
    val combinedRows:ArrayList<ArrayList<String>>
        get() = battleCache.combinedRows
    val enemySummaryRows:ArrayList<ArrayList<String>>
        get() = battleCache.enemySummaryRows
    val friendSummaryRows:ArrayList<ArrayList<String>>
        get() = battleCache.friendSummaryRows
    val enemyCombinedSummaryRows:ArrayList<ArrayList<String>>
        get() = battleCache.enemyCombinedSummaryRows
    val combinedSummaryRows:ArrayList<ArrayList<String>>
        get() = battleCache.combinedSummaryRows
    val friendSakutekiRows:ArrayList<ArrayList<String>>
        get() = battleCache.friendSakutekiRows
    val combinedSakutekiRows:ArrayList<ArrayList<String>>
        get() = battleCache.combinedSakutekiRows
    val battleHP: BattleHP
        get() = battleCache.battleHP
    val dayPhaseOrNull:BattleExDto.Phase?
        get() = battle.phaseList.find { p->p.isNight.not() }
    val nightPhaseOrNull:BattleExDto.Phase?
        get() = battle.phaseList.find { p->p.isNight }
    val dateString:String
        get() = battleCache.dateString
}


data class BattleCache(
        val battle:BattleExDto,
        private var enemyRowsOrNull:ArrayList<ArrayList<String>>? = null,
        private var friendRowsOrNull:ArrayList<ArrayList<String>>? = null,
        private var enemyCombinedRowsOrNull:ArrayList<ArrayList<String>>? = null,
        private var combinedRowsOrNull:ArrayList<ArrayList<String>>? = null,
        private var enemySummaryRowsOrNull:ArrayList<ArrayList<String>>? = null,
        private var friendSummaryRowsOrNull:ArrayList<ArrayList<String>>? = null,
        private var enemyCombinedSummaryRowsOrNull:ArrayList<ArrayList<String>>? = null,
        private var combinedSummaryRowsOrNull:ArrayList<ArrayList<String>>? = null,
        private var friendSakutekiRowsOrNull:ArrayList<ArrayList<String>>? = null,
        private var combinedSakutekiRowsOrNull:ArrayList<ArrayList<String>>? = null,
        private var combinedFlagStringOrNull:String? = null,
        private var battleHPOrNull: BattleHP? = null,
        private var dateStringOrNull:String? = null
)
{
    val combinedFlagString:String
        get(){
            if(combinedFlagStringOrNull == null) {
                combinedFlagStringOrNull =
                        when (battle.combinedKind) {
                            0 -> "通常艦隊"
                            1 -> "機動部隊"
                            2 -> "水上部隊"
                            3 -> "輸送部隊"
                            else -> "不明"
                        }
            }
            return combinedFlagStringOrNull!!
        }
    val enemyRows:ArrayList<ArrayList<String>>
        get(){
            if(enemyRowsOrNull == null){
                val rows = ArrayList<ArrayList<String>>()
                for (i in 0..5) { rows.add(ShipRowBodyBase(battle.enemy?.tryGet(i), battle.maxEnemyHp?.tryGet(i)?:0, i)) }
                enemyRowsOrNull = rows
            }
            return enemyRowsOrNull!!
        }
    val friendRows:ArrayList<ArrayList<String>>
        get(){
            if(friendRowsOrNull == null){
                val rows = ArrayList<ArrayList<String>>()
                for (i in 0..5) { rows.add(ShipRowBodyBase(battle.dock?.ships?.tryGet(i), battle.maxFriendHp?.tryGet(i)?:0, i)) }
                friendRowsOrNull = rows
            }
            return friendRowsOrNull!!
        }
    val enemyCombinedRows:ArrayList<ArrayList<String>>
        get(){
            if(enemyCombinedRowsOrNull == null){
                val rows = ArrayList<ArrayList<String>>()
                for (i in 0..5) { rows.add(ShipRowBodyBase(battle.enemyCombined?.tryGet(i), battle.maxEnemyHpCombined?.tryGet(i)?:0, i+6)) }
                enemyCombinedRowsOrNull = rows
            }
            return enemyCombinedRowsOrNull!!
        }
    val combinedRows:ArrayList<ArrayList<String>>
        get(){
            if(combinedRowsOrNull == null){
                val rows = ArrayList<ArrayList<String>>()
                for (i in 0..5) { rows.add(ShipRowBodyBase(battle.dockCombined?.ships?.tryGet(i), battle.maxFriendHpCombined?.tryGet(i)?:0, i+6)) }
                combinedRowsOrNull = rows
            }
            return combinedRowsOrNull!!
        }
    val enemySummaryRows:ArrayList<ArrayList<String>>
        get(){
            if(enemySummaryRowsOrNull == null){
                val rows = ArrayList<ArrayList<String>>()
                for (i in 0..5) { rows.add(ShipSummaryRowBody(battle.enemy?.tryGet(i))) }
                enemySummaryRowsOrNull = rows
            }
            return enemySummaryRowsOrNull!!
        }
    val friendSummaryRows:ArrayList<ArrayList<String>>
        get(){
            if(friendSummaryRowsOrNull == null){
                val rows = ArrayList<ArrayList<String>>()
                for (i in 0..5) { rows.add(ShipSummaryRowBody(battle.dock?.ships?.tryGet(i))) }
                friendSummaryRowsOrNull = rows
            }
            return friendSummaryRowsOrNull!!
        }
    val enemyCombinedSummaryRows:ArrayList<ArrayList<String>>
        get(){
            if(enemyCombinedSummaryRowsOrNull == null){
                val rows = ArrayList<ArrayList<String>>()
                for (i in 0..5) { rows.add(ShipSummaryRowBody(battle.enemyCombined?.tryGet(i))) }
                enemyCombinedSummaryRowsOrNull = rows
            }
            return enemyCombinedSummaryRowsOrNull!!
        }
    val combinedSummaryRows:ArrayList<ArrayList<String>>
        get(){
            if(combinedSummaryRowsOrNull == null){
                val rows = ArrayList<ArrayList<String>>()
                for (i in 0..5) { rows.add(ShipSummaryRowBody(battle.dockCombined?.ships?.tryGet(i))) }
                combinedSummaryRowsOrNull = rows
            }
            return combinedSummaryRowsOrNull!!
        }
    val friendSakutekiRows:ArrayList<ArrayList<String>>
        get(){
            if(friendSakutekiRowsOrNull == null){
                val rows = ArrayList<ArrayList<String>>()
                for (i in 0..5) { rows.add(ShipSakutekiRowBody(battle.dock?.ships?.tryGet(i),i)) }
                friendSakutekiRowsOrNull = rows
            }
            return friendSakutekiRowsOrNull!!
        }
    val combinedSakutekiRows:ArrayList<ArrayList<String>>
        get(){
            if(combinedSakutekiRowsOrNull == null){
                val rows = ArrayList<ArrayList<String>>()
                for (i in 0..5) { rows.add(ShipSakutekiRowBody(battle.dockCombined?.ships?.tryGet(i),i)) }
                combinedSakutekiRowsOrNull = rows
            }
            return combinedSakutekiRowsOrNull!!
        }

    val battleHP: BattleHP
        get(){
            if(battleHPOrNull == null){
                battleHPOrNull = BattleHP(battle)
            }
            return battleHPOrNull!!
        }

    val dateString:String
        get(){
            if(dateStringOrNull == null) {
                try {
                    val format = FastDateFormat.getInstance(AppConfig.get().builtinDateFormat, TimeZone.getTimeZone("JST"))
                    //dateStringOrNull = format.format(this.battle.battleDate)
                    dateStringOrNull = DateTimeString.toString(this.battle.battleDate)
                } catch (ex: Exception) {
                    dateStringOrNull = DateTimeString.toString(this.battle.battleDate)
                }
            }
            return dateStringOrNull!!
        }
}
