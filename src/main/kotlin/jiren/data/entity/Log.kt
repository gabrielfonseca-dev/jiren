package jiren.data.entity

import jiren.data.enum.LogCode
import jiren.data.enum.LogType
import java.sql.Timestamp
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity(name = "log")
@Table(name = "log")
class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long = 0
    @ManyToOne(optional = true, fetch = FetchType.LAZY, cascade = [CascadeType.MERGE])
    var user: User? = null
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var code: LogCode? = null
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var type: LogType? = null
    @Column(nullable = false)
    @NotNull
    var occurredAt: Timestamp? = null
    var elapsedTime: Long? = null
    @Lob
    var value: String? = null
    @Lob
    var script: String? = null
    var task: String? = null
}