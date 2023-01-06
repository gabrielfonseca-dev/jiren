package jiren.data.repository

import jiren.data.entity.Automation
import jiren.data.enum.StatusAutomation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.sql.Timestamp

@Repository
interface AutomationRepository : JpaRepository<Automation, Long>, JpaSpecificationExecutor<Automation> {
    fun findByName(name: String): Automation?

    fun findByScheduleAndActiveAndStatus(
        scheduleAt: Timestamp,
        active: Boolean,
        status: StatusAutomation
    ): List<Automation>?
}