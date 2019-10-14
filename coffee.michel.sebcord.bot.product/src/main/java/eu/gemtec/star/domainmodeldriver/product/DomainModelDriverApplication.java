/*
 * Copyright GEMTEC GmbH 2019
 *
 * Erstellt am: 8 Oct 2019 17:10:12
 * Erstellt von: Jonas Michel
 */
package eu.gemtec.star.domainmodeldriver.product;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

/**
 * @author Jonas Michel
 *
 */
//@formatter:off
@OpenAPIDefinition(
    info = @Info(
        title = "Easywave Driver", 
        version = "1.2.0", 
        description = "Easywave Driver is a service which centralizes the communication to all Easywave Communicators."
    ),
    security = {
        @SecurityRequirement(name = "basicAuth")
    },
        servers = {
            @Server(
                description = "Localhost",
                url = "http://localhost:8089/services"
            ),
            @Server(
                description = "Localhost - Secure",
                url = "https://localhost:8089/services"
            ),
            @Server(
                description = "Ent-Win",
                url = "http://ent-win-1:8089/services"
            ),
            @Server(
                description = "Ent-Win - Secure",
                url = "https://ent-win-1:8089/services"
            )
        }
    )
@SecurityScheme(
	securitySchemeName = "basicAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "basic"
)
@ApplicationPath("/")
public class DomainModelDriverApplication extends Application {}
