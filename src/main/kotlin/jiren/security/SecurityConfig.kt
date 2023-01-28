package jiren.security

import jiren.data.repository.ParameterRepository
import jiren.data.repository.UserRepository
import jiren.security.credentials.CredentialsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.access.AccessDecisionVoter.*
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.SecurityFilterChain
import java.util.*

@Profile("prod")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
class SecurityConfig : GlobalMethodSecurityConfiguration() {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var credentialsService: CredentialsService

    @Autowired
    private lateinit var parameterRepository: ParameterRepository

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain? {
        http.csrf().disable()
            .requestCache().requestCache(RequestCacheConfig())
            .and().authorizeRequests().antMatchers("/", "/home").permitAll()
            .requestMatchers(Security::isFrameworkInternalRequest).permitAll()
            .requestMatchers(Security::isTwilioRequest).permitAll()
            .and().authorizeRequests().antMatchers(
                "/VAADIN/**",
                "/vaadinServlet/**",
                "/vaadinServlet/UIDL/**",
                "/vaadinServlet/HEARTBEAT/**",
                "/manifest.webmanifest",
                "/sw.js",
                "/offline.html",
                "/icons/**",
                "/images/**",
                "/styles/**",
                "/img/**"
            ).permitAll()
            .antMatchers("/admin/**").hasAnyAuthority("ADMIN")
            .antMatchers("/ti/**").hasAnyAuthority("ADMIN", "TI")
            .anyRequest().authenticated()
            .and().oauth2Login().loginPage("/oauth2/authorization/cognito")
            .and().logout().logoutSuccessUrl("/home")
        return http.build()
    }

    @Bean
    fun oidcUserService(): OAuth2UserService<OidcUserRequest, OidcUser> {
        val delegate = OidcUserService()
        return OAuth2UserService { userRequest: OidcUserRequest? ->
            var oidcUser = delegate.loadUser(userRequest)
            val mappedAuthorities: MutableSet<GrantedAuthority> = HashSet()
            val localUser = userRepository.findByUsername(oidcUser.name ?: "")
            if (localUser == null) {
                throw OAuth2AuthenticationException(OAuth2Error("invalid_user", "invalid_user", ""))
            } else {
                mappedAuthorities.add(SimpleGrantedAuthority(localUser.role?.name))
            }
            oidcUser = DefaultOidcUser(mappedAuthorities, oidcUser.idToken, oidcUser.userInfo, "cognito:username")
            oidcUser
        }
    }

}