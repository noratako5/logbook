package logbook.gui;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.TableItemCreator;
import logbook.scripting.TableItemCreatorProxy;

/**
 * 戦闘報告書
 *
 */
public final class BuiltinCombatReportTable extends DropReportTable {

    private final String key;
    private final TableItemCreatorProxy tableItemCreatorProxy;
    private final String defaultTitleMain;
    private final String titleMain;

    /**
     * @param parent
     */
    public BuiltinCombatReportTable(Shell parent, MenuItem menuItem, String prefix, String defaultTitleMain) {
        super(parent, menuItem);
        this.tableItemCreatorProxy = TableItemCreatorProxy.get(prefix);
        this.defaultTitleMain = defaultTitleMain;
        this.titleMain = this.defaultTitleMain;
        this.key = this.defaultTitleMain;
    }

    /**
     * ウィンドウ識別ID（デフォルト実装はクラス名フルパス）
     * @return ウィンドウ識別ID
     */
    @Override
    public String getWindowId() {
        return this.getClass().getName() + "/" + this.defaultTitleMain;
    }

    @Override
    protected String getTitleMain() {
        return this.titleMain;
    }

    @Override
    protected Point getSize() {
        return new Point(600, 350);
    }

    @Override
    protected String[] getTableHeader() {
        return CreateReportLogic.getBuiltinCombatResultHeader(this.key);
    }

    @Override
    protected void updateTableBody() {
        this.body = CreateReportLogic.getBuiltinCombatResultBody(this.key, this.getFilter());
    }

    @Override
    protected TableItemCreator getTableItemCreator() {
        //return CreateReportLogic.DEFAULT_TABLE_ITEM_CREATOR;
        return this.tableItemCreatorProxy;
    }
}
