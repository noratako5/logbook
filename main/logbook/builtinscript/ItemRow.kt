package logbook.builtinscript

import logbook.dto.ItemDto
import logbook.dto.ItemInfoDto
import logbook.dto.ShipBaseDto
import logbook.dto.ShipDto
import java.util.*

private var _itemRowHeader: ArrayList<String>? = null
fun ItemRowHeader(): ArrayList<String> {
    if (_itemRowHeader == null) {
        val header = ArrayList<String>()
        for (i in 1..5) {
            header.add(String.format("装備%d.名前", i))
            header.add(String.format("装備%d.改修", i))
            header.add(String.format("装備%d.熟練度", i))
            header.add(String.format("装備%d.搭載数", i))
        }
        _itemRowHeader = header
    }
    return _itemRowHeader!!
}

private fun ItemRowBodyConstruct(item: ItemDto?, info: ItemInfoDto?, onSlot: Int?): ArrayList<String> {
    val body = ArrayList<String>()
    body.add(info?.name?:"")
    body.add(item?.level?.toString()?:"")
    body.add(item?.alv?.toString()?:"")
    body.add(info?.run{onSlot?.toString()}?:"")
    return body
}

fun ItemRowBody(ship: ShipBaseDto?): ArrayList<String> {
    val body = ArrayList<String>()
    val shipDto = ship as? ShipDto
    val itemDtos: List<ItemDto>? = shipDto?.item2
    val itemExDto: ItemDto? = shipDto?.slotExItem
    val itemInfoDtos: List<ItemInfoDto>? = ship?.item
    val itemInfoExDto: ItemInfoDto? = itemExDto?.info
    val onSlots: IntArray? = ship?.onSlot
    for(i in 0..3){
        body.addAll(ItemRowBodyConstruct(itemDtos?.tryGet(i),itemInfoDtos?.tryGet(i),onSlots?.tryGet(i)))
    }
    body.addAll(ItemRowBodyConstruct(itemExDto,itemInfoExDto,null))
    return body
}