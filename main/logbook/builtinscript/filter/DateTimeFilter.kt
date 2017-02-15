package logbook.builtinscript.filter

import com.google.gson.internal.LinkedTreeMap
import org.apache.commons.lang3.time.FastDateFormat
import java.text.ParseException
import java.util.*

private val format = FastDateFormat.getInstance("yyyyMMddHHmmss", TimeZone.getTimeZone("JST"))
interface DateTimeFilter {
    fun filter(date: Date): Boolean
}
fun CreateDateTimeFilter(json:Any?):DateTimeFilter{
    return when(json) {
        is LinkedTreeMap<*,*> -> DateTimeAndFilter(json)
        is List<*> -> DateTimeOrFilter(json)
        null -> DateTimeTrueFilter()
        else -> DateTimeFalseFilter()
    }
}

abstract class DateTimeStartEndFilter(dateTimeCode: Any):DateTimeFilter{
    protected val date:Date?
    init{
        this.date =
            if (dateTimeCode is String && dateTimeCode.length == 14) {
                try{ format.parse(dateTimeCode) }
                catch (e:ParseException){ null }
            }
            else { null }
    }
}
class DateTimeStartFilter(dateTimeCode: Any) : DateTimeStartEndFilter(dateTimeCode){
    override fun filter(date: Date): Boolean {
        return this.date?.compareTo(date)?.run{ this <= 0 }  ?:false
    }
}
class DateTimeEndFilter(dateTimeCode: Any) : DateTimeStartEndFilter(dateTimeCode){
    override fun filter(date: Date): Boolean {
        return this.date?.compareTo(date)?.run{ this >= 0 }  ?:false
    }
}

class DateTimeAndFilter(json : Any) : DateTimeFilter {
    private val filterList: List<DateTimeFilter>
    init {
        val list = ArrayList<DateTimeFilter>()
        when(json) {
            is LinkedTreeMap<*,*>-> {
                json.get("開始")?.run { list.add(DateTimeStartFilter(this)) }
                json.get("終了")?.run { list.add(DateTimeEndFilter(this)) }
            }
        }
        this.filterList = list
    }
    override fun filter(date: Date): Boolean {
        return this.filterList.all{ it.filter(date) }
    }
}

class DateTimeOrFilter(json: Any) : DateTimeFilter {
    private val filterList: List<DateTimeFilter>
    init {
        val list = ArrayList<DateTimeFilter>()
        when(json){
            is List<*>-> json.forEach{ list.add(CreateDateTimeFilter(it)) }
        }
        this.filterList = list
    }
    override fun filter(date: Date): Boolean {
        return this.filterList.any{ it.filter(date) }
    }
}

class DateTimeTrueFilter : DateTimeFilter {
    override fun filter(date: Date): Boolean {
        return true
    }
}

class DateTimeFalseFilter : DateTimeFilter {
    override fun filter(date: Date): Boolean {
        return false
    }
}