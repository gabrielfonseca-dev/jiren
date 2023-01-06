package jiren.security.dev

import jiren.data.repository.ParameterRepository
import jiren.security.Security
import jiren.security.credentials.CredentialsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.access.AccessDecisionVoter.*
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import java.util.*

@Profile("dev")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
class SecurityConfig : GlobalMethodSecurityConfiguration() {

    @Autowired
    private lateinit var authenticationManager: jiren.security.dev.AuthenticationManager
    @Autowired
    private lateinit var credentialsService: CredentialsService
    @Autowired
    private lateinit var parameterRepository: ParameterRepository

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain? {
        http.csrf().disable()
            .formLogin().loginPage("/login").successForwardUrl("/home").permitAll()
            .and().logout().logoutSuccessUrl("/home")
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
            .mvcMatchers("/admin/**").hasAuthority("ADMIN")
            .mvcMatchers("/twilio/**").hasAnyAuthority("TWILIO","ADMIN")
            .mvcMatchers("/team/**").hasAnyAuthority("TEAM","ADMIN")
            .anyRequest().authenticated()
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(5)
    }

    override fun authenticationManager(): AuthenticationManager? {
        return authenticationManager
    }

}