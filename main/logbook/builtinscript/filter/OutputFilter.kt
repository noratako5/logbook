package logbook.builtinscript.filter

import com.google.gson.internal.LinkedTreeMap
import java.util.*

fun CreateOutputFilter(json: Any?, indexMap: Map<String, Int>): OutputFilter {
    return when(json){
        is LinkedTreeMap<*,*> -> OutputAndFilter(json,indexMap)
        is List<*> -> OutputOrFilter(json,indexMap)
        null -> OutputTrueFilter()
        else -> OutputFalseFilter()
    }
}

interface OutputFilter {
    fun filter(line: List<String>): Boolean
}

class OutputColumnFilter(private val index: Int, json: Any) : OutputFilter {
    private val filter: ValueFilter
    init {
        this.filter = CreateValueFilter(json)
    }
    override fun filter(line: List<String>): Boolean {
        return this.filter.filter(line[this.index])
    }
}
class OutputAndFilter(json: Any, indexMap: Map<String, Int>) : OutputFilter {
    private val filterList: List<OutputFilter>
    init {
        val list = ArrayList<OutputFilter>()
        when(json){
            is List<*> -> json.forEach { list.add(CreateOutputFilter(json,indexMap)) }
            is LinkedTreeMap<*,*> -> {
                for(key in json.keys){
                    json.get(key)?.run {
                        val value = this
                        when(key){
                            "AND" -> list.add(OutputAndFilter(this,indexMap))
                            "OR" -> list.add(OutputOrFilter(this,indexMap))
                            "NOT" -> list.add(OutputNotFilter(this,indexMap))
                            else -> indexMap[key]?.run { list.add(OutputColumnFilter(this,value)) }
                        }
                    }
                }
            }
            else -> list.add(CreateOutputFilter(json,indexMap))
        }
        this.filterList = list
    }
    override fun filter(line: List<String>): Boolean {
        return this.filterList.all{ f -> f.filter(line) }
    }
}
class OutputOrFilter(json: Any, indexMap: Map<String, Int>) : OutputFilter {
    private val filterList: List<OutputFilter>
    init {
        val list = ArrayList<OutputFilter>()
        when(json){
            is List<*>->json.forEach { CreateOutputFilter(it,indexMap) }
            else -> list.add(CreateOutputFilter(json,indexMap))
        }
        this.filterList = list
    }
    override fun filter(line: List<String>): Boolean {
        return this.filterList.any{ it.filter(line) }
    }
}

class OutputNotFilter(json: Any, indexMap: Map<String, Int>) : OutputFilter {
    private val filter: OutputFilter
    init {
        this.filter = CreateOutputFilter(json, indexMap)
    }
    override fun filter(line: List<String>): Boolean {
        return this.filter.filter(line).not()
    }
}

class OutputFalseFilter : OutputFilter {
    override fun filter(line: List<String>): Boolean {
        return false
    }
}

class OutputTrueFilter : OutputFilter {
    override fun filter(line: List<String>): Boolean {
        return true
    }
}