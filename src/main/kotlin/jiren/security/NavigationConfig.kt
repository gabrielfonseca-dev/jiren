package jiren.security

import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.server.ServiceInitEvent
import com.vaadin.flow.server.UIInitEvent
import com.vaadin.flow.server.VaadinServiceInitListener
import jiren.controller.UserController
import jiren.data.repository.UserRepository
import jiren.security.Security.isUserLoggedIn
import jiren.security.dev.LoginView
import jiren.view.*
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("prod")
@Component
class NavigationConfig(var userRepository: UserRepository, var userController: UserController) : VaadinServiceInitListener {

    override fun serviceInit(event: ServiceInitEvent) {
        event.source.addUIInitListener { uiEvent: UIInitEvent ->
            val ui = uiEvent.ui
            ui.addBeforeEnterListener { event: BeforeEnterEvent -> beforeEnter(event) }
        }
    }

    private fun beforeEnter(event: BeforeEnterEvent) {
        val user = userRepository.findByUsername(SecurityService().authenticatedUser ?: "")

        if (LoginView::class.java != event.navigationTarget && !isUserLoggedIn && HomeView::class.java != event.navigationTarget) {
            event.rerouteTo(LoginView::class.java)
        }
        if (LoginView::class.java == event.navigationTarget && isUserLoggedIn) {
            event.rerouteTo(HomeView::class.java)
        }
        if (UserView::class.java == event.navigationTarget && (user)?.role?.permissions?.find { p -> p.code == UserView::class.simpleName } == null || user?.role?.name != "ADMIN") {
            event.rerouteTo(HomeView::class.java)
        }
        if (SqlView::class.java == event.navigationTarget && (user)?.role?.permissions?.find { p -> p.code == SqlView::class.simpleName } == null) {
            event.rerouteTo(HomeView::class.java)
        }
        if (AutomationView::class.java == event.navigationTarget && (user)?.role?.permissions?.find { p -> p.code == AutomationView::class.simpleName } == null) {
            event.rerouteTo(HomeView::class.java)
        }
        if (MonitoringView::class.java == event.navigationTarget && (user)?.role?.permissions?.find { p -> p.code == MonitoringView::class.simpleName } == null) {
            event.rerouteTo(HomeView::class.java)
        }
        if (ShiftView::class.java == event.navigationTarget && (user)?.role?.permissions?.find { p -> p.code == ShiftView::class.simpleName } == null) {
            event.rerouteTo(HomeView::class.java)
        }
        if (ConfigurationView::class.java == event.navigationTarget && (user)?.role?.permissions?.find { p -> p.code == ConfigurationView::class.simpleName } == null || user?.role?.name != "ADMIN") {
            event.rerouteTo(HomeView::class.java)
        }
        if (ScriptView::class.java == event.navigationTarget && (user)?.role?.permissions?.find { p -> p.code == ScriptView::class.simpleName } == null || user?.role?.name != "ADMIN") {
            event.rerouteTo(HomeView::class.java)
        }
        if (ChatComponent::class.java == event.navigationTarget && (user)?.role?.permissions?.find { p -> p.code == ChatComponent::class.simpleName } == null || user?.role?.name != "ADMIN") {
            event.rerouteTo(HomeView::class.java)
        }
        if (LogView::class.java == event.navigationTarget && (user)?.role?.permissions?.find { p -> p.code == LogView::class.simpleName } == null) {
            event.rerouteTo(HomeView::class.java)
        }
    }

}