package jiren.service.apis.rocketchat

import jiren.data.entity.Monitoring
import jiren.data.enum.MonitoringType
import jiren.data.enum.Parameters
import jiren.data.enum.StatusMonitoring
import jiren.data.repository.ParameterRepository
import org.apache.http.entity.ContentType
import org.json.JSONObject
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Service
class RocketChatClient(private val parameterRepository: ParameterRepository) {

    val webHookURI = "${parameterRepository.findByCode("${Parameters.ROCKETCHAT_WEBHOOK}")?.value}"

    fun sendMessage(monitoring: Monitoring, appendable: String = "") {

        val message = StringBuilder()

        message.appendLine("${monitoring.name} ${if (monitoring.status == StatusMonitoring.OK) "is OK :white_check_mark:" else "on ERROR :setonfire:"}")

        if(monitoring.documentURL?.trim() != "-" && !(monitoring.documentURL.isNullOrEmpty()) ) message.appendLine("Docs -> ${monitoring.documentURL}")

        if (!monitoring.jiraIssue.isNullOrEmpty()) message.appendLine("Task -> ${monitoring.jiraIssue}")

        message.appendLine("AlarmCount -> ${monitoring.errorCount}")

        if (appendable.isNotEmpty() && monitoring.type == MonitoringType.DATABASE) message.appendLine(createTable(appendable))

        val json = JSONObject()
        json.put("text", message.toString())
        if(!monitoring.rocketchatRoom.isNullOrEmpty()) json.put("roomId", monitoring.rocketchatRoom)

        val request = HttpRequest.newBuilder().uri(URI.create(webHookURI)).timeout(Duration.ofMinutes(5))
            .header("Content-Type", ContentType.APPLICATION_JSON.toString())
            .POST(HttpRequest.BodyPublishers.ofString(json.toString())).build()
        HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString()).statusCode()

    }

    fun sendMessage(message: String, roomID: String) {
        val webHookURI = "${parameterRepository.findByCode("${Parameters.ROCKETCHAT_WEBHOOK}")?.value}"
        val json = JSONObject()
        json.put("roomId", roomID)
        json.put("text", message)
        val request = HttpRequest.newBuilder().uri(URI.create(webHookURI)).timeout(Duration.ofMinutes(5))
            .header("Content-Type", "${ContentType.APPLICATION_JSON}")
            .POST(HttpRequest.BodyPublishers.ofString(json.toString())).build()
        HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString())
    }

    private fun createTable(data: String): String {
        val table = StringBuilder()
        table.append("<table><tbody>")
        data.reader().readLines().forEach { line ->
            table.append("<tr>")
            line.split(",").forEach { dataLine ->
                table.append("<td>")
                table.append(dataLine)
                table.append("</td>")
            }
            table.append("</tr>")
        }
        table.append("</tbody></table>")
        return table.toString()
    }

}