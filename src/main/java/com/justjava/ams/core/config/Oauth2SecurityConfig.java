package com.justjava.ams.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AnonymousConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Slf4j
@Configuration
public class Oauth2SecurityConfig {

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http, ClientRegistrationRepository repo) throws Exception {
        log.debug("Configuring OAuth2 security for AMS");

        http.securityMatcher("/**")
                .anonymous(AnonymousConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                .csrf(CsrfConfigurer::disable)
                .oauth2Login(oauth2 -> oauth2
                        // Remove .loginPage("/login") — let Spring handle it
                        .authorizationEndpoint(Customizer.withDefaults())
                        .tokenEndpoint(Customizer.withDefaults())
                        .userInfoEndpoint(Customizer.withDefaults())
                        .successHandler(authenticationSuccessHandler())
                )
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers("/", "/index").permitAll();  // public landing
                    authorize.requestMatchers("/api/**").permitAll();
                    authorize.requestMatchers("/static/**").permitAll();
                    authorize.requestMatchers("/images/**").permitAll();
                    authorize.requestMatchers("/favicon.ico").permitAll();
                    authorize.anyRequest().authenticated();
                })
                .logout(logout -> logout
                        .logoutSuccessHandler(oidcLogoutSuccessHandler(repo))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutUrl("/users/logout")
                );
        return http.build();
    }

    private LogoutSuccessHandler oidcLogoutSuccessHandler(ClientRegistrationRepository repository) {
        OidcClientInitiatedLogoutSuccessHandler logoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(repository);
        logoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
        return logoutSuccessHandler;
    }

    private AuthenticationSuccessHandler authenticationSuccessHandler(){
        return (request, response, authentication) -> {
            response.sendRedirect("/");
        };
    }
}