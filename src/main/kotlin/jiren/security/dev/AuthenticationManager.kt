package jiren.security.dev

import org.springframework.context.annotation.Profile
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import javax.naming.AuthenticationException

@Profile("dev")
@Component
class AuthenticationManager(val customUserDetailsService: UserDetailsManager) : AuthenticationManager {

    @Throws(AuthenticationException::class)
    override fun authenticate(authentication: Authentication): Authentication {
        val userDetail: UserDetails = this.customUserDetailsService.loadUserByUsername(authentication.name)
        if (!BCryptPasswordEncoder(5).matches(authentication.credentials.toString(), userDetail.password)) {
            throw BadCredentialsException("Wrong password")
        }
        return UsernamePasswordAuthenticationToken(
            userDetail.username,
            userDetail.password,
            userDetail.authorities
        )
    }

}
