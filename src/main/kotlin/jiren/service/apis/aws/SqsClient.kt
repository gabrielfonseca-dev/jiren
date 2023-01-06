package jiren.service.apis.aws

import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.*

@Service
class SqsClient {

    fun getMessageQuantity(queueName: String): Int {
        val sqsClient: SqsClient = SqsClient.create()
        try {
            val getQueueUrlResponse: GetQueueUrlResponse =
                sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build())
            val queueUrl: String = getQueueUrlResponse.queueUrl()

            val atts: MutableList<QueueAttributeName> = ArrayList()
            atts.add(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES)
            val attributesRequest: GetQueueAttributesRequest = GetQueueAttributesRequest.builder()
                .queueUrl(queueUrl)
                .attributeNames(atts)
                .build()
            val response: GetQueueAttributesResponse = sqsClient.getQueueAttributes(attributesRequest)
            var queueAtts: Map<String, String> = response.attributesAsStrings()
            queueAtts = queueAtts.filter { it.key == "ApproximateNumberOfMessages" }
            val result = queueAtts.map { it.value.toInt() }
            sqsClient.close()
            return result.first()
        } catch (e: SqsException) {
            System.err.println(e.awsErrorDetails().errorMessage())
            throw(e)
        } finally {
            sqsClient.close()
        }

    }

}