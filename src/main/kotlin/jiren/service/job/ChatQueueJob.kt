package jiren.service.job

import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber
import jiren.controller.LogController
import jiren.controller.UserController
import jiren.controller.twilio.InstanceController
import jiren.data.enum.*
import jiren.security.credentials.CredentialsService
import org.quartz.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean
import org.springframework.stereotype.Component
import java.time.Instant.now
import java.util.*

@Component
@DisallowConcurrentExecution
class ChatQueueJob(
    private val logController: LogController,
    private val instanceController: InstanceController,
    private val userController: UserController,
    private val credentialsService: CredentialsService
) : Job {

    private var repeatInterval: Long = 5000
    private var threadCount: Int = 1
    private var enabled: Boolean = true

    override fun execute(context: JobExecutionContext) {
        try {
            val currentJobs = context.scheduler.currentlyExecutingJobs.toMutableList()
            currentJobs.removeIf { job -> job.jobDetail.key.name != "monitoring" }
            if (currentJobs.size > threadCount) return
            if (enabled) {
                assign()
                val trigger = TriggerBuilder
                    .newTrigger()
                    .withSchedule(
                        SimpleScheduleBuilder
                            .simpleSchedule()
                            .withIntervalInMilliseconds(repeatInterval)
                            .repeatForever()
                    )
                    .startAt(Date.from(now().plusMillis(repeatInterval)))
                    .withIdentity("chatSchedule", "chatGroup")
                context.scheduler.rescheduleJob(context.trigger.key, trigger.build())
            }
        } catch (e: Exception) {
            logController.saveSystemLog(LogCode.JOB_ERROR, LogType.EXECUTE, null, e.stackTraceToString(), "CHAT")
        }
    }

    fun sendMessage(from: String, to: String, msg: String) {
        Twilio.init(
            credentialsService.twilioConfig.getString(credentialsService.twilioClient),
            credentialsService.twilioConfig.getString(credentialsService.twilioSecret)
        )

        Message.creator(
            PhoneNumber(to),
            PhoneNumber(from),
            msg)
            .create()
    }

    fun assign() {
        val instances = instanceController.getPendingChats()
        instances.forEach { instance ->
            val operatorPhone = userController.getAvailableUser(instance)?.split("::")
            if (!operatorPhone.isNullOrEmpty() && operatorPhone.contains("SHIFT")) {
                sendMessage(
                    instance.contact!!,
                    operatorPhone[0],
                    "Ola, por favor entre em contato com ${instance.contactName} atraves do numero ${instance.contact}"
                )
            }
        }
    }

        @Bean("chatSchedule")
        fun chatSchedule(): JobDetail? {
            return JobBuilder
                .newJob(ChatQueueJob::class.java)
                .withIdentity("chat", "chatGroup")
                .storeDurably(true)
                .withDescription("Agenda de atribuição de chat").build()
        }

        @Bean
        fun chatTrigger(@Qualifier("chatSchedule") chatSchedule: JobDetail?): SimpleTriggerFactoryBean? {
            val trigger = SimpleTriggerFactoryBean()
            trigger.setJobDetail(chatSchedule!!)
            trigger.setRepeatInterval(repeatInterval)
            trigger.setGroup("chatGroup")
            trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY)
            return trigger
        }

}