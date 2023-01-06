package jiren.data.repository

import jiren.data.entity.Monitoring
import jiren.data.enum.StatusMonitoring
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.sql.Timestamp

@Repository
interface MonitoringRepository : JpaRepository<Monitoring, Long>, JpaSpecificationExecutor<Monitoring> {

    fun findByScheduleAtIsLessThanAndEnabledAndStatusIsNot(
        scheduleAt: Timestamp, enabled: Boolean, status: StatusMonitoring
    ): List<Monitoring>?

    fun findByShowInPanelAndEnabled(
        inPanel: Boolean, active: Boolean
    ): List<Monitoring>?
    fun findByName(
        name: String
    ): Monitoring?

}