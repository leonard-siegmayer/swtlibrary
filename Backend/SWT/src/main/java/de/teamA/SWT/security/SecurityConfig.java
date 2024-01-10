package de.teamA.SWT.security;

import de.teamA.SWT.service.AuthService;
import de.teamA.SWT.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String[] publicAvailEndpoints = { "/", "/api/configs/*", "/api/media/get/*",
            "/api/media/search", "/api/media/getAll", "/api/media/isAvailable", "/api/borrow/book/*" };

    @Value("${frontend.url}")
    public String frontendUrl;

    @Value("${auth.endpoint}")
    public String loginEndpoint;

    @Autowired
    AuthService authService;

    @Autowired
    UserService userService;

    public GitlabLoginFilter gitlabFilter() {
        final GitlabLoginFilter filter = new GitlabLoginFilter(frontendUrl, loginEndpoint);
        filter.setAuthService(authService);
        filter.setUserService(userService);
        return filter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().cors().and().addFilterAt(gitlabFilter(), BasicAuthenticationFilter.class)
                .authorizeRequests().antMatchers(publicAvailEndpoints).permitAll().anyRequest().authenticated().and()
                .sessionManagement().maximumSessions(-1).expiredSessionStrategy(expiredStrategy())
                .sessionRegistry(sessionRegistry()).and().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
    }

    @Bean
    SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    SessionInformationExpiredStrategy expiredStrategy() {

        return new SessionInformationExpiredStrategy() {
            @Override
            public void onExpiredSessionDetected(SessionInformationExpiredEvent event)
                    throws IOException, ServletException {
                HttpServletResponse res = event.getResponse();
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setHeader("SessionExpired", "true");
                res.flushBuffer();
            }
        };
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration defaultCorsConfig = new CorsConfiguration();
        defaultCorsConfig.applyPermitDefaultValues();
        defaultCorsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        defaultCorsConfig.setAllowedOrigins(Arrays.asList(frontendUrl));
        defaultCorsConfig.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", defaultCorsConfig);
        return source;
    }
}
