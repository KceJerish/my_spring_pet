package com.springweb.service;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.parser.CronParser;
import com.springweb.model.PaymentRequest;
import com.springweb.model.QuartzRequest;
import com.springweb.quartz.PaymentJob;
import com.springweb.quartz.RunCleanup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.JobDetailImpl;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import static com.cronutils.model.definition.CronDefinitionBuilder.instanceDefinitionFor;
import static com.cronutils.model.CronType.QUARTZ;

@Slf4j
@RequiredArgsConstructor
@Service
public class QuartzService {

    private final Scheduler scheduler;

    public void createScheduler(QuartzRequest quartzRequest) throws SchedulerException {
        JobKey jobKey = new JobKey(quartzRequest.getJobName(), quartzRequest.getJobGroup());
        TriggerKey triggerKey = TriggerKey.triggerKey(quartzRequest.getTriggerName(), quartzRequest.getTriggerGroup());

        String scheduleDescription;
        boolean isRunOnce = quartzRequest.getRunOnce() != null && quartzRequest.getRunOnce();
        
        if (isRunOnce) {
            Date executeAt = quartzRequest.getExecuteAt() != null ? quartzRequest.getExecuteAt() : new Date();
            scheduleDescription = "Run once at " + executeAt;
        } else {
            scheduleDescription = getCronDescription(quartzRequest.getCronExpression());
        }

        JobDetail jobDetail;
        if(!scheduler.checkExists(jobKey)) {
             jobDetail = JobBuilder.newJob(RunCleanup.class)
                    .withIdentity(jobKey)
                    .withDescription("Job: " + quartzRequest.getJobName() + " - " + scheduleDescription)
                    .build();
        } else{
            jobDetail = scheduler.getJobDetail(jobKey);
        }

        Trigger trigger;
        if(!scheduler.checkExists(triggerKey)) {
            trigger = buildTrigger(triggerKey, jobDetail, quartzRequest, scheduleDescription, isRunOnce);
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Created job: {} with schedule: {}", jobKey, scheduleDescription);
        } else{
            trigger = buildTrigger(triggerKey, jobDetail, quartzRequest, scheduleDescription, isRunOnce);
            scheduler.rescheduleJob(triggerKey, trigger);
            log.info("Updated job: {} with schedule: {}", jobKey, scheduleDescription);
        }

    }

    private Trigger buildTrigger(TriggerKey triggerKey, JobDetail jobDetail, QuartzRequest quartzRequest, 
                                  String description, boolean isRunOnce) {
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withDescription("Trigger: " + description);
        
        if (jobDetail != null) {
            triggerBuilder.forJob(jobDetail);
        }
        
        if (isRunOnce) {
            // Run once at specified time
            Date executeAt = quartzRequest.getExecuteAt() != null ? quartzRequest.getExecuteAt() : new Date();
            triggerBuilder.startAt(executeAt)
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withMisfireHandlingInstructionFireNow());
        } else {
            // Use cron expression for recurring schedule
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(quartzRequest.getCronExpression()));
        }
        
        return triggerBuilder.build();
    }

    public boolean deleteJob(String JobKey, String jobGroup) throws SchedulerException {
       return scheduler.deleteJob(new JobKey(JobKey, jobGroup));
    }

    private String getCronDescription(String cronExpression) {
        try {
            CronParser parser = new CronParser(instanceDefinitionFor(QUARTZ));
            Cron cron = parser.parse(cronExpression);
            CronDescriptor descriptor = CronDescriptor.instance(Locale.US);
            return descriptor.describe(cron);
        } catch (Exception e) {
            log.warn("Failed to parse cron expression: {}", cronExpression, e);
            return cronExpression;
        }
    }

    public void schedulePayment(PaymentRequest paymentRequest) throws SchedulerException {
        JobKey jobKey = new JobKey(paymentRequest.user() + "_" + System.currentTimeMillis(), "paymentGroup");
        TriggerKey triggerKey = new TriggerKey(paymentRequest.user() + "_" + System.currentTimeMillis(), "paymentGroup");
        
        // Convert Instant to Date for proper Quartz serialization
        Date executeAt = Date.from(Instant.now().plusSeconds(paymentRequest.inSeconds()));
        
        JobDetail jobDetail = JobBuilder.newJob(PaymentJob.class)
                .withIdentity(jobKey)
                .withDescription("Scheduling payment for " + paymentRequest.user())
                .usingJobData("user", paymentRequest.user())
                .usingJobData("amount", paymentRequest.amount())
                .build();
                
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withDescription("Trigger for scheduling payment for " + paymentRequest.user())
                .startAt(executeAt)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();
                
        scheduler.scheduleJob(jobDetail, Set.of(trigger), true);
        log.info("Scheduled payment job for user: {} at: {}", paymentRequest.user(), executeAt);
    }
}
