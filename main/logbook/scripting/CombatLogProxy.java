/**
 * 
 */
package logbook.scripting;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import logbook.dto.BattleExDto;
import logbook.scripting.ScriptLoader.MethodInvoke;
import logbook.scripting.ScriptLoader.TableScriptCollection;

/**
 * @author Nekopanda
 *
 */
public class CombatLogProxy {

    private class AnyBodiesMethod implements MethodInvoke {
        public BattleExDto battle;

        @Override
        public Object invoke(Object arg) {
            return ((CombatLogListener) arg).body(this.battle);
        }
    }

    private final String prefix;
    private final AnyBodiesMethod anyBodiesMethod = new AnyBodiesMethod();

    private final MethodInvoke beginMethod = new MethodInvoke() {
        @Override
        public Object invoke(Object arg) {
            ((CombatLogListener) arg).begin();
            return null;
        }
    };
    private final MethodInvoke endMethod = new MethodInvoke() {
        @Override
        public Object invoke(Object arg) {
            ((CombatLogListener) arg).end();
            return null;
        }
    };

    private static final Map<String, CombatLogProxy> instance = new TreeMap<String, CombatLogProxy>();

    public static CombatLogProxy get(String prefix) {
        CombatLogProxy value = instance.get(prefix);
        if (value == null) {
            value = new CombatLogProxy(prefix);
            instance.put(prefix, value);
        }
        return value;
    }

    public static Collection<CombatLogProxy> getAll() {
        return instance.values();
    }

    public static Map<String, Comparable[][]> bodyAll(BattleExDto battle) {
        Map<String, Comparable[][]> bodyAll = new TreeMap<String, Comparable[][]>();
        for (Map.Entry<String, CombatLogProxy> entry : instance.entrySet()) {
            bodyAll.put(entry.getKey(), entry.getValue().body(battle));
        }
        return bodyAll;
    }

    public static void beginAll() {
        for (CombatLogProxy value : instance.values()) {
            value.begin();
        }
    }

    public static void endAll() {
        List<CombatLogProxy> values = Collections.list(Collections.enumeration(instance.values()));
        Collections.reverse(values);
        for (CombatLogProxy value : values) {
            value.end();
        }
    }

    private CombatLogProxy(String prefix) {
        this.prefix = prefix;
    }

    private TableScriptCollection getScript() {
        return ScriptLoader.getTableScript(this.prefix, CombatLogListener.class);
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String[] header() {
        return this.getScript().header();
    }

    public Comparable[][] body(BattleExDto battle) {
        this.anyBodiesMethod.battle = battle;
        return this.getScript().anyBodies(this.anyBodiesMethod);
    }

    public void begin() {
        this.getScript().invoke(this.beginMethod);
    }

    public void end() {
        this.getScript().invoke(this.endMethod);
    }

}
