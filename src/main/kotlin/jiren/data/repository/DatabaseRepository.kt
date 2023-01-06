package jiren.data.repository

import jiren.data.entity.Database
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface DatabaseRepository : JpaRepository<Database, Long>, JpaSpecificationExecutor<Database> {
    fun findByScriptsEnabledTrue(): List<Database>
    fun findByAutomationEnabledTrue(): List<Database>
    fun findByMonitoringEnabledTrue(): List<Database>
}