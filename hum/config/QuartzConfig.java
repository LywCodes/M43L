//package ita.config;
//
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.quartz.SchedulerFactoryBean;
//
//@Configuration
//public class QuartzConfig {
//
//    private final ApplicationContext applicationContext;
//
//    public QuartzConfig(ApplicationContext applicationContext) {
//        this.applicationContext = applicationContext;
//    }
//
//    @Bean
//    public SchedulerFactoryBean schedulerFactoryBean() {
//        // Buat Job Factory yang "sadar" akan Spring
//        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
//        jobFactory.setApplicationContext(applicationContext);
//
//        // Buat Scheduler Factory
//        SchedulerFactoryBean factory = new SchedulerFactoryBean();
//        factory.setJobFactory(jobFactory);
//        factory.setOverwriteExistingJobs(true); // Ganti job yang sudah ada jika ada perubahan
//
//        factory.setAutoStartup(true);
//
//        return factory;
//    }
//}


package ita.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class QuartzConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private QuartzProperties quartzProperties;

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        JobFactoryConfig jobFactory = new JobFactoryConfig();

        jobFactory.setApplicationContext(applicationContext);

        SchedulerFactoryBean factory = new SchedulerFactoryBean();

        factory.setDataSource(dataSource);
        factory.setQuartzProperties(getQuartzProperties());
        factory.setJobFactory(jobFactory);
        factory.setOverwriteExistingJobs(true);
        factory.setAutoStartup(true);

        return factory;
    }

    private Properties getQuartzProperties() {
        Properties properties = new Properties();

        properties.putAll(quartzProperties.getProperties());

        return properties;
    }
}