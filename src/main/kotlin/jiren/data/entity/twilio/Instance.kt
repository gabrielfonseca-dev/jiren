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

    @Column(nullable = false)
    var createdAt: Timestamp = Timestamp.from(now())

    @ManyToOne
    @JoinColumn(name = "user_id")
    var user: User? = null

}