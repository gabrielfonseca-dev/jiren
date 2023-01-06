package jiren.security.credentials

import org.atmosphere.config.service.Singleton
import org.json.JSONObject
import org.springframework.stereotype.Component

@Singleton
@Component
class CredentialsService(private var secretManager: SecretManager) {

    var database = JSONObject()
    var mailerCredentials = JSONObject()
    var jiraCredentials = JSONObject()
    var externalDatabases = JSONObject()
    var cognitoConfig = JSONObject()
    var twilioConfig = JSONObject()

    final val dbHost = "database-host"
    final val dbSchema = "database-schema"
    final val dbUser = "database-user"
    final val dbPassword = "database-password"
    final val mailerUser = "mailer-user"
    final val mailerPassword = "mailer-password"
    final val jiraUser = "jira-user"
    final val jiraKey = "jira-token"
    final val cognitoClient = "cognito-clientId"
    final val cognitoSecret = "cognito-clientSecret"
    final val twilioClient = "twilio-clientId"
    final val twilioSecret = "twilio-clientSecret"
    final val databases = "databases-credentials"

    fun getCredentials() {

        try {

            val secrets = secretManager.getSecrets()

            this.database = JSONObject()
                .put(this.dbHost, secrets?.get(this.dbHost)?.asText())
                .put(this.dbSchema, secrets?.get(this.dbSchema)?.asText())
                .put(this.dbUser, secrets?.get(this.dbUser)?.asText())
                .put(this.dbPassword, secrets?.get(this.dbPassword)?.asText())

            this.mailerCredentials
                .put(this.mailerUser, secrets?.get(this.mailerUser)?.asText())
                .put(this.mailerPassword, secrets?.get(this.mailerPassword)?.asText())

            this.jiraCredentials
                .put(this.jiraUser, secrets?.get(this.jiraUser)?.asText())
                .put(this.jiraKey, secrets?.get(this.jiraKey)?.asText())

            this.cognitoConfig
                .put(this.cognitoClient,secrets?.get(this.cognitoClient)?.asText())
                .put(this.cognitoSecret,secrets?.get(this.cognitoSecret)?.asText())

            this.twilioConfig
                .put(this.twilioClient,secrets?.get(this.twilioClient)?.asText())
                .put(this.twilioSecret,secrets?.get(this.twilioSecret)?.asText())

            this.externalDatabases = JSONObject(secrets?.get(this.databases)?.asText())

        } catch (e: Exception) {
            System.err.println(e.message)
        }

    }

}