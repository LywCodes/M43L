//package ita.job;
//
//import lombok.extern.slf4j.Slf4j;
//import org.quartz.spi.TriggerFiredBundle;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;
//import org.springframework.scheduling.quartz.SpringBeanJobFactory;
//import org.springframework.stereotype.Component;
//
//@Component
//@Slf4j
//public class SpringJobFactory extends SpringBeanJobFactory {
//
//    @Autowired
//    private ApplicationContext applicationContext;
//
//    @Override
//    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
//        Object job = super.createJobInstance(bundle);
//
//        try{
//            applicationContext.getAutowireCapableBeanFactory().autowireBean(job);
//        }catch(Exception e){
//            log.error("failed to autowire quartz job {}", e.getMessage(),e);
//            throw e;
//        }
//
//        //applicationContext.getAutowireCapableBeanFactory().autowireBean(job);
//
//        return job;
//    }
//}
