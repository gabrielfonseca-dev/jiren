package jiren.controller

import jiren.data.entity.Log
import jiren.data.entity.User
import jiren.data.enum.LogCode
import jiren.data.enum.LogType
import jiren.data.enum.Parameters
import jiren.data.repository.LogRepository
import jiren.data.repository.ParameterRepository
import jiren.data.repository.UserRepository
import jiren.data.repository.specification.LogSpecification
import jiren.security.Security
import jiren.security.SecurityService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Controller
import java.sql.Timestamp
import java.time.Instant.now
import javax.annotation.PostConstruct

@Controller
class LogController(private var logRepository: LogRepository, private var userRepository: UserRepository, private var parameterRepository: ParameterRepository) {

    private var specification = LogSpecification()
    private var systemUser: User? = null

    @PostConstruct
    private fun init() {
        try {
            systemUser = userRepository.findByUsername(parameterRepository.findByCode(Parameters.SYSTEM_USERNAME.toString())?.value ?: Security.defaultSystemUser)
        } catch (e: Exception) {
            println(e.message)
        }
    }

    fun search(text: String): Page<Log>? {
        return logRepository.findAll(Specification.where(specification.script(text).or(specification.task(text)).or(specification.value(text))), Pageable.ofSize(50))
    }

    fun saveLog(code: LogCode, type: LogType, elapsedTime: Long?, value: String, task: String? = null, script: String? = null) {
        val log = Log()
        log.user = userRepository.findByUsername(SecurityService().authenticatedUser ?: "") ?: systemUser
        log.code = code
        log.type = type
        log.occurredAt = Timestamp.from(now())
        log.elapsedTime = elapsedTime
        log.value = value
        log.task = task
        log.script = script
        logRepository.save(log)
    }

    fun saveSystemLog(code: LogCode, type: LogType, elapsedTime: Long?, value: String, task: String? = null, script: String? = null) {
        val log = Log()
        log.user = systemUser
        log.code = code
        log.type = type
        log.occurredAt = Timestamp.from(now())
        log.elapsedTime = elapsedTime
        log.value = value
        log.task = task
        log.script = script
        logRepository.save(log)
    }

}