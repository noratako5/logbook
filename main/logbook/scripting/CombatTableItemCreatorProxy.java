/**
 * 
 */
package logbook.scripting;

import logbook.gui.logic.CombatTableItemCreator;
import logbook.scripting.ScriptLoader.MethodInvoke;
import logbook.scripting.ScriptLoader.Script;

/**
 * @author Nekopanda
 *
 */
public class CombatTableItemCreatorProxy extends TableItemCreatorProxy {

    public static CombatTableItemCreatorProxy get(String prefix) {
        TableItemCreatorProxy value = instance.get(prefix);
        if (value == null) {
            value = new CombatTableItemCreatorProxy(prefix);
            instance.put(prefix, value);
        }
        return (CombatTableItemCreatorProxy) value;
    }

    protected CombatTableItemCreatorProxy(String prefix) {
        super(prefix);
    }

    @Override
    protected Script getScript() {
        return ScriptLoader.getTableStyleScript(this.prefix, CombatTableItemCreator.class);
    }

    public String title(String defaultTitle) {
        String t = (String) this.getScript().invoke(new MethodInvoke() {
            @Override
            public Object invoke(Object arg) {
                return ((CombatTableItemCreator) arg).title(defaultTitle);
            }
        });
        if (t == null) {
            t = defaultTitle;
        }
        return t;
    }
}
