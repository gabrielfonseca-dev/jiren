package jiren.data.repository.twilio

import jiren.data.entity.twilio.Instance
import jiren.data.entity.twilio.Ticket
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TicketRepository : JpaRepository<Ticket, Long> {
    fun findByInstance(instance: Instance): Optional<Ticket>
}