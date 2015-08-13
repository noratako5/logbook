///**
// * 
// */
//package logbook.gui.logic;
//
///**
// * @author Nekopanda
// * データを埋め込んだ番号
// */
//public class TableCell implements Comparable<TableCell> {
//    private final String value;
//    private final Comparable key;
//
//    public TableCell(Comparable entry) {
//        this(entry, entry);
//    }
//
//    public TableCell(Object value, Comparable key) {
//        this.value = String.valueOf(value);
//        this.key = key;
//    }
//
//    @Override
//    public String toString() {
//        return this.value;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (o instanceof TableCell) {
//            return false;
//        }
//        else {
//            return this.compareTo((TableCell) o) == 0;
//        }
//    }
//
//    @Override
//    public int hashCode() {
//        if (this.key == null) {
//            return 0;
//        }
//        else {
//            return this.key.hashCode();
//        }
//    }
//
//    @Override
//    public int compareTo(TableCell o) {
//        if (this.key == null) {
//            if (o.key == null) {
//                return 0;
//            }
//            else {
//                return +1;
//            }
//        }
//        else {
//            if (o.key == null) {
//                return -1;
//            }
//            else {
//                return this.key.compareTo(o.key);
//            }
//        }
//    }
//}
