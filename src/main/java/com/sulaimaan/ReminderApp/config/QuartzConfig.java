package com.sulaimaan.ReminderApp.config;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * Configuration class for Quartz Scheduler with Spring integration
 */
@Configuration
public class QuartzConfig {

    private static final Logger logger = LoggerFactory.getLogger(QuartzConfig.class);

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Creates a custom JobFactory that supports Spring dependency injection in Quartz jobs
     */
    @Bean
    public JobFactory jobFactory() {
        logger.info("Creating custom JobFactory for Quartz with Spring autowiring support");
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        logger.info("JobFactory configured successfully");
        return jobFactory;
    }

    /**
     * Configures the SchedulerFactoryBean with custom JobFactory and startup settings
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory) {
        logger.info("Configuring SchedulerFactoryBean with custom JobFactory");
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setJobFactory(jobFactory);
        schedulerFactoryBean.setAutoStartup(true);
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);
        logger.info("SchedulerFactoryBean configured: autoStartup=true, waitForJobsToCompleteOnShutdown=true");
        return schedulerFactoryBean;
    }

    /**
     * Creates and starts the Quartz Scheduler instance
     */
    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) throws SchedulerException {
        logger.info("Initializing Quartz Scheduler");
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        scheduler.start();
        logger.info("Quartz Scheduler started successfully");
        return scheduler;
    }
}
