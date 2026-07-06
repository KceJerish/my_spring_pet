package com.springweb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuartzRequest {

    private String jobName;
    private String jobGroup ="default";
    private String triggerName;
    private String triggerGroup = "default";
    private String cronExpression;
    private Date executeAt; // For one-time execution at specific time
    private Boolean runOnce = false; // Flag to indicate if job should run only once
}
