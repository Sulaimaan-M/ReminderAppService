package com.sulaimaan.ReminderApp.config;

import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * Custom JobFactory that enables Spring dependency injection in Quartz job instances
 */
public final class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(AutowiringSpringBeanJobFactory.class);

    private transient AutowireCapableBeanFactory beanFactory;

    /**
     * Sets the ApplicationContext and initializes the AutowireCapableBeanFactory
     */
    @Override
    public void setApplicationContext(final ApplicationContext context) {
        beanFactory = context.getAutowireCapableBeanFactory();
        logger.info("AutowireCapableBeanFactory has been configured for Quartz job dependency injection");
    }

    /**
     * Creates a Quartz job instance and autowires its dependencies using Spring
     */
    @Override
    protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
        logger.debug("Creating Quartz job instance for: {}", bundle.getJobDetail().getKey());
        final Object job = super.createJobInstance(bundle);
        beanFactory.autowireBean(job);
        logger.info("Quartz job instance created and autowired: {}", job.getClass().getSimpleName());
        return job;
    }
}
