package com.example.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.SessionLimit;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession;
import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisIndexedWebSession;
import org.springframework.session.security.SpringSessionBackedReactiveSessionRegistry;
import redis.embedded.RedisServer;

import java.io.IOException;

import static org.springframework.security.config.Customizer.withDefaults;

@SpringBootApplication
@EnableRedisIndexedWebSession(redisNamespace = "spring:session:example")
@EnableSpringWebSession
@EnableWebFluxSecurity
public class SessionRedisReactiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(SessionRedisReactiveApplication.class, args);
    }


    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(authorizeRequests -> authorizeRequests
                        .anyExchange()
                        .authenticated()
                )
                .sessionManagement(sessions -> sessions
                        .concurrentSessions(concurrency -> concurrency
                                .maximumSessions(SessionLimit.of(1))
                        )
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(withDefaults())
                .logout(withDefaults());

        return http.build();
    }

    @Bean
    public SpringSessionBackedReactiveSessionRegistry<ReactiveRedisIndexedSessionRepository.RedisSession> sessionRegistry(ReactiveSessionRepository<ReactiveRedisIndexedSessionRepository.RedisSession> sessionRepository, ReactiveRedisIndexedSessionRepository indexedSessionRepository) {
        return new SpringSessionBackedReactiveSessionRegistry<>(sessionRepository, indexedSessionRepository);
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() throws IOException {
        RedisServer redisServer = new RedisServer(16379);
        redisServer.start();
        return new LettuceConnectionFactory("127.0.0.1", 16379);
    }
}
