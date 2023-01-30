package jiren.data.entity.twilio

import jiren.data.entity.User
import java.sql.Timestamp
import java.time.Instant.now
import javax.persistence.*

@Entity(name = "instance")
class Instance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null

    @Column(nullable = false)
    var contact: String? = null

    var contactName: String? = null

    var twilioNumber: String? = null

    @Column(nullable = false)
    var isOpen: Boolean = true
        set(isOpen) {
            field = isOpen
            if(!isOpen) closedAt = Timestamp.from(now())
        }

    var hadTransfer: Boolean = false

    @Column(nullable = false)
    var createdAt: Timestamp = Timestamp.from(now())

    private var closedAt: Timestamp? = null

    var chatAwaitStart: Timestamp? = null

    var chatAwaitEnd: Timestamp? = null

    @ManyToOne
    var evaluationModel: EvaluationModel? = null

    var evaluated: Boolean = false

    @ManyToOne
    @JoinColumn(name = "user_id")
    var user: User? = null

}