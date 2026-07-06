package com.springweb.controller;


import com.springweb.model.PaymentRequest;
import com.springweb.model.QuartzRequest;
import com.springweb.service.QuartzService;
import com.springweb.util.QuartzDatabaseCleaner;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quartz")
@RequiredArgsConstructor
public class SchedulerController {

    private final QuartzService quartzService;
    private final QuartzDatabaseCleaner databaseCleaner;

    @PostMapping("/create")
    public Boolean createJob(@RequestBody QuartzRequest quartzRequest){
        try{
            this.quartzService.createScheduler(quartzRequest);
            return true;
        } catch (SchedulerException e){
            return false;
        }
    }

    @DeleteMapping("/jobkey/{jobKey}/jobGroup/{jobGroup}")
    public boolean deleteJob(@PathVariable String jobKey,@PathVariable String jobGroup) throws SchedulerException {
        return quartzService.deleteJob(jobKey, jobGroup);
    }

    @PostMapping("/schedule/payment")
    public boolean schedulePayment(@RequestBody PaymentRequest paymentRequest){
        try{
            this.quartzService.schedulePayment(paymentRequest);
            return true;
        } catch (SchedulerException e){
            e.printStackTrace();
            return false;
        }
    }
    
    @GetMapping("/diagnose")
    public String diagnoseTriggers() {
        databaseCleaner.diagnoseTriggers();
        return "Check logs for diagnosis results";
    }
    
    @PostMapping("/cleanup/payment")
    public String cleanupPaymentTriggers() {
        try {
            databaseCleaner.cleanupPaymentTriggers();
            return "Payment triggers cleaned up successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @PostMapping("/cleanup/all")
    public String cleanupAllTriggers() {
        try {
            databaseCleaner.cleanupAllTriggers();
            return "All triggers cleaned up successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
