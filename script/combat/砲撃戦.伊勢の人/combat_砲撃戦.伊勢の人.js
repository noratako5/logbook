
ComparableArrayType = Java.type("java.lang.Comparable[]");
ComparableArrayArrayType = Java.type("java.lang.Comparable[][]");
JsonValue = Java.type("javax.json.JsonValue");


function header() {
	return [
		"攻撃艦"
		, "攻レベル"
		, "攻運"
		, "攻装備込火力"
		, "攻cond"
		, "攻弾薬"
		, "攻最大弾薬"
		, "攻装備1"
		, "攻装備2"
		, "攻装備3"
		, "攻装備4"
		, "防御艦"
		, "防ID"
		, "防レベル"
		, "味方索敵"
		, "敵索敵"
		, "味方陣形"
		, "敵陣形"
		, "T字"
		, "ダメージ"
		, "クリティカル"
		, "味方損傷"
		, "敵損傷"
	];
}

function begin() { }

function body(battle) {
	var raw = [];
	var hqLv = battle.hqLv;
	var ships = {};
	var fShips = battle.getFriends().get(0).ships;
	for (var i = 0; i < fShips.length; ++i) {
		ships[i + 1] = fShips.get(i);
	}
	var eShips = battle.enemy;
	for (var i = 0; i < eShips.length; ++i) {
		ships[i + 7] = eShips.get(i);
	}
	onPhase(raw, battle, ships, battle.getPhase1());
	return toComparable(raw);
}

function onPhase(raw, battle, ships, phase) {
	var json = JSON.parse(phase.json);
	onHougeki(raw, battle, ships, phase, json.api_hougeki1);
	onHougeki(raw, battle, ships, phase, json.api_hougeki2);
	onHougeki(raw, battle, ships, phase, json.api_hougeki3);
}

function onHougeki(raw, battle, ships, phase, api_hougeki) {
	if (api_hougeki != null) {
		var phaseJson = JSON.parse(phase.json);
		var api_at_list = api_hougeki.api_at_list;
		var api_df_list = api_hougeki.api_df_list;
		var api_damage = api_hougeki.api_damage;
		var api_cl_list = api_hougeki.api_cl_list;
		for (var i = 1; i < api_at_list.length; ++i) {
			var api_at = api_at_list[i];
			if (1 <= api_at && api_at <= 6) {
				var atShip = ships[api_at];
				var atShipParam = atShip.getParam();
				var atShipParamMax = atShip.getMax();
				var atShipSlotParam = atShip.getSlotParam();
				var atShipInfo = atShip.shipInfo;
				var atItemList = atShip.getItem2();
				var atItem1 = onItem(atItemList, 0);
				var atItem2 = onItem(atItemList, 1);
				var atItem3 = onItem(atItemList, 2);
				var atItem4 = onItem(atItemList, 3);
				var api_df = api_df_list[i];
				var api_cl = api_cl_list[i];
				var api_dam = api_damage[i];
				for (var j = 0; j < api_df.length; ++j) {
					var dfShip = ships[api_df[j].toString()];
					var dfShipInfo = dfShip.getShipInfo();
					raw.push([
						atShipInfo.getFullName().toString()
						, atShip.getLv().toString()
						, atShipParam.getLuck().toString()
						, atShipSlotParam.getHoug().toString()
						, atShip.getCond().toString()
						, atShip.getBull().toString()
						, atShipInfo.getMaxBull()
						, atItem1.name
						, atItem2.name
						, atItem3.name
						, atItem4.name
						, dfShipInfo.getFullName().toString()
						, dfShipInfo.getShipId()
						, dfShip.getLv()
						, phaseJson.api_search[0].toString()
						, phaseJson.api_search[1].toString()
						, battle.getFormation()[0].toString()
						, battle.getFormation()[1].toString()
						, battle.getFormationMatch().toString()
						, Math.floor(api_dam[j]).toString()
						, api_cl[j].toString()
					]);
				}
			}
		}
	}
}

function onItem(itemList, i) {
	if (i < itemList.length) {
		var item = itemList.get(i);
		if (item != null) {
			return {
				name: item.getName().toString()
				, level: item.getLevel().toString()
			}
		}
	}
	return {
		name: "なし"
		, level: "0"
	}
}

function end() { }



// javascriptの配列をそのまま返すと遅いので
// Comparable[]に変換しておく
// undefinedはnullに変換される
function toComparable(raw) {
	if (isArray(raw)) {
		if (isArray(raw[0])) {
			var ret = new ComparableArrayArrayType(raw.length);
			for (var j = 0; j < raw.length; ++j) {
				ret[j] = toComparableArray(raw[j]);
			}
			return ret;
		}
		else {
			return toComparableArray(raw);
		}
	}
	return raw;

	function isArray(o) {
		return Object.prototype.toString.call(o) === '[object Array]';
	}
	
	function toComparableArray(raw) {
		var ret = new ComparableArrayType(raw.length);
		for(var i=0; i<raw.length; ++i) {
			if(raw[i] == null) {
				ret[i] = null;
			}
			else {
				ret[i] = raw[i];
			}
		}
		return ret;
	}
}
