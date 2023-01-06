package jiren.data.entity

import jiren.data.enum.StatusAutomation
import org.json.JSONObject
import java.sql.Timestamp
import java.time.Instant.now
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity(name = "automation")
@Table(name = "automation")
class Automation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @NotBlank
    var name: String? = null

    @NotBlank
    @Column(nullable = false)
    var query: String? = null

    @ManyToOne
    @JoinColumn(name = "database_id")
    @NotNull
    var database: Database? = null

    @NotNull
    @Column(nullable = false)
    var active: Boolean = false

    @NotNull
    var schedule: Timestamp? = null

    @NotNull
    @Column(nullable = false)
    var scheduleConfig: Int = 0
    var ranAt: Timestamp? = null

    @Enumerated(EnumType.STRING)
    var status: StatusAutomation = StatusAutomation.WAITING
    var documentUrl: String? = null

    fun setSchedule(schedule: LocalDateTime?) {
        if (schedule != null) this.schedule = Timestamp.valueOf(schedule)
    }

    fun getSchedule(): LocalDateTime? {
        return schedule?.toLocalDateTime()
    }

    fun reschedule() {
        schedule = Timestamp.from(now().plusMillis((scheduleConfig * 60 * 1000).toLong()))
        ranAt = Timestamp.from(now())
        status = StatusAutomation.WAITING
    }

    fun toJson(): String {
        val json = JSONObject()
        json.put("id",id)
        json.put("name",name)
        json.put("query",query)
        json.put("database",database?.host)
        json.put("active",active)
        json.put("schedule",schedule)
        json.put("scheduleConfig",scheduleConfig)
        json.put("ranAt",ranAt)
        json.put("status",status)
        json.put("document",documentUrl)
        return json.toString()
    }

}