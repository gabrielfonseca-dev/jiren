package jiren.data.repository

import jiren.data.entity.Permission
import jiren.data.enum.PermissionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PermissionRepository : JpaRepository<Permission, Long> {
    @Query("select p from role g join g.permissions p where g.name = :name and p.type = :type and p.active = 1 order by p.position asc")
    fun listRoleMenu(name: String, type: PermissionType): List<Permission>
    fun findByCode(name: String): Permission?
}