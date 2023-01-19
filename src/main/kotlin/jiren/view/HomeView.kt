package jiren.view

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.*
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.component.tabs.TabsVariant
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouteAlias
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import jiren.controller.MonitoringController
import jiren.controller.ShiftController
import jiren.controller.UserController
import jiren.data.entity.Monitoring
import jiren.data.entity.User
import jiren.data.enum.StatusMonitoring
import jiren.data.enum.StatusUser
import jiren.security.Security
import org.springframework.context.annotation.ComponentScan
import java.sql.Timestamp
import java.time.Instant.now
import java.time.format.DateTimeFormatter
import java.util.*
import javax.annotation.PostConstruct

@PageTitle("Início")
@ComponentScan("home-view")
@Route(value = "", layout = MainLayout::class)
@RouteAlias(value = "home", layout = MainLayout::class)
@SpringComponent
@UIScope
class HomeView(
    private val monitoringController: MonitoringController,
    private val shiftController: ShiftController,
    private val userController: UserController
) : VerticalLayout() {

    private val team = shiftController.findDutyOfficer()
    private val monitor = monitoringController.getPanelData()
    private var user = User()
    private var monitoring = Monitoring()
    private val modal = Dialog()
    private val teamView = Tab(Icon("users"), Span("Equipe"))
    private val monitoringView = Tab(Icon("warning"), Span("Monitoramento"))
    private val shitView = Tab(Icon("user-clock"), Span("Plantões"))
    private val tabs = Tabs(monitoringView, teamView, shitView)
    private val content = VerticalLayout()

    @PostConstruct
    fun init() {
        tabs.addThemeVariants(TabsVariant.LUMO_CENTERED)
        tabs.orientation = Tabs.Orientation.HORIZONTAL
        tabs.addSelectedChangeListener { event -> setContent(event.selectedTab) }
        setContent(tabs.selectedTab)

        this.monitoringView.isVisible = Security.isUserLoggedIn
        this.monitoringView.isEnabled = Security.isUserLoggedIn

        add(tabs, content)
        setSizeFull()
        defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        style["text-align"] = "center"

    }

    private fun setContent(tab: Tab) {
        content.removeAll()
        when (tab) {
            teamView -> {
                content.add(createTeamView())
            }
            monitoringView -> {
                if (Security.isUserLoggedIn) content.add(createMonitoringView())
            }
            shitView -> {
                content.add(createShiftView())
            }
        }
    }

    private fun createTeamView(): VerticalLayout {
        val teamView = VerticalLayout()
        val table = HorizontalLayout()
        teamView.add(H3("Equipe"), table)
        var column = VerticalLayout()
        var row = 0

        for (u in 0 until (team?.size ?: 0)) {
            if (team?.isEmpty() == true) break
            val user = team?.get(u)
            val userBadge = Button(user?.status.toString().lowercase(Locale.getDefault()))
            userBadge.setId("${user?.id}")
            userBadge.addClassNames("badge", getUserBadge(user?.status ?: StatusUser.DESCONECTADO), "hover", "marginl")
            userBadge.addClickListener {
                this.user = userController.findById(userBadge.id.get().toLong()).get()
                this.modal.removeAll()
                this.modal.add(createAnalystFormView())
                this.modal.open()
            }
            val name = Span(user?.name)
            val card = Div(name, userBadge)
            column.add(card)

            if (row == 2 || team!!.last() == team[u]) {
                table.add(column)
                column = VerticalLayout()
                row = 0
            } else {
                row++
            }
        }

        table.isSpacing = false
        teamView.isSpacing = false
        teamView.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER

        return teamView
    }

    private fun createMonitoringView(): VerticalLayout {
        val monitoringView = VerticalLayout()
        val table = HorizontalLayout()
        monitoringView.add(H3("Monitoramento"), table)
        var column = VerticalLayout()
        var row = 0

        for (u in 0 until (monitor?.size ?: 1)) {
            if (monitor == null) break
            monitoring = monitor[u]
            val monitoringBadge = Button(monitoring.name.lowercase(Locale.getDefault()))
            monitoringBadge.addClassNames("badge", getAlertBadge(monitoring.status ?: StatusMonitoring.NOK), "hover")
            monitoringBadge.setId("${monitoring.id}")
            monitoringBadge.addClickListener {
                this.monitoring = monitoringController.findById(monitoringBadge.id.get().toLong()).get()
                this.modal.removeAll()
                this.modal.add(createMonitoringFormView())
                this.modal.open()
            }

            val card = Div(monitoringBadge)
            column.add(card)

            if (row == 2 || monitor.last() == monitor[u]) {
                table.add(column)
                column = VerticalLayout()
                row = 0
            } else {
                row++
            }
        }
        table.isSpacing = false
        monitoringView.isSpacing = false
        monitoringView.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        return monitoringView
    }

    private fun createShiftView(): VerticalLayout {
        val shiftView = VerticalLayout()
        val table = FormLayout()
        table.setResponsiveSteps(
            FormLayout.ResponsiveStep("0px", 1),
            FormLayout.ResponsiveStep("600px", 7)
        )
        shiftView.add(H3("Plantões"), table)

        val today = Timestamp.from(now()).toLocalDateTime()
        var dayColumn = VerticalLayout()

        for (i in 0..6) {
            val columnDay = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(today.plusDays(i.toLong()))
            val date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(today.plusDays(i.toLong()))
            val dateFrom = Timestamp.valueOf("$date 00:00:00")
            dayColumn.add(columnDay)

            val dayPersons = shiftController.search(dateFrom.toLocalDateTime())
            dayPersons?.forEach {
                val span = Span(it.plantonyst?.name)
                span.addClassNames("badge", "badge-secondary", "spansize")
                val row = VerticalLayout(
                    span, Span(
                        "${
                            DateTimeFormatter.ofPattern("HH:mm").format(it.start?.toLocalDateTime())
                        } até ${DateTimeFormatter.ofPattern("HH:mm").format(it.end?.toLocalDateTime())}"
                    )
                )
                row.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
                dayColumn.add(row)
            }
            dayColumn.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
            table.add(dayColumn)
            dayColumn = VerticalLayout()
        }
        shiftView.isSpacing = false
        shiftView.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        return shiftView
    }

    private fun createAnalystFormView(): VerticalLayout {
        val analystForm = VerticalLayout()
        val nameLabel = Label("Nome: ${user.name}")
        val statusLabel = Label("Status: ${user.status.toString().lowercase()}")
        val emailLabel = Label("E-Mail: ${user.email}")
        val phoneLabel = Label("Telefone: ${user.phone}")
        val chatLabel = Label("RocketChat: ${user.chat}")
        analystForm.add(nameLabel, statusLabel, emailLabel, phoneLabel, chatLabel)
        analystForm.defaultHorizontalComponentAlignment = FlexComponent.Alignment.START
        return analystForm
    }

    private fun createMonitoringFormView(): VerticalLayout {
        val monitoringForm = VerticalLayout()
        val nameLabel = Label("Nome: ${monitoring.name}")
        val statusLabel = Label("Status: ${monitoring.status.toString().lowercase()}")
        val ranAtLabel = Label(
            "Última verificação: ${
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").format(monitoring.ranAt?.toLocalDateTime())
            }"
        )
        val typeLabel = Label("Tipo: ${monitoring.type.toString().lowercase()}")
        val systemLabel = Label("Sistema: ${monitoring.databaseOne.toString().lowercase()}")
        systemLabel.isVisible = (typeLabel.text == "sql")
        val documentLink = Anchor(monitoring.documentURL ?: "", "Confluence")
        monitoringForm.add(nameLabel, statusLabel, ranAtLabel, typeLabel, systemLabel, documentLink)
        monitoringForm.defaultHorizontalComponentAlignment = FlexComponent.Alignment.START
        return monitoringForm
    }

    private fun getUserBadge(status: StatusUser): String {
        return when (status) {
            StatusUser.CONECTADO -> "badge-success"
            StatusUser.DESCONECTADO -> "badge-danger"
            StatusUser.ALMOCANDO -> "badge-warning"
            StatusUser.AUSENTE -> "badge-warning"
            StatusUser.FERIAS -> "badge-secondary"
        }
    }

    private fun getAlertBadge(status: StatusMonitoring): String {
        return when (status) {
            StatusMonitoring.OK -> "badge-success"
            StatusMonitoring.NOK -> "badge-danger"
            StatusMonitoring.RUNNING -> "badge-success"
            StatusMonitoring.FAILED -> "badge-danger"
        }
    }

}