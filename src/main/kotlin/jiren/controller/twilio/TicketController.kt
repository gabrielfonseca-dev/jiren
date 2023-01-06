package jiren.controller.twilio

import jiren.data.entity.twilio.Ticket
import jiren.data.repository.twilio.TicketRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class TicketController {

    @Autowired
    private lateinit var ticketRepository: TicketRepository

    fun save(ticket: Ticket) {
        ticketRepository.save(ticket)
    }

}