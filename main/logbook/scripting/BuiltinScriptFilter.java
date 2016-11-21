package logbook.scripting;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import com.google.gson.internal.LinkedTreeMap;

import logbook.dto.BattleExDto;
import logbook.dto.ItemDto;
import logbook.dto.ItemInfoDto;
import logbook.dto.ShipBaseDto;
import logbook.dto.ShipDto;
import logbook.internal.ItemTypeApi2;

public class BuiltinScriptFilter {
    private static final double THRESHOLD = 0.0001;
    public final String key;
    private final DateTimeFilter dateTimeFilter;
    private final CountItemFilter attackCountItemFilter;
    private final CountItemFilter defenceCountItemFilter;
    private final OutputFilter outputFilter;


    private interface DateTimeFilter{
        public boolean filter(Date date);
        public static DateTimeFilter create(Object object){
            if(object instanceof LinkedTreeMap){
                return new DateTimeAndFilter(object);
            }else if(object instanceof List){
                return new DateTimeOrFilter(object);
            }else if(object == null){
                return new DateTimeTrueFilter();
            }else{
                return new DateTimeFalseFilter();
            }
        }
    }
    private static class DateTimeStartFilter implements DateTimeFilter{
        private static final FastDateFormat format = FastDateFormat.getInstance("yyyyMMddHHmmss",TimeZone.getTimeZone("JST"));
        private final Date start;
        public DateTimeStartFilter(Object dateTimeCode){
            if(dateTimeCode instanceof String){
                String startString = (String)dateTimeCode;
                Date date = null;
                try {
                    date = (startString.length() == 14)?format.parse(startString) :null;
                } catch (ParseException e) {
                    date = null;
                }
                this.start = date;
            }else{
                this.start = null;
            }
        }
        public boolean filter(Date date){
            if(this.start == null){
                return false;
            }else{
                int result = this.start.compareTo(date);
                return result <= 0;
            }
        }
    }
    private static class DateTimeEndFilter implements DateTimeFilter{
        private static final FastDateFormat format = FastDateFormat.getInstance("yyyyMMddHHmmss",TimeZone.getTimeZone("JST"));
        private final Date end;
        public DateTimeEndFilter(Object dateTimeCode){
            if(dateTimeCode instanceof String){
                String endString = (String)dateTimeCode;
                Date date = null;
                try {
                    date = (endString.length() == 14)?format.parse(endString) :null;
                } catch (ParseException e) {
                    date = null;
                }
                this.end = date;
            }else{
                this.end = null;
            }
        }
        public boolean filter(Date date){
            if(this.end == null){
                return false;
            }else{
                int result = this.end.compareTo(date);
                return result >= 0;
            }
        }
    }
    private static class DateTimeAndFilter implements DateTimeFilter{
        private final List<DateTimeFilter> filterList;
        public DateTimeAndFilter(Object object){
            List<DateTimeFilter> list = new ArrayList<>();
            if(object instanceof LinkedTreeMap){
                LinkedTreeMap map = (LinkedTreeMap)object;
                if(map.containsKey("開始")){list.add(new DateTimeStartFilter(map.get("開始")));}
                if(map.containsKey("終了")){list.add(new DateTimeEndFilter(map.get("終了")));}
            }
            this.filterList = list;
        }
        public boolean filter(Date date){
            return this.filterList.stream().allMatch(f->f.filter(date));
        }
    }
    private static class DateTimeOrFilter implements DateTimeFilter{
        private final List<DateTimeFilter> filterList;
        public DateTimeOrFilter(Object object){
            List<DateTimeFilter>list = new ArrayList<>();
            if(object instanceof List){
                for(Object item:(List)object){
                    list.add(DateTimeFilter.create(item));
                }
            }
            this.filterList = list;
        }
        public boolean filter(Date date){
            return !this.filterList.stream().allMatch(f->!f.filter(date));
        }
    }
    private static class DateTimeTrueFilter implements DateTimeFilter{
        public boolean filter(Date date){
            return true;
        }
    }
    private static class DateTimeFalseFilter implements DateTimeFilter{
        public boolean filter(Date date){
            return false;
        }
    }

    private interface OutputFilter{
        public boolean filter(List<String> line);
        public static OutputFilter create(Object object,Map<String,Integer> indexMap){
            if(object instanceof LinkedTreeMap){
                return new OutputAndFilter(object, indexMap);
            }else if(object instanceof List){
                return new OutputOrFilter(object, indexMap);
            }else if(object == null){
                return new OutputTrueFilter();
            }else{
                return new OutputFalseFilter();
            }
        }
    }
    private static class OutputColumnFilter implements OutputFilter{
        private final int index;
        private final ValueFilter filter;
        public OutputColumnFilter(int index,Object object){
            this.index = index;
            this.filter = ValueFilter.create(object);
        }
        public boolean filter(List<String> line){
            return this.filter.filter(line.get(this.index));
        }
    }
    private static class OutputAndFilter implements OutputFilter{
        private final List<OutputFilter> filterList;
        public OutputAndFilter(Object object,Map<String,Integer> indexMap){
            List<OutputFilter> list = new ArrayList<>();
            if(object instanceof List){
                for(Object item:(List)object){
                    list.add(OutputFilter.create(item, indexMap));
                }
            }else if(object instanceof LinkedTreeMap){
                LinkedTreeMap map = (LinkedTreeMap)object;
                for(Object keyObject:map.keySet()){
                    String key = keyObject.toString();
                    if(key.equals("AND")){
                        list.add(new OutputAndFilter(map.get(key),indexMap));
                    }else if(key.equals("OR")){
                        list.add(new OutputOrFilter(map.get(key),indexMap));
                    }else if(key.equals("NOT")){
                        list.add(new OutputNotFilter(map.get(key),indexMap));
                    }else{
                        Integer index = indexMap.get(key);
                        if(index != null){
                            list.add(new OutputColumnFilter(index.intValue(),map.get(key)));
                        }
                    }
                }
            }else{
                list.add(OutputFilter.create(object, indexMap));
            }
            this.filterList = list;
        }
        public boolean filter(List<String> line){
            return this.filterList.stream().allMatch(f->f.filter(line));
        }
    }
    private static class OutputOrFilter implements OutputFilter{
        private final List<OutputFilter> filterList;
        public OutputOrFilter(Object object,Map<String,Integer> indexMap){
            List<OutputFilter> list = new ArrayList<>();
            if(object instanceof List){
                for(Object item:(List)object){
                    list.add(OutputFilter.create(item, indexMap));
                }
            }else{
                list.add(OutputFilter.create(object, indexMap));
            }
            this.filterList = list;
        }
        public boolean filter(List<String> line){
            return !this.filterList.stream().allMatch(f->!f.filter(line));
        }
    }
    private static class OutputNotFilter implements OutputFilter{
        private final OutputFilter filter;
        public OutputNotFilter(Object object,Map<String,Integer> indexMap){
            this.filter = OutputFilter.create(object, indexMap);
        }
        public boolean filter(List<String> line){
            return !this.filter.filter(line);
        }
    }
    private static class OutputFalseFilter implements OutputFilter{
        public boolean filter(List<String> line){
            return false;
        }
    }
    private static class OutputTrueFilter implements OutputFilter{
        public boolean filter(List<String> line){
            return true;
        }
    }

    private interface CountItemFilter{
        public boolean filter(ShipBaseDto ship);
        public static CountItemFilter create(Object object){
            if(object instanceof List){
                return new CountItemOrFilter(object);
            }else if(object instanceof LinkedTreeMap){
                return new CountItemAndFilter(object);
            }else if(object == null){
                return new CountItemTrueFilter();
            }else{
                return new CountItemFalseFilter();
            }
        }
    }
    private static class CountItemAndFilter implements CountItemFilter{
        private final List<CountItemFilter> filterList;
        public CountItemAndFilter(Object object){
            List<CountItemFilter>list = new ArrayList<>();
            if(object instanceof List){
                for(Object item:(List)object){
                    list.add(CountItemFilter.create(item));
                }
            }else if(object instanceof LinkedTreeMap){
                LinkedTreeMap map = (LinkedTreeMap)object;
                if(map.containsKey("AND")){list.add(new CountItemAndFilter(map.get("AND")));}
                if(map.containsKey("OR")){list.add(new CountItemOrFilter(map.get("OR")));}
                if(map.containsKey("NOT")){list.add(new CountItemNotFilter(map.get("NOT")));}
                if(map.containsKey("装備数")){list.add(new CountItemCountFilter(map.get("装備数"),map.get("条件")));}
            }
            this.filterList = list;
        }
        public boolean filter(ShipBaseDto ship){
            return this.filterList.stream().allMatch(f->f.filter(ship));
        }
    }
    private static class CountItemOrFilter implements CountItemFilter{
        private final List<CountItemFilter> filterList;
        public CountItemOrFilter(Object object){
            List<CountItemFilter>list = new ArrayList<>();
            if(object instanceof List){
                for(Object item:(List)object){
                    list.add(CountItemFilter.create(item));
                }
            }else{
                list.add(CountItemFilter.create(object));
            }
            this.filterList = list;
        }
        public boolean filter(ShipBaseDto ship){
            return !this.filterList.stream().allMatch(f->!f.filter(ship));
        }
    }
    private static class CountItemNotFilter implements CountItemFilter{
        private final CountItemFilter filter;
        public CountItemNotFilter(Object object){
            this.filter = CountItemFilter.create(object);
        }
        public boolean filter(ShipBaseDto ship){
            return !this.filter.filter(ship);
        }
    }
    private static class CountItemFalseFilter implements CountItemFilter{
        public boolean filter(ShipBaseDto ship){
            return false;
        }
    }
    private static class CountItemTrueFilter implements CountItemFilter{
        public boolean filter(ShipBaseDto ship){
            return true;
        }
    }
    private static class CountItemCountFilter implements CountItemFilter{
        private final ValueFilter countFilter;
        private final ItemFilter filter;
        public CountItemCountFilter(Object countObject,Object object){
            this.countFilter = ValueFilter.create(countObject);
            this.filter = ItemFilter.create(object);
        }
        public boolean filter(ShipBaseDto ship){
            int count = 0;
            for(int i=0;i<5;i++){
                if(this.filter.filter(ship, i)){
                    count++;
                }
            }
            return this.countFilter.filter(count);
        }
    }

    private interface ItemFilter{
        public boolean filter(ShipBaseDto ship,int index);
        public static ItemFilter create(Object object){
            if(object instanceof List){
                return new ItemOrFilter(object);
            }else if(object instanceof LinkedTreeMap){
                return new ItemAndFilter(object);
            }else if(object == null){
                return new ItemExistFilter();
            }else{
                return new ItemFalseFilter();
            }
        }
        public static ItemInfoDto getInfo(ShipBaseDto ship,int index){
            if(index == 4 && ship instanceof ShipDto){
                ShipDto s = (ShipDto)ship;
                ItemDto ex = s.getSlotExItem();
                if(ex != null){
                    return ex.getInfo();
                }
            }else if(index < ship.getItem().size()){
                return ship.getItem().get(index);
            }
            return null;
        }
        public static ItemDto getItem(ShipDto ship,int index){
            if(index == 4){
                return ship.getSlotExItem();
            }else if(index < ship.getItem2().size()){
                return ship.getItem2().get(index);
            }else{
                return null;
            }
        }
    }
    private static class ItemAndFilter implements ItemFilter{
        private final List<ItemFilter> filterList;
        public ItemAndFilter(Object object){
            List<ItemFilter>list = new ArrayList<>();
            if(object instanceof List){
                for(Object item:(List)object){
                    list.add(ItemFilter.create(item));
                }
            }else if(object instanceof LinkedTreeMap){
                LinkedTreeMap map = (LinkedTreeMap)object;
                if(map.containsKey("AND")){list.add(new ItemAndFilter(map.get("AND")));}
                if(map.containsKey("OR")){list.add(new ItemOrFilter(map.get("OR")));}
                if(map.containsKey("NOT")){list.add(new ItemNotFilter(map.get("NOT")));}
                if(map.containsKey("装備名")){list.add(new ItemNameFilter(map.get("装備名")));}
                if(map.containsKey("装備ID")){list.add(new ItemIdFilter(map.get("装備ID")));}
                if(map.containsKey("装備カテゴリ")){list.add(new ItemCategoryFilter(map.get("装備カテゴリ")));}
                if(map.containsKey("api_type2")){list.add(new ItemApiType2Filter(map.get("api_type2")));}
                if(map.containsKey("熟練度")){list.add(new ItemAlvFilter(map.get("熟練度")));}
                if(map.containsKey("改修")){list.add(new ItemLevelFilter(map.get("改修")));}
                if(map.containsKey("搭載数")){list.add(new ItemOnSlotFilter(map.get("搭載数")));}
                if(map.containsKey("装甲")){list.add(new ItemSoukouFilter(map.get("装甲")));}
                if(map.containsKey("火力")){list.add(new ItemKaryokuFilter(map.get("火力")));}
                if(map.containsKey("雷装")){list.add(new ItemRaisouFilter(map.get("雷装")));}
                if(map.containsKey("爆装")){list.add(new ItemBakusouFilter(map.get("爆装")));}
                if(map.containsKey("対空")){list.add(new ItemTaikuFilter(map.get("対空")));}
                if(map.containsKey("対潜")){list.add(new ItemTaisenFilter(map.get("対潜")));}
                if(map.containsKey("命中")){list.add(new ItemMeichuFilter(map.get("命中")));}
                if(map.containsKey("回避")){list.add(new ItemKaihiFilter(map.get("回避")));}
                if(map.containsKey("索敵")){list.add(new ItemSakutekiFilter(map.get("索敵")));}
                if(map.containsKey("射程")){list.add(new ItemLengthFilter(map.get("射程")));}
            }else{
                list.add(ItemFilter.create(object));
            }
            this.filterList = list;
        }
        public boolean filter(ShipBaseDto ship,int index){
            return this.filterList.stream().allMatch(f->f.filter(ship,index));
        }
    }
    private static class ItemOrFilter implements ItemFilter{
        private final List<ItemFilter> filterList;
        public ItemOrFilter(Object object){
            List<ItemFilter>list = new ArrayList<>();
            if(object instanceof List){
                for(Object item:(List)object){
                    list.add(ItemFilter.create(item));
                }
            }else{
                list.add(ItemFilter.create(object));
            }
            this.filterList = list;
        }
        public boolean filter(ShipBaseDto ship,int index){
            return !this.filterList.stream().allMatch(f->!f.filter(ship,index));
        }
    }
    private static class ItemNotFilter implements ItemFilter{
        private final ItemFilter filter;
        public ItemNotFilter(Object object){
            this.filter = ItemFilter.create(object);
        }
        public boolean filter(ShipBaseDto ship,int index){
            return !this.filter.filter(ship, index);
        }
    }
    private static class ItemFalseFilter implements ItemFilter{
        public boolean filter(ShipBaseDto ship,int index){
            return false;
        }
    }

    private static class ItemExistFilter implements ItemFilter{
        public boolean filter(ShipBaseDto ship,int index){
            if(ItemFilter.getInfo(ship, index) != null){
                return true;
            }else{
                return false;
            }
        }
    }
    private static class ItemNameFilter implements ItemFilter{
        private final ValueFilter filter;
        public ItemNameFilter(Object object){
            this.filter = ValueFilter.create(object);
        }
        public boolean filter(ShipBaseDto ship,int index){
            ItemInfoDto info = ItemFilter.getInfo(ship, index);
            if(info != null){
                return this.filter.filter(info.getName());
            }else{
                return false;
            }
        }
    }
    private static class ItemIdFilter implements ItemFilter{
        private final ValueFilter filter;
        public ItemIdFilter(Object object){
            this.filter = ValueFilter.create(object);
        }
        public boolean filter(ShipBaseDto ship,int index){
            ItemInfoDto info = ItemFilter.getInfo(ship, index);
            if(info != null){
                return this.filter.filter(info.getId());
            }else{
                return false;
            }
        }
    }
    private static class ItemCategoryFilter implements ItemFilter{
        private final ValueFilter filter;
        public ItemCategoryFilter(Object object){
            this.filter = ValueFilter.create(object);
        }
        public boolean filter(ShipBaseDto ship,int index){
            ItemInfoDto info = ItemFilter.getInfo(ship, index);
            if(info != null){
                return this.filter.filter(ItemTypeApi2.get(info.getType2()));
            }else{
                return false;
            }
        }
    }
    private static class ItemApiType2Filter implements ItemFilter{
        private final ValueFilter filter;
        public ItemApiType2Filter(Object object){
            this.filter = ValueFilter.create(object);
        }
        public boolean filter(ShipBaseDto ship,int index){
            ItemInfoDto info = ItemFilter.getInfo(ship, index);
            if(info != null){
                return this.filter.filter(info.getType2());
            }else{
                return false;
            }
        }
    }
    private static class ItemAlvFilter implements ItemFilter{
        private final ValueFilter filter;
        public ItemAlvFilter(Object object){
            this.filter = ValueFilter.create(object);
        }
        public boolean filter(ShipBaseDto ship,int index){
            if(ship instanceof ShipDto){
                ItemDto item = ItemFilter.getItem((ShipDto)ship,index);
                if(item != null){
                    return this.filter.filter(item.getAlv());
                }else{
                    return false;
                }
            }else{
                if(ItemFilter.getInfo(ship, index) != null){
                    return this.filter.filter("");
                }else{
                    return false;
                }
            }
        }
    }
    private static class ItemLevelFilter implements ItemFilter{
        private final ValueFilter filter;
        public ItemLevelFilter(Object object){
            this.filter = ValueFilter.create(object);
        }
        public boolean filter(ShipBaseDto ship,int index){
            if(ship instanceof ShipDto){
                ItemDto item = ItemFilter.getItem((ShipDto)ship,index);
                if(item != null){
                    return this.filter.filter(item.getLevel());
                }else{
                    return false;
                }
            }else{
                if(ItemFilter.getInfo(ship, index) != null){
                    return this.filter.filter("");
                }else{
                    return false;
                }
            }
        }
    }
    private static class ItemOnSlotFilter implements ItemFilter{
        private final ValueFilter filter;
        public ItemOnSlotFilter(Object object){
            this.filter = ValueFilter.create(object);
        }
        public boolean filter(ShipBaseDto ship,int index){
            if(ItemFilter.getInfo(ship, index) == null){
                return false;
            }
            int[] onslot = ship.getOnSlot();
            if(onslot != null && index < onslot.length){
                return this.filter.filter(onslot[index]);
            }else{
                return this.filter.filter("");
            }
        }
    }
    private static class ItemSoukouFilter implements ItemFilter{
        private final ValueFilter filter;
        public ItemSoukouFilter(Object object){
            this.filter = ValueFilter.create(object);
        }
        public boolean filter(ShipBaseDto ship,int index){
            ItemInfoDto info = ItemFilter.getInfo(ship, index);
            if(info != null){
                return this.filter.filter(info.getParam().getSoukou());
            }else{
                return false;
            }
        }
    }
    private static class ItemKaryokuFilter implements ItemFilter{
        private final ValueFilter filter;
        public ItemKaryokuFilter(Object object){
            this.filter = ValueFilter.create(object);
        }
        public boolean filter(ShipBaseDto ship,int index){
            ItemInfoDto info = ItemFilter.getInfo(ship, index);
            if(info != null){
                return this.filter.filter(info.getParam().getKaryoku());
            }else{
                return false;
            }
        }
    }
    private static class ItemRaisouFilter implements ItemFilter{
        private final ValueFilter filter;
        public ItemRaisouFilter(Object object){
            this.filter = ValueFilter.create(object);
        }
        public boolean filter(ShipBaseDto ship,int index){
            ItemInfoDto info = ItemFilter.getInfo(ship, index);
            if(info != null){
                return this.filter.filter(info.getParam().getRaisou());
            }else{
                return false;
            }
        }
    }
    private static class ItemBakusouFilter implements ItemFilter{
        private final ValueFilter filter;
        public ItemBakusouFilter(Object object){
            this.filter = ValueFilter.create(object);
        }
        public boolean filter(ShipBaseDto ship,int index){
            ItemInfoDto info = ItemFilter.getInfo(ship, index);
            if(info != null){
                return this.filter.filter(info.getParam().getBaku());
            }else{
                return false;
            }
        }
    }
    private static class ItemTaikuFilter implements ItemFilter{
        private final ValueFilter filter;
        public ItemTaikuFilter(Object object){
            this.filter = ValueFilter.create(object);
        }
        public boolean filter(ShipBaseDto ship,int index){
            ItemInfoDto info = ItemFilter.getInfo(ship, index);
            if(info != null){
                return this.filter.filter(info.getParam().getTaiku());
            }else{
                return false;
            }
        }
    }
    private static class ItemTaisenFilter implements ItemFilter{
        private final ValueFilter filter;
        public ItemTaisenFilter(Object object){
            this.filter = ValueFilter.create(object);
        }
        public boolean filter(ShipBaseDto ship,int index){
            ItemInfoDto info = ItemFilter.getInfo(ship, index);
            if(info != null){
                return this.filter.filter(info.getParam().getTaisen());
            }else{
                return false;
            }
        }
    }
    private static class ItemMeichuFilter implements ItemFilter{
        private final ValueFilter filter;
        public ItemMeichuFilter(Object object){
            this.filter = ValueFilter.create(object);
        }
        public boolean filter(ShipBaseDto ship,int index){
            ItemInfoDto info = ItemFilter.getInfo(ship, index);
            if(info != null){
                return this.filter.filter(info.getParam().getHoum());
            }else{
                return false;
            }
        }
    }
    private static class ItemKaihiFilter implements ItemFilter{
        private final ValueFilter filter;
        public ItemKaihiFilter(Object object){
            this.filter = ValueFilter.create(object);
        }
        public boolean filter(ShipBaseDto ship,int index){
            ItemInfoDto info = ItemFilter.getInfo(ship, index);
            if(info != null){
                return this.filter.filter(info.getParam().getKaihi());
            }else{
                return false;
            }
        }
    }
    private static class ItemSakutekiFilter implements ItemFilter{
        private final ValueFilter filter;
        public ItemSakutekiFilter(Object object){
            this.filter = ValueFilter.create(object);
        }
        public boolean filter(ShipBaseDto ship,int index){
            ItemInfoDto info = ItemFilter.getInfo(ship, index);
            if(info != null){
                return this.filter.filter(info.getParam().getSakuteki());
            }else{
                return false;
            }
        }
    }
    private static class ItemLengthFilter implements ItemFilter{
        private final ValueFilter filter;
        public ItemLengthFilter(Object object){
            this.filter = ValueFilter.create(object);
        }
        public boolean filter(ShipBaseDto ship,int index){
            ItemInfoDto info = ItemFilter.getInfo(ship, index);
            if(info != null){
                return this.filter.filter(info.getParam().getLeng());
            }else{
                return false;
            }
        }
    }



    private interface ValueFilter{
        public boolean filter(String value);
        public boolean filter(double value);
        public static ValueFilter create(Object object){
            if(object instanceof Number){
                return new NumberSameValueFilter(object);
            }else if(object instanceof String){
                return new StringSameValueFilter(object);
            }else if(object instanceof List){
                List list = (List)object;
                if(list.stream().allMatch(x->x instanceof Number)){
                    return new NumberSameValueFilter(list);
                }else if(list.stream().allMatch(x->x instanceof String)){
                    return new StringSameValueFilter(list);
                }else{
                    return new ValueOrFilter(list);
                }
            }else if(object instanceof LinkedTreeMap){
                return new ValueAndFilter(object);
            }else{
                //多分null混入
                return new ValueFalseFilter();
            }
        }
    }
    private static class ValueAndFilter implements ValueFilter{
        private final List<ValueFilter> filterList;
        public ValueAndFilter(Object object){
            List<ValueFilter>list = new ArrayList<>();
            if(object instanceof List){
                for(Object item:(List)object){
                    list.add(ValueFilter.create(item));
                }
            }else if(object instanceof LinkedTreeMap){
                LinkedTreeMap map = (LinkedTreeMap)object;
                if(map.containsKey("AND")){list.add(new ValueAndFilter(map.get("AND")));}
                if(map.containsKey("OR")){list.add(new ValueOrFilter(map.get("OR")));}
                if(map.containsKey("NOT")){list.add(new ValueNotFilter(map.get("NOT")));}
                if(map.containsKey("一致")){list.add(ValueFilter.create(map.get("一致")));}
                if(map.containsKey("含む")){list.add(new StringContainValueFilter(map.get("含む")));}
                if(map.containsKey("正規表現")){list.add(new StringRegexValueFilter(map.get("正規表現")));}
                if(map.containsKey("以上")){list.add(new NumberAndMoreFilter(map.get("以上")));}
                if(map.containsKey("より大きい")){list.add(new NumberMoreThanFilter(map.get("より大きい")));}
                if(map.containsKey("以下")){list.add(new NumberAndLessFilter(map.get("以下")));}
                if(map.containsKey("より小さい")){list.add(new NumberLessThanFilter(map.get("より小さい")));}
            }else{
                list.add(ValueFilter.create(object));
            }
            this.filterList = list;
        }
        public boolean filter(String value){
            return this.filterList.stream().allMatch(f->f.filter(value));
        }
        public boolean filter(double value){
            return this.filterList.stream().allMatch(f->f.filter(value));
        }
    }
    private static class ValueOrFilter implements ValueFilter{
        private final List<ValueFilter> filterList;
        public ValueOrFilter(Object object){
            List<ValueFilter>list = new ArrayList<>();
            if(object instanceof List){
                for(Object item:(List)object){
                    list.add(ValueFilter.create(item));
                }
            }else{
                list.add(ValueFilter.create(object));
            }
            this.filterList = list;
        }
        public boolean filter(String value){
            return !this.filterList.stream().allMatch(f->!f.filter(value));
        }
        public boolean filter(double value){
            return !this.filterList.stream().allMatch(f->!f.filter(value));
        }
    }
    private static class ValueNotFilter implements ValueFilter{
        private final ValueFilter filter;
        public ValueNotFilter(Object object){
            this.filter = ValueFilter.create(object);
        }
        public boolean filter(String value){
            return !this.filter.filter(value);
        }
        public boolean filter(double value){
            return !this.filter.filter(value);
        }
    }
    private static class ValueFalseFilter implements ValueFilter{
        public boolean filter(String value){
            return false;
        }
        public boolean filter(double value){
            return false;
        }
    }

    private static abstract class StringValueFilterBase implements ValueFilter{
        public boolean filter(double value){
            return filter(Double.toString(value));
        }
    }
    private static class StringSameValueFilter extends StringValueFilterBase{
        private final List<String> filterList;
        public StringSameValueFilter(Object value){
            List<String> list = new ArrayList<>();
            if(value instanceof List){
                for(Object object:(List)value){
                    list.add(object.toString());
                }
            }else{
                list.add(value.toString());
            }
            this.filterList = list;
        }
        public boolean filter(String value){
            if(value == null){
                value = "";
            }
            for(String filter:filterList){
                if(filter.equals(value)){
                    return true;
                }
            }
            return false;
        }
    }
    private static class StringContainValueFilter extends StringValueFilterBase{
        private final List<String> filterList;
        public StringContainValueFilter(Object value){
            List<String> list = new ArrayList<>();
            if(value instanceof List){
                for(Object object:(List)value){
                    list.add(object.toString());
                }
            }else{
                list.add(value.toString());
            }
            this.filterList = list;
        }
        public boolean filter(String value){
            if(value == null){
                value = "";
            }
            for(String filter:filterList){
                if(value.contains(filter)){
                    return true;
                }
            }
            return false;
        }
    }
    private static class StringRegexValueFilter extends StringValueFilterBase{
        private final List<Pattern> filterList;
        public StringRegexValueFilter(Object value){
            List<Pattern> list = new ArrayList<>();
            if(value instanceof List){
                for(Object object:(List)value){
                    list.add(Pattern.compile(object.toString()));
                }
            }else{
                list.add(Pattern.compile(value.toString()));
            }
            this.filterList = list;
        }
        public boolean filter(String value){
            if(value == null){
                value = "";
            }
            for(Pattern filter:filterList){
                if(filter.matcher(value).matches()){
                    return true;
                }
            }
            return false;
        }
    }

    private static abstract class NumberValueFilterBase implements ValueFilter{
        public boolean filter(String value){
            if(value == null || value.length() == 0){
                return filter(0);
            }else if(!NumberUtils.isParsable(value)){
                return false;
            }else{
                return filter(Double.parseDouble(value));
            }
        }
    }
    private static class NumberSameValueFilter extends NumberValueFilterBase{
        private final List<Number> filterList;
        public NumberSameValueFilter(Object value){
            List<Number> list = new ArrayList<>();
            if(value instanceof List){
                for(Object object:(List)value){
                    if(object instanceof Number)
                    list.add((Number)object);
                }
            }else{
                if(value instanceof Number){
                    list.add((Number)value);
                }
            }
            this.filterList = list;
        }

        public boolean filter(double number){
            for(Number filter:filterList){
                if(Math.abs(filter.doubleValue() - number) < THRESHOLD){
                    return true;
                }
            }
            return false;
        }
    }
    private static class NumberMoreThanFilter extends NumberValueFilterBase{
        private final Number filter;
        public NumberMoreThanFilter(Object value){
            if(value instanceof Number){
                filter = (Number)value;
            }else{
                filter = null;
            }
        }
        public boolean filter(double number){
            if(filter == null){
                return false;
            }
            return number > filter.doubleValue() +THRESHOLD;
        }
    }
    private static class NumberAndMoreFilter extends NumberValueFilterBase{
        private final Number filter;
        public NumberAndMoreFilter(Object value){
            if(value instanceof Number){
                filter = (Number)value;
            }else{
                filter = null;
            }
        }
        public boolean filter(double number){
            if(filter == null){
                return false;
            }
            return number > filter.doubleValue() -THRESHOLD;
        }
    }
    private static class NumberLessThanFilter extends NumberValueFilterBase{
        private final Number filter;
        public NumberLessThanFilter(Object value){
            if(value instanceof Number){
                filter = (Number)value;
            }else{
                filter = null;
            }
        }
        public boolean filter(double number){
            if(filter == null){
                return false;
            }
            return number < filter.doubleValue() -THRESHOLD;
        }
    }
    private static class NumberAndLessFilter extends NumberValueFilterBase{
        private final Number filter;
        public NumberAndLessFilter(Object value){
            if(value instanceof Number){
                filter = (Number)value;
            }else{
                filter = null;
            }
        }
        public boolean filter(double number){
            if(filter == null){
                return false;
            }
            return number < filter.doubleValue() +THRESHOLD;
        }
    }


    private BuiltinScriptFilter(String key,LinkedTreeMap body){
        this.key = key;
        {
            Map<String,Integer> map = new HashMap<>();
            String[] header = BattleExDto.BuiltinScriptHeaderWithKey(key);
            for(int i=0;i<header.length;i++){
                map.put(header[i], Integer.valueOf(i));
            }
            this.outputFilter = OutputFilter.create(body.get("出力"),map);
        }
        this.dateTimeFilter = DateTimeFilter.create(body.get("日時"));
        this.attackCountItemFilter = CountItemFilter.create(body.get("攻撃艦装備"));
        this.defenceCountItemFilter = CountItemFilter.create(body.get("防御艦装備"));
    }
    private BuiltinScriptFilter(){
        this.key = null;
        this.outputFilter = OutputFilter.create(null,null);
        this.dateTimeFilter = DateTimeFilter.create(null);
        this.attackCountItemFilter = CountItemFilter.create(null);
        this.defenceCountItemFilter = CountItemFilter.create(null);
    }
    public static BuiltinScriptFilter create(LinkedTreeMap filter){
        if(!(filter.get("種別") instanceof String)){
            return null;
        }
        return new BuiltinScriptFilter((String)filter.get("種別"),filter);
    }
    public static BuiltinScriptFilter createTrueFilter(){
        return new BuiltinScriptFilter();
    }

    public boolean filterDateTime(Date date){
        return this.dateTimeFilter.filter(date);
    }
    public boolean filterAttackCountItem(ShipBaseDto ship){
        return this.attackCountItemFilter.filter(ship);
    }
    public boolean filterDefenceCountItem(ShipBaseDto ship){
        return this.defenceCountItemFilter.filter(ship);
    }
    public boolean filterOutput(List<String> line){
        return this.outputFilter.filter(line);
    }
    public boolean filterHougekiAttackDefence(BattleExDto battle,int at,int df,boolean isSecond){
        ShipBaseDto attack =
                (at >= 7)?battle.getEnemy().get(at-7):
                (isSecond)?battle.getDockCombined().getShips().get(at-1):
                battle.getDock().getShips().get(at-1);
        ShipBaseDto defence =
                (df >= 7)?battle.getEnemy().get(df-7):
                (isSecond)?battle.getDockCombined().getShips().get(df-1):
                battle.getDock().getShips().get(df-1);
        return this.filterAttackCountItem(attack) && this.filterDefenceCountItem(defence);
    }
    public boolean filterHougekiAttackDefenceECNight(BattleExDto battle,int at,int df,boolean isSecond,boolean enemyIsSecond){
        ShipBaseDto attack =
                (at >= 7)
                    ?((enemyIsSecond)?battle.getEnemyCombined().get(at-7):battle.getEnemy().get(at-7))
                    :((isSecond)?battle.getDockCombined().getShips().get(at-1):battle.getDock().getShips().get(at-1));
        ShipBaseDto defence =
                (df >= 7)
                    ?((enemyIsSecond)?battle.getEnemyCombined().get(df-7):battle.getEnemy().get(df-7))
                    :((isSecond)?battle.getDockCombined().getShips().get(df-1):battle.getDock().getShips().get(df-1));
        return this.filterAttackCountItem(attack) && this.filterDefenceCountItem(defence);
    }
    public boolean filterHougekiAttackDefenceEC(BattleExDto battle,int at,int df,int eflag){
        ShipBaseDto attack =
            (eflag == 0)?((at < 7)?battle.getDock().getShips().get(at-1):battle.getDockCombined().getShips().get(at-7))
            :((at < 7)?battle.getEnemy().get(at-1):battle.getEnemyCombined().get(at-7));


        ShipBaseDto defence =
            (eflag == 0)?((df < 7)?battle.getEnemy().get(df-1):battle.getEnemyCombined().get(df-7))
            :((df < 7)?battle.getDock().getShips().get(df-1):battle.getDockCombined().getShips().get(df-7));
        return this.filterAttackCountItem(attack) && this.filterDefenceCountItem(defence);
    }
    public boolean filterRaigekiAttackDefence(BattleExDto battle,int at,int df,boolean isSecond,boolean toEnemy){
        ShipBaseDto attack =
                (!toEnemy)?battle.getEnemy().get(at-1):
                (isSecond)?battle.getDockCombined().getShips().get(at-1):
                battle.getDock().getShips().get(at-1);
        ShipBaseDto defence =
                (toEnemy)?battle.getEnemy().get(df-1):
                (isSecond)?battle.getDockCombined().getShips().get(df-1):
                battle.getDock().getShips().get(df-1);
        return this.filterAttackCountItem(attack) && this.filterDefenceCountItem(defence);
    }
    public boolean filterRaigekiAttackDefenceEC(BattleExDto battle,int at,int df,boolean toEnemy){
        ShipBaseDto attack =
                (!toEnemy)?((at<7)?battle.getEnemy().get(at-1):battle.getEnemyCombined().get(at-7))
                :((at<7)?battle.getDock().getShips().get(at-1):battle.getDockCombined().getShips().get(at-7));
        ShipBaseDto defence =
                (toEnemy)?((df<7)?battle.getEnemy().get(df-1):battle.getEnemyCombined().get(df-7))
                :((df<7)?battle.getDock().getShips().get(df-1):battle.getDockCombined().getShips().get(df-7));
        return this.filterAttackCountItem(attack) && this.filterDefenceCountItem(defence);
    }
}
