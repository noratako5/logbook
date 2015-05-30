package logbook.gui;

import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.TableItemCreator;
import logbook.scripting.CombatLogProxy;
import logbook.scripting.TableItemCreatorProxy;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * 戦闘報告書
 *
 */
public final class CombatReportTable extends DropOrCombatReportTable {

    private final String title;
    private final String prefix;

    /**
     * @param parent
     */
    public CombatReportTable(Shell parent, MenuItem menuItem, String title, String prefix) {
        super(parent, menuItem);
        this.title = title;
        this.prefix = prefix;
        CombatLogProxy.put(prefix);
    }

    /**
     * ウィンドウ識別ID（デフォルト実装はクラス名フルパス）
     * @return ウィンドウ識別ID
     */
    @Override
    public String getWindowId() {
        return this.getClass().getName() + "/" + this.title;
    }

    @Override
    protected String getTitleMain() {
        return this.title;
    }

    @Override
    protected Point getSize() {
        return new Point(600, 350);
    }

    @Override
    protected String[] getTableHeader() {
        return CreateReportLogic.getCombatResultHeader(this.prefix);
    }

    @Override
    protected void updateTableBody() {
        this.body = CreateReportLogic.getCombatResultBody(this.prefix, this.getFilter());
    }

    @Override
    protected TableItemCreator getTableItemCreator() {
        //return CreateReportLogic.DEFAULT_TABLE_ITEM_CREATOR;
        return TableItemCreatorProxy.get(this.prefix);
    }
}
