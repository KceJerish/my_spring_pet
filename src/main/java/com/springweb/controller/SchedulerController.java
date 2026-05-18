package com.springweb.controller;


import com.springweb.model.QuartzRequest;
import com.springweb.service.QuartzService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.web.bind.annotation.DeleteMapping;
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
}
