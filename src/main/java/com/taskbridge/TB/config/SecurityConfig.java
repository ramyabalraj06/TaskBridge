package com.taskbridge.TB.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(PathRequest.toH2Console()).permitAll()
						.requestMatchers("/audit/**", "/notifications/**").authenticated()
						.requestMatchers(HttpMethod.GET, "/**").permitAll()
						.requestMatchers(HttpMethod.HEAD, "/**").permitAll()
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.anyRequest().authenticated())
				.cors(withDefaults())
				.csrf(csrf -> csrf.ignoringRequestMatchers(PathRequest.toH2Console()))
				.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
				.formLogin(withDefaults())
				.logout(withDefaults());
		return http.build();
	}
}