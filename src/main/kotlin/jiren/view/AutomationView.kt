package jiren.view

import com.vaadin.flow.component.Unit
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import com.vaadin.flow.component.datetimepicker.DateTimePicker
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
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.validator.BeanValidator
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import jiren.controller.AutomationController
import jiren.controller.DatabaseController
import jiren.controller.UserController
import jiren.data.entity.Automation
import jiren.data.enum.Privileges
import jiren.data.enum.SGBD
import jiren.data.enum.StatusAutomation
import java.util.*
import javax.annotation.PostConstruct

@PageTitle("Automações")
@Route(value = "routines", layout = MainLayout::class)
@SpringComponent
@UIScope
class AutomationView(
    private val automationController: AutomationController,
    private val userController: UserController,
    databaseController: DatabaseController
) : VerticalLayout() {
    private val table = Grid(Automation::class.java, true)
    private val form = FormLayout()
    private val databaseOptions = databaseController.automationDatabaseOptions().toMutableList()
    private val notificationPosition = Notification.Position.TOP_END
    private var modal = Dialog()
    private var binder = Binder(Automation::class.java)
    private var automation = Automation()

    @PostConstruct
    fun init() {
        this.databaseOptions.forEach { option -> if(option.sgbd == SGBD.MongoDb) databaseOptions.remove(option) }
        this.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        this.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        this.style["text-align"] = "center"
        this.add(createSearch(), createMenu(), createTable(), createModal())
        this.setSizeFull()
    }

    private fun createTable(): Scroller {
        this.table.addItemClickListener {
            automation = it.item
            modal.open()
            binder.readBean(automation)
        }
        this.table.setColumns("name", "schedule", "ranAt", "scheduleConfig", "status", "database")
        this.table.columns[0].setHeader("Nome")
        this.table.columns[1].setHeader("Próxima execução")
        this.table.columns[2].setHeader("Última execução")
        this.table.columns[3].setHeader("Intervalo de Execução")
        this.table.columns[4].setHeader("Status")
        this.table.columns[5].setHeader("Banco de Dados")
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
            automation = Automation()
            binder.readBean(automation)
            modal.open()
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


        val statusBox = ComboBox("Status", EnumSet.allOf(StatusAutomation::class.java))
        val sysBox = ComboBox("Banco de Dados", databaseOptions)
        val boxGroup = FormLayout()
        boxGroup.add(statusBox, sysBox)
        boxGroup.setWidthFull()
        boxGroup.setResponsiveSteps(
            FormLayout.ResponsiveStep("0px", 1),
            FormLayout.ResponsiveStep("600px", 2)
        )

        val searchPanel = VerticalLayout()
        searchPanel.setWidthFull()
        searchPanel.add(btnGroup, boxGroup)
        searchPanel.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        searchPanel.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        searchPanel.isSpacing = false

        btnSearch.addClickListener {
            table.setItems(
                automationController.search(
                    searchField.value,
                    sysBox.value,
                    statusBox.value,
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
            .withValidator(BeanValidator(Automation::class.java, "name"))
            .bind(Automation::name, Automation::name.setter)
            .validate(true)
        this.form.add(name)

        val query = TextArea("Query")
        query.isRequiredIndicatorVisible = true
        this.binder.forField(query)
            .withValidator(BeanValidator(Automation::class.java, "query"))
            .bind(Automation::query, Automation::query.setter)
            .validate(true)
        this.form.add(query)

        val sysBox = ComboBox("Banco de Dados", databaseOptions)
        sysBox.isRequiredIndicatorVisible = true
        this.binder.forField(sysBox)
            .withValidator(BeanValidator(Automation::class.java, "database"))
            .bind(Automation::database, Automation::database.setter)
            .validate(true)
        this.form.add(sysBox)

        val active = Checkbox("Ativo")
        active.isRequiredIndicatorVisible = true
        this.binder.forField(active)
            .withValidator(BeanValidator(Automation::class.java, "active"))
            .bind(Automation::active, Automation::active.setter)
            .validate(true)
        this.form.add(active)

        val schedule = DateTimePicker("Agendamento")
        this.binder.forField(schedule)
            .withValidator(BeanValidator(Automation::class.java, "schedule"))
            .bind(Automation::getSchedule, Automation::setSchedule)
            .validate(true)
        this.form.add(schedule)

        val scheduleConfig = IntegerField("Intervalo em Minutos")
        this.binder.forField(scheduleConfig)
            .withValidator(BeanValidator(Automation::class.java, "scheduleConfig"))
            .bind(Automation::scheduleConfig, Automation::scheduleConfig.setter)
            .validate(true)
        this.form.add(scheduleConfig)

        val document = TextField("Confluence")
        this.binder.forField(document)
            .withValidator(BeanValidator(Automation::class.java, "documentUrl"))
            .bind(Automation::documentUrl, Automation::documentUrl.setter)
            .validate(true)
        this.form.add(document)

        val save = Button("Salvar", Icon("check-circle")) {
            try {
                if (!userController.loggedUserHasPermission("${Privileges.WRITE}")) throw Exception("Você não tem permissão")
                if (!binder.validate().hasErrors()) {
                    binder.writeBean(automation)
                    automationController.save(automation)
                    table.setItems(automationController.findByName(automation.name ?: ""))
                    modal.close()
                    Notification.show("Sucesso", 5000, notificationPosition)
                }
            } catch (e: Exception) {
                Notification.show(e.message, 5000, notificationPosition)
            }
        }

        val cancel = Button("Fechar", Icon("close-circle")) { modal.close() }

        val deleteConfirmationDialog = ConfirmDialog()
        deleteConfirmationDialog.setText("Confirma a exclusão ?")
        deleteConfirmationDialog.isCloseOnEsc = true
        deleteConfirmationDialog.setCancelButton("Cancelar") {
            deleteConfirmationDialog.close()
        }
        deleteConfirmationDialog.addConfirmListener {
            if (!userController.loggedUserHasPermission("${Privileges.DELETE}")) {
                Notification.show("Você não tem permissão", 5000, notificationPosition)
            } else {
                automationController.delete(automation)
                Notification.show("Sucesso", 5000, notificationPosition)
                modal.close()
            }
        }

        val delete = Button("Excluir", Icon("trash")) { deleteConfirmationDialog.open() }

        val execConfirmationDialog = ConfirmDialog()
        execConfirmationDialog.setText("Confirma a execução ?")
        execConfirmationDialog.isCloseOnEsc = true
        execConfirmationDialog.setCancelButton("Cancelar") {
            execConfirmationDialog.close()
        }
        execConfirmationDialog.addConfirmListener {
            if (!userController.loggedUserHasPermission("${Privileges.EXECUTE}")) throw Exception("Você não tem permissão")
            else {
                try {
                    automationController.execute(automation)
                    Notification.show("${automation.name} executado com sucesso", 5000, notificationPosition)
                } catch (e: Exception) {
                    Notification.show(
                        "${automation.name} executado com erros\n${e.message}",
                        5000,
                        notificationPosition
                    )
                }
                execConfirmationDialog.close()
            }
        }

        val execute = Button("Executar", Icon("play")) { execConfirmationDialog.open() }

        this.modal.addOpenedChangeListener {
            execute.isEnabled = (automation.id > 0 && !execConfirmationDialog.isOpened)
            delete.isEnabled = (automation.id > 0 && !deleteConfirmationDialog.isOpened)
        }

        val btnGrp = FormLayout(cancel, delete, save, execute)
        btnGrp.setResponsiveSteps(
            FormLayout.ResponsiveStep("0px", 2),
            FormLayout.ResponsiveStep("600px", 4)
        )
        val head = HorizontalLayout()
        head.add(H3("Automação"))
        head.setWidthFull()
        head.justifyContentMode = FlexComponent.JustifyContentMode.START
        head.defaultVerticalComponentAlignment = FlexComponent.Alignment.START
        head.isSpacing = false

        this.modal.add(head, form, btnGrp)
        this.modal.isResizable = true
        this.modal.isModal = true
        this.modal.isDraggable = true
        return modal
    }

}