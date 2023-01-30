package jiren.data.entity.twilio

import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity(name = "evaluation_answer")
class EvaluationAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null

    @ManyToOne
    var instance: Instance? = null

    @NotNull
    @OneToOne
    var question: Question? = null

    @NotNull
    var answer: String? = null

}