/**
 * 
 */
package logbook.gui.logic;

/**
 * @author Nekopanda
 * データを埋め込んだ番号
 */
public class TableRowHeader implements Comparable<TableRowHeader> {
    private final int number;
    private final Object data;
    private final Comparable comparable;

    public TableRowHeader(int number, Object data) {
        this.number = number;
        this.data = data;
        this.comparable = number;
    }

    public TableRowHeader(int number, Object data, Comparable comparable) {
        this.number = number;
        this.data = data;
        this.comparable = comparable;
    }

    public Object get() {
        return this.data;
    }

    public int getNumber() {
        return this.number;
    }

    @Override
    public String toString() {
        return String.valueOf(this.comparable);
    }

    @Override
    public int compareTo(TableRowHeader o) {
        return this.comparable.compareTo(o.comparable);
    }
}
