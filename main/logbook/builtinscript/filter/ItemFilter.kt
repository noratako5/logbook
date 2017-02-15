package logbook.builtinscript.filter

import com.google.gson.internal.LinkedTreeMap
import logbook.dto.ItemDto
import logbook.dto.ItemInfoDto
import logbook.dto.ShipBaseDto
import logbook.dto.ShipDto
import logbook.internal.ItemTypeApi2
import java.util.*

private fun getInfo(ship: ShipBaseDto, index: Int): ItemInfoDto? {
    if (index == 4 && ship is ShipDto) {
        return ship.slotExItem?.info
    }
    else if (index < ship.item.size) {
        return ship.item[index]
    }
    return null
}

private fun getItem(ship: ShipDto, index: Int): ItemDto? {
    if (index == 4) {
        return ship.slotExItem
    }
    else if (index < ship.item2.size) {
        return ship.item2[index]
    }
    else {
        return null
    }
}

fun CreateItemFilter(json:Any?):ItemFilter{
    return when(json){
        is List<*>->ItemOrFilter(json)
        is LinkedTreeMap<*,*>->ItemAndFilter(json)
        null -> ItemExistFilter()
        else -> ItemFalseFilter()
    }
}
interface ItemFilter {
    fun filter(ship: ShipBaseDto, index: Int): Boolean
}

class ItemAndFilter(json: Any) : ItemFilter {
    private val filterList: List<ItemFilter>
    init {
        val list = ArrayList<ItemFilter>()
        when(json){
            is List<*>->json.forEach {list.add(CreateItemFilter(it))}
            is LinkedTreeMap<*,*>->{
                json.get("AND")?.run{list.add(ItemAndFilter(this))}
                json.get("OR")?.run{list.add(ItemOrFilter(this))}
                json.get("NOT")?.run{list.add(ItemNotFilter(this))}
                json.get("装備名")?.run{list.add(ItemNameFilter(this))}
                json.get("装備ID")?.run{list.add(ItemIdFilter(this))}
                json.get("装備カテゴリ")?.run{list.add(ItemCategoryFilter(this))}
                json.get("api_type2")?.run{list.add(ItemApiType2Filter(this))}
                json.get("熟練度")?.run{list.add(ItemAlvFilter(this))}
                json.get("改修")?.run { list.add(ItemLevelFilter(this)) }
                json.get("搭載数")?.run { list.add(ItemOnSlotFilter(this)) }
                json.get("装甲")?.run{list.add(ItemSoukouFilter(this))}
                json.get("火力")?.run{list.add(ItemKaryokuFilter(this))}
                json.get("雷装")?.run { list.add(ItemRaisouFilter(this)) }
                json.get("爆装")?.run{ list.add(ItemBakusouFilter(this))}
                json.get("対空")?.run { list.add(ItemTaikuFilter(this)) }
                json.get("対潜")?.run { list.add(ItemTaisenFilter(this)) }
                json.get("命中")?.run { list.add(ItemMeichuFilter(this)) }
                json.get("回避")?.run { list.add(ItemKaihiFilter(this)) }
                json.get("索敵")?.run { list.add(ItemSakutekiFilter(this)) }
                json.get("射程")?.run { list.add(ItemLengthFilter(this)) }
            }
            else -> list.add(CreateItemFilter(json))
        }
        this.filterList = list
    }
    override fun filter(ship: ShipBaseDto, index: Int): Boolean {
        return this.filterList.all{ f -> f.filter(ship, index) }
    }
}

class ItemOrFilter(json: Any) : ItemFilter {
    private val filterList: List<ItemFilter>
    init {
        val list = ArrayList<ItemFilter>()
        when(json){
            is List<*> -> json.forEach{list.add(CreateItemFilter(json))}
            else -> list.add(CreateItemFilter(json))
        }
        this.filterList = list
    }
    override fun filter(ship: ShipBaseDto, index: Int): Boolean {
        return this.filterList.any{ f -> !f.filter(ship, index) }
    }
}

class ItemNotFilter(json: Any) : ItemFilter {
    private val filter: ItemFilter
    init {
        this.filter = CreateItemFilter(json)
    }
    override fun filter(ship: ShipBaseDto, index: Int): Boolean {
        return this.filter.filter(ship, index).not()
    }
}

class ItemFalseFilter : ItemFilter {
    override fun filter(ship: ShipBaseDto, index: Int): Boolean {
        return false
    }
}

class ItemExistFilter : ItemFilter {
    override fun filter(ship: ShipBaseDto, index: Int): Boolean {
        return getInfo(ship, index) != null
    }
}

abstract class ItemValueFilter(json:Any):ItemFilter{
    protected val filter :ValueFilter
    init {
        this.filter = CreateValueFilter(json)
    }
}
class ItemNameFilter(json: Any) : ItemValueFilter(json) {
    override fun filter(ship: ShipBaseDto, index: Int): Boolean {
        return getInfo(ship,index)?.run{filter.filter(this.name)}?:false
    }
}
class ItemIdFilter(json: Any) :ItemValueFilter(json) {
    override fun filter(ship: ShipBaseDto, index: Int): Boolean {
        return getInfo(ship,index)?.run{filter.filter(this.id.toDouble())}?:false
    }
}
class ItemCategoryFilter(json: Any) :ItemValueFilter(json){
    override fun filter(ship: ShipBaseDto, index: Int): Boolean {
        return getInfo(ship,index)?.run{filter.filter(ItemTypeApi2.get(this.type2))}?:false
    }
}
class ItemApiType2Filter(json: Any) : ItemValueFilter(json) {
    override fun filter(ship: ShipBaseDto, index: Int): Boolean {
        return getInfo(ship,index)?.run{filter.filter(this.type2.toDouble())}?:false
    }
}
class ItemAlvFilter(json: Any) : ItemValueFilter(json) {
    override fun filter(ship: ShipBaseDto, index: Int): Boolean {
        if (ship is ShipDto) {
            return getItem(ship,index)?.run{filter.filter(this.alv.toDouble())}?:false
        }
        else {
            return getInfo(ship, index)?.run { filter.filter("") }?:false
        }
    }
}
class ItemLevelFilter(json: Any) : ItemValueFilter(json) {
    override fun filter(ship: ShipBaseDto, index: Int): Boolean {
        if (ship is ShipDto) {
            return getItem(ship,index)?.run{ filter.filter(this.level.toDouble()) }?:false
        }
        else {
            return getInfo(ship,index)?.run { filter.filter("") }?:false
        }
    }
}
class ItemOnSlotFilter(json: Any) : ItemValueFilter(json){
    override fun filter(ship: ShipBaseDto, index: Int): Boolean {
        if (getInfo(ship, index) == null) {
            return false
        }
        val onslot = ship.onSlot
        if (onslot != null && index < onslot.size) {
            return this.filter.filter(onslot[index].toDouble())
        }
        else {
            return this.filter.filter("")
        }
    }
}
class ItemSoukouFilter(json: Any) : ItemValueFilter(json) {
    override fun filter(ship: ShipBaseDto, index: Int): Boolean {
        return getInfo(ship,index)?.run{filter.filter(this.param.soukou.toDouble())}?:false
    }
}
class ItemKaryokuFilter(json: Any): ItemValueFilter(json)  {
    override fun filter(ship: ShipBaseDto, index: Int): Boolean {
        return getInfo(ship,index)?.run{filter.filter(this.param.karyoku.toDouble())}?:false
    }
}
class ItemRaisouFilter(json: Any) : ItemValueFilter(json) {
    override fun filter(ship: ShipBaseDto, index: Int): Boolean {
        return getInfo(ship,index)?.run{filter.filter(this.param.raisou.toDouble())}?:false
    }
}
class ItemBakusouFilter(json: Any) : ItemValueFilter(json) {
    override fun filter(ship: ShipBaseDto, index: Int): Boolean {
        return getInfo(ship,index)?.run{filter.filter(this.param.baku.toDouble())}?:false
    }
}
class ItemTaikuFilter(json: Any) : ItemValueFilter(json) {
    override fun filter(ship: ShipBaseDto, index: Int): Boolean {
        return getInfo(ship,index)?.run{filter.filter(this.param.taiku.toDouble())}?:false
    }
}
class ItemTaisenFilter(json: Any) : ItemValueFilter(json) {
    override fun filter(ship: ShipBaseDto, index: Int): Boolean {
        return getInfo(ship,index)?.run{filter.filter(this.param.taisen.toDouble())}?:false
    }
}
class ItemMeichuFilter(json: Any) : ItemValueFilter(json) {
    override fun filter(ship: ShipBaseDto, index: Int): Boolean {
        return getInfo(ship,index)?.run{filter.filter(this.param.houm.toDouble())}?:false
    }
}
class ItemKaihiFilter(json: Any) : ItemValueFilter(json) {
    override fun filter(ship: ShipBaseDto, index: Int): Boolean {
        return getInfo(ship,index)?.run{filter.filter(this.param.kaihi.toDouble())}?:false
    }
}
class ItemSakutekiFilter(json: Any) : ItemValueFilter(json) {
    override fun filter(ship: ShipBaseDto, index: Int): Boolean {
        return getInfo(ship,index)?.run{filter.filter(this.param.sakuteki.toDouble())}?:false
    }
}
class ItemLengthFilter(json: Any) :  ItemValueFilter(json) {
    override fun filter(ship: ShipBaseDto, index: Int): Boolean {
        return getInfo(ship,index)?.run{filter.filter(this.param.leng.toDouble())}?:false
    }
}


