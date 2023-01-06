package jiren.controller

import com.mashape.unirest.http.Unirest
import com.mongodb.client.MongoDatabase
import jiren.data.entity.Database
import jiren.data.entity.Monitoring
import jiren.data.enum.*
import jiren.data.repository.MonitoringRepository
import jiren.data.repository.specification.MonitoringSpecification
import jiren.service.apis.aws.CloudWatchClient
import jiren.service.apis.aws.SqsClient
import jiren.service.database.DataAccessObject
import jiren.service.database.DatabasePicker
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.entity.ContentType
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContextBuilder
import org.bson.BsonDocument
import org.bson.BsonInt64
import org.json.JSONObject
import org.springframework.core.io.ClassPathResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Controller
import java.io.InputStreamReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.security.cert.X509Certificate
import java.sql.Connection
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant.now
import java.time.temporal.ChronoUnit
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

@Controller
class MonitoringController(
    private var monitoringRepository: MonitoringRepository,
    private var dataAccessObject: DataAccessObject,
    private var databasePicker: DatabasePicker,
    private var logController: LogController,
    private var cloudWatchClient: CloudWatchClient,
    private var sqsClient: SqsClient
) {

    private var specification = MonitoringSpecification()

    fun search(text: String, db: Database?, type: MonitoringType?, status: StatusMonitoring?, inactive: Boolean): Page<Monitoring>? {
        if (db == null && type == null && status == null) {
            return monitoringRepository.findAll(
                Specification.where(
                    specification.inactive(inactive).and(specification.name(text).or(specification.command1(text)))
                ), Pageable.ofSize(50)
            )
        } else if (db != null && type != null && status != null) {
            return monitoringRepository.findAll(
                Specification.where(
                    specification.inactive(inactive).and(specification.name(text).or(specification.command1(text))).and(
                        specification.status(status).and(
                            specification.type(type).and(specification.system(db))
                        )
                    )
                ), Pageable.ofSize(50)
            )
        } else if (db != null && type != null) {
            return monitoringRepository.findAll(
                Specification.where(
                    specification.inactive(inactive).and(
                        specification.name(text).or(specification.command1(text)).and(
                            specification.type(type).and(specification.system(db))
                        )
                    )
                ), Pageable.ofSize(50)
            )
        } else if (db != null && status != null) {
            return monitoringRepository.findAll(
                Specification.where(
                    specification.inactive(inactive).and(
                        specification.name(text).or(specification.command1(text)).and(
                            specification.status(status).and(specification.system(db))
                        )
                    )
                ), Pageable.ofSize(50)
            )
        } else if (type != null && status != null) {
            return monitoringRepository.findAll(
                Specification.where(
                    specification.inactive(inactive).and(
                        specification.name(text).or(specification.command1(text)).and(
                            specification.status(status).and(specification.type(type))
                        )
                    )
                ), Pageable.ofSize(50)
            )
        } else if (db != null) {
            return monitoringRepository.findAll(
                Specification.where(
                    specification.inactive(inactive).and(
                        specification.name(text).or(specification.command1(text)).and(specification.system(db))
                    )
                ), Pageable.ofSize(50)
            )
        } else if (type != null) {
            return monitoringRepository.findAll(
                Specification.where(
                    specification.inactive(inactive).and(
                        specification.name(text).or(specification.command1(text)).and(
                            specification.type(type)
                        )
                    )
                ), Pageable.ofSize(50)
            )
        } else {
            return monitoringRepository.findAll(
                Specification.where(
                    specification.inactive(inactive).and(
                        specification.name(text).or(specification.command1(text)).and(
                            specification.status(status)
                        )
                    )
                ), Pageable.ofSize(50)
            )
        }
    }

    fun findMonitoringToRun(): List<Monitoring>? {
        return monitoringRepository.findByScheduleAtIsLessThanAndEnabledAndStatusIsNot(Timestamp.from(now()), true, StatusMonitoring.RUNNING)
    }

    fun getPanelData(): List<Monitoring>? {
        return monitoringRepository.findByShowInPanelAndEnabled(inPanel = true, active = true)
    }

    fun save(monitoring: Monitoring) {
        logController.saveLog(LogCode.MONITORING_INFO, LogType.UPDATE, null, monitoring.toJson(), monitoring.name,null)
        monitoringRepository.save(monitoring)
    }

    fun saveAll(monitorings: List<Monitoring>) {
        monitoringRepository.saveAll(monitorings)
    }

    fun delete(monitoring: Monitoring) {
        logController.saveLog(LogCode.MONITORING_INFO, LogType.DELETE, null, monitoring.toJson(), monitoring.name,null)
        monitoringRepository.delete(monitoring)
    }

    fun findById(id: Long): Optional<Monitoring> {
        return monitoringRepository.findById(id)
    }

    fun findByName(name: String): Monitoring? {
        return monitoringRepository.findByName(name)
    }

    fun execute(monitoring: Monitoring): String? {

        return when (monitoring.type!!) {

            MonitoringType.DATABASE -> {
                executeByDatabase(monitoring)
            }

            MonitoringType.HTTP -> {
                executeByHttp(monitoring)
            }

            MonitoringType.DATABASE_COMPARISON -> {
                executeByComparison(monitoring)
            }

            MonitoringType.SQS -> {
                executeBySQS(monitoring)
            }

            MonitoringType.CLOUDWATCH -> {
                executeByCloudWatch(monitoring)
            }

            MonitoringType.SAT -> {
                executeBySAT(monitoring)
            }

        }

    }

    private fun executeByDatabase(monitoring: Monitoring): String? {
        var result: String? = null

        when (monitoring.databaseOne!!.sgbd) {

            SGBD.MongoDb -> {

                try {
                    val mongodb = databasePicker.getConnection(monitoring.databaseOne!!) as MongoDatabase
                    val command = BsonDocument(monitoring.queryOne, BsonInt64(1))
                    val startTime = now()
                    result = mongodb.runCommand(command).toString()
                    logController.saveLog(LogCode.MONITORING_INFO, LogType.EXECUTE, now().minusMillis(startTime.toEpochMilli()).toEpochMilli(), "NOSQL_SUCCESS", monitoring.name, monitoring.queryOne)
                    if (result.isNotEmpty()) monitoring.onError()
                    else monitoring.onSuccess()
                    monitoring.reschedule()
                    monitoringRepository.save(monitoring)
                } catch (e: Exception) {
                    logController.saveLog(LogCode.MONITORING_ERROR, LogType.EXECUTE, null, e.stackTraceToString(), monitoring.name, monitoring.queryOne)
                }
            }

            else -> {
                try {
                    result = dataAccessObject.executeMonitoring(monitoring.queryOne, databasePicker.getConnection(monitoring.databaseOne!!) as Connection, monitoring.name)
                    if (result.isNotEmpty()) monitoring.onError()
                    else monitoring.onSuccess()
                    monitoring.reschedule()
                    monitoringRepository.save(monitoring)
                } catch (e: Exception) {
                    logController.saveLog(LogCode.MONITORING_ERROR, LogType.EXECUTE, null, e.stackTraceToString(), monitoring.name, null)
                }
            }
        }
        return result
    }

    private fun executeByHttp(monitoring: Monitoring): String? {

        val config = JSONObject(monitoring.httpConfig)
        val httpRequestUrl = config.getString("url")
        val httpTimeout = config.getLong("timeout")
        val httpContentType = config.getString("contentType")
        val httpResponseCode = config.getInt("expectedCode")
        val httpMethod = config.getString("method")
        val httpBody = config.getString("body")

        var result: String? = null

        val request = when (HttpAllowedMethods.valueOf(httpMethod)) {

            HttpAllowedMethods.GET -> HttpRequest.newBuilder().uri(URI.create(httpRequestUrl))
                .timeout(Duration.ofMinutes(httpTimeout))
                .header("Content-Type", httpContentType).GET().build()

            HttpAllowedMethods.POST -> {
                HttpRequest.newBuilder().uri(URI.create(httpContentType))
                    .timeout(Duration.ofMinutes(httpTimeout))
                    .header("Content-Type", httpContentType)
                    .POST(HttpRequest.BodyPublishers.ofString(httpBody)).build()
            }

        }

        try {
            val response = HttpClient.newBuilder().build().send(request, BodyHandlers.ofString())
            if (response.statusCode() != httpResponseCode) {
                monitoring.onError()
            } else {
                monitoring.onSuccess()
            }
            result = "${response.statusCode()}\n${response.body()}"
            logController.saveLog(LogCode.MONITORING_INFO, LogType.EXECUTE, null, "URL $httpRequestUrl responded with code $result", monitoring.name, null)
        } catch (e: Exception) {
            logController.saveLog(LogCode.MONITORING_ERROR, LogType.EXECUTE, null, e.stackTraceToString(), monitoring.name, null)
        }
        monitoring.reschedule()
        monitoringRepository.save(monitoring)
        return result

    }

    private fun executeByComparison(monitoring: Monitoring): String? {
        var result: String? = null

        val sourceResult: MutableList<String>? =
            getResultList(monitoring.name, monitoring.databaseOne!!, monitoring.queryOne)

        val targetResult: MutableList<String>? =
            getResultList(monitoring.name, monitoring.databaseTwo!!, monitoring.queryTwo)

        try {
            compare(sourceResult!!, targetResult!!).let { comparison ->
                if (!comparison.isNullOrEmpty()) {
                    monitoring.onError()
                    result = comparison.toString()
                } else {
                    monitoring.onSuccess()
                }
                monitoring.reschedule()
                monitoringRepository.save(monitoring)
            }
        } catch (e: Exception) {
            logController.saveLog(LogCode.MONITORING_ERROR, LogType.EXECUTE, null, e.stackTraceToString(), monitoring.name, null)
        }

        return result

    }

    private fun executeByCloudWatch(monitoring: Monitoring): String {

        val config = JSONObject(monitoring.cwConfig)
        val filter = config.getString("filter")
        val group = config.getString("logGroup")
        val range = config.getLong("minutesRange")
        val limit = config.getInt("logLimit")
        val start = now().minus(range, ChronoUnit.MINUTES).toEpochMilli()
        val end = now().toEpochMilli()
        val logArray = ArrayList<String>()
        val result = StringBuilder()
        var logQty = (limit + 1)

        try {
            val startTime = now()
            val logs = cloudWatchClient.getLogEvents(filter, start, end, group)
            if (logs != null) {
                logQty = logs.size
                logs.forEach { log ->
                    val logString = StringBuilder()
                    logString.appendLine("-- START --")
                    logString.appendLine("LogID/Hash => ${log.eventId()}")
                    logString.appendLine("Timestamp => ${log.timestamp()}")
                    logString.appendLine("LogStreamName => ${log.logStreamName()}")
                    logString.appendLine("Message => ${log.message()}")
                    logString.appendLine("-- END --")
                    logArray.add(logString.toString())
                }
            }
            logController.saveLog(LogCode.MONITORING_INFO, LogType.EXECUTE, now().minusMillis(startTime.toEpochMilli()).toEpochMilli(), "Lambda $group has $logQty logs", monitoring.name, filter)
        } catch (e: Exception) {
            logController.saveLog(LogCode.MONITORING_ERROR, LogType.EXECUTE, null, e.stackTraceToString(), monitoring.name, filter)
            logArray.add("${e.stackTrace}")
        }

        if (logQty > limit) {
            monitoring.onError()
            result.appendLine(monitoring.description)
            result.appendLine("CloudWatch LogGroups => $group")
            result.appendLine("LogsResult Qty => $logQty")
            result.appendLine("Log Filter => $filter")
            if ( monitoring.documentURL?.trim() != "-" && !(monitoring.documentURL.isNullOrEmpty()) ) {
                result.appendLine("Documento => ${monitoring.documentURL}")
            }
            result.appendLine("::logs::")
            logArray.forEach { logLine -> result.appendLine(logLine) }
        } else {
            monitoring.onSuccess()
        }

        monitoring.reschedule()
        monitoringRepository.save(monitoring)
        return result.toString()

    }

    private fun executeBySQS(monitoring: Monitoring): String {

        val config = JSONObject(monitoring.sqsConfig)
        val queueName = config.getString("queueName")
        val limit = config.getInt("msgLimit")
        val startTime = now()
        val result = StringBuilder()
        var msgQty = (limit + 1)

        try {
            msgQty = sqsClient.getMessageQuantity(queueName)
            logController.saveLog(LogCode.MONITORING_INFO, LogType.EXECUTE, now().minusMillis(startTime.toEpochMilli()).toEpochMilli(), "Queue: $queueName has $msgQty messages", monitoring.name, null)
        } catch (e: Exception) {
            result.appendLine(e.message)
            logController.saveLog(LogCode.MONITORING_ERROR, LogType.EXECUTE, null, e.stackTraceToString(), monitoring.name, null)
        }

        if (msgQty > limit) {
            monitoring.onError()
            result.appendLine("DLQ => $queueName")
            result.appendLine("Message Qty => $msgQty")
            result.appendLine("Message Limit => $limit")
        } else {
            monitoring.onSuccess()
        }

        monitoring.reschedule()
        monitoringRepository.save(monitoring)
        return result.toString()

    }

    private fun executeBySAT(monitoring: Monitoring): String {

        val sslContext = SSLContextBuilder().loadTrustMaterial(null, object : TrustSelfSignedStrategy() {
            override fun isTrusted(chain: Array<X509Certificate?>?, authType: String?): Boolean {
                return true
            }
        }).build()

        val customHttpClient: org.apache.http.client.HttpClient = HttpClients.custom().setSSLContext(sslContext)
            .setSSLHostnameVerifier(NoopHostnameVerifier()).build() as org.apache.http.client.HttpClient
        Unirest.setHttpClient(customHttpClient)

        val config = JSONObject(monitoring.satConfig)
        val range = config.getLong("daysRange")
        val satNumber = config.getLong("satNumber")
        val ufCode = config.getInt("ufCode")
        val key = config.getString("key")
        var saleQty = 0

        try {

            val dtf = SimpleDateFormat("dd-MM-yyyy")
            val startDay = dtf.format(Date.from(now().minus(range, ChronoUnit.DAYS).minus(3, ChronoUnit.HOURS)))
                .toString().replace("-", "")
            val today = dtf.format(Date.from(now().minus(3, ChronoUnit.HOURS)))
                .toString().replace("-", "")

            val template = InputStreamReader(ClassPathResource("META-INF/resources/templates/soap-template.xml").inputStream).readText()

            val body = template
                .replace("//satNumber","$satNumber")
                .replace("//startDate","${startDay}000000")
                .replace("//endDate","${today}235959")
                .replace("//key",key)
                .replace("//ufCode","$ufCode")

            val startTime = now()
            val response = Unirest.post("https://wssatsp.fazenda.sp.gov.br/CfeConsultarLotes/CfeConsultarLotes.asmx")
                .header("Content-Type", "${ContentType.TEXT_XML}")
                .body(body)
                .asString()

            if (response.status != 200) {
                logController.saveLog(LogCode.MONITORING_ERROR, LogType.EXECUTE, null, "statusCode:${response.status}\n${response.body}", monitoring.name, null)
            }

            val responseBody = response.body.replace("&gt;",">").replace("&lt;","<")

            val xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(responseBody.byteInputStream())
            xml.documentElement.normalize()

            saleQty = xml.getElementsByTagName("soap:Body").item(0)
                .childNodes.item(0)
                .childNodes.item(0)
                .childNodes.item(0)
                .childNodes.item(1)
                .childNodes.item(5).textContent.toInt()

            logController.saveLog(LogCode.MONITORING_INFO, LogType.EXECUTE, now().minusMillis(startTime.toEpochMilli()).toEpochMilli(), "SAT $satNumber has $saleQty sales", monitoring.name, null)

            if(saleQty > 0) {
                monitoring.onSuccess()
            } else {
                monitoring.onError()
            }

        } catch (e: Exception) {
            monitoring.onError()
            logController.saveLog(LogCode.MONITORING_ERROR, LogType.EXECUTE, null, e.stackTraceToString(), monitoring.name, null)
        }

        monitoring.reschedule()
        monitoringRepository.save(monitoring)
        return "SAT $satNumber has $saleQty sales"

    }

    private fun getResultList(monitoring: String, database: Database, cmd: String): MutableList<String>? {
        when (database.sgbd) {
            SGBD.MongoDb -> {
                return try {
                    val resultList: MutableList<String> = ArrayList()
                    val mongodb = databasePicker.getConnection(database) as MongoDatabase
                    val command = BsonDocument(cmd, BsonInt64(1))
                    val startTime = now()
                    mongodb.runCommand(command).values.forEach { resultList.add("$it") }
                    logController.saveLog(LogCode.MONITORING_INFO, LogType.EXECUTE, now().minusMillis(startTime.toEpochMilli()).toEpochMilli(), "NOSQL_SUCCESS", monitoring, cmd)
                    resultList
                } catch (e: Exception) {
                    logController.saveLog(LogCode.MONITORING_ERROR, LogType.EXECUTE, null, e.stackTraceToString(), monitoring, cmd)
                    null
                }
            }
            else -> {
                return try {
                    dataAccessObject.executeMonitoringToList(cmd, databasePicker.getConnection(database) as Connection, monitoring)
                } catch (e: Exception) {
                    logController.saveLog(LogCode.MONITORING_ERROR, LogType.EXECUTE, null, e.stackTraceToString(), monitoring, cmd)
                    null
                }
            }

        }
    }

    private fun compare(sourceList: MutableList<String>, targetList: MutableList<String>): MutableList<String>? {
        val diff = targetList.containsAll(sourceList)
        if (!diff) {
            sourceList.removeAll(targetList.toSet())
            return sourceList
        }
        return null
    }

}