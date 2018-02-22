package logbook.builtinscript

import logbook.dto.ItemDto
import logbook.dto.ItemInfoDto
import logbook.dto.ShipBaseDto
import logbook.dto.ShipDto
import java.util.*

private var _itemSakutekiRowHeader: ArrayList<String>? = null
fun ItemSakutekiRowHeader(): ArrayList<String> {
    if (_itemSakutekiRowHeader == null) {
        val header = ArrayList<String>()
        for (i in 1..6) {
            header.add(String.format("装備%d.名前", i))
            header.add(String.format("装備%d.カテゴリ", i))
            header.add(String.format("装備%d.索敵", i))
            header.add(String.format("装備%d.改修", i))
            header.add(String.format("装備%d.改修索敵加算値", i))
            header.add(String.format("装備%d.素索敵スコア", i))
            header.add(String.format("装備%d.改修索敵スコア", i))
            header.add(String.format("装備%d.合算索敵スコア", i))
        }
        _itemSakutekiRowHeader = header
    }
    return _itemSakutekiRowHeader!!
}

private fun ItemSakutekiRowBodyConstruct(item: ItemDto?, info: ItemInfoDto?, onSlot: Int?): ArrayList<String> {
    val body = ArrayList<String>()
    body.add(item?.name?:"")
    body.add(item?.typeName?:"")
    body.add(item?.param?.sakuteki?.toString()?:"")
    body.add(item?.level?.toString()?:"")
    body.add(item?.kaisyuKasan?.Kirisute(10)?:"")
    body.add(item?.sakutekiScoreWithoutKaisyu?.Kirisute(10)?:"")
    body.add(item?.kaisyuSakutekiScore?.Kirisute(10)?:"")
    body.add(item?.sakutekiScore?.Kirisute(10)?:"")
    return body
}

fun ItemSakutekiRowBody(ship: ShipBaseDto?): ArrayList<String> {
    val body = ArrayList<String>()
    val shipDto = ship as? ShipDto
    val itemDtos: List<ItemDto>? = shipDto?.item2
    val itemExDto: ItemDto? = shipDto?.slotExItem
    val itemInfoDtos: List<ItemInfoDto>? = ship?.item
    val itemInfoExDto: ItemInfoDto? = itemExDto?.info
    val onSlots: IntArray? = ship?.onSlot
    for(i in 0..4){
        body.addAll(ItemSakutekiRowBodyConstruct(itemDtos?.tryGet(i),itemInfoDtos?.tryGet(i),onSlots?.tryGet(i)))
    }
    body.addAll(ItemSakutekiRowBodyConstruct(itemExDto,itemInfoExDto,null))
    return body
}