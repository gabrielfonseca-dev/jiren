package jiren.security.credentials

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

@Service
class SecretManager(var env: Environment) {

    private val clientBuilder: AWSSecretsManagerClientBuilder = AWSSecretsManagerClientBuilder.standard()
    private val objectMapper: ObjectMapper = ObjectMapper()
    private var secretsJson: JsonNode? = null

    fun getSecrets(): JsonNode? {

        val endpoints = env.getProperty("spring.aws.secretsmanager.endpoint")
        val awsRegion = env.getProperty("spring.aws.secretsmanager.region")
        val secretName = env.getProperty("spring.aws.secretsmanager.name")

        var getSecretValueResponse: GetSecretValueResult? = null

        try {

            val config: AwsClientBuilder.EndpointConfiguration =
                AwsClientBuilder.EndpointConfiguration(endpoints, awsRegion)
            val getSecretValueRequest: GetSecretValueRequest = GetSecretValueRequest().withSecretId(secretName)

            clientBuilder.setEndpointConfiguration(config)
            val client: AWSSecretsManager = clientBuilder.build()

            getSecretValueResponse = client.getSecretValue(getSecretValueRequest)

        } catch (e: Exception) {
            System.err.println("The requested secret $secretName was not found")
        }

        val secret: String? = getSecretValueResponse?.secretString

        if (secret != null) {
            secretsJson = objectMapper.readTree(secret)
        }

        return secretsJson

    }

}