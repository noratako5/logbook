/**
 * 
 */
package logbook.scripting;

import java.util.Map;
import java.util.TreeMap;

import logbook.constants.AppConstants;
import logbook.gui.logic.ColorManager;
import logbook.gui.logic.TableItemCreator;
import logbook.scripting.ScriptLoader.MethodInvoke;
import logbook.scripting.ScriptLoader.Script;
import logbook.util.ReportUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author Nekopanda
 *
 */
public class TableItemCreatorProxy implements TableItemCreator {

    private class CreateMethod implements MethodInvoke {
        public Table table;
        public Comparable[] data;
        public int index;

        @Override
        public Object invoke(Object arg) {
            return ((TableItemCreator) arg).create(this.table, this.data, this.index);
        }
    }

    protected final String prefix;
    private final CreateMethod createMethod = new CreateMethod();

    protected static final Map<String, TableItemCreatorProxy> instance = new TreeMap<String, TableItemCreatorProxy>();

    public static TableItemCreatorProxy get(String prefix) {
        TableItemCreatorProxy value = instance.get(prefix);
        if (value == null) {
            value = new TableItemCreatorProxy(prefix);
            instance.put(prefix, value);
        }
        return value;
    }

    protected TableItemCreatorProxy(String prefix) {
        this.prefix = prefix;
    }

    protected Script getScript() {
        return ScriptLoader.getTableStyleScript(this.prefix);
    }

    @Override
    public TableItem create(Table table, Comparable[] data, int index) {
        this.createMethod.table = table;
        this.createMethod.data = data;
        this.createMethod.index = index;
        TableItem item = (TableItem) this.getScript().invoke(this.createMethod);
        if (item == null) {
            // 作れてなかったらデフォルトロジックで作る
            item = this.defaultCreate(table, data, index);
        }
        return item;
    }

    private TableItem defaultCreate(Table table, Comparable[] data, int index) {
        TableItem item = new TableItem(table, SWT.NONE);
        // 偶数行に背景色を付ける
        if ((index % 2) != 0) {
            item.setBackground(ColorManager.getColor(AppConstants.ROW_BACKGROUND));
        }
        item.setText(ReportUtils.toStringArray(data));
        return item;
    }

    @Override
    public void begin(final String[] header) {
        this.getScript().invoke(new MethodInvoke() {
            @Override
            public Object invoke(Object arg) {
                ((TableItemCreator) arg).begin(header);
                return null;
            }
        });
    }

    @Override
    public void end() {
        this.getScript().invoke(new MethodInvoke() {
            @Override
            public Object invoke(Object arg) {
                ((TableItemCreator) arg).end();
                return null;
            }
        });
    }

}
