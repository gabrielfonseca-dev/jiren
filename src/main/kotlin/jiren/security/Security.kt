package jiren.security

import com.vaadin.flow.server.HandlerHelper
import com.vaadin.flow.shared.ApplicationConstants
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.util.stream.Stream
import javax.servlet.http.HttpServletRequest

object Security {

    fun isFrameworkInternalRequest(request: HttpServletRequest): Boolean {
        val parameterValue = request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER)
        return (parameterValue != null
                && Stream.of(*HandlerHelper.RequestType.values())
            .anyMatch { r: HandlerHelper.RequestType -> r.identifier == parameterValue })
    }

    fun isTwilioRequest(request: HttpServletRequest): Boolean { return request.servletPath.startsWith("/twilio/webhook") }

    val isUserLoggedIn: Boolean
        get() {
            val authentication = SecurityContextHolder.getContext().authentication
            return (authentication != null && authentication !is AnonymousAuthenticationToken && authentication.isAuthenticated)
        }

    const val defaultSystemUser = "admin"

}