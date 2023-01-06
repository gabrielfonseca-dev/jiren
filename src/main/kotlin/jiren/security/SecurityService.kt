package jiren.security

import com.vaadin.flow.component.UI
import com.vaadin.flow.server.VaadinServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler

class SecurityService {

    val authenticatedUser: String?
        get() {
            return try {
                when (val principal = SecurityContextHolder.getContext().authentication.principal) {
                    is UserDetails -> principal.username
                    is DefaultOidcUser -> principal.name
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }

    fun logout() {
        UI.getCurrent().page.setLocation("/")
        val logoutHandler = SecurityContextLogoutHandler()
        logoutHandler.setClearAuthentication(true)
        logoutHandler.isInvalidateHttpSession = true
        logoutHandler.logout(VaadinServletRequest.getCurrent().httpServletRequest, null, null)
    }

}