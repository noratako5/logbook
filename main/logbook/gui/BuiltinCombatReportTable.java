package logbook.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import logbook.config.AppConfig;
import logbook.config.bean.TableConfigBean;
import logbook.config.bean.TableConfigBean.Column;
import logbook.config.bean.TableConfigBean.SortKey;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.TableItemCreator;
import logbook.gui.logic.TableRowHeader;
import logbook.scripting.TableItemCreatorProxy;

/**
 * 戦闘報告書
 *
 */
public final class BuiltinCombatReportTable extends DropReportTable {
    private final int MAX_COLUMN = 200;
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

    @Override
    protected void createContents() {
        super.createContents();
        if(this.key.equals("航空戦撃墜")){
            int index = -1;
            List<MenuItem>itemList = Arrays.asList(this.opemenu.getItems());
            for(int i = 0;i<itemList.size();i++){
                MenuItem item = itemList.get(i);
                if(item.getText().equals("列を全て表示")){
                    index = i;
                    break;
                }
            }
            if(index > 0){
                MenuItem soubi = new MenuItem(this.opemenu, SWT.NONE,index+1);
                soubi.setText("装備を非表示");
                soubi.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        String[] header = BuiltinCombatReportTable.this.header;
                        boolean[] visibles = BuiltinCombatReportTable.this.getConfig().getVisibleColumn();
                        for(int i=0;i<header.length;i++){
                            if(header[i].contains("艦")&&header[i].contains("装備")){
                                visibles[i] = false;
                            }
                        }
                        BuiltinCombatReportTable.this.setColumnVisible(visibles);
                    }
                });
                MenuItem status = new MenuItem(this.opemenu, SWT.NONE,index+2);
                status.setText("パラメータを非表示");
                status.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        String[] header = BuiltinCombatReportTable.this.header;
                        boolean[] visibles = BuiltinCombatReportTable.this.getConfig().getVisibleColumn();
                        for(int i=0;i<header.length;i++){
                            if(header[i].contains("艦")
                                &&(header[i].contains(".編成順")
                                    ||header[i].contains(".種別")
                                    ||header[i].contains(".疲労")
                                    ||header[i].contains(".残耐久")
                                    ||header[i].contains(".最大耐久")
                                    ||header[i].contains(".損傷")
                                    ||header[i].contains(".残燃料")
                                    ||header[i].contains(".最大燃料")
                                    ||header[i].contains(".残弾薬")
                                    ||header[i].contains(".最大弾薬")
                                    ||header[i].contains(".Lv")
                                    ||header[i].contains(".速力")
                                    ||header[i].contains(".火力")
                                    ||header[i].contains(".雷装")
                                    ||header[i].contains(".対空")
                                    ||header[i].contains(".装甲")
                                    ||header[i].contains(".回避")
                                    ||header[i].contains(".対潜")
                                    ||header[i].contains(".索敵")
                                    ||header[i].contains(".運")
                                    ||header[i].contains(".射程")
                                )
                            ){
                                visibles[i] = false;
                            }
                        }
                        BuiltinCombatReportTable.this.setColumnVisible(visibles);
                    }
                });
            }
        }
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

    @Override
    protected void updateConfig() {

        Map<String, Column> columns = this.config.getColumns();
        String[] oldIds = this.config.getHeaderNames();
        boolean[] oldVisibles = this.config.getVisibleColumn();
        int[] oldWidth = this.config.getColumnWidth();
        int[] oldOrder = this.config.getColumnOrder();

        int oldLength = oldVisibles.length;

        // 各カラムの位置
        int[] oldPos = new int[oldLength];
        for (int i = 0; i < oldLength; ++i) {
            oldPos[i] = i;//諦めた
        }

        // 互換性維持
        if (oldWidth == null) {
            oldWidth = new int[oldLength];
        }
        if (oldIds == null) {
            // ヘッダー情報がない場合は今のヘッダーから作る
            oldLength = Math.min(oldLength, this.header.length);
            oldIds = ArrayUtils.subarray(this.headerId, 0, oldLength);
        }

        // pos順にする
        Column[] oldColumns = new Column[oldLength];
        for (int i = 0; i < oldLength; ++i) {
            oldColumns[i] = new Column(oldIds[i], oldVisibles[i], oldWidth[i], oldPos[i]);
        }
        Arrays.sort(oldColumns, comparePosition);

        // columnsデータに追加
        int next = 0;
        for (Column col : oldColumns) {
            Column colm = columns.get(col.id);
            if (colm != null) {
                // 順番を維持するためposをすりあわせる
                next = Math.max(next, colm.pos);
            }
            col.pos = next++;
            columns.put(col.id, col);
        }

        // columnsデータのpos番号を整理
        renumberColumnPosision(columns.values().toArray(new Column[0]));

        // 設定情報を引き継いだデータを作成
        Column[] newColumns = new Column[this.header.length];
        int nextNew = columns.size();
        for (int i = 0; i < this.header.length; ++i) {
            String id = this.headerId[i];
            Column colm = columns.get(id);
            if (colm != null) {
                newColumns[i] = colm.clone();
            }
            else {
                newColumns[i] = new Column(id, true, 0, nextNew++);
            }
        }

        // pos番号を整理
        renumberColumnPosision(newColumns.clone());

        // 完成したのでデータを戻す
        boolean[] visibles = new boolean[this.header.length];
        int[] columnWidth = new int[this.header.length];
        int[] columnOrder = new int[this.header.length];
        for (int i = 0; i < this.header.length; ++i) {
            visibles[i] = newColumns[i].visible;
            columnWidth[i] = newColumns[i].width;
            columnOrder[newColumns[i].pos] = i;
        }

        this.config.setColumns(columns);
        this.config.setHeaderNames(this.headerId);
        this.config.setVisibleColumn(visibles);
        this.config.setColumnWidth(columnWidth);
        this.config.setColumnOrder(columnOrder);

        // sortOrderをチェック
        SortKey[] sortKeys = this.config.getSortKeys();
        if (sortKeys == null) {
            sortKeys = new SortKey[3];
            this.config.setSortKeys(sortKeys);
        }
        for (int i = 0; i < sortKeys.length; ++i) {
            if (sortKeys[i] != null) {
                if (sortKeys[i].index >= this.header.length) {
                    // 超えてる
                    sortKeys[i] = null;
                }
            }
        }
        this.config.setSortKeys(sortKeys);
    }

    /**
     * テーブルヘッダーをセットする
     */
    @Override
    protected void setTableHeader() {
        while(this.table.getColumnCount() > 0){
            table.getColumn(0).dispose();
        }
        boolean[]visibles = this.getConfig().getVisibleColumn();
        SelectionListener listener = this.getHeaderSelectionListener();
        int counter = 0;
        for (int i = 0; i < this.header.length; i++) {
            if(visibles[i] == false){
                continue;
            }
            if(counter == MAX_COLUMN){
                break;
            }
            counter++;
            TableColumn col = new TableColumn(this.table, SWT.LEFT);
            col.setText(this.header[i]);
            col.setMoveable(true);
            col.addSelectionListener(listener);
            col.setData(i); // カラム番号を入れておく
            col.setToolTipText(this.headerId[i]);
            if (i == 0) {
                // No.列はソートしない
                col.setData("unsortable", new Object());
            }
        }
        if (this.config.getColumnOrder() != null) {
            this.setColumnOrder(this.config.getColumnOrder());
        }
    }
    @Override
    protected void setColumnOrder(int[] order){
        TableColumn[]columns = this.table.getColumns();
        if(order.length == columns.length){
            this.table.setColumnOrder(order);
        }
    }
    @Override
    protected int[] defaultColumnOrder() {
        int[] columnOrder = new int[this.table.getColumnCount()];
        for (int i = 0; i < columnOrder.length; ++i) {
            columnOrder[i] = i;
        }
        return columnOrder;
    }
    @Override
    protected void resetColumnOrder() {
        int[] columnOrder = this.defaultColumnOrder();
        if (this.config != null) {
            this.config.setColumnOrder(columnOrder);
        }
        this.setColumnOrder(columnOrder);
    }
    /**
     * テーブルボディーをセットする
     */
    @Override
    protected void setTableBody() {
        boolean[] visibles = this.getConfig().getVisibleColumn();
        TableItemCreator creator = this.getTableItemCreator();
        creator.begin(this.getTableHeader());
        // 表示最大件数を制限する
        int numPrintItems = Math.min(AppConfig.get().getMaxPrintItems(), this.body.size());
        for (int i = 0; i < numPrintItems; i++) {
            Comparable[] line = this.body.get(i);
            TableRowHeader rowHeader = (TableRowHeader) line[0];
            rowHeader.setNumber(i + 1); // ソート順に関係ない番号
            List<Comparable> itemList = new ArrayList<>();
            int counter = 0;
            for(int j = 0; j<visibles.length && j<line.length;j++){
                if(counter == MAX_COLUMN){
                    break;
                }
                if(visibles[j]){
                    itemList.add(line[j]);
                    counter++;
                }
            }
            Comparable[] line2 = itemList.toArray(new Comparable[0]);
            TableItem item = creator.create(this.table, line2, i);
        }
        creator.end();
    }

    @Override
    protected void setSortDirectionToHeader() {
        TableConfigBean.SortKey[] sortKeys = this.getConfig().getSortKeys();
        if ((sortKeys != null) && (sortKeys[0] != null)) {
            TableColumn[] columns = this.table.getColumns();
            int index = sortKeys[0].index;
            TableColumn headerColumn = null;
            for(TableColumn c:columns){
                if(((Number)c.getData()).intValue() == index){
                    headerColumn = c;
                    break;
                }
            }
            if(headerColumn == null){
                return;
            }
            boolean orderflg = sortKeys[0].order;
            if (orderflg) {
                this.table.setSortColumn(headerColumn);
                this.table.setSortDirection(SWT.UP);
            } else {
                this.table.setSortColumn(headerColumn);
                this.table.setSortDirection(SWT.DOWN);
            }
        }
    }

    /**表示設定に合わせてヘッダとか削ったりする*/
    private void resetTableHeader(){
        this.table.setRedraw(false);
        this.setTableHeader();
        this.disposeTableBody();
        this.setTableBody();
        this.restoreColumnWidth(false);
        this.setSortDirectionToHeader();
        this.table.setRedraw(true);
    }

    /** １列だけ操作する */
    @Override
    protected void setColumnVisible(int index, boolean visible) {
        this.resetTableHeader();
    }

    /** まとめて変更する */
    @Override
    public void setColumnVisible(boolean[] visibles) {
        this.table.setRedraw(false);
        boolean[] old = this.getConfig().getVisibleColumn();
        for (int i = 0; i < old.length; i++) {
            old[i] = visibles[i];
        }
        this.resetTableHeader();
        this.table.setRedraw(true);
    }

    /**
     * テーブルヘッダーの幅を復元する
     */
    @Override
    protected void restoreColumnWidth(boolean resetAll) {
        //不可視のはテーブルから消すようにした
        int[] widths = this.getConfig().getColumnWidth();
        TableColumn[] columns = this.table.getColumns();
        for (int i = 0; i < columns.length; i++) {
            int index = ((Number)columns[i].getData()).intValue();
            if ((widths[index] < 5) || resetAll) {
                columns[i].pack();
            }
            else {
                columns[i].setWidth(widths[index]);
            }
        }
    }

}
