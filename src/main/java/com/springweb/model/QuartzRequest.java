package com.springweb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuartzRequest {

    private String jobName;
    private String jobGroup ="default";
    private String triggerName;
    private String triggerGroup = "default";
    private String cronExpression;
}
