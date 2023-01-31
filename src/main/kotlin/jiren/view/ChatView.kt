package jiren.view

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.Unit
import com.vaadin.flow.component.contextmenu.MenuItem
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.*
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.menubar.MenuBar
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.Scroller
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import jiren.controller.UserController
import jiren.controller.twilio.InstanceController
import jiren.data.entity.User
import jiren.data.entity.twilio.Instance
import java.util.*
import javax.annotation.PostConstruct

@PageTitle("Atendimentos")
@Route(value = "admin/livechat", layout = MainLayout::class)
@SpringComponent
@UIScope
class ChatView(
    private val userController: UserController,
    private val instanceController: InstanceController
) : VerticalLayout() {
    private val table = Grid(InstanceProps::class.java)
    private lateinit var tma: Span
    private lateinit var tme: Span
    private lateinit var ongoing: Span
    private lateinit var waiting: Span
    private lateinit var satisfaction: Span
    private val modalLayout = VerticalLayout()
    private val notificationPosition = Notification.Position.TOP_END

    private var modal = Dialog()

    @PostConstruct
    fun init() {
        this.isSpacing = false
        this.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        this.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        this.style["text-align"] = "center"
        this.add(createResumePanel(), createTable())
        this.setSizeFull()
        this.refreshUI()
    }

    private fun createTable(): Scroller {
        this.table.addItemClickListener {
            modal.removeAll()
            modal.add(
                Span(Label("${it.item.customer} (${it.item.status})"), Paragraph(it.item.phone)),
                it.item.actions
            )
            modal.open()
        }
        this.table.setColumns("customer", "phone", "status", "analyst")
        this.table.columns[0].setHeader("Cliente")
        this.table.columns[1].setHeader("Telefone")
        this.table.columns[2].setHeader("Status")
        this.table.columns[3].setHeader("Analista")
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

    private fun createResumePanel(
        tmaValue: String = "00h00m",
        tmeValue: String = "00h00m",
        ongoingValue: String = "0",
        waitingValue: String = "0",
        satisfactionValue: String = "0"
    ): VerticalLayout {
        val title = H3("Métricas de Atendimento")
        tma = Span(Label("TMA"), Paragraph(tmaValue))
        tme = Span(Label("TME"), Paragraph(tmeValue))
        ongoing = Span(Label("Em andamento"), Paragraph(ongoingValue))
        waiting = Span(Label("Em espera"), Paragraph(waitingValue))
        satisfaction = Span(Label("Satisfação"), Paragraph(satisfactionValue))
        val dataView = HorizontalLayout()
        dataView.add(tma, tme, ongoing, waiting, satisfaction)
        dataView.defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        dataView.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        val container = VerticalLayout(title, dataView)
        container.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        container.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        return container
    }

    // @Scheduled(cron = "")
    private fun refreshUI() {
        UI.getCurrent().access {
            val instances = instanceController.getAllOpen()
            val instancesProps = instances.map { instance ->
                var status = ""
                if(instance.chatAwaitStart != null && instance.chatAwaitEnd == null) status = "Aguardando atendimento"
                if(instance.chatAwaitStart != null && instance.chatAwaitEnd != null) status = "Em atendimento"
                if(instance.chatAwaitStart == null) status = "Em auto-atendimento"
                val mappedObj = InstanceProps(
                    instance,
                    instance.contactName ?: "",
                    instance.contact ?: "",
                    status,
                    instance.user,
                    createActionsMenu(instance)
                )
                mappedObj
            }
            this.table.setItems(instancesProps)
        }
        UI.getCurrent().push()
    }

    private fun createActionsMenu(instance: Instance): MenuBar {
        val actionsMenu = MenuBar()
        actionsMenu.addItem("Ações")
        val item: MenuItem = actionsMenu.addItem(Icon(VaadinIcon.CHEVRON_DOWN))
        item.subMenu.addItem("Atribuir") {

        }
        item.subMenu.addItem("Desatribuir") {

        }
        item.subMenu.addItem("Encerrar") {

        }
        return actionsMenu
    }

    class InstanceProps(
        var instance: Instance? = null,
        var customer: String = "",
        var phone: String = "",
        var status: String = "",
        var analyst: User? = null,
        var actions: MenuBar? = null
    )

}