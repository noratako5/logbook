package logbook.builtinscript

import logbook.dto.AirBattleDto
import logbook.dto.BattleAtackDto
import logbook.dto.ItemInfoDto
import logbook.internal.Item
import logbook.util.GsonUtil
import java.util.*

/**
 * リスト内のindex指定時は中身、リスト外のindex指定時はnull返す
 */
fun <T> List<T>.tryGet(index:Int) =
    if(0<=index && index<this.size)
        this.get(index)
    else
        null

/**
 * 配列外のindex指定時はnull返す
 */
fun IntArray.tryGet(index:Int) =
    if (0<=index && index<this.size)
        this[index]
    else
        null
/**
 * 配列外のindex指定時はnull返す
 */
fun DoubleArray.tryGet(index:Int) =
    if (0<=index && index<this.size)
        this[index]
    else
        null
/**
 * 配列外のindex指定時はnull返す
 */
fun <T> Array<T>.tryGet(index:Int) =
    if (0<=index && index<this.size)
        this[index]
    else
        null


fun ArrayList<IntArray>.deepClone():ArrayList<IntArray>{
    val result = ArrayList<IntArray>(this)
    for(i in this.indices){
        result[i] = this[i].clone()
    }
    return result
}


/**
 * 小数点指定桁数より下を切り捨てた文字列表現を返す
 * double型の限界が15桁前後なのでそれより十分小さい桁数指定すること
 */
fun Double.Kirisute(keta:Int):String {
    val small = 0.00000000000001
    val geta = Math.pow(10.0, keta.toDouble())
    val formatString = "%."+keta.toString()+"f"
    return String.format(formatString,(Math.floor((this+small)*geta)+0.1)/geta)
}


fun Int.toItemInfo(): ItemInfoDto? = Item.get(this)

/**
 * ダメージ末尾に0.1が付いてるやつかどうか判定
 */
fun Double.isKabau():Boolean = this - Math.floor(this) > 0.05
/**
 * かばってたら"1"、そうでなかったら"0"
 */
fun Double.toKabauString():String = if(this.isKabau()) "1" else "0"