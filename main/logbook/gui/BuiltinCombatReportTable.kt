package logbook.gui

import logbook.config.AppConfig
import logbook.config.bean.TableConfigBean
import logbook.gui.logic.CreateReportLogic
import logbook.gui.logic.TableItemCreator
import logbook.gui.logic.TableRowHeader
import logbook.scripting.BuiltinScriptFilter
import logbook.scripting.TableItemCreatorProxy
import org.apache.commons.lang3.ArrayUtils
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Point
import java.util.*
import logbook.builtinscript.HeaderWithKey
import logbook.dto.BattleResultDto
import logbook.internal.BattleResultFilter
import logbook.internal.BattleResultServer
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.events.SelectionListener
import org.eclipse.swt.widgets.*
import java.io.File
import java.io.IOException
import java.io.Serializable
import java.util.stream.Collectors
import kotlin.concurrent.thread

/**
 * 戦闘報告書

 */
class BuiltinCombatReportTable
/**
 * @param parent
 */
(parent: Shell, menuItem: MenuItem, prefix: String, private val defaultTitleMain: String) : DropReportTable(parent, menuItem) {
    private val MAX_COLUMN = 200
    private val key: String
    private val tableItemCreatorProxy: TableItemCreatorProxy
    private val titleMain: String
    private var version = 0
    private var bodyCache = mutableListOf<Array<Comparable<*>>>()
    private var subKey:String? = null
    private var builtinFilterWindow:BuiltinFilterWindow? = null
    init {
        this.tableItemCreatorProxy = TableItemCreatorProxy.get(prefix)
        this.titleMain = this.defaultTitleMain
        this.key = this.defaultTitleMain
    }

    override fun createContents() {
        super.createContents()
        //既存のCSV出力は列数制限下でどう動くかよくわからないので殺す
        this.filemenu.items.toList().filter { item -> item.text.contains("CSV") }.forEach { item -> item.dispose() }
        //代わりに列デフォで全部吐くのを追加
        val savecsv = MenuItem(this.filemenu, SWT.NONE)
        savecsv.text = "行数制限を無視し全ての列をCSVファイルに保存"
        savecsv.addSelectionListener(BuiltinScriptSaveSelectionAdapter(shell = this.shell,key = this.key,filter = this.filter))
        //列数制限下で可視列コピーの挙動が怪しいので殺す　全列コピーだけ残す
        this.tablemenu.items.toList().filter { item -> item.text.contains("可視列") }.forEach { item -> item.dispose() }

        if (false && !this.isNoMenubar) {
            // フィルターメニュー
            val filter = MenuItem(this.menubar, SWT.PUSH)
            filter.text = "組み込みフィルター"
            filter.addSelectionListener(object : SelectionAdapter() {
                override fun widgetSelected(e: SelectionEvent?) {
                    if(this@BuiltinCombatReportTable.builtinFilterWindow == null){
                        this@BuiltinCombatReportTable.builtinFilterWindow = BuiltinFilterWindow(this@BuiltinCombatReportTable)
                    }
                    this@BuiltinCombatReportTable.builtinFilterWindow!!.open()
                }
            })
        }
    }
    private inner class BuiltinScriptSaveSelectionAdapter(val shell: Shell,val key:String, val filter: BattleResultFilter ):SelectionAdapter(){
        override fun widgetSelected(e: SelectionEvent?) {
            super.widgetSelected(e)
            val dialog = FileDialog(this.shell, SWT.SAVE)
            dialog.fileName = this.key + ".csv"
            dialog.filterExtensions = arrayOf("*.csv")
            val filename = dialog.open()
            if(filename == null){ return }
            val file = File(filename)
            if (file.exists()) {
                val messageBox = MessageBox(this.shell, SWT.YES or SWT.NO)
                messageBox.text = "確認"
                messageBox.message = "指定されたファイルは存在します。\n上書きしますか？"
                if (messageBox.open() == SWT.NO) { return }
            }
            try {
                val battleResults = BattleResultServer.get().getFilteredList(this.filter).sortedByDescending { b->b.battleDate }
                val header = HeaderWithKey(key).toMutableList().run{ this.add(0,"No.");this }.toTypedArray()
                val tmp = ArrayList<BattleResultDto>()
                var append = false
                var index = 0
                for(item in battleResults) {
                    tmp.add(item)
                    if(tmp.size >= 500) {
                        val body = BattleResultServer.loadBuiltinBattleResultsBody(this.key, tmp, index)
                        index += body.size
                        CreateReportLogic.writeCsv(file, header, body, append)
                        append = true
                        tmp.clear()
                    }
                }
                val body = BattleResultServer.loadBuiltinBattleResultsBody(this.key, tmp, index)
                CreateReportLogic.writeCsv(file, header, body, append)
                tmp.clear()
            }
            catch (e: IOException) {
                val messageBox = MessageBox(this.shell, SWT.ICON_ERROR)
                messageBox.text = "書き込めませんでした"
                messageBox.message = e.toString()
                messageBox.open()
            }
        }
    }
    override fun open() {
        super.open()
        this.reloadTable()
    }

    /**
     * ウィンドウ識別ID（デフォルト実装はクラス名フルパス）
     * @return ウィンドウ識別ID
     */
    override fun getWindowId(): String {
        return this.javaClass.name + "/" + this.defaultTitleMain
    }

    override fun getTitleMain(): String {
        return this.titleMain
    }

    override fun getSize(): Point {
        return Point(600, 350)
    }

    override fun getTableHeader(): Array<String> {
        val header = HeaderWithKey(key).toMutableList()
        header.add(0,"No.")
        return header.toTypedArray()
    }

    override fun updateTableBody() {
        this.body = this.bodyCache.toList()
    }
    //キャッシュクリアしてBody読み込み
    private fun clearBodyCacheAndRestart(){
        this.version++
        this.bodyCache.clear()
        val result = mutableListOf<Array<Comparable<*>>>()
        val currentDisplay = Display.getCurrent()
        thread {
            val currentVersion = this.version
            val battleResults = BattleResultServer.get().getFilteredList(this.filter).sortedByDescending { b->b.battleDate }
            val limit = AppConfig.get().maxPrintItems
            val targets = ArrayList<BattleResultDto>()
            for (item in battleResults) {
                //データが存在する場合はとりあえず読む
                //基地航空非対応海域の基地航空ログだとかそういうのだけ飛ばせば十分
                val c = item.getBuiltinCombatDataRowCount(this.key)
                if (c == 0) { continue }
                targets.add(item)
            }
            val tmp = ArrayList<BattleResultDto>()
            for(item in targets) {
                if (this.version != currentVersion) { break }
                tmp.add(item)
                if(tmp.size >= limit/10 && tmp.size >= 100) {
                    result.addAll(BattleResultServer.loadBuiltinBattleResultsBody(this.key, tmp, result.size))
                    this.bodyCache = result
                    currentDisplay.syncExec {this.reloadTableWIthoutClearCache()  }
                    if (result.count() > limit) { break }
                    tmp.clear()
                }
            }
            if(this.version == currentVersion) {
                result.addAll(BattleResultServer.loadBuiltinBattleResultsBody(this.key,tmp,result.size))
                this.bodyCache = result
                currentDisplay.syncExec {this.reloadTableWIthoutClearCache()  }
            }
        }
    }

    override fun getTableItemCreator(): TableItemCreator {
        //return CreateReportLogic.DEFAULT_TABLE_ITEM_CREATOR;
        return this.tableItemCreatorProxy
    }

    override fun updateConfig() {

        val columns = this.config.columns
        var oldIds: Array<String>? = this.config.headerNames
        val oldVisibles = this.config.visibleColumn
        var oldWidth: IntArray? = this.config.columnWidth
        val oldOrder = this.config.columnOrder

        var oldLength = oldVisibles.size

        // 各カラムの位置
        val oldPos = IntArray(oldLength)
        for (i in 0..oldLength - 1) {
            oldPos[i] = i//諦めた
        }

        // 互換性維持
        if (oldWidth == null) {
            oldWidth = IntArray(oldLength)
        }
        if (oldIds == null) {
            // ヘッダー情報がない場合は今のヘッダーから作る
            oldLength = Math.min(oldLength, this.header.size)
            oldIds = ArrayUtils.subarray(this.headerId, 0, oldLength)
        }

        // pos順にする
        val oldColumns = arrayOfNulls<TableConfigBean.Column>(oldLength)
        for (i in 0..oldLength - 1) {
            oldColumns[i] = TableConfigBean.Column(oldIds!![i], oldVisibles[i], oldWidth[i], oldPos[i])
        }
        Arrays.sort<TableConfigBean.Column>(oldColumns, AbstractTableDialog.comparePosition)

        // columnsデータに追加
        var next = 0
        for (col in oldColumns) {
            val colm = columns[col!!.id]
            if (colm != null) {
                // 順番を維持するためposをすりあわせる
                next = Math.max(next, colm.pos)
            }
            col.pos = next++
            columns.put(col.id, col)
        }

        // columnsデータのpos番号を整理
        AbstractTableDialog.renumberColumnPosision(columns.values.toTypedArray())

        // 設定情報を引き継いだデータを作成
        val newColumns = arrayOfNulls<TableConfigBean.Column>(this.header.size)
        var nextNew = columns.size
        for (i in this.header.indices) {
            val id = this.headerId[i]
            val colm = columns[id]
            if (colm != null) {
                newColumns[i] = colm.clone()
            } else {
                newColumns[i] = TableConfigBean.Column(id, true, 0, nextNew++)
            }
        }

        // pos番号を整理
        AbstractTableDialog.renumberColumnPosision(newColumns.clone())

        // 完成したのでデータを戻す
        val visibles = BooleanArray(this.header.size)
        val columnWidth = IntArray(this.header.size)
        val columnOrder = IntArray(this.header.size)
        for (i in this.header.indices) {
            visibles[i] = newColumns[i]!!.visible
            columnWidth[i] = newColumns[i]!!.width
            columnOrder[newColumns[i]!!.pos] = i
        }

        this.config.columns = columns
        this.config.headerNames = this.headerId
        this.config.visibleColumn = visibles
        this.config.columnWidth = columnWidth
        this.config.columnOrder = columnOrder

        // sortOrderをチェック
        var sortKeys: Array<TableConfigBean.SortKey?>? = this.config.sortKeys
        if (sortKeys == null) {
            sortKeys = arrayOfNulls<TableConfigBean.SortKey>(3)
            this.config.sortKeys = sortKeys
        }
        for (i in sortKeys.indices) {
            if (sortKeys[i] != null) {
                if (sortKeys[i]!!.index >= this.header.size) {
                    // 超えてる
                    sortKeys[i] = null
                }
            }
        }
        this.config.sortKeys = sortKeys
    }

    /**
     * テーブルヘッダーをセットする
     */
    override fun setTableHeader() {
        while (this.table.columnCount > 0) {
            table.getColumn(0).dispose()
        }
        val visibles = this.getConfig().visibleColumn
        val listener = this.headerSelectionListener
        var counter = 0
        for (i in this.header.indices) {
            if (visibles[i] == false) {
                continue
            }
            if (counter == MAX_COLUMN) {
                break
            }
            counter++
            val col = TableColumn(this.table, SWT.LEFT)
            col.text = this.header[i]
            col.moveable = true
            col.addSelectionListener(listener)
            col.data = i // カラム番号を入れておく
            col.toolTipText = this.headerId[i]
            if (i == 0) {
                // No.列はソートしない
                col.setData("unsortable", Any())
            }
        }
        if (this.config.columnOrder != null) {
            this.setColumnOrder(this.config.columnOrder)
        }
    }

    override fun setColumnOrder(order: IntArray) {
        val columns = this.table.columns
        if (order.size == columns.size) {
            this.table.columnOrder = order
        }
    }

    override fun defaultColumnOrder(): IntArray {
        val columnOrder = IntArray(this.table.columnCount)
        for (i in columnOrder.indices) {
            columnOrder[i] = i
        }
        return columnOrder
    }

    override fun resetColumnOrder() {
        val columnOrder = this.defaultColumnOrder()
        if (this.config != null) {
            this.config.columnOrder = columnOrder
        }
        this.setColumnOrder(columnOrder)
    }

    /**
     * テーブルボディーをセットする
     */
    override fun setTableBody() {
        val visibles = this.getConfig().visibleColumn
        val creator = this.tableItemCreator
        creator.begin(this.tableHeader)
        // 表示最大件数を制限する
        val numPrintItems = Math.min(AppConfig.get().maxPrintItems, this.body.size)
        for (i in 0..numPrintItems - 1) {
            val line = this.body[i]
            val rowHeader = line[0] as TableRowHeader
            rowHeader.number = i + 1 // ソート順に関係ない番号
            val itemList = ArrayList<Comparable<*>>()
            var counter = 0
            var j = 0
            while (j < visibles.size && j < line.size) {
                if (counter == MAX_COLUMN) {
                    break
                }
                if (visibles[j]) {
                    itemList.add(line[j])
                    counter++
                }
                j++
            }
            val line2 = itemList.toTypedArray()
            val item = creator.create(this.table, line2, i)
        }
        creator.end()
    }

    override fun setSortDirectionToHeader() {
        val sortKeys = this.getConfig().sortKeys
        if (sortKeys != null && sortKeys[0] != null) {
            val columns = this.table.columns
            val index = sortKeys[0].index
            var headerColumn: TableColumn? = null
            for (c in columns) {
                if ((c.data as Number).toInt() == index) {
                    headerColumn = c
                    break
                }
            }
            if (headerColumn == null) {
                return
            }
            val orderflg = sortKeys[0].order
            if (orderflg) {
                this.table.sortColumn = headerColumn
                this.table.sortDirection = SWT.UP
            } else {
                this.table.sortColumn = headerColumn
                this.table.sortDirection = SWT.DOWN
            }
        }
    }

    /**表示設定に合わせてヘッダとか削ったりする */
    private fun resetTableHeader() {
        this.table.setRedraw(false)
        this.setTableHeader()
        this.disposeTableBody()
        this.setTableBody()
        this.restoreColumnWidth(false)
        this.setSortDirectionToHeader()
        this.table.setRedraw(true)
    }

    override fun reloadTable() {
        this.clearBodyCacheAndRestart()
        super.reloadTable()
    }
    /**
     * テーブルをリロードするがキャッシュ削除を呼ばない表示更新のみ
     */
    fun reloadTableWIthoutClearCache() {
        super.reloadTable()
    }
    /** １列だけ操作する  */
    override fun setColumnVisible(index: Int, visible: Boolean) {
        this.resetTableHeader()
    }

    /** まとめて変更する  */
    override fun setColumnVisible(visibles: BooleanArray) {
        this.table.setRedraw(false)
        val old = this.getConfig().visibleColumn
        for (i in old.indices) {
            old[i] = visibles[i]
        }
        this.resetTableHeader()
        this.table.setRedraw(true)
    }

    /**
     * テーブルヘッダーの幅を復元する
     */
    override fun restoreColumnWidth(resetAll: Boolean) {
        //不可視のはテーブルから消すようにした
        val widths = this.getConfig().columnWidth
        val columns = this.table.columns
        for (i in columns.indices) {
            val index = (columns[i].data as Number).toInt()
            if (widths[index] < 5 || resetAll) {
                columns[i].pack()
            } else {
                columns[i].width = widths[index]
            }
        }
    }

    //全行数を把握するコストが大きいので超過時はカウント諦める
    override fun getTitle(): String {
        var title = this.getTitleMain()
        if (this.body != null && this.table != null) {
            if (this.table.itemCount != this.body.size) {
                title += " |行数制限超過　" + this.table.itemCount + "件のみ表示"
            }
            val selectionCount = this.table.selectionCount
            if (selectionCount > 1) {
                title += " " + selectionCount + "件選択中"
            }
        }
        return title
    }

    override fun hideWindow() {
        //読み込みを止める
        this.version++;
        super.hideWindow()
    }


}
