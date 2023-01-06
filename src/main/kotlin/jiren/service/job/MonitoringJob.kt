package jiren.service.job

import jiren.controller.LogController
import jiren.controller.MonitoringController
import jiren.data.entity.Monitoring
import jiren.data.enum.*
import jiren.data.repository.ParameterRepository
import jiren.service.apis.jira.JiraClient
import jiren.service.apis.rocketchat.RocketChatClient
import jiren.service.util.SpringMail
import org.quartz.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean
import org.springframework.stereotype.Component
import java.io.File
import java.time.Instant
import java.time.Instant.now
import java.util.*
import javax.annotation.PostConstruct

@Component
class MonitoringJob(
    private val monitoringController: MonitoringController,
    private val parameterRepository: ParameterRepository,
    private val logController: LogController,
    private val springMail: SpringMail,
    private val rocketChatClient: RocketChatClient,
    private val jiraClient: JiraClient
) : Job {

    private var repeatInterval: Long = 1000
    private var threadCount: Int = 1
    private var enabled: Boolean = false

    override fun execute(context: JobExecutionContext) {
        try {
            this.setParameters()
            val currentJobs = context.scheduler.currentlyExecutingJobs.toMutableList()
            currentJobs.removeIf { job -> job.jobDetail.key.name != "monitoring" }
            if(currentJobs.size > threadCount) return
            if (enabled) {
                //val startTime = now().toEpochMilli()
                executeMonitorings()
                val trigger = TriggerBuilder
                    .newTrigger()
                    .withSchedule(
                        SimpleScheduleBuilder
                            .simpleSchedule()
                            .withIntervalInMilliseconds(repeatInterval)
                            .repeatForever()
                    )
                    .startAt(Date.from(Instant.now().plusMillis(repeatInterval)))
                    .withIdentity("monitoringSchedule","monitoringGroup")
                context.scheduler.rescheduleJob(context.trigger.key, trigger.build())
                //logController.saveSystemLog(LogCode.JOB_INFO, LogType.EXECUTE, now().toEpochMilli() - startTime, "SUCCESS", "MONITORING")
            }
        } catch (e: Exception) {
            logController.saveSystemLog(LogCode.JOB_ERROR, LogType.EXECUTE, null, e.stackTraceToString(), "MONITORING")
        }
    }

    @PostConstruct
    private fun setParameters() {
        try {
            repeatInterval = parameterRepository.findByCode(Parameters.JOB_MONITORING_INTERVAL.toString())?.value.let { value ->
                if (value.isNullOrEmpty()) "300000".toLong()
                else value.toLong()
            }
            threadCount = parameterRepository.findByCode(Parameters.JOB_MONITORING_THREAD_COUNT.toString())?.value.let { value ->
                if (value.isNullOrEmpty()) { 1 } else value.toInt()
            }
            enabled = parameterRepository.findByCode(Parameters.JOB_MONITORING_STATUS.toString())?.value.let {
                if (it.isNullOrEmpty()) false
                else it.toBoolean()
            }
        } catch (e: Exception) {
            logController.saveSystemLog(LogCode.JOB_ERROR, LogType.EXECUTE, null, e.stackTraceToString(), "MONITORING")
        }
    }

    @Bean("monitoringSchedule")
    fun monitoringSchedule(): JobDetail? {
        return JobBuilder
            .newJob(MonitoringJob::class.java)
            .withIdentity("monitoring", "monitoringGroup")
            .storeDurably(true)
            .withDescription("Agenda de execução de alertas").build()
    }

    @Bean
    fun monitoringTrigger(@Qualifier("monitoringSchedule") monitoringSchedule: JobDetail?): SimpleTriggerFactoryBean? {
        val trigger = SimpleTriggerFactoryBean()
        trigger.setJobDetail(monitoringSchedule!!)
        trigger.setRepeatInterval(repeatInterval)
        trigger.setGroup("monitoringGroup")
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY)
        return trigger
    }

    private fun executeMonitorings() {

        val monitoringList: List<Monitoring>? = monitoringController.findMonitoringToRun()
        if (monitoringList.isNullOrEmpty()) return

        monitoringList.forEach { it.status = StatusMonitoring.RUNNING }
        monitoringController.saveAll(monitoringList.toList())

        monitoringList.forEach { monitoring ->
            val result = monitoringController.execute(monitoring)
            val fileExtension = when (monitoring.type) {
                MonitoringType.DATABASE -> ".csv"
                else -> ".txt"
            }
            val resultFile = File.createTempFile("${monitoring.name}-${now().toEpochMilli()}",fileExtension)
            if (!resultFile.exists()) resultFile.createNewFile()
            resultFile.writer().append(result).flush()
            resultFile.deleteOnExit()

            val sendNotifications =
                (monitoring.status == StatusMonitoring.NOK || monitoring.lastStatus == StatusMonitoring.NOK)

            if (sendNotifications) {
                if (monitoring.emailNotification) {
                    springMail.send(
                        mailTo = "${monitoring.mailTo}",
                        subject = "${monitoring.name} is ${monitoring.status}",
                        mailMessage = "${monitoring.description}${if (monitoring.jiraIssue.isNullOrEmpty()) "" else "\nTicket: ${monitoring.jiraIssue}"}",
                        attachment = resultFile
                    )
                }
                if (monitoring.jiraNotification) {
                    if (jiraClient.hasOpenIssue("${monitoring.jiraIssue}")) {
                        jiraClient.addComment("${monitoring.name} is ${monitoring.status}", monitoring.jiraIssue!!)
                        if (resultFile.readText(Charsets.UTF_8).isNotEmpty()) jiraClient.addAttachment(
                            resultFile,
                            monitoring.jiraIssue!!
                        )
                    } else {
                        monitoring.jiraIssue = jiraClient.createIssue(
                            title = "Monitoramento: ${monitoring.name}",
                            description = "${monitoring.description}\nDocumento -> ${monitoring.documentURL}\n${monitoring.queryOne}",
                            attachment = resultFile,
                            labels = monitoring.jiraLabels?.split(",")
                        ).toString()
                        monitoringController.save(monitoring)
                    }
                }
                if (monitoring.rocketNotification) {
                    rocketChatClient.sendMessage(monitoring, (if (monitoring.rocketchatAttachment) result else "") as String)
                }
            }

        }

    }

}
