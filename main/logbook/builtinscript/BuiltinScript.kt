package logbook.builtinscript

import com.google.gson.Gson
import logbook.dto.BattleExDto
import com.google.gson.internal.LinkedTreeMap
import logbook.internal.LoggerHolder
import logbook.scripting.BuiltinScriptFilter
import java.util.*


//全出力用　砲撃戦夜戦は中身が重複するので除外
val Keys = listOf<String>("砲撃戦","夜戦","雷撃戦","航空戦","航空戦撃墜","基地航空戦","編成","編成索敵","砲撃順","赤仮砲撃戦","赤仮夜戦","赤仮雷撃戦","赤仮航空戦","赤仮基地航空戦","赤仮支援","赤仮航空支援")
private val LOG = LoggerHolder("builtinScript")

//ヘッダをパラメータ入れて細かく増減させそうなので一旦キャッシュを無効化
//そもそも大して重い処理ではないし
fun HeaderWithKey(key: String): Array<String> {
    try {
        return when (key) {
            "砲撃戦" -> HougekiRowHeader().toTypedArray()
            "夜戦" -> YasenRowHeader().toTypedArray()
            "砲撃戦夜戦" -> HougekiRowHeader().toTypedArray()
            "雷撃戦" -> RaigekiRowHeader().toTypedArray()
            "航空戦" -> AirRowHeader().toTypedArray()
            "航空戦撃墜" -> AirLostRowHeader().toTypedArray()
            "基地航空戦" ->BaseAirRowHeader().toTypedArray()
            "編成" -> HenseiRowHeader().toTypedArray()
            "編成索敵" -> HenseiSakutekiRowHeader().toTypedArray()
            "砲撃順"-> HougekiIndexRowHeader().toTypedArray()
            "赤仮砲撃戦"-> AkakariHougekiRowHeader().toTypedArray()
            "赤仮夜戦"->AkakariYasenRowHeader().toTypedArray()
            "赤仮雷撃戦"-> AkakariRaigekiRowHeader().toTypedArray()
            "赤仮航空戦"-> AkakariAirRowHeader().toTypedArray()
            "赤仮基地航空戦"-> AkakariBaseAirRowHeader().toTypedArray()
            "赤仮支援"-> AkakariSienRowHeader().toTypedArray()
            "赤仮航空支援"-> AkakariSienAirRowHeader().toTypedArray()
            //"My雷撃戦" -> MyRaigekiRowHeader().toTypedArray()
            //"My砲撃戦" -> MyHougekiRowHeader().toTypedArray()
            else-> arrayOf()
        }
    }
    catch (e: Exception) {
        return arrayOf()
    }
}
fun AllHeader():Map<String,Array<String>>{
    return Keys.map { s->Pair(s, HeaderWithKey(s)) }.toMap()
}
fun Keys():List<String>{
    return Keys
}
fun BodyWithKey(arg:ScriptArg,key:String):Array<Array<String>>{
    if(arg.battle.exVersion < 2){
        return arrayOf()
    }
    try {
        val result =
                when (key) {
                    "砲撃戦" -> HougekiRowBody(arg)
                    "夜戦" -> YasenRowBody(arg)
                    "砲撃戦夜戦" -> {
                        val body = HougekiRowBody(arg)
                        body.addAll(YasenRowBody(arg))
                        body
                    }
                    "雷撃戦" -> RaigekiRowBody(arg)
                    "航空戦" -> AirRowBody(arg)
                    "航空戦撃墜" -> AirLostRowBody(arg)
                    "基地航空戦" -> BaseAirRowBody(arg)
                    "編成" -> HenseiRowBody(arg)
                    "編成索敵" -> HenseiSakutekiRowBody(arg)
                    "砲撃順"-> HougekiIndexRowBody(arg)
                    "赤仮砲撃戦"-> AkakariHougekiRowBody(arg)
                    "赤仮夜戦"-> AkakariYasenRowBody(arg)
                    "赤仮砲撃戦夜戦" -> {
                        val body = AkakariHougekiRowBody(arg)
                        body.addAll(AkakariYasenRowBody(arg))
                        body
                    }
                    "赤仮雷撃戦"-> AkakariRaigekiRowBody(arg)
                    "赤仮航空戦"-> AkakariAirRowBody(arg)
                    "赤仮基地航空戦"-> AkakariBaseAirRowBody(arg)
                    "赤仮支援" -> AkakariSienRowBody(arg)
                    "赤仮航空支援" -> AkakariSienAirRowBody(arg)
                    //"My雷撃戦" -> MyRaigekiRowBody(arg)
                    //"My砲撃戦" -> MyHougekiRowBody(arg)
                    //"My航空戦" -> MyAirRowBody(arg)
                    else -> arrayListOf()
                }
        return result.map { x -> x.toTypedArray() }.toTypedArray()
    }catch(e:Exception){
        LOG.get().warn("出力に失敗しました", e)
        val row = Array<String>(HeaderWithKey(key).size,{i->"例外発生"})
        row[0] = arg.dateString
        val error = arrayOf(row)
        return error
    }
}

fun BodyWithKey(key:String,battle:BattleExDto,filter: BuiltinScriptFilter?=null):Array<Array<String>>{
    val arg = ScriptArg(battle, filter)
    if(arg.filter.filterDateTime(battle.battleDate)) {
        return BodyWithKey(arg, key)
    }
    else{
        return arrayOf()
    }
}
fun BodyWithFilter(battle:BattleExDto,filter:BuiltinScriptFilter):Array<Array<String>>{
    return BodyWithKey(key = filter.key, battle = battle, filter = filter)
}


fun AllBody(battle:BattleExDto):Map<String,Array<Array<String>>>{
    val arg = ScriptArg(battle = battle)
    return Keys.map { s ->
            Pair<String,Array<Array<String>>>(s, BodyWithKey(arg,s))
        }.toMap()
}