package jiren.security

import jiren.security.credentials.CredentialsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod

@Profile("prod")
@Configuration
class SecurityOAuthConfig(private val credentialsService: CredentialsService) {

    @Bean
    fun clientRegistrationRepository(): ClientRegistrationRepository {
        return InMemoryClientRegistrationRepository(clientRegistration())
    }

    private fun clientRegistration(): ClientRegistration {
        credentialsService.getCredentials()
        val cognito = credentialsService.cognitoConfig
        return ClientRegistration.withRegistrationId("cognito")
            .clientId(cognito.getString(credentialsService.cognitoClient))
            .clientSecret(cognito.getString(credentialsService.cognitoSecret))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("https://52.206.172.79/login/oauth2/code/cognito")
            .scope("openid")
            .issuerUri("https://cognito-idp.us-east-1.amazonaws.com/us-east-1_lmHf6WPqk")
            .authorizationUri("https://jiren.auth.us-east-1.amazoncognito.com/oauth2/authorize")
            .tokenUri("https://jiren.auth.us-east-1.amazoncognito.com/oauth2/token")
            .userInfoUri("https://jiren.auth.us-east-1.amazoncognito.com/oauth2/userinfo")
            .userNameAttributeName("cognito:username")
            .jwkSetUri("https://cognito-idp.us-east-1.amazonaws.com/us-east-1_lmHf6WPqk/.well-known/jwks.json")
            .clientName("jiren")
            .build()
    }

}