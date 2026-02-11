package com.financetracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HttpSessionIdResolver;

/**
 * Spring Session Configuration with Redis backend
 * Manages distributed user sessions stored in Redis
 * Supports session tracking and concurrent session control
 */
@Configuration
public class SessionConfig {

    /**
     * Configure HTTP session ID resolution using cookies
     */
    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return new CookieHttpSessionIdResolver();
    }

    /**
     * Configure session cookie properties
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("JSESSIONID");
        serializer.setCookiePath("/api");
        serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
        serializer.setUseHttpOnlyCookie(true);
        serializer.setUseBase64Encoding(false);
        serializer.setSameSite("Strict");
        return serializer;
    }

    /**
     * Session Registry for managing and tracking active sessions
     * Allows querying principal sessions and session information
     */
    @Bean
    public SessionRegistry sessionRegistry(FindByIndexNameSessionRepository sessionRepository) {
        return new SpringSessionBackedSessionRegistry(sessionRepository);
    }
}
