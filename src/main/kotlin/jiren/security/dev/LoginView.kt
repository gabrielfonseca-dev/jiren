package jiren.security.dev

import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.login.LoginForm
import com.vaadin.flow.component.login.LoginI18n
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import jiren.view.MainLayout
import org.springframework.context.annotation.Profile

@Profile("dev")
@PageTitle("Login")
@Route(value = "/login", layout = MainLayout::class)
@UIScope
@SpringComponent
class LoginView : VerticalLayout(), BeforeEnterObserver {

    private val loginForm = LoginForm()

    init {
        this.loginForm.setI18n(createLogin())
        this.loginForm.action = "login"
        this.loginForm.isForgotPasswordButtonVisible = false
        this.style["text-align"] = "center"
        this.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        this.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        this.add(Image("img/logo-minor.png", "sti"), loginForm)
        this.setSizeFull()
    }

    private fun createLogin(): LoginI18n? {
        val i18n = LoginI18n.createDefault()
        i18n.form.title = ""
        i18n.errorMessage.title = "Usuário ou Senha inválidos"
        i18n.errorMessage.message = "Tente novamente ou entre em contato com um administrador"
        return i18n
    }

    override fun beforeEnter(bee: BeforeEnterEvent?) {
        if (bee?.location?.queryParameters?.parameters!!.containsKey("error")) loginForm.isError = true
    }

}