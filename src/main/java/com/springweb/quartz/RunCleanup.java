package com.springweb.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Slf4j
public class RunCleanup implements Job {

    @Override
    public void execute(JobExecutionContext context) {
        context.getJobDetail().getJobDataMap().forEach((k, v )-> System.out.println(k + " : " + v));
        log.info("RunCleanup started");
        try {
            Thread.sleep(Long.parseLong("1000"));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("RunCleanup over");
    }
}
