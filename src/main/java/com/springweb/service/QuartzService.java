package com.springweb.service;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.parser.CronParser;
import com.springweb.model.QuartzRequest;
import com.springweb.quartz.RunCleanup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;

import java.util.Locale;

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

        // Parse cron expression to human-readable description
        String cronDescription = getCronDescription(quartzRequest.getCronExpression());

        JobDetail jobDetail = null;
        if(!scheduler.checkExists(jobKey)) {
             jobDetail = JobBuilder.newJob(RunCleanup.class)
                    .withIdentity(jobKey)
                    .withDescription("Job: " + quartzRequest.getJobName() + " - " + cronDescription)
                    .build();
        } else{
            jobDetail = scheduler.getJobDetail(jobKey);
        }

        Trigger trigger;
        if(!scheduler.checkExists(triggerKey)) {
            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withDescription("Trigger: " + cronDescription)
                    .withSchedule(CronScheduleBuilder.cronSchedule(quartzRequest.getCronExpression()))
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Created job: {} with cron: {}", jobKey, cronDescription);
        } else{
            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .forJob(jobDetail)
                    .withDescription("Trigger: " + cronDescription)
                    .withSchedule(CronScheduleBuilder
                            .cronSchedule(quartzRequest.getCronExpression()))
                    .build();
            scheduler.rescheduleJob(triggerKey, trigger);
            log.info("Updated job: {} with cron: {}", jobKey, cronDescription);
        }

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
    }
