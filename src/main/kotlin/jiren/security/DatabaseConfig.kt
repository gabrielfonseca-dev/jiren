package jiren.security

import jiren.security.credentials.CredentialsService
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import javax.sql.DataSource

@Profile("prod")
@Configuration
class DatabaseConfig(private val credentialsService: CredentialsService) {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    fun appDataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean
    @Primary
    fun init(): DataSource? {
        return try {
            credentialsService.getCredentials()
            val host: String? = credentialsService.database.getString(credentialsService.dbHost)
            val dbname: String? = credentialsService.database.getString(credentialsService.dbSchema)
            val username: String? = credentialsService.database.getString(credentialsService.dbUser)
            val password: String? = credentialsService.database.getString(credentialsService.dbPassword)
            val config = appDataSourceProperties()
            config.url = "jdbc:mysql://$host:3306/$dbname"
            config.username = username
            config.password = password
            config.initializeDataSourceBuilder()?.build()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}