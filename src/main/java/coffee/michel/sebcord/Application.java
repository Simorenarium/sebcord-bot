package coffee.michel.sebcord;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import coffee.michel.sebcord.bot.core.JDADCClient;
import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;

@SpringBootApplication
public class Application {

	public static JDADCClient						client;
	public static ConfigurationPersistenceManager	cpm;

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
		client = ctx.getBean(JDADCClient.class);
		cpm = ctx.getBean(ConfigurationPersistenceManager.class);
	}

}
