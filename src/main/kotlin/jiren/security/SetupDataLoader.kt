package jiren.security

import jiren.data.entity.Parameter
import jiren.data.entity.Permission
import jiren.data.entity.Role
import jiren.data.entity.User
import jiren.data.enum.Parameters
import jiren.data.enum.PermissionType
import jiren.data.repository.ParameterRepository
import jiren.data.repository.PermissionRepository
import jiren.data.repository.RoleRepository
import jiren.data.repository.UserRepository
import jiren.view.*
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SetupDataLoader(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val privilegeRepository: PermissionRepository,
    private val parameterRepository: ParameterRepository
) : ApplicationListener<ContextRefreshedEvent?> {

    private var alreadySetup = false

    @Transactional
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        if (alreadySetup) return

        val views = listOf(
            AutomationView::class.java.simpleName,
            ConfigurationView::class.java.simpleName,
            ChatView::class.java.simpleName,
            LogView::class.java.simpleName,
            MonitoringView::class.java.simpleName,
            ScriptView::class.java.simpleName,
            ShiftView::class.java.simpleName,
            SqlView::class.java.simpleName,
            UserView::class.java.simpleName
        )

        val writePrivilege: Permission = createPrivilegeIfNotFound("WRITE")
        val deletePrivilege: Permission = createPrivilegeIfNotFound("DELETE")
        val executePrivilege: Permission = createPrivilegeIfNotFound("EXECUTE")

        val adminPrivileges: MutableList<Permission> =
            listOf(
                writePrivilege,
                deletePrivilege,
                executePrivilege
            ).toMutableList()

        views.forEach {
            adminPrivileges.add(createViewsIfNotFound(it, views.indexOf(it)))
        }

        createRoleIfNotFound("ADMIN", adminPrivileges)
        createRoleIfNotFound("TI", listOf(executePrivilege))
        createRoleIfNotFound("USER", listOf(executePrivilege))

        val adminRole: Role? = roleRepository.findByName("ADMIN")
        var defaultUser = parameterRepository.findByCode("${Parameters.SYSTEM_USERNAME}")?.value
        if (defaultUser.isNullOrEmpty()) defaultUser = Security.defaultSystemUser
        if (userRepository.findByUsername(defaultUser) == null) {
            val user = User()
            user.name = defaultUser
            user.username = defaultUser
            user.password = defaultUser
            user.email = "admin@jiren.com.br"
            user.role = adminRole
            user.enabled = true
            user.document = "000.000.000-00"
            user.changePassword = false
            userRepository.save<User>(user)
        }
        createParametersIfNotFound()
        alreadySetup = true
    }

    @Transactional
    fun createPrivilegeIfNotFound(name: String?): Permission {
        var privilege: Permission? = privilegeRepository.findByCode(name.toString())
        if (privilege == null) {
            privilege = Permission()
            privilege.code = name!!
            privilege.type = PermissionType.AUTHORITY
            privilege.active = 1
            privilegeRepository.save(privilege)
        }
        return privilege
    }

    @Transactional
    fun createRoleIfNotFound(name: String, privileges: List<Permission>): Role? {
        var role: Role? = name.let { roleRepository.findByName(it) }
        if (role == null) {
            role = Role()
            role.name = name
            role.permissions = privileges.toMutableList()
            roleRepository.save(role)
        }
        return role
    }

    @Transactional
    fun createParametersIfNotFound() {
        Parameters.values().forEach {
            if (parameterRepository.findByCode(it.toString()) == null) {
                val p = Parameter()
                p.code = it.toString()
                parameterRepository.save(p)
            }
        }
    }

    @Transactional
    fun createViewsIfNotFound(code: String, position: Int): Permission {
        var view: Permission? = privilegeRepository.findByCode(code)
        if (view == null) {
            view = Permission()
            view.code = code
            view.position = position
            view.description = code.substringBefore("View")
            view.active = 1
            view.icon = getIcon(code)
            view.type = PermissionType.MENU
            privilegeRepository.save(view)
        }
        return view
    }

    fun getIcon(view: String): String {
        return when(view) {
            "SqlView" -> "code"
            "AutomationView" -> "clock"
            "MonitoringView" -> "warning"
            "ShiftView" -> "user-clock"
            "UserView" -> "users"
            "ConfigurationView" -> "cogs"
            "LogView" -> "file-text"
            "ScriptView" -> "file"
            "LiveChatView" -> "chat"
            else -> ""
        }
    }

}