package jiren.data.repository

import jiren.data.entity.Shift
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.sql.Timestamp

@Repository
interface ShiftRepository : JpaRepository<Shift, Long> {
    @Query("select s from shifts s where s.start between :startDate and :endDate")
    fun findByStartDateBetween(startDate: Timestamp?, endDate: Timestamp?): List<Shift>?
}

