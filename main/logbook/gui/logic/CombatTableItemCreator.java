package logbook.gui.logic;


/**
 * 戦闘報告書のスタイルを作成するインターフェイスです
 */
public interface CombatTableItemCreator extends TableItemCreator {
    /**
     * タイトル作成時に呼び出されます。
     * @param defaultTitle デフォルトタイトル
     */
    String title(String defaultTitle);
}
