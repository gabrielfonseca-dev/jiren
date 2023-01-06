package jiren.data.entity

import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity(name = "parameter")
@Table(name = "parameter")
class Parameter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
    @Column(nullable = false, unique = true)
    @NotBlank
    var code: String? = ""
    @Lob
    var value: String? = ""
    override fun toString(): String {
        return this.value.toString()
    }
}
