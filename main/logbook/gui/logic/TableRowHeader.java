/**
 * 
 */
package logbook.gui.logic;

/**
 * @author Nekopanda
 * データを埋め込んだ番号
 */
public class TableRowHeader extends TableCell {
    private final Object data;

    public TableRowHeader(int number, Object data) {
        super(number, number);
        this.data = data;
    }

    public Object get() {
        return this.data;
    }
}
