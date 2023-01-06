package jiren.data.entity

import com.sun.istack.NotNull
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.persistence.*

@Entity(name = "shifts")
@Table(name = "shifts")
class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @NotNull
    @ManyToOne
    @JoinColumn(name = "partner_id", nullable = false)
    var plantonyst: User? = null

    @NotNull
    @Column(nullable = false)
    var start: Timestamp? = null

    @NotNull
    @Column(nullable = false)
    var end: Timestamp? = null

    fun setStart(time: LocalDateTime?) {
        if (time != null) this.start = Timestamp.valueOf(time)
    }

    fun getStart(): LocalDateTime? {
        return start?.toLocalDateTime()
    }

    fun setEnd(time: LocalDateTime?) {
        if (time != null) this.end = Timestamp.valueOf(time)
    }

    fun getEnd(): LocalDateTime? {
        return end?.toLocalDateTime()
    }

}