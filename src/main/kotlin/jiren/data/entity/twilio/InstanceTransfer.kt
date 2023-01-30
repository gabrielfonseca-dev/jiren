package jiren.data.entity.twilio

import jiren.data.entity.User
import java.sql.Timestamp
import java.time.Instant.now
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity(name = "instance_transfer")
class InstanceTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne
    var instance: Instance? = null

    @ManyToOne
    var fromUser: User? = null

    @ManyToOne
    var toUser: User? = null

    var occurredAt: Timestamp = Timestamp.from(now())

}