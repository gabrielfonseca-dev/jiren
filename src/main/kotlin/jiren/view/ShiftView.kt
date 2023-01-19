package jiren.view

import com.vaadin.flow.component.Unit
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.combobox.ComboBox.ItemFilter
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
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.validator.BeanValidator
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import jiren.controller.ShiftController
import jiren.controller.UserController
import jiren.data.entity.Shift
import jiren.data.entity.User
import jiren.data.enum.Privileges
import java.io.IOException
import java.util.*
import javax.annotation.PostConstruct

@PageTitle("Plantões")
@Route(value = "shifts", layout = MainLayout::class)
@SpringComponent
@UIScope
class ShiftView(
    private var shiftController: ShiftController,
    private val userController: UserController
) : VerticalLayout() {
    private val table = Grid(Shift::class.java)
    private val modalLayout = VerticalLayout()
    private val notificationPosition = Notification.Position.TOP_END
    private var binder = Binder(Shift::class.java)
    private var shift = Shift()
    private var modal = Dialog()

    @PostConstruct
    fun init() {
        this.isSpacing = false
        this.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        this.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        this.style["text-align"] = "center"
        this.add(createSearch(), createMenu(), createTable(), createModal())
        this.setSizeFull()
    }

    private fun createTable(): Scroller {
        this.table.addItemClickListener {
            shift = it.item
            modal.open()
            binder.readBean(shift)
        }
        this.table.setColumns("plantonyst", "start", "end")
        this.table.columns[0].setHeader("Plantonista")
        this.table.columns[1].setHeader("Início")
        this.table.columns[2].setHeader("Fim")
        this.table.setSelectionMode(Grid.SelectionMode.SINGLE)
        this.table.isRowsDraggable = true
        this.table.isColumnReorderingAllowed = true
        this.table.isVerticalScrollingEnabled = true
        this.table.columns.forEach { it.isResizable = true }
        this.table.setWidthFull()
        this.table.setHeight(95F, Unit.PERCENTAGE)
        val tableScroller = Scroller(this.table)
        tableScroller.setSizeFull()
        return tableScroller
    }

    private fun createMenu(): HorizontalLayout {
        val newShift = Button("Novo", Icon("plus"))
        newShift.addClickListener {
            this.shift = Shift()
            this.binder.readBean(this.shift)
            this.modal.open()
        }
        val btnLayout = HorizontalLayout()
        btnLayout.add(newShift)
        btnLayout.setWidthFull()
        btnLayout.justifyContentMode = FlexComponent.JustifyContentMode.END
        btnLayout.defaultVerticalComponentAlignment = FlexComponent.Alignment.END
        return btnLayout
    }

    private fun createSearch(): FormLayout {
        val startDate = DateTimePicker("Do dia")
        val searchButton = Button("Buscar", Icon("search"))
        searchButton.addClickListener {
            if (startDate.value == null) {
                startDate.helperText = "Campo obrigatório"
            } else {
                this.table.setItems(this.shiftController.search(startDate.value))
                startDate.helperText = ""
            }
        }
        val optionsGroup = HorizontalLayout(searchButton)
        optionsGroup.defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        val searchPanel = FormLayout()
        searchPanel.add(startDate, optionsGroup)
        return searchPanel
    }

    private fun createModal(): Dialog {
        val shiftStart = DateTimePicker("Início")
        this.binder.forField(shiftStart)
            .withValidator(BeanValidator(Shift::class.java, "start"))
            .bind(Shift::getStart, Shift::setStart)
            .validate(true)

        val shiftEnd = DateTimePicker("Fim")
        this.binder.forField(shiftEnd)
            .withValidator(BeanValidator(Shift::class.java, "end"))
            .bind(Shift::getEnd, Shift::setEnd)
            .validate(true)

        val shiftOfficer = ComboBox<User>("Plantonista")
        shiftOfficer.isRequiredIndicatorVisible = true
        shiftOfficer.isRequired = true
        shiftOfficer.isAllowCustomValue = false
        shiftOfficer.placeholder = "Digite para filtrar"
        shiftOfficer.setWidthFull()

        val filter: ItemFilter<User> = ItemFilter<User> { officer: User, filterString: String ->
            officer.name.lowercase().startsWith(filterString.lowercase(Locale.getDefault()))
        }
        shiftOfficer.setItems(filter, this.shiftController.findDutyOfficer())
        this.modal.addOpenedChangeListener { shiftOfficer.value = this.shift.plantonyst }

        val componentsLayout = FormLayout()
        componentsLayout.add(shiftStart, shiftEnd, shiftOfficer)
        componentsLayout.setResponsiveSteps(
            FormLayout.ResponsiveStep("0px",1)
        )

        val save = Button("Confirmar", Icon("check-circle")) {
            if (!this.userController.loggedUserHasPermission("${Privileges.WRITE}")) {
                Notification.show("Você não tem permissão", 5000, this.notificationPosition)
            } else {
                try {
                    if (shiftOfficer.value == null) throw (IOException("Plantonista inválido"))
                    if (!this.binder.validate().hasErrors()) {
                        this.binder.writeBean(shift)
                        this.shift.plantonyst = shiftOfficer.value
                        this.table.setItems(
                            shiftController.save(shift)
                        )
                        this.modal.close()
                        Notification.show("Sucesso", 5000, this.notificationPosition)
                    }
                } catch (e: Exception) {
                    Notification.show(e.message, 5000, this.notificationPosition)
                }
            }
        }

        val cancel = Button("Fechar", Icon("close-circle")) {
            this.modal.close()
        }

        val delete = Button("Excluir", Icon("trash")) {
            if (!this.userController.loggedUserHasPermission("${Privileges.DELETE}")) {
                Notification.show("Você não tem permissão", 5000, this.notificationPosition)
            } else {
                this.shiftController.delete(this.shift)
                Notification.show("Sucesso", 5000, this.notificationPosition)
                this.modal.close()
            }
        }

        val buttonGroup = FormLayout(cancel, delete, save)
        buttonGroup.setResponsiveSteps(
            FormLayout.ResponsiveStep("0px",3)
        )

        this.modalLayout.add(H3("Plantão"), componentsLayout, buttonGroup)
        this.modalLayout.setSizeFull()
        this.modalLayout.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        this.modalLayout.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        this.modalLayout.isPadding = false
        this.modalLayout.isSpacing = false
        this.modalLayout.isMargin = false

        this.modal.add(Scroller(modalLayout))
        this.modal.isResizable = true
        this.modal.isModal = true
        this.modal.isDraggable = true
        return this.modal
    }

}

