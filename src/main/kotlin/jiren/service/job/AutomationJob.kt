package jiren.service.job

import jiren.controller.AutomationController
import jiren.controller.LogController
import jiren.data.entity.Automation
import jiren.data.enum.LogCode
import jiren.data.enum.LogType
import jiren.data.enum.Parameters
import jiren.data.enum.StatusAutomation
import jiren.data.repository.ParameterRepository
import jiren.service.util.SpringMail
import org.quartz.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import javax.annotation.PostConstruct

@Component
class AutomationJob(
    private val automationController: AutomationController,
    private val parameterRepository: ParameterRepository,
    private val logController: LogController,
    private val mailer: SpringMail,
) : Job {
    private var repeatInterval: Long = 60000
    private var threadCount: Int = 1
    private var enabled: Boolean = false
    private var mailTo: String = ""

    @Override
    override fun execute(context: JobExecutionContext) {
        try {
            this.setParameters()
            val currentJobs = context.scheduler.currentlyExecutingJobs.toMutableList()
            currentJobs.removeIf { job -> job.jobDetail.key.name != "automation" }
            if (currentJobs.size > threadCount) return
            if (enabled) {
                //val startTime = Instant.now().toEpochMilli()
                executeAutomations()
                val trigger = TriggerBuilder
                    .newTrigger()
                    .withSchedule(
                        SimpleScheduleBuilder
                            .simpleSchedule()
                            .withIntervalInMilliseconds(repeatInterval)
                            .repeatForever()
                    )
                    .startAt(Date.from(Instant.now().plusMillis(repeatInterval)))
                    .withIdentity("automationSchedule", "automationGroup")
                context.scheduler.rescheduleJob(context.trigger.key, trigger.build())
                //logController.saveSystemLog(LogCode.JOB_INFO, LogType.EXECUTE, Instant.now().toEpochMilli() - startTime, "SUCCESS", "AUTOMATION")
            }
        } catch (e: Exception) {
            logController.saveSystemLog(LogCode.JOB_ERROR, LogType.EXECUTE, null, e.stackTraceToString(), "AUTOMATION")
        }
    }

    @PostConstruct
    private fun setParameters() {
        try {
            mailTo =
                parameterRepository.findByCode(Parameters.JOB_AUTOMATION_FAILURE_EMAIL.toString())?.value.toString()
            enabled = parameterRepository.findByCode(Parameters.JOB_AUTOMATION_STATUS.toString())?.value.let {
                if (it.isNullOrEmpty()) false
                else it.toBoolean()
            }
            threadCount = parameterRepository.findByCode(Parameters.JOB_MONITORING_THREAD_COUNT.toString())?.value.let { value ->
                if (value.isNullOrEmpty()) { 1 } else value.toInt()
            }
            repeatInterval = parameterRepository.findByCode(Parameters.JOB_AUTOMATION_INTERVAL.toString())?.value.let {
                if (it.isNullOrEmpty()) "300000".toLong()
                else it.toLong()
            }
        } catch (e: Exception) {
            logController.saveSystemLog(LogCode.JOB_ERROR, LogType.EXECUTE, null, e.stackTraceToString(), "AUTOMATION")
        }
    }

    @Bean("automationSchedule")
    fun automationSchedule(): JobDetail? {
        return JobBuilder
            .newJob(AutomationJob::class.java)
            .withIdentity("automation", "automationGroup")
            .storeDurably(true)
            .withDescription("Agenda de execução de automações")
            .build()
    }

    @Bean
    fun automationTrigger(@Qualifier("automationSchedule") automationSchedule: JobDetail?): SimpleTriggerFactoryBean? {
        val trigger = SimpleTriggerFactoryBean()
        trigger.setJobDetail(automationSchedule!!)
        trigger.setRepeatInterval(repeatInterval)
        trigger.setGroup("automationGroup")
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY)
        return trigger
    }

    private fun executeAutomations() {
        val automationList: List<Automation>? = automationController.findAutomationToRun()
        if (automationList.isNullOrEmpty()) return
        automationList.forEach { it.status = StatusAutomation.RUNNING }
        automationController.saveAll(automationList.toList())
        automationList.forEach { automation ->
            try {
                automationController.execute(automation)
            } catch (e: Exception) {
                sendMail(automation, e.message)
                logController.saveSystemLog(LogCode.JOB_ERROR, LogType.EXECUTE, null, e.stackTraceToString(), "AUTOMATION")
            }
        }
    }

    private fun sendMail(obj: Automation, error: String?) {
        val msg = StringBuilder()
        msg.appendLine("Automation ${obj.name}")
        msg.appendLine("Confluence ${obj.documentUrl}")
        msg.appendLine("Error -> $error")
        mailer.send(mailTo, "AUTOMATION ${obj.name} HAS FAILED", msg.toString(), null)
    }

}

