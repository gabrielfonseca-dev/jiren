package jiren.view

import com.vaadin.flow.component.Unit
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.Scroller
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.validator.BeanValidator
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import jiren.controller.DatabaseController
import jiren.controller.ScriptController
import jiren.controller.UserController
import jiren.data.entity.Automation
import jiren.data.entity.Script
import jiren.data.enum.Privileges
import jiren.data.enum.SGBD
import java.sql.Timestamp
import java.time.Instant.now
import java.util.*
import javax.annotation.PostConstruct

@PageTitle("Scripts")
@Route(value = "ti/scripts", layout = MainLayout::class)
@SpringComponent
@UIScope
class ScriptView(
    private val scriptController: ScriptController,
    private val userController: UserController,
    databaseController: DatabaseController
) : VerticalLayout() {
    private val table = Grid(Script::class.java, true)
    private var modal = Dialog()
    private val form = FormLayout()
    private var binder = Binder(Script::class.java)
    private var script = Script()
    private val databaseOptions = databaseController.scriptingDatabaseOptions().toMutableList()
    private val notificationPosition = Notification.Position.TOP_END

    @PostConstruct
    fun init() {
        this.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        this.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        this.style["text-align"] = "center"
        this.databaseOptions.forEach { option -> if(option.sgbd == SGBD.MongoDb) databaseOptions.remove(option) }
        this.add(createSearch(), createMenu(), createTable(), createModal())
        this.setSizeFull()
    }

    private fun createTable(): Scroller {
        this.table.addItemClickListener {
            this.script = it.item
            this.modal.open()
            this.binder.readBean(script)
        }
        this.table.setColumns("name", "description", "query", "active", "database")
        this.table.columns[0].setHeader("Nome")
        this.table.columns[1].setHeader("Descrição")
        this.table.columns[2].setHeader("Script")
        this.table.columns[3].setHeader("Ativo")
        this.table.columns[4].setHeader("BD")
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

    private fun createMenu(): HorizontalLayout {
        val newBtn = Button("Novo", Icon("plus"))
        newBtn.addClickListener {
            this.script = Script()
            this.script.created = Timestamp.from(now())
            this.binder.readBean(script)
            this.modal.open()
        }
        val btnLayout = HorizontalLayout()
        btnLayout.setWidthFull()
        btnLayout.add(newBtn)
        btnLayout.isSpacing = false
        btnLayout.justifyContentMode = FlexComponent.JustifyContentMode.END
        btnLayout.defaultVerticalComponentAlignment = FlexComponent.Alignment.END
        return btnLayout
    }

    private fun createSearch(): VerticalLayout {
        val searchField = TextField()
        searchField.placeholder = "Digite para buscar"
        val btnSearch = Button("Buscar", Icon("search"))
        val inactiveFilter = Checkbox("Inativo")
        val btnGroup = HorizontalLayout()
        btnGroup.add(searchField, btnSearch, inactiveFilter)
        btnGroup.defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        btnGroup.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        btnGroup.setWidthFull()


        val sysBox = ComboBox("Banco de Dados", databaseOptions)
        val boxGroup = FormLayout()
        boxGroup.add(sysBox)
        boxGroup.setWidthFull()
        boxGroup.setResponsiveSteps(
            FormLayout.ResponsiveStep("0px", 1),
            FormLayout.ResponsiveStep("600px", 1)
        )

        val searchPanel = VerticalLayout()
        searchPanel.setWidthFull()
        searchPanel.add(btnGroup, boxGroup)
        searchPanel.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        searchPanel.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        searchPanel.isSpacing = false

        btnSearch.addClickListener {
            this.table.setItems(
                scriptController.search(
                    searchField.value,
                    sysBox.value,
                    inactiveFilter.value
                )?.toList()
            )
        }
        return searchPanel
    }

    private fun createModal(): Dialog {
        val name = TextField("Nome")
        name.isRequiredIndicatorVisible = true
        this.binder.forField(name)
            .withValidator(BeanValidator(Script::class.java, "name"))
            .bind(Script::name, Script::name.setter)
            .validate(true)
        this.form.add(name)

        val description = TextField("Descrição")
        description.isRequiredIndicatorVisible = true
        this.binder.forField(description)
            .withValidator(BeanValidator(Script::class.java, "description"))
            .bind(Script::description, Script::description.setter)
            .validate(true)
        this.form.add(description)

        val query = TextArea("Query")
        query.isRequiredIndicatorVisible = true
        this.binder.forField(query)
            .withValidator(BeanValidator(Script::class.java, "query"))
            .bind(Script::query, Script::query.setter)
            .validate(true)
        this.form.add(query)

        val sysBox = ComboBox("Banco de Dados", databaseOptions)
        sysBox.isRequiredIndicatorVisible = true
        this.binder.forField(sysBox)
            .withValidator(BeanValidator(Automation::class.java, "database"))
            .bind(Script::database, Script::database.setter)
            .validate(true)
        this.form.add(sysBox)

        val active = Checkbox("Ativo")
        active.isRequiredIndicatorVisible = true
        this.binder.forField(active)
            .withValidator(BeanValidator(Script::class.java, "active"))
            .bind(Script::active, Script::active.setter)
            .validate(true)
        this.form.add(active)

        val save = Button("Salvar", Icon("check-circle")) {
            try {
                if (!this.userController.loggedUserHasPermission("${Privileges.WRITE}")) throw Exception("Você não tem permissão")
                if (!this.binder.validate().hasErrors()) {
                    this.binder.writeBean(script)
                    this.scriptController.save(script)
                    this.table.setItems(this.scriptController.findByName(this.script.name ?: ""))
                    this.modal.close()
                    Notification.show("Sucesso", 5000, this.notificationPosition)
                }
            } catch (e: Exception) {
                Notification.show(e.message, 5000, this.notificationPosition)
            }
        }

        val cancel = Button("Fechar", Icon("close-circle")) { modal.close() }

        val delConfirmDialog = ConfirmDialog()
        delConfirmDialog.setText("Confirma a exclusão ?")
        delConfirmDialog.isCloseOnEsc = true
        delConfirmDialog.setCancelButton("Cancelar") {
            delConfirmDialog.close()
        }
        delConfirmDialog.addConfirmListener {
            if (!this.userController.loggedUserHasPermission("${Privileges.DELETE}")) {
                Notification.show("Você não tem permissão", 5000, this.notificationPosition)
            } else {
                this.scriptController.delete(this.script)
                Notification.show("Sucesso", 5000, this.notificationPosition)
                this.modal.close()
            }
        }

        val delete = Button("Excluir", Icon("trash")) { delConfirmDialog.open() }

        this.modal.addOpenedChangeListener {
            delete.isEnabled = (this.script.id > 0 && !delConfirmDialog.isOpened)
        }

        val btnGrp = FormLayout(cancel, delete, save)
        btnGrp.setResponsiveSteps(
            FormLayout.ResponsiveStep("0px", 2),
            FormLayout.ResponsiveStep("600px", 4)
        )
        val head = HorizontalLayout()
        head.add(H3("Scripts"))
        head.setWidthFull()
        head.justifyContentMode = FlexComponent.JustifyContentMode.START
        head.defaultVerticalComponentAlignment = FlexComponent.Alignment.START
        head.isSpacing = false

        this.modal.add(head, form, btnGrp)
        this.modal.isResizable = true
        this.modal.isModal = true
        this.modal.isDraggable = true
        return this.modal
    }

}