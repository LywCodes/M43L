package ita.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Configuration
public class ValidationConfig {

    @Autowired
    private Environment environment;

    private static final String MESSAGE_PREFIX = "messages";

    @Bean
    public MessageSource messageSource() {
        StaticMessageSource messageSource = new StaticMessageSource();
        Map<String, String> defaultMessages = new HashMap<>();

        ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;

        for (PropertySource<?> ps: configurableEnvironment.getPropertySources()) {
            if (ps instanceof EnumerablePropertySource) {
                EnumerablePropertySource<?> eps = (EnumerablePropertySource<?>) ps;
                for (String key : eps.getPropertyNames()) {
                    if (key.startsWith(MESSAGE_PREFIX)) {
                        String value = environment.getProperty(key);

                        String cleanedKey = key.substring(MESSAGE_PREFIX.length() + 1);

                        defaultMessages.put(cleanedKey, value);
                    }
                }
            }
        }

        messageSource.addMessages(defaultMessages, Locale.getDefault());

        return messageSource;
    }

    @Bean
    public LocalValidatorFactoryBean getValidator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();

        bean.setValidationMessageSource(messageSource());

        return bean;
    }

}
