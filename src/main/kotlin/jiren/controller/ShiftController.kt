package jiren.controller

import jiren.data.entity.Shift
import jiren.data.entity.User
import jiren.data.repository.ShiftRepository
import jiren.data.repository.UserRepository
import org.springframework.stereotype.Controller
import java.sql.Timestamp
import java.time.LocalDateTime

@Controller
class ShiftController(private var userRepository: UserRepository, private var shiftRepository: ShiftRepository) {

    fun findDutyOfficer(): List<User>? {
        return userRepository.findShiftOfficers()
    }

    fun search(start: LocalDateTime, end: LocalDateTime): List<Shift>? {
        return shiftRepository.findByStartDateBetween(Timestamp.valueOf(start), Timestamp.valueOf(end))
    }

    fun save(shift: Shift): Shift {
        return shiftRepository.save(shift)
    }

    fun delete(shift: Shift) {
        shiftRepository.delete(shift)
    }

}