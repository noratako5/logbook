package logbook.builtinscript

import com.google.gson.internal.LinkedTreeMap
import logbook.config.AppConfig
import logbook.scripting.BuiltinScriptFilter
import java.util.*
import java.util.stream.Collectors

fun HenseiSakutekiRowHeader(): ArrayList<String> {
    val header = PhaseRowHeader()
    header.add("索敵スコア")
    header.add("分岐点係数")
    header.add("素索敵スコア合計")
    header.add("装備スコア合計")
    header.add("司令部スコア")
    header.add("人数スコア")
    for (i in 1..7) {
        val index = i.toString()
        ShipSakutekiRowHeader().forEach{ s -> header.add("自軍$index.$s") }
    }
    return header
}

fun HenseiSakutekiRowBody(arg:ScriptArg): ArrayList<ArrayList<String>> {
    val body = ArrayList<ArrayList<String>>()
    var withoutItemScore = 0.0
    var itemScore = 0.0
    val levelScore = -Math.ceil(0.4 * arg.battle.getHqLv())
    var countScore = 0
    var indexList = listOf<Int>()
    arg.battle.dock?.ships?.run{
        for (ship in this) {
            withoutItemScore += ship.getSakutekiScoreWithoutItem()
            itemScore += ship.getItemSakutekiScore()
        }
        countScore = 2 * (6 - this.size)
        indexList = IntArray(this.size,{i->i}).sortedBy { i->this[i].shipId }
    }
    val row = PhaseRowBody(arg)
    val bunki = AppConfig.get().bunkitenKeisu
    row.add((withoutItemScore + itemScore * bunki + levelScore + countScore.toDouble()).Kirisute(10))
    row.add(bunki.Kirisute(2))
    row.add(withoutItemScore.Kirisute(10))
    row.add(itemScore.Kirisute(10))
    row.add(levelScore.Kirisute(0))
    row.add(countScore.toString())
    for (i in 0..6) {
        row.addAll(arg.friendSakutekiRows[indexList.tryGet(i)?:i])
    }
    if (arg.filter.filterOutput(row)) {
        body.add(row)
    }
    return body
}
