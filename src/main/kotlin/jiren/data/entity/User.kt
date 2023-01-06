package jiren.data.entity

import jiren.data.enum.StatusUser
import org.json.JSONObject
import org.springframework.lang.Nullable
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

@Entity(name = "user")
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false)
    var enabled: Boolean = false

    @NotBlank
    @Column(nullable = false)
    var name: String = ""

    @Column(nullable = true, unique = true)
    @NotBlank
    @Email
    var email: String = ""

    @NotBlank
    @Column(nullable = false, unique = true)
    var username: String = ""

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.MERGE], targetEntity = Role::class, optional = true)
    @Nullable
    var role: Role? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    var status: StatusUser? = null

    @NotBlank
    @Column(unique = true)
    var document: String? = null
    var phone: String? = null
    var chat: String? = null
    var changePassword: Boolean = true
    var enableShift: Boolean = false

    @NotBlank
    @Column(nullable = false)
    var password: String? = null
        set(password) {
            if (password != null) field = BCryptPasswordEncoder(5).encode(password)
        }

    fun toJson(): String {
        val json = JSONObject()
        json.put("id",id)
        json.put("name",name)
        json.put("enabled",enabled)
        json.put("email",email)
        json.put("username",username)
        json.put("role",role?.name)
        json.put("status",status)
        json.put("document",document)
        json.put("phone",phone)
        json.put("chat",chat)
        return json.toString()
    }

    override fun toString(): String {
        return this.username
    }

}