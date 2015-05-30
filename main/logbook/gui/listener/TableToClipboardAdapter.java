package logbook.gui.listener;

import logbook.config.bean.TableConfigBean;
import logbook.gui.AbstractTableDialog;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;

/**
 * テーブルをクリップボードにコピーするアダプターです
 *
 */
public final class TableToClipboardAdapter extends SelectionAdapter {

    /** テーブルヘッダー */
    private final String[] header;

    /** テーブル */
    private final Table table;

    /** 設定 */
    private final TableConfigBean config;

    /**
     * コンストラクター
     * 
     * @param header ヘッダー
     * @param table テーブル
     */
    public TableToClipboardAdapter(String[] header, Table table, TableConfigBean config) {
        this.header = header;
        this.table = table;
        this.config = config;
    }

    @Override
    public void widgetSelected(SelectionEvent arg) {
        copyTable(this.header, this.table, this.config);
    }

    /**
     * テーブルの選択されている部分をヘッダー付きでクリップボードにコピーします
     * 
     * @param header ヘッダー
     * @param table テーブル
     * @param config 設定
     */
    public static void copyTable(String[] header, Table table, TableConfigBean config) {
        AbstractTableDialog.TextTable textTable = AbstractTableDialog.getTextTable(header, table, table.getSelection(),
                config);
        StringBuilder sb = new StringBuilder();
        if (textTable.getHeader() != null) {
            sb.append(StringUtils.join(textTable.getHeader(), "\t"));
            sb.append("\r\n");
        }
        if (textTable.getBody() != null) {
            for (Comparable[] textRow : textTable.getBody()) {
                sb.append(StringUtils.join(textRow, "\t"));
                sb.append("\r\n");
            }
        }
        Clipboard clipboard = new Clipboard(Display.getDefault());
        clipboard.setContents(new Object[] { sb.toString() }, new Transfer[] { TextTransfer.getInstance() });
    }
}
