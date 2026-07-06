package com.springweb.quartz;

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
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
//@Component
@RequiredArgsConstructor
public  class QuartzJobRunner implements ApplicationRunner {

    private final Scheduler scheduler;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        scheduleMyJob();
    }

    private void scheduleMyJob() throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("myJob", "default");
        TriggerKey triggerKey = TriggerKey.triggerKey("myJobTrigger", "default");

        // Always ensure the job exists (idempotent — safe across all cluster instances)
        JobDetail jobDetail = JobBuilder.newJob(MyJob.class)
                .withIdentity(jobKey)
                .withDescription("My Quartz Job — picked up by ANY available cluster instance")
                .storeDurably()
                .requestRecovery(true)   // re-fire on instance failure/restart
                .build();

        scheduler.addJob(jobDetail, true);
        log.info("[{}] Quartz job registered: {}", scheduler.getSchedulerInstanceId(), jobKey);

        // Only schedule the trigger if it doesn't exist yet —
        // In a cluster, all instances share the same trigger via PostgreSQL.
        // Quartz's locking ensures only ONE instance fires it at a time.
        if (!scheduler.checkExists(triggerKey)) {
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .forJob(jobKey)
                    .withDescription("Runs every 20 seconds")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0/20 * * * * ?"))
                    .build();
            scheduler.scheduleJob(trigger);
            log.info("[{}] Quartz trigger scheduled: {}", scheduler.getSchedulerInstanceId(), triggerKey);
        } else {
            log.info("[{}] Quartz trigger already exists, skipping registration", scheduler.getSchedulerInstanceId());
        }
    }
}
