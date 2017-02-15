package logbook.builtinscript.filter

import com.google.gson.internal.LinkedTreeMap
import logbook.dto.ShipBaseDto
import java.util.*

fun CreateCountItemFilter(json:Any?):CountItemFilter{
    return when(json){
        is List<*>->CountItemOrFilter(json)
        is LinkedTreeMap<*,*>->CountItemAndFilter(json)
        null -> CountItemTrueFilter()
        else -> CountItemFalseFilter()
    }
}

interface CountItemFilter {
    fun filter(ship: ShipBaseDto): Boolean
}

class CountItemAndFilter(json: Any) : CountItemFilter {
    private val filterList: List<CountItemFilter>
    init {
        val list = ArrayList<CountItemFilter>()
        when(json){
            is List<*>->json.forEach { list.add(CreateCountItemFilter(it)) }
            is LinkedTreeMap<*,*>->{
                json.get("AND")?.run{list.add(CountItemAndFilter(this))}
                json.get("OR")?.run{list.add(CountItemOrFilter(this))}
                json.get("NOT")?.run{list.add(CountItemNotFilter(this))}
                json.get("装備数")?.run{val count = this; json.get("条件")?.run{list.add(CountItemCountFilter(count,this))}}
            }
        }
        this.filterList = list
    }
    override fun filter(ship: ShipBaseDto): Boolean {
        return this.filterList.all{ f -> f.filter(ship) }
    }
}

class CountItemOrFilter(json: Any) : CountItemFilter {
    private val filterList: List<CountItemFilter>

    init {
        val list = ArrayList<CountItemFilter>()
        when(json){
            is List<*>->json.forEach { list.add(CreateCountItemFilter(it)) }
            else -> list.add(CreateCountItemFilter(json))
        }
        this.filterList = list
    }
    override fun filter(ship: ShipBaseDto): Boolean {
        return this.filterList.any{ it.filter(ship) }
    }
}

class CountItemNotFilter(json: Any) : CountItemFilter {
    private val filter: CountItemFilter
    init {
        this.filter = CreateCountItemFilter(json)
    }
    override fun filter(ship: ShipBaseDto): Boolean {
        return !this.filter.filter(ship)
    }
}

class CountItemFalseFilter : CountItemFilter {
    override fun filter(ship: ShipBaseDto): Boolean {
        return false
    }
}

class CountItemTrueFilter : CountItemFilter {
    override fun filter(ship: ShipBaseDto): Boolean {
        return true
    }
}

class CountItemCountFilter(countJson: Any, json: Any) : CountItemFilter {
    private val countFilter: ValueFilter
    private val filter: ItemFilter

    init {
        this.countFilter = CreateValueFilter(countJson)
        this.filter = CreateItemFilter(json)
    }

    override fun filter(ship: ShipBaseDto): Boolean {
        val count = (0..4).count { this.filter.filter(ship, it) }
        return this.countFilter.filter(count.toDouble())
    }
}