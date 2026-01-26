package ita;

import ita.property.ResponseProperty;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableConfigurationProperties(ResponseProperty.class)
public class EmailApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmailApplication.class, args);
	}

}
