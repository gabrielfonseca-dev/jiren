package jiren.data.entity.twilio

import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity(name = "evaluation_model")
class EvaluationModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null

    var title: String? = null

    @Column(unique = true)
    var active: Boolean? = null
        set(active) { field = if(active == true) active else null }

    @NotNull
    @ManyToMany
    var questions: MutableSet<Question>? = null

}