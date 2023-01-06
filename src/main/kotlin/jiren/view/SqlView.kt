package jiren.view

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.splitlayout.SplitLayout
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import jiren.controller.ScriptController
import jiren.service.database.ScriptExecutor

@PageTitle("SQL")
@Route(value = "ti/sql", layout = MainLayout::class)
@SpringComponent
@UIScope
class SqlView(private val scriptExecutor: ScriptExecutor, scriptController: ScriptController) : VerticalLayout() {

    private val scriptArea = TextArea()
    private val outputArea = TextArea()
    private val databasesOptions = scriptController.options()
    private val notificationPosition = Notification.Position.TOP_END
    private var modal = Dialog()

    init {
        val splitView = SplitLayout(buildExecutionView(), buildOutputView())
        splitView.orientation = SplitLayout.Orientation.HORIZONTAL
        splitView.setSizeFull()
        this.add(createMenu(), splitView, createModal())
        this.setSizeFull()
        this.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        this.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        this.style["text-align"] = "center"
    }

    private fun createMenu(): FormLayout {

        val taskField = TextField("Task", "ST-00000")

        val varBtn = Button("Definir Variáveis", Icon("notebook"))
        varBtn.addClickListener {
            this.modal.open()
        }

        val scriptOptions = ComboBox("Script", databasesOptions)
        scriptOptions.addValueChangeListener {
            this.scriptArea.value = scriptOptions.value.query
            createModal()
            this.modal.open()
        }

        val confirmDialog =  ConfirmDialog()
        val execBtn = Button("Executar", Icon("play"))

        confirmDialog.setText("Confirma a execução do script ?")
        confirmDialog.isCloseOnEsc = true
        confirmDialog.setCancelButton("Cancelar") {
            confirmDialog.close()
        }
        confirmDialog.addConfirmListener {
            try {
                execBtn.isEnabled = false
                if (this.scriptArea.value.isNullOrEmpty() || scriptOptions.value == null) {
                    Notification.show("Script ou Banco de Dados inválido", 5000, this.notificationPosition)
                } else {
                    this.scriptExecutor.execute(this.scriptArea.value, scriptOptions.value.database!!, taskField.value).let { response ->
                        Notification.show(response.split("!#!").first(), 5000, this.notificationPosition)
                        this.outputArea.value = response.split("!#!").last()
                    }
                }
            } catch (e: Exception) {
                Notification.show(e.message, 5000, this.notificationPosition)
            } finally {
                confirmDialog.close()
                execBtn.isEnabled = true
            }
        }

        execBtn.addClickListener {
            confirmDialog.open()
        }

        val menuLayout = FormLayout()
        menuLayout.add(scriptOptions, taskField, HorizontalLayout(varBtn,execBtn), confirmDialog)
        return menuLayout

    }

    private fun buildExecutionView(): VerticalLayout {
        this.scriptArea.isReadOnly = true
        this.scriptArea.label = "SQL"
        this.scriptArea.setSizeFull()
        val executionView = VerticalLayout()
        executionView.add(this.scriptArea)
        executionView.setSizeFull()
        return executionView
    }

    private fun buildOutputView(): VerticalLayout {
        this.outputArea.isReadOnly = true
        this.outputArea.label = "OUTPUT"
        this.outputArea.setSizeFull()
        val outputView = VerticalLayout()
        outputView.add(this.outputArea)
        outputView.setSizeFull()
        return outputView
    }

    private fun createModal(): Dialog {
        val script = this.scriptArea.value
        val variables = Regex("([$])\\w+").findAll(script, 0).map { it.value }.toList()
        val variablesCopy: MutableList<String> = ArrayList()
        variables.forEach { if(!variablesCopy.contains(it)) variablesCopy.add(it) }
        val closeBtn = Button("OK") {
            this.modal.close()
        }
        this.modal.removeAll()
        this.modal.add(buildVariablesComponents(variables), closeBtn)
        this.modal.isResizable = true
        this.modal.isModal = true
        this.modal.isDraggable = true
        return this.modal
    }

    private fun buildVariablesComponents(variables: List<String>): VerticalLayout {
        val componentContainer = VerticalLayout()
        variables.forEach { variable ->
            val component = HorizontalLayout()
            val key = TextField()
            key.value = variable
            key.isReadOnly = true
            val value = TextField()
            value.addValueChangeListener {
                this.scriptArea.value = this.scriptArea.value.replace((variable), value.value)
            }
            component.add(key, value)
            val closeModal = Button("Fechar")
            closeModal.addClickListener {
                this.modal.close()
            }
            componentContainer.add(component)
        }
        return componentContainer
    }

}