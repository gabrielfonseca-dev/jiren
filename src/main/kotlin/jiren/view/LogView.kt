package jiren.view

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.Unit
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.Scroller
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import jiren.controller.LogController
import jiren.data.entity.Log
import jiren.service.util.CSVParser
import org.springframework.data.domain.Pageable
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
    val searchField = TextField()
    private var list: MutableList<Log>? = null
    private var page: Pageable? = null
    private var totalPages: Int = 0

    @PostConstruct
    fun init() {
        this.isSpacing = false
        this.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        this.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        this.style["text-align"] = "center"
        this.add(createSearch(), createTable(), createPagination(), createModal())
        this.setSizeFull()
    }

    private fun createPagination(): HorizontalLayout {
        val pageLabel = Label("${page?.pageNumber ?: ""}")

        val btnBefore = Button("Anterior", Icon(VaadinIcon.ARROW_LEFT)) {
            val pg = logController.search(searchField.value, (page?.pageNumber ?: 0) - 1)
            page = pg.pageable
            list = pg.toList()
            totalPages = pg.totalPages
            this.table.setItems(list)
            UI.getCurrent().access {
                pageLabel.text = "${page?.pageNumber.let { if(it != null) it + 1 else "" } }"
            }
            UI.getCurrent().push()
        }
        btnBefore.isIconAfterText = true

        val btnNext = Button("Próximo", Icon(VaadinIcon.ARROW_RIGHT)) {
            if ((page?.pageNumber ?: 0) < (totalPages - 1)) {
                val pg = logController.search(searchField.value, (page?.pageNumber ?: 0) + 1)
                totalPages = pg.totalPages
                page = pg.pageable
                list = pg.toList()
                this.table.setItems(list)
                UI.getCurrent().access {
                    pageLabel.text = "${page?.pageNumber.let { if(it != null) it + 1 else "" } }"
                }
                UI.getCurrent().push()
            }
        }
        return HorizontalLayout(btnBefore, pageLabel, btnNext)
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
        val searchButton = Button("Buscar", Icon("search"))
        val download = Anchor(StreamResource("export.csv", InputStreamFactory { CSVParser().parse(list).inputStream() }), "")
        download.element.setAttribute("Exportar", true)
        download.add(Button(Icon(VaadinIcon.DOWNLOAD_ALT)))
        val searchPanel = HorizontalLayout()
        searchPanel.add(searchField, searchButton, download)
        searchPanel.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        searchPanel.defaultVerticalComponentAlignment = FlexComponent.Alignment.BASELINE
        searchButton.addClickListener {
            val pg = logController.search(searchField.value)
            totalPages = pg.totalPages
            page = pg.pageable
            list = pg.toList()
            this.table.setItems(list)
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