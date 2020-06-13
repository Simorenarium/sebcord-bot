package coffee.michel.sebcord;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.security.core.context.SecurityContextHolder;

import coffee.michel.spring.adapter.VaadinSecurityContextHolderStrategy;

@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
public class Application {

	public static void main(String[] args) {
		System.setProperty(SecurityContextHolder.SYSTEM_PROPERTY,
				VaadinSecurityContextHolderStrategy.class.getCanonicalName());

		SpringApplication.run(Application.class, args);
	}

}
