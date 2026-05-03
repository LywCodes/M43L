package ita.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
//V1 WORK

@Slf4j
@RequiredArgsConstructor
public class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory {

    private final AutowireCapableBeanFactory beanFactory;

    @Override
    @NonNull
    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
        Class<?> jobClass = bundle.getJobDetail().getJobClass();

        try {
            return beanFactory.createBean(jobClass);
        } catch (Exception e) {
            log.error("Failed to create Quartz job instance: {}", jobClass.getName(), e);
            throw e;
        }
    }
}
//v2 setter inject
//@Slf4j
//public class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {
//
//    private AutowireCapableBeanFactory beanFactory;
//
//    @Override
//    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
//        this.beanFactory = applicationContext.getAutowireCapableBeanFactory();
//    }
//
//    @Override
//    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
//        Class<?> jobClass = bundle.getJobDetail().getJobClass();
//
//        try {
//            return beanFactory.createBean(jobClass);
//        } catch (Exception e) {
//            log.error("Failed to create Quartz job instance for class: {}", jobClass.getName(), e);
//            throw e;
//        }
//    }
//}
