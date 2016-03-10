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
public final class CombatReportTable extends DropReportTable {

    private final CombatLogProxy logProxy;
    private final TableItemCreatorProxy tableItemCreatorProxy;
    private final String defaultTitleMain;
    private final String titleMain;

    /**
     * @param parent
     */
    public CombatReportTable(Shell parent, MenuItem menuItem, String prefix, String defaultTitleMain) {
        super(parent, menuItem);
        CombatLogProxy.set(prefix, defaultTitleMain);
        this.logProxy = CombatLogProxy.get(prefix);
        this.tableItemCreatorProxy = TableItemCreatorProxy.get(prefix);
        this.defaultTitleMain = defaultTitleMain;
        this.titleMain = this.defaultTitleMain;
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
        return CreateReportLogic.getCombatResultHeader(this.logProxy);
    }

    @Override
    protected void updateTableBody() {
        this.body = CreateReportLogic.getCombatResultBody(this.logProxy, this.getFilter());
    }

    @Override
    protected TableItemCreator getTableItemCreator() {
        //return CreateReportLogic.DEFAULT_TABLE_ITEM_CREATOR;
        return this.tableItemCreatorProxy;
    }
}
