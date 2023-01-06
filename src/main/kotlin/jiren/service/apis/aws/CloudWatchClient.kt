package jiren.service.apis.aws

import org.springframework.stereotype.Service
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsRequest
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsResponse
import software.amazon.awssdk.services.cloudwatchlogs.model.FilteredLogEvent

@Service
class CloudWatchClient {

    fun getLogEvents(query: String, start: Long, end: Long, groups: String): MutableList<FilteredLogEvent>? {
        val client = CloudWatchLogsClient.create()
        val response: FilterLogEventsResponse
        try {
            val logRequest = FilterLogEventsRequest.builder()
                .filterPattern(query)
                .startTime(start)
                .endTime(end)
                .logGroupName(groups)
                .build()
            response = client.filterLogEvents(logRequest)
            return response.events()
        } catch (e: Exception) {
            throw(e)
        } finally {
            client.close()
        }
    }

}