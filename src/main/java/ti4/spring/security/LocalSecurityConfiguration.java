package ti4.spring.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

/**
 * LOCAL CUSTOMIZATION: Exempts /images/** from Spring Security's filter chain entirely,
 * allowing the web frontend to load bot classpath images without authentication.
 */
@Configuration
public class LocalSecurityConfiguration {

    @Bean
    public WebSecurityCustomizer localWebSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/images/**");
    }
}
