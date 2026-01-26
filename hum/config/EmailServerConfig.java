package ita.config;

import jakarta.mail.Session;
import jakarta.mail.Transport;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Data
@Configuration
@Component
@ConfigurationProperties(prefix = "smtp")
public class EmailServerConfig {

    private boolean auth;
    private String host;
    private String port;


    @Bean("smtpSession")
    public Session session() {
        Properties properties = new Properties();

        properties.put("mail.smtp.auth", this.auth);
        properties.put("mail.smtp.host", this.host);
        properties.put("mail.smtp.port", this.port);
        properties.put("mail.smtp.connectiontimeout", 30000);
        properties.put("mail.smtp.timeout", 30000);

        return Session.getDefaultInstance(properties);
    }

}
