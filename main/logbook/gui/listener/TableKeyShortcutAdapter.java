package logbook.gui.listener;

import logbook.config.bean.TableConfigBean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Table;

/**
 * テーブルウィジェットのキー操作のアダプターです
 *
 */
public final class TableKeyShortcutAdapter extends KeyAdapter {

    /** テーブルヘッダー */
    private final String[] header;

    /** テーブル */
    private final Table table;

    /** 設定 */
    private final TableConfigBean config;

    /**
     * コンストラクター
     */
    public TableKeyShortcutAdapter(String[] header, Table table, TableConfigBean config) {
        this.header = header;
        this.table = table;
        this.config = config;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if ((e.stateMask == SWT.CTRL) && (e.keyCode == 'c')) {
            TableToClipboardAdapter.copyTable(null, this.table, this.config);
        }
        if ((e.stateMask == SWT.CTRL) && (e.keyCode == 'a')) {
            this.table.selectAll();
        }
    }
}
