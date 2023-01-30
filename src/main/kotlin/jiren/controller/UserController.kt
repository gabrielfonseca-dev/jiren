package jiren.controller

import jiren.controller.twilio.InstanceController
import jiren.data.entity.Permission
import jiren.data.entity.User
import jiren.data.entity.twilio.Instance
import jiren.data.enum.*
import jiren.data.repository.ParameterRepository
import jiren.data.repository.PermissionRepository
import jiren.data.repository.ShiftRepository
import jiren.data.repository.UserRepository
import jiren.data.repository.specification.UserSpecification
import jiren.security.SecurityService
import jiren.service.apis.rocketchat.RocketChatClient
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant.now
import java.util.*
import java.util.stream.Collectors

@Service
class UserController(
    private var userRepository: UserRepository,
    private var shiftRepository: ShiftRepository,
    private var permissionRepository: PermissionRepository,
    private var parameterRepository: ParameterRepository,
    private var rocketChatClient: RocketChatClient,
    private var instanceController: InstanceController,
    private var logController: LogController
) {

    private val specification = UserSpecification()

    fun search(param: String, boolean: Boolean): Page<User> {
        return userRepository.findAll(
            Specification.where(
                specification.name(param).or(
                    specification.login(param)
                )
                    .or(specification.email(param))
                    .or(specification.document(param))
            )
                .and(specification.inactive(boolean)),
            Pageable.ofSize(50)
        )
    }

    fun userAlreadyExists(name: String, username: String, email: String, document: String): Boolean {
        return userRepository.findByNameOrUsernameOrEmailOrDocument(name, username, email, document) != null
    }

    fun findById(id: Long): Optional<User> {
        return userRepository.findById(id)
    }

    fun findMenuListByRole(role: String): List<Permission> {
        return permissionRepository.listRoleMenu(role, PermissionType.MENU)
    }

    fun findByUsername(name: String): User? {
        return userRepository.findByUsername(name)
    }

    fun loggedUserHasPermission(permissionCode: String): Boolean {
        return userRepository.findByUsername(
            SecurityService().authenticatedUser ?: ""
        )?.role?.permissions?.find { it.code == permissionCode } != null
    }

    fun updateStatus(user: User) {
        userRepository.save(user)
        try {
            if(user.enableShift) {
                val roomId = "${parameterRepository.findByCode("${Parameters.ROCKETCHAT_TEAM_ROOM}")}"
                rocketChatClient.sendMessage("${user.name} está ${user.status.toString().lowercase()}", roomId)
            }
        } catch (e: Exception) {
            logController.saveLog(LogCode.SYSTEM_ERROR, LogType.EXECUTE, null, e.stackTraceToString(), null, null)
        }
    }

    fun save(user: User): User {
        logController.saveLog(LogCode.USER_INFO, LogType.UPDATE, null, user.toJson(), null, null)
        userRepository.save(user)
        return user
    }

    fun getAvailableUser(instance: Instance): String? {
        val connectedUsers = userRepository.findByStatus(StatusUser.CONECTADO)?.toMutableList()
        val shifts = shiftRepository.dateBetween(Timestamp.from(now()))
        val user: User?
        if(connectedUsers.isNullOrEmpty()) {
            return if(shifts.isNullOrEmpty()) {
                null
            } else {
                val availableOfficers: MutableList<User?> = shifts.map { it.plantonyst }.toMutableList()
                user = availableOfficers[0]
                assigneeInstance(instance, user!!)
                "whatsapp:+55${user.phone?.replace("-","")?.replace("(","")?.replace(")","")}::SHIFT"
            }
        } else {
            val availableUsers: MutableList<User> = ArrayList()
            availableUsers.addAll(connectedUsers)
            availableUsers.removeIf { cUser -> getOpenInstances(cUser).isNotEmpty() }
            return if(availableUsers.isEmpty()) {
                null
            } else {
                user = availableUsers[0]
                assigneeInstance(instance, user)
                "whatsapp:+55${user.phone?.replace("-","")?.replace("(","")?.replace(")","")}"
            }
        }
    }

    fun assigneeInstance(instance: Instance, user: User) {
        instance.user = user
        instance.chatAwaitEnd = Timestamp.from(now())
        instanceController.instanceRepository.save(instance)
    }

    fun closeInstance(instance: Instance) {
        instanceController.close(instance)
    }

    fun getOpenInstances(user: User?): List<Instance> {
        return if (user != null) {
            instanceController.instanceRepository.findOpenInstanceByUser(user.id!!)
        } else {
            listOf()
        }
    }

    fun generatePassword(): String {
        val upperCaseLetters = RandomStringUtils.random(4, 65, 90, true, true)
        val lowerCaseLetters = RandomStringUtils.random(4, 97, 122, true, true)
        val numbers = RandomStringUtils.randomNumeric(4)
        val specialChar = RandomStringUtils.random(4, 33, 47, false, false)
        val totalChars = RandomStringUtils.randomAlphanumeric(4)
        val combinedChars = upperCaseLetters + lowerCaseLetters + numbers + specialChar + totalChars
        val pwdChars = combinedChars.chars()
            .mapToObj { c: Int -> c.toChar() }
            .collect(Collectors.toList())
        pwdChars.shuffle()
        return pwdChars.stream().map(Char::toString).collect(Collectors.joining())
    }

}