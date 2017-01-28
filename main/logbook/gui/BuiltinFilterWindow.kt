package logbook.gui

import logbook.config.AppConfig
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.Text
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.custom.ScrolledComposite
import org.eclipse.swt.events.*
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Group

class BuiltinFilterWindow(val parent:BuiltinCombatReportTable):WindowBase(){
    var dateTimeGroup:Group? = null
    init {
        super.createContents(parent, SWT.CLOSE or SWT.TITLE or SWT.RESIZE, false)
    }

    override fun open() {
        if (!this.isWindowInitialized) {
            this.createContents()
            this.registerEvents()
            // 閉じたときに dispose しない
            this.shell.addShellListener(object : ShellAdapter() {
                override fun shellClosed(e: ShellEvent?) {
                    e!!.doit = false
                    this@BuiltinFilterWindow.visible = false
                }
            })
            this.isWindowInitialized = true
        }
        if (this.visible.not()) {
            this.shell.pack()
            this.visible = true
        }
        this.shell.setActive()
        return
    }
    private fun createContents() {
        this.shell.layout = FillLayout()
        val scroll = ScrolledComposite(this.shell, SWT.H_SCROLL or SWT.V_SCROLL)
        scroll.apply {
            layout = FillLayout()
            expandHorizontal = true
            expandVertical = true
            val composite = Composite(scroll,SWT.NONE).apply {
                layout = GridLayout(1,false)
                this@BuiltinFilterWindow.dateTimeGroup = Group(this,SWT.PUSH).apply {
                    text = "日時"
                    layout = GridLayout(4,false)
                    data = GridData(SWT.LEFT,SWT.CENTER,true,true,1,1)
                    Button(this,SWT.PUSH).apply {
                        text = "+"
                        data = GridData(SWT.LEFT,SWT.CENTER,true,true,2,1)
                        addSelectionListener(object:SelectionAdapter(){
                            override fun widgetSelected(e: SelectionEvent?) {
                            }
                        })
                    }
                    Button(this,SWT.PUSH).apply {
                        text = "-"
                        data = GridData(SWT.LEFT,SWT.CENTER,true,true,2,1)
                        addSelectionListener(object:SelectionAdapter(){
                            override fun widgetSelected(e: SelectionEvent?) {
                            }
                        })
                    }
                }
            }
            content = composite
            setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT))
        }
        this.shell.pack()
    }

}