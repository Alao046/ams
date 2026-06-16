package com.justjava.ams.core.keycloak;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.ws.rs.client.Client;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Slf4j
@Configuration
public class KeyclaokAdminConfig {

    @Value("${keycloak.base-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.base-realm}")
    private String baseRealm;

    @Value("${keycloak.admin-client-id}")
    private String clientId;

    @Value("${keycloak.client-id}")
    private String baseClientId;

    @Value("${keycloak.admin-client-secret}")
    private String clientSecret;

    @Value("${keycloak.client-secret}")
    private String baseClientSecret;


    @Provider
    public static class LenientObjectMapperResolver implements ContextResolver<ObjectMapper> {
        private final ObjectMapper mapper = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        @Override
        public ObjectMapper getContext(Class<?> type) {
            return mapper;
        }
    }

    private Client lenientResteasyClient() {
        return ResteasyClientBuilder.newBuilder()
                .register(new LenientObjectMapperResolver())
                .build();
    }

    @Bean
    @Qualifier("adminKeycloak")
    @Primary
    public Keycloak keycloakAdminClient() {
        log.info("Initializing adminKeycloak bean: serverUrl={}, realm={}, clientId={}", serverUrl, realm, clientId);
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .resteasyClient(lenientResteasyClient())
                .build();
    }

    @Bean("baseKeycloak")
    @Qualifier("baseKeycloak")
    public Keycloak keycloakBaseClient() {
        log.info("Initializing baseKeycloak bean: serverUrl={}, realm={}, clientId={}", serverUrl, baseRealm, baseClientId);
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(baseRealm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(baseClientId)
                .clientSecret(baseClientSecret)
                .resteasyClient(lenientResteasyClient())
                .build();
    }
}