package coffee.michel.sebcord;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.security.core.context.SecurityContextHolder;

import coffee.michel.spring.adapter.VaadinSecurityContextHolderStrategy;

@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
public class Application {

	public static String predefToken;
	public static String predefUrl;
	public static String predefClientID;
	public static String predefClientSecret;
	public static String predefGuildID;

	public static void main(String[] args) {
		for (String string : args) {
			if (string.startsWith("sebcord.token"))
				predefToken = string.split("=")[1];
			if (string.startsWith("sebcord.id"))
				predefClientID = string.split("=")[1];
			if (string.startsWith("sebcord.guildId"))
				predefGuildID = string.split("=")[1];
			if (string.startsWith("sebcord.url"))
				predefUrl = string.split("=")[1];
			if (string.startsWith("sebcord.secret"))
				predefClientSecret = string.split("=")[1];
		}
		
		System.setProperty(SecurityContextHolder.SYSTEM_PROPERTY,
				VaadinSecurityContextHolderStrategy.class.getCanonicalName());

		SpringApplication.run(Application.class, args);
	}

}
