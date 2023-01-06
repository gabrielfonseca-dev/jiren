package jiren.controller.twilio

import jiren.controller.UserController
import jiren.data.entity.twilio.Instance
import jiren.data.entity.twilio.Message
import jiren.data.repository.twilio.MessageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MessageController {

    @Autowired
    private lateinit var messageRepository: MessageRepository

    @Autowired
    lateinit var userController: UserController

    fun listMessages(instance: Instance?): List<Message> {
        return if(instance != null) {
            messageRepository.findAllByInstance(instance)
        } else{
            listOf()
        }
    }

    fun save(message: Message) {
        messageRepository.save(message)
    }

}