package jiren.service.apis.jira

import com.helger.commons.csv.CSVWriter
import com.mashape.unirest.http.Unirest
import jiren.data.enum.Parameters
import jiren.data.repository.ParameterRepository
import jiren.security.credentials.CredentialsService
import org.apache.http.entity.ContentType
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.springframework.stereotype.Service
import java.io.File
import java.net.URLEncoder
import java.util.*
import javax.annotation.PostConstruct

@Service
class JiraClient(private val parameterRepository: ParameterRepository, private val credentialsService: CredentialsService) {

    private lateinit var token: String
    private lateinit var projectKey: String
    private lateinit var requestType: String
    private lateinit var uri: String

    @PostConstruct
    fun init() {
        try {
            token = Base64.getEncoder()
                .encode("${credentialsService.jiraCredentials.getString(credentialsService.jiraUser)}:${credentialsService.jiraCredentials.getString(credentialsService.jiraKey)}".toByteArray())
                .toString()
            projectKey = "${parameterRepository.findByCode("${Parameters.JIRA_PROJECT}") ?: "ST"}"
            requestType = "${parameterRepository.findByCode("${Parameters.JIRA_ISSUE_TYPE}") ?: "Task"}"
            uri = "${parameterRepository.findByCode("${Parameters.JIRA_API_URI}") ?: ""}"
        } catch (e: Exception) {}
    }

    fun createIssue(title: String, description: String, attachment: File?, labels: List<String>? = null): String? {
        val jsonObject = JSONObject()
        val projectObject = JSONObject()
        projectObject.put("key", projectKey)
        jsonObject.put("project", projectObject)
        jsonObject.put("summary", title)
        jsonObject.put("description", description)
        if(!labels.isNullOrEmpty()) {
            val labelsArray = JSONArray()
            labels.forEach { label -> labelsArray.put(label.trim()) }
            jsonObject.put("customfield_15010",labelsArray)
        }
        val requestTypeObject = JSONObject()
        requestTypeObject.put("name", requestType)
        jsonObject.put("issuetype", requestTypeObject)

        if (projectKey == "ST") {
            val customerRequestType = "st/ace274fc-bbf3-4957-849c-faaee2fd6ddb"
            jsonObject.put("customfield_10022", customerRequestType)
            val priority = JSONObject()
            priority.put("id", "30962")
            jsonObject.put("customfield_16029", priority)
            val areaCatalog = JSONObject()
            areaCatalog.put("id", "31967")
            jsonObject.put("customfield_16040", areaCatalog)
            val branch = JSONObject()
            branch.put("id", "30776")
            jsonObject.put("customfield_16026", branch)
            val product = JSONObject()
            product.put("id", "23499")
            jsonObject.put("customfield_15119", product)
            val module = JSONObject()
            module.put("id", "28573")
            jsonObject.put("customfield_15103", module)
        } else if (projectKey == "FIS") {
            val customerRequestType = "fis/98363da5-0ef3-40f0-b8e8-5cf91ccbfe73"
            jsonObject.put("customfield_10022", customerRequestType)
        }
        val payload = JSONObject()
        payload.put("fields", jsonObject)

        val createIssueResponse = Unirest.post("$uri/issue/")
            .header("Authorization", "Basic $token")
            .header("Content-Type", ContentType.APPLICATION_JSON.toString())
            .header("Accept", "application/json")
            .body(payload)
            .asJson()

        val createdIssueKey = createIssueResponse.body.`object`.getString("key")

        if (attachment != null && createIssueResponse.status == 201) {
            Unirest.post("$uri/issue/$createdIssueKey/attachments")
                .header("Authorization", "Basic $token")
                .header("Accept", "application/json")
                .header("X-Atlassian-Token", "no-check")
                .field("file", attachment)
                .asJson()
        }
        return createdIssueKey
    }

    fun addAttachment(attachment: File, task: String) {
        Unirest.post("$uri/issue/$task/attachments")
                .header("Authorization", "Basic $token")
                .header("Accept", "application/json")
                .header("X-Atlassian-Token", "no-check")
                .field("file", attachment)
                .asJson()
    }

    fun addComment(text: String, task: String): Boolean {
        val visibilityObject = JSONObject()
        visibilityObject.put("identifier", "Users")
        visibilityObject.put("type", "role")
        val payload = JSONObject()
        payload.put("visibility", visibilityObject)
        payload.put("body", text)

        return try {
           val response = Unirest.post("$uri/issue/$task/comment")
                .header("Authorization", "Basic $token")
                .header("Content-Type", ContentType.APPLICATION_JSON.toString())
                .header("Accept", "application/json")
                .asJson()
            if(response.status == 200) true
            else false
        } catch (e: Exception) {
            false
        }

    }

    fun hasOpenIssue(issueKey: String): Boolean {
        if (issueKey.isEmpty()) return false
        val response = Unirest.get("$uri/search?jql=key=$issueKey&fields=status")
            .header("Authorization", "Basic $token")
            .header("Content-Type", ContentType.APPLICATION_JSON.toString())
            .header("Accept", "application/json")
            .asJson()
        return try {
            val status = response.body.`object`
                .getJSONArray("issues")
                .getJSONObject(0)
                .getJSONObject("fields")
                .getJSONObject("status")
                .getString("name")
            (status != "Resolved" && status != "Reject" && status != "Rejected" && !status.isNullOrEmpty())
        } catch (e: Exception) {
            false
        }
    }

    fun getIssues(jql: String, fields: String, returnAsCsv: Boolean? = null): Any? {

        val url = "$uri/search?jql=${URLEncoder.encode(jql, Charsets.UTF_8)}&fields=${URLEncoder.encode(fields, Charsets.UTF_8)}"

        val response = Unirest.get(url)
            .header("Authorization", "Basic $token")
            .header("Content-Type", ContentType.APPLICATION_JSON.toString())
            .header("Accept", "application/json")
            .asJson()

        val fieldsList = fields.split(",")
        val objList: MutableList<JSONObject> = ArrayList()
        val issues = response.body.`object`.getJSONArray("issues")

        issues.forEach { issue ->
            issue as JSONObject
            val obj = JSONObject()
            val key = issue.getString("key")
            obj.put(getFieldName("key"), key)
            val fieldsObject = issue.getJSONObject("fields")
            fieldsList.forEach { requiredField ->
                val type = getFieldType(requiredField)
                var value: String
                value = try {
                    when (type) {
                        "Object" -> fieldsObject.getJSONObject(requiredField).get("value").toString()
                        "ObjectStatus" -> fieldsObject.getJSONObject(requiredField).get("name").toString()
                        "ObjectUser" -> fieldsObject.getJSONObject(requiredField).get("displayName").toString()
                        "SLA" -> fieldsObject.getJSONObject(requiredField)
                            .getJSONObject("ongoingCycle")
                            .getJSONObject("remainingTime")
                            .get("friendly")
                            .toString()
                        "ObjectComment" -> {
                            val comments = fieldsObject.getJSONObject("comment").getJSONArray("comments")
                            comments.getJSONObject(comments.indexOf(comments.last())).getString("body")
                        }
                        else -> fieldsObject.get(requiredField).toString()
                    }
                } catch (e: JSONException) {
                    ""
                }
                if (value.contains(",")) value = value.substringBefore(",")
                when (value) {
                    "true" -> value = "Sim"
                    "false" -> value = "Não"
                    "null" -> value = ""
                }
                obj.put(getFieldName(requiredField), value)
            }
            objList.add(obj)
        }
        return if(returnAsCsv == true) {
            issuesToCSV(objList, fields)
        } else {
            objList
        }
    }

    fun getIssueByOwner(user: String): String {
        @Suppress("UNCHECKED_CAST") val issues = getIssues("reporter=${user}","fields=key") as MutableList<JSONObject>
        return try {
            val response = StringBuilder()
            issues.map { issue -> response.appendLine(issue.getString("key")) }
            response.toString()
        } catch (e: Exception) {
            "Ocorreu um erro, por favor informe ao TI"
        }
    }

    fun getIssueDetails(issueKey: String): String {
        return try {
            @Suppress("UNCHECKED_CAST") val issues = getIssues("key=${issueKey}","fields=key,comment,customfield_16038,status,assignee,created") as MutableList<JSONObject>
            val details = StringBuilder()
            details.appendLine("Chamado: ${issues[0].getString("key")}")
            details.appendLine("Status: ${issues[0].getString("status")}")
            details.appendLine("Última mensagem: ${issues[0].getString("comment")}")
            details.appendLine("Responsável: ${issues[0].getString("assignee")}")
            details.appendLine("Data Chamado: ${issues[0].getString("created")}")
            details.appendLine("Data Prazo (SLA): ${issues[0].getString("customfield_16038")}")
            details.toString()
        } catch (e: Exception) {
            "Ocorreu um erro, por favor informe ao TI"
        }
    }

    private fun issuesToCSV(objList: MutableList<JSONObject>, fields: String): String {
        val file = File.createTempFile("${this.hashCode()}","csv")
        file.deleteOnExit()
        val csvWriter = CSVWriter(file.writer(Charsets.UTF_8))
        csvWriter.flushQuietly()
        val lines: MutableList<MutableList<String>> = ArrayList()
        if (objList.isNotEmpty()) {
            var columns: MutableList<String> = ArrayList()
            val fieldList = fields.split(",")
            columns.add("${getFieldName("key")}")
            fieldList.forEach { columns.add("${getFieldName(it)}}") }
            lines.add(columns)
            objList.forEach { jsonObject ->
                columns = ArrayList()
                columns.add("${jsonObject.getString(getFieldName("key"))}")
                fieldList.forEach { field ->
                    columns.add("${jsonObject.getString(getFieldName(field))}")
                }
                lines.add(columns)
            }
        }
        csvWriter.writeAll(lines)
        csvWriter.flush()
        return file.readText(Charsets.UTF_8)
    }

    private fun getFieldType(field: String): String? {
        val props = Properties()
        props.setProperty("summary", "String")
        props.setProperty("created", "Date")
        props.setProperty("duedate", "Date")
        props.setProperty("customfield_13800", "Date")
        props.setProperty("status", "ObjectStatus")
        props.setProperty("assignee", "ObjectUser")
        props.setProperty("reporter", "ObjectUser")
        props.setProperty("comment", "ObjectComment")
        props.setProperty("customfield_15949", "Object")
        props.setProperty("customfield_16029", "Object")
        props.setProperty("customfield_16038", "SLA")
        return try {
            props.getProperty(field)
        } catch (e: Exception) {
            "String"
        }
    }

    private fun getFieldName(field: String): String? {
        val props = Properties()
        props.setProperty("key", "Chamado")
        props.setProperty("status", "Status")
        props.setProperty("summary", "Descrição")
        props.setProperty("created", "Data Criação")
        props.setProperty("duedate", "Data Acordada")
        props.setProperty("customfield_13800", "Data Acordada")
        props.setProperty("assignee", "Responsável")
        props.setProperty("reporter", "Solicitante")
        props.setProperty("customfield_15949", "Time Responsável")
        props.setProperty("customfield_16029", "Criticidade")
        props.setProperty("customfield_16038", "SLA Suporte")
        return try {
            props.getProperty(field)
        } catch (e: Exception) {
            field
        }
    }

}