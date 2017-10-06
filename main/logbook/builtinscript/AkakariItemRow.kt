package logbook.builtinscript

import com.fasterxml.jackson.databind.node.ArrayNode
import logbook.builtinscript.akakariLog.AkakariSyutsugekiLogReader
import logbook.dto.ItemDto
import logbook.dto.ItemInfoDto
import logbook.dto.ShipBaseDto
import logbook.dto.ShipDto
import logbook.util.JacksonUtil
import java.util.*

private var _itemRowHeader: ArrayList<String>? = null
fun AkakariItemRowHeader(): ArrayList<String> {
    if (_itemRowHeader == null) {
        val header = ArrayList<String>()
        for (i in 1..5) {
            header.add(String.format("装備%d.名前", i))
            header.add(String.format("装備%d.改修", i))
            header.add(String.format("装備%d.熟練度", i))
            header.add(String.format("装備%d.搭載数", i))
            header.add(String.format("装備%d.戦闘後搭載数", i))
        }
        _itemRowHeader = header
    }
    return _itemRowHeader!!
}

private fun AkakariItemRowBodyConstruct(item: ItemDto?, info: ItemInfoDto?, onSlot: Int?, onSlotAfterBattle:Int?): ArrayList<String> {
    val body = ArrayList<String>()
    body.add(info?.name?:"")
    body.add(item?.level?.toString()?:"")
    body.add(item?.alv?.toString()?:"")
    body.add(info?.run{onSlot?.toString()}?:"")
    body.add(info?.run{onSlotAfterBattle?.toString()}?:"")
    return body
}

fun AkakariItemRowBody(ship: ShipBaseDto?,date:Date?): ArrayList<String> {
    val body = ArrayList<String>()
    val shipDto = ship as? ShipDto
    val itemDtos: List<ItemDto>? = shipDto?.item2
    val itemExDto: ItemDto? = shipDto?.slotExItem
    val itemInfoDtos: List<ItemInfoDto>? = ship?.item
    val itemInfoExDto: ItemInfoDto? = itemExDto?.info
    val onSlots: IntArray? = ship?.onSlot

    val shipJson = shipDto?.let{AkakariSyutsugekiLogReader.shipAfterBattle(date,it.id)}
    val onSlotsAfterBattle =
            shipJson
                    ?.let{ it.get("api_onslot") as? ArrayNode}
                    ?.let {
                        val intArray = IntArray(it.size())
                        for(i in 0..it.size()-1){
                            intArray[i] = JacksonUtil.toInt(it.get(i))
                        }
                        intArray
                    }
    for(i in 0..3){
        body.addAll(AkakariItemRowBodyConstruct(itemDtos?.tryGet(i),itemInfoDtos?.tryGet(i),onSlots?.tryGet(i),onSlotsAfterBattle?.tryGet(i)))
    }
    body.addAll(AkakariItemRowBodyConstruct(itemExDto,itemInfoExDto,null,null))
    return body
}