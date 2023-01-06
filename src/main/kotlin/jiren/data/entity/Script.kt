package jiren.data.entity

import org.json.JSONObject
import java.sql.Timestamp
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity(name = "script")
@Table(name = "script")
class Script {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @NotBlank
    var name: String? = null

    @NotBlank
    var description: String? = null

    @NotBlank
    @Column(nullable = false)
    var query: String? = null

    @ManyToOne
    @JoinColumn(name = "database_id")
    @NotNull
    var database: Database? = null

    @NotNull
    @Column(nullable = false)
    var active: Boolean = false

    @NotNull
    var created: Timestamp? = null

    @OneToOne
    var owner: User? = null

    fun toJson(): String {
        val json = JSONObject()
        json.put("id",id)
        json.put("name",name)
        json.put("description",description)
        json.put("query",query)
        json.put("database",database?.host)
        json.put("active",active)
        json.put("created",created)
        json.put("username",owner?.username)
        return json.toString()
    }

    override fun toString(): String {
        return "${this.name}"
    }

}