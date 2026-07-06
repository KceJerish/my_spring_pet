package com.springweb.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class PaymentJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Payment Job Started");
        System.out.println("PaymentJob executed at " + LocalDateTime.now()+", name: "
                + context.getJobDetail().getKey().getName());
        context.getJobDetail().getJobDataMap()
                .forEach((key, value) -> log.info("Key: {}, Value: {}", key, value));
    }
}
