load("script/util/underscore.js");


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
}

function onHougeki(raw, battle, phase, phaseJson, atacks, hougekiHps, api_hougeki, hougekiIndex) {
	if (atacks != null && hougekiHps != null && api_hougeki != null) {
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
						atShipInfo.getFullName()
						, atShip.getLv()
						, atShipParam.getLuck()
						, atShipSlotParam.getHoug()
						, atShip.getCond()
						, atShip.getBull()
						, atShipInfo.getMaxBull()
						, getShipItemName(atShip, 0)
						, getShipItemName(atShip, 1)
						, getShipItemName(atShip, 2)
						, getShipItemName(atShip, 3)
						, dfShipInfo.getFullName()
						, dfShipInfo.getShipId()
						, dfShip.getLv()
						, phaseJson.api_search[0]
						, phaseJson.api_search[1]
						, battle.getFormation()[0]
						, battle.getFormation()[1]
						, battle.getFormationMatch()
						, damage
						, api_cl[j]
						, ([
							"大破"
							, "中破"
							, "小破"
							, "小破未満"
							, "小破未満"
						])[Math.floor(4 * originHp / atShipParam.getHP())]
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
	var shipItem = getShipItem(ships, i);
	if (shipItem != null) {
		return shipItem.getInfo().getName();
	}
	else {
		return "なし";
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

function getBattleHps(battle) {
	shipHps = {
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
}

function getAirHps(shipHps, air) {
	if (air != null) {
		return getHps(shipHps, air.atacks);
	}
	else {
		return null;
	}
}

function getHps(shipHps, atacks) {
	if (shipHps != null) {
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
				originHps = shipHps.friend;
				targetHps = shipHps.enemy;
			}
			else {
				originHps = shipHps.enemy;
				targetHps = shipHps.friend;
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
			targetHps[t] = Math.max(0, targetHp - damage[i])
			return targetHp;
		});
	}
	else {
		return null;
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
			if (_.isUndefined(r)) {
				return "undefined";
			}
			else if (_.isNull(r)) {
				return "null";
			}
			else {
				return r;
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
