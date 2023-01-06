package jiren.data.entity.twilio

import javax.persistence.*

@Entity(name = "ticket")
@Table(name = "tickets", schema = "jiren")
class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null

    @OneToOne
    lateinit var instance: Instance
    var ticketKey: String? = null
    var title: String? = null
    var description: String? = null
    var owner: String? = null
    var hasAttachments: Boolean = false
    var attachments: String? = null
}