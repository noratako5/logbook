package logbook.gui;

import logbook.constants.AppConstants;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.TableItemCreator;
import logbook.scripting.TableItemCreatorProxy;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * ドロップ報告書
 *
 */
public final class DropReportTable extends DropOrCombatReportTable {
    /**
     * @param parent
     */
    public DropReportTable(Shell parent, MenuItem menuItem) {
        super(parent, menuItem);
    }

    @Override
    protected String getTitleMain() {
        return "ドロップ報告書";
    }

    @Override
    protected Point getSize() {
        return new Point(600, 350);
    }

    @Override
    protected String[] getTableHeader() {
        return CreateReportLogic.getBattleResultHeader();
    }

    @Override
    protected void updateTableBody() {
        this.body = CreateReportLogic.getBattleResultBody(this.getFilter());
    }

    @Override
    protected TableItemCreator getTableItemCreator() {
        //return CreateReportLogic.DEFAULT_TABLE_ITEM_CREATOR;
        return TableItemCreatorProxy.get(AppConstants.DROPTABLE_PREFIX);
    }
}
