package jiren.controller

import jiren.data.entity.Database
import jiren.data.entity.Script
import jiren.data.enum.LogCode
import jiren.data.enum.LogType
import jiren.data.repository.ScriptRepository
import jiren.data.repository.specification.ScriptSpecification
import jiren.security.SecurityService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Controller

@Controller
class ScriptController(
    private var scriptRepository: ScriptRepository,
    private var logController: LogController,
    private var userController: UserController
) {

    private var specification = ScriptSpecification()

    fun search(text: String, db: Database?, inactive: Boolean): Page<Script>? {
        return if (db == null) {
            scriptRepository.findAll(
                Specification.where(
                    specification.inactive(inactive)
                        .and(specification.name(text).or(specification.query(text)))
                ), Pageable.ofSize(50)
            )
        }  else {
            scriptRepository.findAll(
                Specification.where(
                    specification.inactive(inactive)
                        .and(specification.name(text).or(specification.query(text))).and(specification.database(db))
                ), Pageable.ofSize(50)
            )
        }
    }

    fun saveAll(scripts: List<Script>) {
        scriptRepository.saveAll(scripts)
    }

    fun options(): List<Script> {
        return scriptRepository.findByScriptActiveTrue()
    }

    fun findByName(name: String): Script? {
        return scriptRepository.findByName(name)
    }

    fun delete(script: Script) {
        logController.saveLog(LogCode.SCRIPT_INFO, LogType.DELETE, null, script.toJson(), "SCRIPT_TEMPLATE", script.query)
        return scriptRepository.deleteById(script.id)
    }

    fun save(script: Script): Script {
        logController.saveLog(LogCode.SCRIPT_INFO, LogType.UPDATE, null, script.toJson(), script.name, script.query)
        script.owner = userController.findByUsername(SecurityService().authenticatedUser!!)
        scriptRepository.save(script)
        return script
    }

}