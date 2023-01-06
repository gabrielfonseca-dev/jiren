package jiren.controller.twilio

import jiren.data.entity.twilio.Instance
import jiren.data.entity.twilio.Ticket
import jiren.data.repository.twilio.InstanceRepository
import jiren.data.repository.twilio.TicketRepository
import org.springframework.stereotype.Component

@Component
class InstanceController(val instanceRepository: InstanceRepository, val ticketRepository: TicketRepository) {

    fun getInstance(from: String, to: String? = null, profileName: String? = null): Instance {
        val existingInstance = instanceRepository.findOpenInstance(from)
        return if (existingInstance.isPresent) {
            existingInstance.get()
        } else {
            val newInstance = Instance()
            newInstance.contact = from
            newInstance.contactName = profileName
            newInstance.twilioNumber = to
            instanceRepository.save(newInstance)
            newInstance
        }
    }

    fun getTicket(instance: Instance): Ticket? {
        val ticket = ticketRepository.findByInstance(instance)
        return if (ticket.isPresent) ticket.get()
        else null
    }

    fun close(instance: Instance) {
       instance.isOpen = false
       instanceRepository.save(instance)
    }

}