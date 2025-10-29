package com.sulaimaan.ReminderApp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Provides static access to the Spring ApplicationContext for retrieving beans outside of Spring-managed components
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationContextProvider.class);

    private static ApplicationContext context;

    /**
     * Callback method invoked by Spring to set the ApplicationContext
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextProvider.context = applicationContext;
        logger.info("ApplicationContext has been set and is now available for static access");
    }

    /**
     * Retrieves a Spring-managed bean by its class type
     */
    public static <T> T getBean(Class<T> beanClass) {
        logger.debug("Retrieving bean of type: {}", beanClass.getSimpleName());
        T bean = context.getBean(beanClass);
        logger.debug("Successfully retrieved bean: {}", beanClass.getSimpleName());
        return bean;
    }
}
