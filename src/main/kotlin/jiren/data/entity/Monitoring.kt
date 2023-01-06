package jiren.data.entity

import jiren.data.enum.MonitoringType
import jiren.data.enum.StatusMonitoring
import org.json.JSONObject
import java.sql.Timestamp
import java.time.Instant.now
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity(name = "monitoring")
@Table(name = "monitoring")
class Monitoring {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @NotBlank
    @Column(nullable = false, unique = true)
    var name: String = ""

    var description: String = ""

    @NotNull
    var executionInterval: Int = 999

    @NotNull
    var errorCount: Int = 0

    @NotNull
    var enabled: Boolean = false

    @NotNull
    var showInPanel: Boolean = false

    @NotNull
    var emailNotification: Boolean = false
    var mailTo: String? = null

    @NotNull
    var rocketNotification: Boolean = false
    var rocketchatRoom: String? = null
    var rocketchatAttachment: Boolean = false

    @NotNull
    var jiraNotification: Boolean = false
    var jiraIssue: String? = null
    var jiraLabels: String? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    var type: MonitoringType? = null

    var httpConfig: String = ""
    var satConfig: String = ""
    var sqsConfig: String = ""
    var cwConfig: String = ""

    @Enumerated(EnumType.STRING)
    var status: StatusMonitoring? = null
        set(status: StatusMonitoring?) {
            lastStatus = field
            field = status
        }

    @Enumerated(EnumType.STRING)
    var lastStatus: StatusMonitoring? = null

    @NotNull
    var scheduleAt: Timestamp? = null

    @ManyToOne
    @JoinColumn(nullable = true)
    var databaseOne: Database? = null

    @ManyToOne
    @JoinColumn(nullable = true)
    var databaseTwo: Database? = null

    var queryOne: String = ""
    var queryTwo: String = ""

    var documentURL: String? = null
    var firstReport: Timestamp? = null
    var lastReport: Timestamp? = null
    var ranAt: Timestamp? = null

    fun onSuccess() {
        status = StatusMonitoring.OK
        firstReport = null
        lastReport = null
        ranAt = Timestamp.from(now())
        errorCount = 0
    }

    fun onError() {
        status = StatusMonitoring.NOK
        if (firstReport == null) firstReport = Timestamp.from(now())
        lastReport = Timestamp.from(now())
        ranAt = Timestamp.from(now())
        errorCount++
    }

    fun reschedule() {
        scheduleAt = Timestamp.from(now().plusMillis((executionInterval * 60 * 1000).toLong()))
    }

    fun setScheduleAt(scheduleAt: LocalDateTime?) {
        if (scheduleAt != null) this.scheduleAt = Timestamp.valueOf(scheduleAt)
    }

    fun getScheduleAt(): LocalDateTime? {
        return scheduleAt?.toLocalDateTime()
    }

    fun toJson(): String {
        val json = JSONObject()
        json.put("id",id)
        json.put("name",name)
        json.put("description",description)
        json.put("executionInterval",executionInterval)
        json.put("errorCount",errorCount)
        json.put("enabled",enabled)
        json.put("showInPanel",showInPanel)
        json.put("emailNotification",emailNotification)
        json.put("mailTo",mailTo)
        json.put("rocketNotification",rocketNotification)
        json.put("rocketchatRoom",rocketchatRoom)
        json.put("rocketchatAttachment",rocketchatAttachment)
        json.put("jiraNotification",jiraNotification)
        json.put("jiraIssue",jiraIssue)
        json.put("jiraLabels",jiraLabels)
        json.put("type",type)
        json.put("httpConfig",httpConfig)
        json.put("satConfig",satConfig)
        json.put("sqsConfig",sqsConfig)
        json.put("cwconfig",cwConfig)
        json.put("satus",status)
        json.put("lastStatus",lastStatus)
        json.put("scheduleAt",scheduleAt)
        json.put("databaseOne",databaseOne?.host)
        json.put("databaseTwo",databaseTwo?.host)
        json.put("queryOne",queryOne)
        json.put("queryTwo",queryTwo)
        json.put("documentURL",documentURL)
        json.put("firstReport",firstReport)
        json.put("lastReport",lastReport)
        json.put("ranAt",ranAt)
        return json.toString()
    }

}