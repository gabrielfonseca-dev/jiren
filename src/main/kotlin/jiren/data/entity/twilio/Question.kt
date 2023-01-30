package jiren.data.entity.twilio

import javax.persistence.*

@Entity(name = "question")
class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null

    var text: String? = null

    var order: Int = 0

}