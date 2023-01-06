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
import jiren.controller.DatabaseController
import jiren.controller.MonitoringController
import jiren.controller.UserController
import jiren.data.entity.Monitoring
import jiren.data.enum.MonitoringType
import jiren.data.enum.Privileges
import jiren.data.enum.StatusMonitoring
import org.springframework.core.io.ClassPathResource
import java.io.InputStreamReader
import java.util.*
import javax.annotation.PostConstruct

@PageTitle("Monitoramento")
@Route(value = "/monitor", layout = MainLayout::class)
@SpringComponent
@UIScope
class MonitoringView(
    private val monitoringController: MonitoringController,
    private val userController: UserController,
    databaseController: DatabaseController
) : VerticalLayout() {
    private val table = Grid(Monitoring::class.java, true)
    private val modal = Dialog()
    private val form = FormLayout()
    private val databaseOptions = databaseController.monitoringDatabaseOptions()
    private val notificationPosition = Notification.Position.TOP_END
    private var binder = Binder(Monitoring::class.java)
    private var monitoring = Monitoring()

    @PostConstruct
    private fun init() {
        this.add(createSearch(), createMenu(), createTable(), createModal())
        this.setSizeFull()
        this.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        this.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        this.style["text-align"] = "center"
    }

    private fun createTable(): Scroller {
        this.table.addItemClickListener {
            monitoring = it.item
            modal.open()
            binder.readBean(monitoring)
        }
        this.table.setColumns(
            "name",
            "type",
            "executionInterval",
            "scheduleAt",
            "ranAt",
            "status",
            "firstReport",
            "lastReport",
            "errorCount",
            "documentURL"
        )
        this.table.columns[0].setHeader("Nome")
        this.table.columns[1].setHeader("Tipo")
        this.table.columns[2].setHeader("Intervalo de Execução")
        this.table.columns[3].setHeader("Próxima Execução")
        this.table.columns[4].setHeader("Última execução")
        this.table.columns[5].setHeader("Status")
        this.table.columns[6].setHeader("Primeiro Report")
        this.table.columns[7].setHeader("Último Report")
        this.table.columns[8].setHeader("Reports")
        this.table.columns[9].setHeader("Docs")
        this.table.setSelectionMode(Grid.SelectionMode.SINGLE)
        this.table.isRowsDraggable = true
        this.table.isColumnReorderingAllowed = true
        this.table.isVerticalScrollingEnabled = true
        this.table.setWidthFull()
        this.table.setHeight(95F, Unit.PERCENTAGE)
        this.table.columns.forEach { it.isResizable = true }
        val tableScroller = Scroller(table)
        tableScroller.setSizeFull()
        return tableScroller
    }

    private fun createMenu(): HorizontalLayout {
        val newMonitoring = Button("Novo", Icon("plus"))
        newMonitoring.addClickListener {
            this.monitoring = Monitoring()
            this.binder.readBean(monitoring)
            this.modal.open()
        }
        val btnLayout = HorizontalLayout()
        btnLayout.setWidthFull()
        btnLayout.add(newMonitoring)
        btnLayout.justifyContentMode = FlexComponent.JustifyContentMode.END
        btnLayout.defaultVerticalComponentAlignment = FlexComponent.Alignment.END
        return btnLayout
    }

    private fun createSearch(): VerticalLayout {
        val searchField = TextField()
        searchField.placeholder = "Digite para buscar"
        val btnSearch = Button("Buscar", Icon("search"))
        val inactiveFilter = Checkbox("Inativo")
        val btnGroup = FormLayout()
        val optionsGroup = HorizontalLayout(btnSearch, HorizontalLayout(inactiveFilter))
        optionsGroup.defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        btnGroup.add(searchField, optionsGroup)

        val statusBox = ComboBox("Status", EnumSet.allOf(StatusMonitoring::class.java))
        val sysBox = ComboBox("Banco de Dados", databaseOptions)
        val typeBox = ComboBox("Tipo", EnumSet.allOf(MonitoringType::class.java))
        val boxGroup = FormLayout()
        boxGroup.setResponsiveSteps(
            FormLayout.ResponsiveStep("0px", 3), FormLayout.ResponsiveStep("600px", 3)
        )
        boxGroup.add(statusBox, sysBox, typeBox)

        val searchPanel = VerticalLayout()
        searchPanel.add(btnGroup, boxGroup)
        searchPanel.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        searchPanel.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        searchPanel.isSpacing = false

        btnSearch.addClickListener {
            this.table.setItems(
                this.monitoringController.search(
                    searchField.value, sysBox.value, typeBox.value, statusBox.value, inactiveFilter.value
                )?.toList()
            )
        }
        return searchPanel
    }

    private fun createModal(): Dialog {
        val nameField = TextField("Nome")
        nameField.isRequiredIndicatorVisible = true
        this.binder.forField(nameField).withValidator(BeanValidator(Monitoring::class.java, "name"))
            .bind(Monitoring::name, Monitoring::name.setter).validate(true)
        this.form.add(nameField)

        val titleField = TextField("Título")
        titleField.isRequiredIndicatorVisible = true
        this.binder.forField(titleField).withValidator(BeanValidator(Monitoring::class.java, "description"))
            .bind(Monitoring::description, Monitoring::description.setter).validate(true)
        this.form.add(titleField)

        val documentUrlField = TextField("Confluence")
        this.binder.forField(documentUrlField).withValidator(BeanValidator(Monitoring::class.java, "documentURL"))
            .bind(Monitoring::documentURL, Monitoring::documentURL.setter).validate(true)
        this.form.add(documentUrlField)

        val sqlSystemField = ComboBox("Banco de Dados", databaseOptions)
        sqlSystemField.isRequiredIndicatorVisible = true
        sqlSystemField.isVisible = false
        this.binder.forField(sqlSystemField).withValidator(BeanValidator(Monitoring::class.java, "databaseOne"))
            .bind(Monitoring::databaseOne, Monitoring::databaseOne.setter).validate(true)
        this.form.add(sqlSystemField)

        val cmdField = TextArea("SQL")
        cmdField.isRequiredIndicatorVisible = true
        cmdField.isVisible = false
        this.binder.forField(cmdField).withValidator(BeanValidator(Monitoring::class.java, "queryOne"))
            .bind(Monitoring::queryOne, Monitoring::queryOne.setter).validate(true)
        this.form.add(cmdField)

        val sqlSystemField2 = ComboBox("Banco de Dados 2", databaseOptions)
        sqlSystemField2.isRequiredIndicatorVisible = true
        sqlSystemField2.isVisible = false
        this.binder.forField(sqlSystemField2).withValidator(BeanValidator(Monitoring::class.java, "databaseTwo"))
            .bind(Monitoring::databaseTwo, Monitoring::databaseTwo.setter).validate(true)
        this.form.add(sqlSystemField2)

        val cmdField2 = TextArea("SQL 2")
        cmdField2.isRequiredIndicatorVisible = true
        cmdField2.isVisible = false
        this.binder.forField(cmdField2).withValidator(BeanValidator(Monitoring::class.java, "queryTwo"))
            .bind(Monitoring::queryTwo, Monitoring::queryTwo.setter).validate(true)
        this.form.add(cmdField2)

        val satConfig = TextArea("SAT Config")
        satConfig.isRequiredIndicatorVisible = true
        satConfig.isVisible = false
        this.binder.forField(satConfig).withValidator(BeanValidator(Monitoring::class.java, "satConfig"))
            .bind(Monitoring::satConfig, Monitoring::satConfig.setter).validate(true)
        this.form.add(satConfig)

        val httpConfig = TextArea("HTTP Config")
        httpConfig.isRequiredIndicatorVisible = true
        httpConfig.isVisible = false
        this.binder.forField(httpConfig).withValidator(BeanValidator(Monitoring::class.java, "httpConfig"))
            .bind(Monitoring::httpConfig, Monitoring::httpConfig.setter).validate(true)
        this.form.add(httpConfig)

        val sqsConfig = TextArea("SQS Config")
        sqsConfig.isRequiredIndicatorVisible = true
        sqsConfig.isVisible = false
        this.binder.forField(sqsConfig).withValidator(BeanValidator(Monitoring::class.java, "sqsConfig"))
            .bind(Monitoring::sqsConfig, Monitoring::sqsConfig.setter).validate(true)
        this.form.add(sqsConfig)

        val cwConfig = TextArea("CloudWatch Config")
        cwConfig.isRequiredIndicatorVisible = true
        cwConfig.isVisible = false
        this.binder.forField(cwConfig).withValidator(BeanValidator(Monitoring::class.java, "cwConfig"))
            .bind(Monitoring::cwConfig, Monitoring::cwConfig.setter).validate(true)
        this.form.add(cwConfig)

        val typeField = ComboBox("Tipo", EnumSet.allOf(MonitoringType::class.java))
        typeField.isRequiredIndicatorVisible = true
        this.binder.forField(typeField).withValidator(BeanValidator(Monitoring::class.java, "type"))
            .bind(Monitoring::type, Monitoring::type.setter).validate(true)
        this.form.add(typeField)

        val scheduleConfigField = IntegerField("Intervalo em Minutos")
        scheduleConfigField.isRequiredIndicatorVisible = true
        this.binder.forField(scheduleConfigField).withValidator(BeanValidator(Monitoring::class.java, "executionInterval"))
            .bind(Monitoring::executionInterval, Monitoring::executionInterval.setter).validate(true)
        this.form.add(scheduleConfigField)

        val scheduleField = DateTimePicker("Agendamento")
        scheduleField.isRequiredIndicatorVisible = true
        this.binder.forField(scheduleField).withValidator(BeanValidator(Monitoring::class.java, "scheduleAt"))
            .bind(Monitoring::getScheduleAt, Monitoring::setScheduleAt).validate()
        this.form.add(scheduleField)

        val showInPanelField = Checkbox("Painel")
        this.binder.forField(showInPanelField).withValidator(BeanValidator(Monitoring::class.java, "showInPanel"))
            .bind(Monitoring::showInPanel, Monitoring::showInPanel.setter).validate(true)

        val sendMailField = Checkbox("E-Mail", false)
        this.binder.forField(sendMailField).withValidator(BeanValidator(Monitoring::class.java, "emailNotification"))
            .bind(Monitoring::emailNotification, Monitoring::emailNotification.setter).validate(true)

        val rocketChatField = Checkbox("RocketChat", false)
        rocketChatField.value = true
        this.binder.forField(rocketChatField).withValidator(BeanValidator(Monitoring::class.java, "rocketNotification"))
            .bind(Monitoring::rocketNotification, Monitoring::rocketNotification.setter).validate(true)

        val jiraNotificationField = Checkbox("Jira", false)
        jiraNotificationField.value = true
        this.binder.forField(jiraNotificationField).withValidator(BeanValidator(Monitoring::class.java, "jiraNotification"))
            .bind(Monitoring::jiraNotification, Monitoring::jiraNotification.setter).validate(true)

        val activeField = Checkbox("Ativo", false)
        binder.forField(activeField).withValidator(BeanValidator(Monitoring::class.java, "enabled"))
            .bind(Monitoring::enabled, Monitoring::enabled.setter).validate(true)

        val checkBoxGroup = FormLayout(
            showInPanelField,
            sendMailField,
            rocketChatField,
            jiraNotificationField,
            activeField
        )
        checkBoxGroup.setResponsiveSteps(
            FormLayout.ResponsiveStep("0px", 2), FormLayout.ResponsiveStep("600px", 4)
        )
        this.form.add(checkBoxGroup)

        val mailToField = TextField("Destinatários", "email@email,email2@email")
        mailToField.isVisible = sendMailField.value
        this.binder.forField(mailToField).bind(Monitoring::mailTo, Monitoring::mailTo.setter)
        this.form.add(mailToField)

        val rocketChatRoomField = TextField("Rocketchat RoomID")
        this.binder.forField(rocketChatRoomField).bind(Monitoring::rocketchatRoom, Monitoring::rocketchatRoom.setter)
        this.form.add(rocketChatRoomField)

        typeField.addValueChangeListener {
            val isComparison = (typeField.value == MonitoringType.DATABASE_COMPARISON)

            if (isComparison) {
                sqlSystemField.isVisible = true
                sqlSystemField2.isVisible = true
                cmdField.isVisible = true
                cmdField2.isVisible = true
                this.binder.forField(sqlSystemField).asRequired("Campo obrigatório")
                    .bind(Monitoring::databaseOne, Monitoring::databaseOne.setter)
                this.binder.forField(cmdField).asRequired("Campo obrigatório")
                    .bind(Monitoring::queryOne, Monitoring::queryOne.setter)
                this.binder.forField(sqlSystemField2).asRequired("Campo obrigatório")
                    .bind(Monitoring::databaseTwo, Monitoring::databaseTwo.setter)
                this.binder.forField(cmdField2).asRequired("Campo obrigatório")
                    .bind(Monitoring::queryTwo, Monitoring::queryTwo.setter)
                cmdField.isRequiredIndicatorVisible = true
                cmdField2.isRequiredIndicatorVisible = true
                sqlSystemField.isRequiredIndicatorVisible = true
                sqlSystemField2.isRequiredIndicatorVisible = true
            } else {
                sqlSystemField.isVisible = false
                sqlSystemField2.isVisible = false
                cmdField.isVisible = false
                cmdField2.isVisible = false
                this.binder.forField(sqlSystemField).bind(Monitoring::databaseOne, Monitoring::databaseOne.setter)
                this.binder.forField(cmdField).bind(Monitoring::queryOne, Monitoring::queryOne.setter)
                this.binder.forField(sqlSystemField2).bind(Monitoring::databaseTwo, Monitoring::databaseTwo.setter)
                this.binder.forField(cmdField2).bind(Monitoring::queryTwo, Monitoring::queryTwo.setter)
                cmdField.isRequiredIndicatorVisible = false
                cmdField2.isRequiredIndicatorVisible = false
                sqlSystemField.isRequiredIndicatorVisible = false
                sqlSystemField2.isRequiredIndicatorVisible = false
            }

            val isSQL = (typeField.value == MonitoringType.DATABASE)

            if (isSQL) {
                sqlSystemField.isVisible = true
                cmdField.isVisible = true
                this.binder.forField(sqlSystemField).asRequired("Campo obrigatório").bind(Monitoring::databaseOne, Monitoring::databaseOne.setter)
                this.binder.forField(cmdField).asRequired("Campo obrigatório").bind(Monitoring::queryOne, Monitoring::queryOne.setter)
                cmdField.isRequiredIndicatorVisible = false
            } else if (!isComparison) {
                sqlSystemField.isVisible = false
                cmdField.isVisible = false
                this.binder.forField(sqlSystemField).bind(Monitoring::databaseOne, Monitoring::databaseOne.setter)
                this.binder.forField(cmdField).bind(Monitoring::queryOne, Monitoring::queryOne.setter)
                cmdField.isRequiredIndicatorVisible = true
            }

            val isHTTP = (typeField.value == MonitoringType.HTTP)

            if (isHTTP) {
                httpConfig.isVisible = true
                this.binder.forField(httpConfig).asRequired("Campo obrigatório")
                    .bind(Monitoring::httpConfig, Monitoring::httpConfig.setter)
                httpConfig.isRequiredIndicatorVisible = false
            } else {
                httpConfig.isVisible = false
                this.binder.forField(httpConfig)
                    .bind(Monitoring::httpConfig, Monitoring::httpConfig.setter)
                httpConfig.isRequiredIndicatorVisible = true
            }

            val isSAT = (typeField.value == MonitoringType.SAT)

            if (isSAT) {
                satConfig.isVisible = true
                this.binder.forField(satConfig).asRequired("Campo obrigatório")
                    .bind(Monitoring::satConfig, Monitoring::satConfig.setter)
                satConfig.isRequiredIndicatorVisible = false
            } else {
                satConfig.isVisible = false
                this.binder.forField(satConfig)
                    .bind(Monitoring::satConfig, Monitoring::satConfig.setter)
                satConfig.isRequiredIndicatorVisible = true
            }

            val isSQS = (typeField.value == MonitoringType.SQS)

            if (isSQS) {
                sqsConfig.isVisible = true
                this.binder.forField(sqsConfig).asRequired("Campo obrigatório")
                    .bind(Monitoring::sqsConfig, Monitoring::sqsConfig.setter)
                sqsConfig.isRequiredIndicatorVisible = false
            } else {
                sqsConfig.isVisible = false
                this.binder.forField(sqsConfig)
                    .bind(Monitoring::sqsConfig, Monitoring::sqsConfig.setter)
                sqsConfig.isRequiredIndicatorVisible = true
            }

            val isCW = (typeField.value == MonitoringType.CLOUDWATCH)

            if (isCW) {
                cwConfig.isVisible = true
                this.binder.forField(cwConfig).asRequired("Campo obrigatório")
                    .bind(Monitoring::cwConfig, Monitoring::cwConfig.setter)
                cwConfig.isRequiredIndicatorVisible = false
            } else {
                cwConfig.isVisible = false
                this.binder.forField(cwConfig)
                    .bind(Monitoring::cwConfig, Monitoring::cwConfig.setter)
                cwConfig.isRequiredIndicatorVisible = true
            }

            val httpTemplate = InputStreamReader(ClassPathResource("META-INF/resources/templates/http-template.json").inputStream).readText()
            httpConfig.value = httpTemplate
            val satTemplate = InputStreamReader(ClassPathResource("META-INF/resources/templates/sat-template.json").inputStream).readText()
            satConfig.value = satTemplate
            val sqsTemplate = InputStreamReader(ClassPathResource("META-INF/resources/templates/sqs-template.json").inputStream).readText()
            sqsConfig.value = sqsTemplate
            val cwTemplate = InputStreamReader(ClassPathResource("META-INF/resources/templates/cw-template.json").inputStream).readText()
            cwConfig.value = cwTemplate

        }

        sendMailField.addValueChangeListener {
            val isMailActive = sendMailField.value
            mailToField.isVisible = isMailActive
            mailToField.isRequired = isMailActive
            if (isMailActive) {
                this.binder.forField(mailToField).asRequired("Campo obrigatório")
                    .bind(Monitoring::mailTo, Monitoring::mailTo.setter).validate(true)
            } else {
                this.binder.forField(mailToField).bind(Monitoring::mailTo, Monitoring::mailTo.setter)
            }
        }

        rocketChatField.addValueChangeListener {
            val isRocketActive = rocketChatField.value
            rocketChatRoomField.isVisible = isRocketActive
            rocketChatRoomField.isRequired = isRocketActive
            if (isRocketActive) {
                this.binder.forField(rocketChatRoomField).asRequired("Campo obrigatório")
                    .bind(Monitoring::rocketchatRoom, Monitoring::rocketchatRoom.setter).validate(true)
            } else {
                this.binder.forField(rocketChatRoomField)
                    .bind(Monitoring::rocketchatRoom, Monitoring::rocketchatRoom.setter)
            }
        }

        val save = Button("Salvar", Icon("check-circle")) {
            try {
                if (!this.userController.loggedUserHasPermission("${Privileges.WRITE}")) throw Exception("Você não tem permissão")
                if (!this.binder.validate().hasErrors()) {
                    this.binder.writeBean(monitoring)
                    this.monitoringController.save(monitoring)
                    this.table.setItems(monitoringController.findByName(monitoring.name))
                    this.modal.close()
                    Notification.show("Sucesso", 5000, notificationPosition)
                }
            } catch (e: Exception) {
                Notification.show(e.message ?: "", 5000, notificationPosition)
            }
        }

        val cancel = Button("Fechar", Icon("close-circle")) { modal.close() }

        val exclusionConfirmDialog = ConfirmDialog()
        exclusionConfirmDialog.setText("Confirma a exclusão ?")
        exclusionConfirmDialog.isCloseOnEsc = true
        exclusionConfirmDialog.setCancelButton("Cancelar") {
            exclusionConfirmDialog.close()
        }
        exclusionConfirmDialog.addConfirmListener {
            if (!this.userController.loggedUserHasPermission("${Privileges.DELETE}")) {
                Notification.show("Você não tem permissão", 5000, notificationPosition)
            } else {
                this.monitoringController.delete(monitoring)
                this.modal.close()
                exclusionConfirmDialog.close()
                Notification.show("Sucesso", 5000, notificationPosition)
            }
        }

        val delete = Button("Excluir", Icon("trash")) { exclusionConfirmDialog.open() }

        val executeConfirmationDialog = ConfirmDialog()
        exclusionConfirmDialog.setText("Confirma a execução ?")
        exclusionConfirmDialog.isCloseOnEsc = true
        exclusionConfirmDialog.setCancelButton("Cancelar") {
            exclusionConfirmDialog.close()
        }
        executeConfirmationDialog.addConfirmListener {
            if (!this.userController.loggedUserHasPermission("${Privileges.EXECUTE}")) {
                Notification.show("Você não tem permissão", 5000, notificationPosition)
            } else {
                this.monitoringController.execute(monitoring).let { response ->
                    if (response != null) {
                        Notification.show("${monitoring.name} executado - OK", 5000, notificationPosition)
                    } else {
                        Notification.show("${monitoring.name} executado - NOK", 5000, notificationPosition)
                    }
                }
            }
            executeConfirmationDialog.close()
        }

        val execute = Button("Executar", Icon("play")) { executeConfirmationDialog.open() }

        val header = HorizontalLayout()
        header.justifyContentMode = FlexComponent.JustifyContentMode.START
        header.defaultVerticalComponentAlignment = FlexComponent.Alignment.START
        header.add(H3("Monitoramento"))
        header.setWidthFull()
        header.isSpacing = false

        val btnGrp = FormLayout(cancel, delete, save, execute, exclusionConfirmDialog)
        btnGrp.setResponsiveSteps(
            FormLayout.ResponsiveStep("0px", 2), FormLayout.ResponsiveStep("600px", 4)
        )

        this.modal.addOpenedChangeListener {
            execute.isEnabled = (monitoring.id > 0 && !(executeConfirmationDialog.isOpened))
            delete.isEnabled = (monitoring.id > 0 && !(exclusionConfirmDialog.isOpened))
        }

        this.modal.add(header, Scroller(form), btnGrp)
        this.modal.isResizable = true
        this.modal.isModal = true
        this.modal.isDraggable = true
        return this.modal

    }

}