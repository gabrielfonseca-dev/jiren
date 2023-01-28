package jiren

import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.server.PWA
import com.vaadin.flow.shared.communication.PushMode
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import java.util.*

@SpringBootApplication(exclude = [MongoAutoConfiguration::class])
@EnableCaching
@EnableAutoConfiguration
@EnableWebMvc
@Push(PushMode.MANUAL)
@Theme(themeClass = Lumo::class)
@PWA(name = "Jiren", shortName = "Jiren")
class JirenApp : SpringBootServletInitializer(), AppShellConfigurator {

    @Override
    override fun configure(builder: SpringApplicationBuilder?): SpringApplicationBuilder {
        return builder!!.sources(JirenApp::class.java)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<JirenApp>(*args)
        }
    }

}