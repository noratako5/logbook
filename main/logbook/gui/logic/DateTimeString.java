/**
 *
 */
package logbook.gui.logic;

import java.util.Date;

import org.apache.commons.lang3.time.FastDateFormat;

import logbook.constants.AppConstants;

/**
 * アプリケーションのフォーマットで文字列化可能な日時
 * @author Nekopanda
 */
public class DateTimeString implements Comparable<DateTimeString> {
    private static FastDateFormat format = FastDateFormat.getInstance(AppConstants.DATE_FORMAT);
    private final Date date;

    public DateTimeString(Date date) {
        if (date == null) {
            throw new NullPointerException();
        }
        this.date = date;
    }

    @Override
    public String toString() {
        return format.format(this.date);
    }

    public static String toString(Date date){
        return format.format(date);
    }

    @Override
    public int compareTo(DateTimeString o) {
        return Long.compare(this.date.getTime(), o.date.getTime());
    }
}
