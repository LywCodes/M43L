package ita.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Configuration
@Slf4j
public class ValidationConfig {

    private static final String MESSAGE_PREFIX = "messages";

    @Bean
    public MessageSource messageSource(Environment environment) {
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

        Locale locale = LocaleContextHolder.getLocale();

        messageSource.addMessages(defaultMessages, locale);

        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        Locale locale = LocaleContextHolder.getLocale();

        SessionLocaleResolver localeResolver = new SessionLocaleResolver();

        localeResolver.setDefaultLocale(locale);

        return localeResolver;
    }

    @Bean
    public LocalValidatorFactoryBean getValidator(Environment environment) {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();

        bean.setValidationMessageSource(messageSource(environment));

        return bean;
    }

}
