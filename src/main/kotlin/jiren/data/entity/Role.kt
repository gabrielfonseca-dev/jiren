package jiren.data.entity

import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity(name = "role")
@Table(name = "role")
class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @NotBlank
    @Column(nullable = false, unique = true)
    lateinit var name: String

    @ManyToMany(fetch = FetchType.EAGER, cascade = [CascadeType.MERGE])
    lateinit var permissions: MutableList<Permission>

    override fun toString(): String {
        return name
    }

}