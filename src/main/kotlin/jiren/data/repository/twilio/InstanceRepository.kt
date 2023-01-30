package jiren.data.repository.twilio

import jiren.data.entity.twilio.Instance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface InstanceRepository : JpaRepository<Instance, Long> {
    @Query("select i from instance i where i.contact = :from and i.isOpen = true")
    fun findOpenInstance(from: String): Optional<Instance>

    @Query("select i from instance i join i.user u where u.id = :userId and i.isOpen = true")
    fun findOpenInstanceByUser(userId: Long): List<Instance>

    @Query("select i from instance i where i.isOpen = true and i.chatAwaitStart is not null and i.chatAwaitEnd is null")
    fun findOpenInstanceForChat(): List<Instance>

    @Query("select i from instance i where i.isOpen = true")
    fun findAllOpen(): List<Instance>
}