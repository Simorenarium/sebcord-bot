package coffee.michel.sebcord.ui;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.dv8tion.jda.api.Permission;

@Retention(RUNTIME)
@Target(TYPE)
public @interface Permissions {

	Permission[] value();

}
