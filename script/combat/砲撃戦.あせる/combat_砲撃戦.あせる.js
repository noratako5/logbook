load("script/util/underscore.js");


function header() {
	return [
		"提督名"
		, "提督レベル"
		, "攻撃艦"
		, "攻レベル"
		, "攻運"
		, "攻装備"
		, "攻装備1"
		, "攻装備2"
		, "攻装備3"
		, "攻装備4"
		, "攻損傷"
		, "防御艦"
		, "索敵"
		, "T字"
		, "巡目"
		, "ダメージ"
		, "クリティカル"
		, "攻装備込火力"
		, "攻cond"
		, "攻弾薬"
		, "攻最大弾薬"
		, "攻装備レベル1"
		, "攻装備レベル2"
		, "攻装備レベル3"
		, "攻装備レベル4"
		, "防ID"
		, "防レベル"
		, "味方索敵"
		, "敵索敵"
		, "味方陣形"
		, "敵陣形"
		, "攻編成順"
	];
}

function begin() { }

function body(battle) {
	var battleHps = getBattleHps(battle);
	var raw = [];
	var phaseList = battle.getPhaseList();
	_.forEach(_.range(phaseList.length), function (i) {
		var phase = phaseList[i];
		var phaseHps = battleHps[i];
		var phaseJson = JSON.parse(phase.json);
		onHougeki(raw, battle, phase, phaseJson, phase.getHougeki1(), phaseHps.hougeki1, phaseJson.api_hougeki1, 1);
		onHougeki(raw, battle, phase, phaseJson, phase.getHougeki2(), phaseHps.hougeki2, phaseJson.api_hougeki2, 2);
		onHougeki(raw, battle, phase, phaseJson, phase.getHougeki3(), phaseHps.hougeki3, phaseJson.api_hougeki3, 3);
	});
	return toComparable(raw);

	function onHougeki(raw, battle, phase, phaseJson, atacks, hougekiHps, api_hougeki, hougekiIndex) {
		if (atacks != null && hougekiHps != null && api_hougeki != null) {
			if (battle.getBasicJsonString() != null) {
				var nickname = JSON.parse(battle.getBasicJsonString()).api_nickname;
			}
			else {
				var nickname 
			}
			var api_cl_list = api_hougeki.api_cl_list;
			_.forEach(_.range(atacks.length), function (i) {
				var atack = atacks[i];
				if (atack.friendAtack) {
					var o = atack.origin[0];
					var atShip = getAtShip(battle, atack, o);
					var atShipParam = atShip.getParam();
					var atShipParamMax = atShip.getMax();
					var atShipSlotParam = atShip.getSlotParam();
					var atShipInfo = atShip.getShipInfo();
					var atShipItemList = atShip.getItem2();
					var atackHps = hougekiHps[i];
					var originHp = atackHps.origin[0];
					var api_cl = api_cl_list[i + 1];
					_.forEach(atack.target, function (t, j) {
						var dfShip = getDfShip(battle, atack, t);
						var dfShipInfo = dfShip.getShipInfo();
						var targetHp = atackHps.target[j];
						var damage = atack.damage[j];
						raw.push([
							nickname
							, battle.getHqLv()
							, atShip.getFullName()
							, atShip.getLv()
							, atShipParam.getLuck()
							, ""
							, getShipItemName(atShip, 0)
							, getShipItemName(atShip, 1)
							, getShipItemName(atShip, 2)
							, getShipItemName(atShip, 3)
							, ([
								"轟沈"
								, "大破"
								, "中破"
								, "小破"
								, "小破未満"
							])[Math.ceil(4 * originHp / atShipParamMax.getHP())]
							, dfShip.getFullName()
							, ({
								1: "発見：索敵機有り"
								, 2: "発見：索敵機有り(未帰還機有り)"
								, 3: "発見できず：索敵機有り(未帰還機有り)"
								, 4: "発見できず：索敵機有り"
								, 5: "発見：索敵機無し"
								, 6: "索敵フェイズ無し"
							})[phaseJson.api_search[0]]
							, ({
								1: "同航戦"
								, 2: "反航戦"
								, 3: "T字有利"
								, 4: "T字不利"
							})[phaseJson.api_formation[2]]
							, hougekiIndex
							, damage
							, api_cl[j] >>> 0
							, atShipParam.getHoug()
							, atShip.getCond()
							, atShip.getBull()
							, atShipInfo.getMaxBull()
							, getShipItemLevel(atShip, 0)
							, getShipItemLevel(atShip, 1)
							, getShipItemLevel(atShip, 2)
							, getShipItemLevel(atShip, 3)
							, dfShipInfo.getShipId()
							, dfShip.getLv()
							, phaseJson.api_search[0] >>> 0
							, phaseJson.api_search[1] >>> 0
							, battle.getFormation()[0]
							, battle.getFormation()[1]
							, (o + 1) >>> 0
						]);
					});
				}
			});
		}
	}

	function getAtShip(battle, atack, i) {
		if (atack.friendAtack) {
			return getFriendShip(battle, i);
		}
		else {
			return getEnemyShip(battle, i);
		}
	}

	function getDfShip(battle, atack, i) {
		if (atack.friendAtack) {
			return getEnemyShip(battle, i);
		}
		else {
			return getFriendShip(battle, i);
		}
	}

	function getFriendShip(battle, i) {
		if (i < 6) {
			return battle.getDock().getShips()[i];
		}
		else {
			return battle.getDockCombined().getShips()[i - 6];
		}
	}

	function getEnemyShip(battle, i) {
		return battle.getEnemy()[i];
	}
	
	function getShipItemName(ships, i) {
		var itemInfo = getShipItemInfo(ships, i);
		if (itemInfo != null) {
			return itemInfo.getName();
		}
		else {
			return "なし";
		}
	}
	
	function getShipItemLevel(ships, i) {
		var item = getShipItem(ships, i);
		if (item != null) {
			return item.getLevel();
		}
		else {
			return 0;
		}
	}

	function getShipItem(ships, i) {
		var shipItemList = ships.getItem2();
		if (i < shipItemList.length) {
			return shipItemList[i];
		}
		else {
			return null;
		}
	}
	
	function getShipItemInfo(ships, i) {
		var shipItemList = ships.getItem();
		if (i < shipItemList.length) {
			return shipItemList[i];
		}
		else {
			return null;
		}
	}
}

function getBattleHps(battle) {
	var shipHps = {
		friend: new Array(6 * 2)
		, enemy: new Array(6)
	};
	_.forEach(battle.getStartFriendHp(), function (hp, i) {
		shipHps.friend[i] = hp;
	});
	_.forEach(battle.getStartEnemyHp(), function (hp, i) {
		shipHps.enemy[i] = hp;
	});
	if (battle.isCombined()) {
		_.forEach(battle.getStartFriendHpCombined(), function (hp, i) {
			shipHps.friend[i + 6] = hp;
		});
	}
	return battleHps = _.map(battle.getPhaseList(), function (phase) {
		var phaseHps = {};
		phaseHps.air = getAirHps(shipHps, phase.getAir());
		phaseHps.support = getHps(shipHps, phase.getSupport());
		phaseHps.opening = getHps(shipHps, phase.getOpening());
		phaseHps.air2 = getAirHps(shipHps, phase.getAir2());
		phaseHps.hougeki = getHougekiHps(shipHps, phase.getHougeki());
		phaseHps.hougeki1 = getHougekiHps(shipHps, phase.getHougeki1());
		if (phase.getKind().toString() === "COMBINED_BATTLE") {
			phaseHps.raigeki = getHps(shipHps, phase.getRaigeki());
			phaseHps.hougeki2 = getHougekiHps(shipHps, phase.getHougeki2());
			phaseHps.hougeki3 = getHougekiHps(shipHps, phase.getHougeki3());
		}
		else {
			phaseHps.hougeki2 = getHougekiHps(shipHps, phase.getHougeki2());
			phaseHps.hougeki3 = getHougekiHps(shipHps, phase.getHougeki3());
			phaseHps.raigeki = getHps(shipHps, phase.getRaigeki());
		}
		return phaseHps;
	});

	function getAirHps(shipHps, air) {
		if (air != null) {
			return getHps(shipHps, air.atacks);
		}
		else {
			return null;
		}
	}

	function getHps(shipHps, atacks) {
		if (atacks != null) {
			var beforeShipHps = _.clone(shipHps);
			getHougekiHps(shipHps, atacks);
			return beforeShipHps;
		}
		else {
			return null;
		}
	}

	function getHougekiHps(shipHps, atacks) {
		if (atacks != null) {
			return _.map(atacks, function (atack) {
				if (atack.friendAtack) {
					var originHps = shipHps.friend;
					var targetHps = shipHps.enemy;
				}
				else {
					var originHps = shipHps.enemy;
					var targetHps = shipHps.friend;
				}
				return {
					origin: getOriginHps(originHps, atack)
					, target: getTargetHps(targetHps, atack)
				}
			});
		}
		else {
			return null;
		}
	}

	function getOriginHps(originHps, atack) {
		var origin = atack.origin;
		if (origin != null) {
			return _.map(origin, function (o) {
				return originHps[o];
			});
		}
		else {
			return null;
		}
	}

	function getTargetHps(targetHps, atack) {
		var target = atack.target;
		var damage = atack.damage;
		if (target != null && damage != null) {
			return _.map(target, function (t, i) {
				var targetHp = targetHps[t];
				targetHps[t] = Math.max(0, targetHp - damage[i]);
				return targetHp;
			});
		}
		else {
			return null;
		}
	}
}

function end() { }



// javascriptの配列をそのまま返すと遅いので
// Comparable[]に変換しておく
// undefinedはnullに変換される
function toComparable(raw) {
	if (_.isArray(raw)) {
		var ComparableArrayType = Java.type("java.lang.Comparable[]");
		if (_.isArray(raw[0])) {
			var ComparableArrayArrayType = Java.type("java.lang.Comparable[][]");
			return Java.to(_.map(raw, toComparableArray), ComparableArrayArrayType);
		}
		else {
			return toComparableArray(raw);
		}
	}
	else {
		return raw;
	}
	
	function toComparableArray(raw) {
		return Java.to(_.map(raw, function (r) {
			if (r != null) {
				return r;
			}
			else {
				return null;
			}
		}), ComparableArrayType);
	}
}

// メッセージボックス表示
function alert(str) {
	SWT = Java.type("org.eclipse.swt.SWT");
	MessageBox = Java.type("org.eclipse.swt.widgets.MessageBox");
	Display = Java.type("org.eclipse.swt.widgets.Display");
	var shell = Display.getDefault().getActiveShell();
	var box = new MessageBox(shell,SWT.OK);
	box.setMessage(String(str));
	box.open();
}
