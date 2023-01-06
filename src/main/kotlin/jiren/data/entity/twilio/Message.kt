package jiren.data.entity.twilio

import jiren.data.enum.MessageTypes
import org.joda.time.DateTime
import java.sql.Timestamp
import java.time.Instant.now
import javax.persistence.*

@Entity(name = "message")
class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null
    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.REFRESH], targetEntity = Instance::class)
    var instance: Instance? = null
    @Enumerated(EnumType.STRING)
    var type: MessageTypes? = null
    @Column(nullable = false)
    var fromNumber: String? = null
    @Column(nullable = false)
    var toNumber: String? = null
    @Column(nullable = false)
    var body: String? = null
    var code: String? = null
    var answered: Boolean = false
    var messageSid: String? = null
    var numMedia: Int? = null
    var referralNumMedia: Int? = null
    var mediaContentType: String? = null
    var mediaUrl: String? = null
    var errorMessage: String? = null
    var price: String? = null
    var direction: String? = null
    var dateUpdated: String? = null
    var uri: String? = null
    var dateSent: String? = null
    var dateCreated: String? = null
    var errorCode: String? = null
    var priceUnit: String? = null
    var apiVersion: String? = null
    var statusCallback: String? = null
    var profileName: String? = null
    var forwarded: String? = null
    var createdAt: Timestamp? = Timestamp.from(now())

    fun outcoming(code: String, instance: Instance, from: String, to: String, body: String): Message {
        this.code = code
        this.instance = instance
        this.fromNumber = from
        this.toNumber = to
        this.body = body
        this.type = MessageTypes.OUTCOMING
        this.createdAt = Timestamp.from(now())
        return this
    }

    fun incoming(code: String, instance: Instance, answered: Boolean): Message {
        this.code = code
        this.instance = instance
        this.answered = answered
        this.type = MessageTypes.INCOMING
        this.createdAt = Timestamp.from(now())
        return this
    }

}