package jiren.view

import com.vaadin.flow.component.Unit
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.Scroller
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import jiren.controller.LogController
import jiren.data.entity.Log
import javax.annotation.PostConstruct

@PageTitle("Logs")
@Route(value = "log", layout = MainLayout::class)
@SpringComponent
@UIScope
class LogView(
    private var logController: LogController
) : VerticalLayout() {
    private val table = Grid(Log::class.java)
    private var modal = Dialog()
    private var log = Log()

    @PostConstruct
    fun init() {
        this.isSpacing = false
        this.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        this.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        this.style["text-align"] = "center"
        this.add(createSearch(), createTable(), createModal())
        this.setSizeFull()
    }

    private fun createTable(): Scroller {
        this.table.addItemClickListener {
            log = it.item
            modal.open()
        }
        this.table.setColumns("user", "occurredAt", "elapsedTime", "code", "value", "script", "type", "task")
        this.table.columns[0].setHeader("Usuário")
        this.table.columns[1].setHeader("Data")
        this.table.columns[2].setHeader("Tempo (ms)")
        this.table.columns[3].setHeader("Código")
        this.table.columns[4].setHeader("Valor")
        this.table.columns[5].setHeader("script")
        this.table.columns[6].setHeader("Tipo")
        this.table.columns[7].setHeader("Task")
        this.table.setSelectionMode(Grid.SelectionMode.SINGLE)
        this.table.isRowsDraggable = true
        this.table.isColumnReorderingAllowed = true
        this.table.isVerticalScrollingEnabled = true
        this.table.columns.forEach { it.isResizable = true }
        this.table.setWidthFull()
        this.table.setHeight(95F, Unit.PERCENTAGE)
        val tableScroller = Scroller(table)
        tableScroller.setSizeFull()
        return tableScroller
    }

    private fun createSearch(): HorizontalLayout {
        val searchField = TextField()
        val searchButton = Button("Buscar", Icon("search"))
        val searchPanel = HorizontalLayout()
        searchPanel.add(searchField, searchButton)
        searchPanel.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        searchPanel.defaultVerticalComponentAlignment = FlexComponent.Alignment.BASELINE
        searchButton.addClickListener {
            this.table.setItems(logController.search(searchField.value)?.toList())
        }
        return searchPanel
    }

    private fun createModal(): Dialog {

        val code = TextField("Code")
        code.isReadOnly = true

        val tempo = TextField("Tempo")
        tempo.isReadOnly = true

        val data = TextField("Data")
        data.isReadOnly = true

        val type = TextField("Tipo")
        type.isReadOnly = true

        val script = TextField("Script")
        script.isReadOnly = true

        val task = TextField("Task")
        task.isReadOnly = true

        val user = TextField("Usuário")
        user.isReadOnly = true

        val value = TextField("Valor")
        value.isReadOnly = true


        this.modal.addOpenedChangeListener {
            user.value = log.user.toString()
            value.value = log.value.toString()
            code.value = log.code.toString()
            tempo.value = log.elapsedTime.toString()
            data.value = log.occurredAt.toString()
            type.value = log.type.toString()
            script.value = log.script.toString()
            task.value = log.task.toString()
        }

        val form = FormLayout(code, tempo, data, type, script, task, user, value)
        this.modal.add(Scroller(form))
        this.modal.isResizable = true
        this.modal.isModal = true
        this.modal.isDraggable = true
        return this.modal

    }

}

