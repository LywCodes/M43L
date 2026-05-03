package ita.config;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.quartz.autoconfigure.QuartzProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;
//old
//@Configuration
//public class QuartzConfig {
//
//    @Autowired
//    private ApplicationContext applicationContext;
//
//    @Autowired
//    private DataSource dataSource;
//
//    @Autowired
//    private QuartzProperties quartzProperties;
//
//    @Bean
//    public SchedulerFactoryBean schedulerFactoryBean() {
//        JobFactoryConfig jobFactory = new JobFactoryConfig();
//
//        jobFactory.setApplicationContext(applicationContext);
//
//        SchedulerFactoryBean factory = new SchedulerFactoryBean();
//
//        factory.setDataSource(dataSource);
//        factory.setQuartzProperties(getQuartzProperties());
//        factory.setJobFactory(jobFactory);
//        factory.setOverwriteExistingJobs(true);
//        factory.setAutoStartup(true);
//
//        return factory;
//    }
//
//    private Properties getQuartzProperties() {
//        Properties properties = new Properties();
//
//        properties.putAll(quartzProperties.getProperties());
//
//        return properties;
//    }
//}

//V1 WORK
@Configuration
@RequiredArgsConstructor
public class QuartzConfig {

    private final DataSource dataSource;
    private final QuartzProperties quartzProperties;
    private final AutowireCapableBeanFactory beanFactory;

    @Bean
    public AutowiringSpringBeanJobFactory quartzJobFactory() {
        return new AutowiringSpringBeanJobFactory(beanFactory);
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(AutowiringSpringBeanJobFactory jobFactory) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();

        factory.setDataSource(dataSource);
        factory.setJobFactory(jobFactory);
        factory.setQuartzProperties(quartzProperties());

        factory.setOverwriteExistingJobs(true);
        factory.setWaitForJobsToCompleteOnShutdown(true);
        factory.setAutoStartup(true); //new

        return factory;
    }

    private Properties quartzProperties() {
        Properties properties = new Properties();
        properties.putAll(quartzProperties.getProperties());
        return properties;
    }
}

//v2
//@Configuration
//@RequiredArgsConstructor
//public class QuartzConfig {
//
//    private final ApplicationContext applicationContext;
//    private final DataSource dataSource;
//    private final QuartzProperties quartzProperties;
//
//    @Bean
//    public AutowiringSpringBeanJobFactory quartzJobFactory() {
//        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
//        jobFactory.setApplicationContext(applicationContext);
//        return jobFactory;
//    }
//
//    @Bean
//    public SchedulerFactoryBean schedulerFactoryBean(AutowiringSpringBeanJobFactory quartzJobFactory) {
//        SchedulerFactoryBean factory = new SchedulerFactoryBean();
//        factory.setDataSource(dataSource);
//        factory.setQuartzProperties(quartzProperties());
//        factory.setJobFactory(quartzJobFactory);
//        factory.setOverwriteExistingJobs(true);
//        factory.setAutoStartup(true);
//        return factory;
//    }
//
//    @Bean
//    public Properties quartzProperties() {
//        Properties properties = new Properties();
//        properties.putAll(quartzProperties.getProperties());
//        return properties;
//    }
//}