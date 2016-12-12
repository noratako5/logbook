package logbook.gui.listener;

import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;

import logbook.config.bean.TableConfigBean;
import logbook.gui.AbstractTableDialog;

/**
 * テーブルをクリップボードにコピーするアダプターです
 *
 */
public final class TableToClipboardAdapter extends SelectionAdapter {

    /** テーブルヘッダー */
    private final String[] header;

    /**不可視列を含むボディ*/
    private final Supplier<List<Comparable[]>> body;

    /** テーブル */
    private final Table table;

    /** 設定 */
    private final TableConfigBean config;

    private final boolean all;

    /**
     * コンストラクター
     *
     * @param header ヘッダー
     * @param table テーブル
     */
    public TableToClipboardAdapter(String[] header,Supplier<List<Comparable[]>> body, Table table, TableConfigBean config, boolean all) {
        this.header = header;
        this.body = body;
        this.table = table;
        this.config = config;
        this.all = all;
    }

    @Override
    public void widgetSelected(SelectionEvent arg) {
        if(all){
            copyTableAll(this.header,this.body.get(),this.table);
        }else{
            copyTable(this.header, this.table, this.config);
        }
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
    /**
     * テーブルの選択されている行をヘッダー付きでクリップボードにコピーします
     *
     * @param header ヘッダー
     * @param table テーブル
     * @param config 設定
     */
    public static void copyTableAll(String[] header,List<Comparable[]> body, Table table) {
        int[] selected = table.getSelectionIndices();
        StringBuilder sb = new StringBuilder();
        if (header != null) {
            sb.append(StringUtils.join(header, "\t"));
            sb.append("\r\n");
        }
        if (body != null) {
            for (int i : selected) {
                sb.append(StringUtils.join(body.get(i), "\t"));
                sb.append("\r\n");
            }
        }
        Clipboard clipboard = new Clipboard(Display.getDefault());
        clipboard.setContents(new Object[] { sb.toString() }, new Transfer[] { TextTransfer.getInstance() });
    }
}
