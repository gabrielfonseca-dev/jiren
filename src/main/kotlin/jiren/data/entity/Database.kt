package jiren.data.entity

import com.sun.istack.NotNull
import jiren.data.enum.SGBD
import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity(name = "databases")
@Table(name = "system_databases")
class Database {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @NotBlank
    @Column(nullable = false, unique = true)
    var name = ""

    @NotBlank
    @Column(nullable = false)
    var host = ""

    @NotNull
    @Column(nullable = false)
    var port: Int = 0

    @NotBlank
    @Column(nullable = false)
    var user = ""

    @NotBlank
    @Column(nullable = false)
    var secretName = ""

    @NotNull
    @Column(nullable = false)
    var sgbd: SGBD? = null

    @NotNull
    @Column(nullable = false)
    var timeout: Int = 120000

    @Column(nullable = false)
    var automationEnabled = false
        set(automationEnabled) {
            field = if(sgbd != SGBD.MongoDb) automationEnabled
            else false
        }

    @Column(nullable = false)
    var monitoringEnabled = false

    @Column(nullable = false)
    var scriptsEnabled = false
        set(scriptsEnable) {
            field = if(sgbd != SGBD.MongoDb) scriptsEnable
            else false
        }

    var sid: String? = ""

    var schemaName: String? = ""

    override fun toString(): String {
        return name
    }

}