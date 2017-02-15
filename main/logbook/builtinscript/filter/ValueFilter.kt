package logbook.builtinscript.filter

import com.google.gson.internal.LinkedTreeMap
import org.apache.commons.lang3.math.NumberUtils
import java.util.*
import java.util.regex.Pattern

private val  THRESHOLD = 0.000000000001

fun CreateValueFilter(json:Any?):ValueFilter{
    return when(json){
        is Number -> NumberSameValueFilter(json)
        is String -> StringSameValueFilter(json)
        is List<*> ->
            if(json.all{ it is Number}){ NumberSameValueFilter(json) }
            else if(json.all{ it is String}){ StringSameValueFilter(json) }
            else { ValueOrFilter(json) }
        is LinkedTreeMap<*,*> -> ValueAndFilter(json)
        else -> ValueFalseFilter()
    }
}

interface ValueFilter {
    fun filter(value: String): Boolean
    fun filter(value: Double): Boolean
}

class ValueAndFilter(json: Any) : ValueFilter {
    private val filterList: List<ValueFilter>
    init {
        val list = ArrayList<ValueFilter>()
        when(json){
            is List<*>-> json.forEach{ list.add(CreateValueFilter(it)) }
            is LinkedTreeMap<*,*>->{
                json.get("AND")?.run{ list.add(ValueAndFilter(this)) }
                json.get("OR")?.run{ list.add(ValueOrFilter(this)) }
                json.get("NOT")?.run{ list.add(ValueNotFilter(this)) }
                json.get("一致")?.run{ list.add(CreateValueFilter(this)) }
                json.get("含む")?.run{ list.add(StringContainValueFilter(this)) }
                json.get("正規表現")?.run{ list.add(StringRegexValueFilter(this)) }
                json.get("以上")?.run{ list.add(NumberAndMoreFilter(this)) }
                json.get("より大きい")?.run{ list.add(NumberMoreThanFilter(this)) }
                json.get("以下")?.run{ list.add(NumberAndLessFilter(this)) }
                json.get("より小さい")?.run{ list.add(NumberLessThanFilter(this)) }
            }
            else -> list.add(CreateValueFilter(json))
        }
        this.filterList = list
    }
    override fun filter(value: String): Boolean {
        return this.filterList.all({ it.filter(value) })
    }
    override fun filter(value: Double): Boolean {
        return this.filterList.all({ it.filter(value) })
    }
}

class ValueOrFilter(json: Any) : ValueFilter {
    private val filterList: List<ValueFilter>
    init {
        val list = ArrayList<ValueFilter>()
        when(json){
            is List<*>-> json.forEach { list.add(CreateValueFilter(it)) }
            else -> list.add(CreateValueFilter(json))
        }
        this.filterList = list
    }
    override fun filter(value: String): Boolean {
        return this.filterList.any({ it.filter(value) })
    }
    override fun filter(value: Double): Boolean {
        return this.filterList.any({ it.filter(value) })
    }
}

class ValueNotFilter(json: Any) : ValueFilter {
    private val filter: ValueFilter
    init {
        this.filter = CreateValueFilter(json)
    }
    override fun filter(value: String): Boolean {
        return this.filter.filter(value).not()
    }
    override fun filter(value: Double): Boolean {
        return this.filter.filter(value).not()
    }
}

class ValueFalseFilter : ValueFilter {
    override fun filter(value: String): Boolean {
        return false
    }
    override fun filter(value: Double): Boolean {
        return false
    }
}

abstract  class StringValueFilterBase:ValueFilter{
    override fun filter(value: Double): Boolean {
        return filter(java.lang.Double.toString(value))
    }
}
abstract class StringValueStringListFilter(json: Any) : StringValueFilterBase() {
    protected val filterList: List<String>
    init {
        val list = ArrayList<String>()
        if (json is List<*>) { json.forEach{ item -> list.add(item.toString()) } }
        else { list.add(json.toString()) }
        this.filterList = list
    }
}
class StringSameValueFilter(json: Any) : StringValueStringListFilter(json) {
    override fun filter(value: String): Boolean {
        return filterList.any{ it == value}
    }
}
class StringContainValueFilter(json: Any) : StringValueStringListFilter(json) {
    override fun filter(value: String): Boolean {
        return filterList.any{ value.contains(it) }
    }
}
class StringRegexValueFilter(json: Any) : StringValueFilterBase()  {
    private val filterList: List<Pattern>
    init {
        val list = ArrayList<Pattern>()
        if (json is List<*>) { json.forEach { item -> list.add(Pattern.compile(item.toString())) } }
        else { list.add(Pattern.compile(json.toString())) }
        this.filterList = list
    }
    override fun filter(value: String): Boolean {
        return filterList.any{ it.matcher(value).matches() }
    }
}

abstract class NumberValueFilterBase : ValueFilter {
    override fun filter(value: String): Boolean {
        if (value.isEmpty()) { return filter(0.0) }
        else if (NumberUtils.isParsable(value).not()) { return false }
        else { return filter(java.lang.Double.parseDouble(value)) }
    }
}

class NumberSameValueFilter(json: Any) : NumberValueFilterBase() {
    private val filterList: List<Number>
    init {
        val list = ArrayList<Number>()
        if (json is List<*>) { json.forEach { (it as? Number)?.run{list.add(this)} } }
        else { (json as? Number)?.run{list.add(this)} }
        this.filterList = list
    }
    override fun filter(value: Double): Boolean {
        return filterList.any{ Math.abs(it.toDouble() - value) < THRESHOLD}
    }
}

abstract class NumberOneNumberFilter(json:Any) : NumberValueFilterBase() {
    protected val filterValue: Number?
    init {
        filterValue = json as? Number
    }
}
class NumberMoreThanFilter(json: Any) : NumberOneNumberFilter(json) {
    override fun filter(value: Double): Boolean {
        return filterValue?.toDouble()?.run{ value > this + THRESHOLD  } ?:false
    }
}
class NumberAndMoreFilter(json: Any) : NumberOneNumberFilter(json) {
    override fun filter(value: Double): Boolean {
        return filterValue?.toDouble()?.run{ value > this - THRESHOLD  } ?:false
    }
}
class NumberLessThanFilter(json: Any)  : NumberOneNumberFilter(json){
    override fun filter(value: Double): Boolean {
        return filterValue?.toDouble()?.run{ value < this - THRESHOLD  } ?:false
    }
}
class NumberAndLessFilter(json: Any) : NumberOneNumberFilter(json){
    override fun filter(value: Double): Boolean {
        return filterValue?.toDouble()?.run{ value < this + THRESHOLD  } ?:false
    }
}

