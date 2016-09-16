package logbook.gui;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
}
