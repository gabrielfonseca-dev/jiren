package jiren.data.repository.twilio

import jiren.data.entity.twilio.Instance
import jiren.data.entity.twilio.Message
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository : JpaRepository<Message, Long> {
    @Query("select m from message m where m.instance = :instance order by m.id asc")
    fun findAllByInstance(instance: Instance): List<Message>
}