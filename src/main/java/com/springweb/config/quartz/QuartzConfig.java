package com.springweb.config.quartz;

import com.springweb.quartz.MyJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.spi.JobFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

//@Configuration
public class QuartzConfig {

//
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource,
                                                     PlatformTransactionManager transactionManager,
                                                     JobFactory jobFactory) {
        SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
        scheduler.setDataSource(dataSource);
        scheduler.setTransactionManager(transactionManager);
        scheduler.setJobFactory(jobFactory);
        return scheduler;
    }

    @Bean
    public JobFactory jobFactory(ApplicationContext applicationContext) {
        var factory = new SpringBeanJobFactory();
        factory.setApplicationContext(applicationContext);
        return factory;
    }

}
