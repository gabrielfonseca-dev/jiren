package jiren.view

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.applayout.DrawerToggle
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dependency.CssImport
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.html.*
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.router.RouterLink
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import com.vaadin.flow.theme.material.Material
import jiren.controller.UserController
import jiren.controller.twilio.MessageController
import jiren.data.*
import jiren.data.entity.Permission
import jiren.data.entity.User
import jiren.data.enum.StatusUser
import jiren.security.Security
import jiren.security.SecurityService
import jiren.service.apis.twilio.TwilioClient
import java.util.*
import javax.annotation.PostConstruct

@SpringComponent
@UIScope
@CssImport(value = "./styles/default.css")
@JsModule("./script/chat.js")
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
class MainLayout(val userController: UserController, val messageController: MessageController, val twilioClient: TwilioClient) : AppLayout() {

    private val toggle = DrawerToggle()
    private val header = Header()
    private val menuBar = HorizontalLayout()
    private val nav = Nav()
    private val menuList = VerticalLayout()
    private val livechat = HorizontalLayout()
    private val chatList: MutableList<ChatView.ChatInfo> = ArrayList()
    private var permissions: MutableList<Permission> = ArrayList()
    private var binder = Binder(User::class.java)

    @PostConstruct
    private fun init() {
        this.primarySection = Section.NAVBAR
        this.isDrawerOpened = false
        this.addToNavbar(true, createHeaderContent())
        this.addToDrawer(createDrawerContent())
    }

    init {
        UI.getCurrent().page.addStyleSheet("https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css")
        this.toggle.isVisible = Security.isUserLoggedIn
        this.toggle.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED)
        this.toggle.element.setAttribute("aria-label", "Menu toggle")
        this.livechat.addClassNames("d-flex", "justify-content-center")
        this.livechat.alignItems = FlexComponent.Alignment.CENTER
    }

    private fun createChat(chatView: ChatView) {
        var openConversations = userController.getOpenInstances(userController.findByUsername(SecurityService().authenticatedUser ?: ""))
        if (openConversations.isNotEmpty()) {
            for (i in 0 until livechat.componentCount) {
                openConversations = openConversations.filter { it.id.toString() != livechat.getComponentAt(i).id.toString() }
            }
            chatView.chatList = this.chatList
            chatView.livechat = this.livechat
            chatView.createChat(openConversations)
        }
    }

    private fun createHeaderContent(): Component {
        createTopMenuBar()
        this.menuBar.alignItems = FlexComponent.Alignment.CENTER
        this.menuBar.setWidthFull()
        this.header.setWidthFull()
        this.header.add(menuBar)
        return header
    }

    private fun createTopMenuBar() {

        val logoutBtn = Button("Sair", Icon("exit"))
        logoutBtn.isVisible = Security.isUserLoggedIn
        logoutBtn.isIconAfterText = true
        logoutBtn.addClickListener { SecurityService().logout() }

        val loginBtn = Button("Entrar", Icon("sign-in")) { UI.getCurrent().page.open("/oauth2/authorization/cognito", "_self") }
        loginBtn.isVisible = !Security.isUserLoggedIn

        val homeBtn = Button("Início", Icon("home")) { UI.getCurrent().page.open("/home", "_self") }
        homeBtn.isIconAfterText = true

        val colorBtn = Button("", Icon("adjust")) {
            val themeList = UI.getCurrent().element.themeList
            if (themeList.contains(Material.DARK)) {
                themeList.remove(Material.DARK)
            } else {
                themeList.add(Material.DARK)
            }
        }

        val headerLayout = HorizontalLayout(toggle)
        headerLayout.setWidthFull()

        val userMenu = HorizontalLayout(colorBtn, logoutBtn, loginBtn)
        userMenu.justifyContentMode = FlexComponent.JustifyContentMode.END

        val menuComponents = HorizontalLayout(homeBtn, livechat)

        if((userController.findByUsername(SecurityService().authenticatedUser ?: "")?.enableShift == true)) {
            val chatView = ChatView(messageController, userController, twilioClient)
            chatView.chatList = this.chatList
            chatView.livechat = this.livechat
            chatView.setId("refresher")
            createChat(chatView)
            menuComponents.add(chatView)
        }

        val titleDiv = HorizontalLayout()
        titleDiv.setWidthFull()
        titleDiv.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        titleDiv.defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        headerLayout.add(menuComponents, titleDiv, userMenu)
        headerLayout.defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        headerLayout.isSpacing = false
        headerLayout.setWidthFull()

        this.menuBar.add(headerLayout)

    }

    private fun createDrawerContent(): Component {
        return Section(createNavigation())
    }

    private fun createNavigation(): Nav {

        nav.element.setAttribute("aria-labelledby", "views")
        menuList.alignItems = FlexComponent.Alignment.CENTER
        menuList.setWidthFull()
        menuList.add(Image("img/logo-minor.png", "support"))
        nav.add(menuList)
        createLinks()

        val user = userController.findByUsername(SecurityService().authenticatedUser ?: "") ?: return nav

        val statusLabel = Span(user.status.toString().lowercase(Locale.getDefault()))
        statusLabel.addClassNames("badge", getUserBadge(user.status ?: StatusUser.DESCONECTADO))

        val statusPicker = Select<StatusUser>()
        statusPicker.setItems(StatusUser.values().toMutableList())
        statusPicker.placeholder = "Trocar Status"
        statusPicker.value = user.status
        statusPicker.isVisible = false
        this.binder.forField(statusPicker).bind(User::status, User::status.setter)

        statusLabel.addClickListener {
            statusPicker.isVisible = !statusPicker.isVisible
        }

        statusPicker.addValueChangeListener {
            user.status = statusPicker.value as StatusUser
            userController.updateStatus(user)
            UI.getCurrent().access {
                statusLabel.removeClassNames(
                    "badge-success",
                    "badge-danger",
                    "badge-warning",
                    "badge-warning",
                    "badge-secondary"
                )
                statusLabel.addClassNames(getUserBadge(user.status ?: StatusUser.DESCONECTADO))
                statusLabel.text = user.status.toString().lowercase(Locale.getDefault())
                statusPicker.isVisible = false
            }
            UI.getCurrent().push()
        }

        val info = VerticalLayout()
        info.add(Span(user.name), statusLabel, statusPicker)
        info.isPadding = false
        info.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER

        menuList.add(info)

        return nav

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

    private fun createLinks() {
        if (Security.isUserLoggedIn) {
            val role = userController.findByUsername(SecurityService().authenticatedUser ?: "")?.role ?: return
            permissions = userController.findMenuListByRole(role.name).toMutableList()

            if (permissions.isNotEmpty()) permissions.forEach { i ->
                @Suppress("UNCHECKED_CAST") val div = Div(
                    Icon(i.icon ?: ""), createLink(
                        MenuItemInfo(
                            i.description ?: "", (Class.forName("jiren.view.${i.code}") as Class<out Component>)
                        )
                    )
                )
                div.setSizeFull()
                div.addClassNames("d-flex", "justify-content-between")
                menuList.add(div)
            }

        }
    }

    private fun createLink(menuItemInfo: MenuItemInfo): RouterLink {
        val link = RouterLink()
        link.setRoute(menuItemInfo.view)
        val label = Label(menuItemInfo.text)
        link.add(label)
        return link
    }

    private class MenuItemInfo(val text: String, val view: Class<out Component>)

}