package jiren.security

import jiren.security.Security.isFrameworkInternalRequest
import org.springframework.context.annotation.Profile
import org.springframework.security.web.savedrequest.HttpSessionRequestCache
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Profile("prod")
@Component
internal class RequestCacheConfig : HttpSessionRequestCache() {
    override fun saveRequest(request: HttpServletRequest, response: HttpServletResponse) {
        if (!isFrameworkInternalRequest(request) || !request.requestURL.contains("/login")) {
            super.saveRequest(request, response)
        }
    }
}