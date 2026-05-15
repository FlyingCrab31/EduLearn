package com.edulearn.auth.security;

import com.edulearn.auth.service.AuthService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            // Disable Spring Security's CORS - the API Gateway handles all CORS
            .cors(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Allow all OPTIONS preflight requests
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/v1/auth/**", "/login/**", "/oauth2/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler((request, response, authentication) -> {
                    org.springframework.security.oauth2.core.user.OAuth2User oauth2User =
                        (org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal();
                    
                    String email = oauth2User.getAttribute("email");
                    if (email == null) email = oauth2User.getAttribute("unique_name");
                    if (email == null) email = oauth2User.getAttribute("sub") + "@oauth.edulearn.com";
                    
                    String name = oauth2User.getAttribute("name");
                    if (name == null) name = oauth2User.getAttribute("given_name");
                    if (name == null) name = email.split("@")[0];

                    AuthService service = org.springframework.web.context.support.WebApplicationContextUtils
                        .getRequiredWebApplicationContext(request.getServletContext())
                        .getBean(AuthService.class);

                    String jwt = service.processOAuthPostLogin(email, name);
                    com.edulearn.auth.entity.User user = service.getUserByEmail(email);

                    // Redirect back to our dedicated callback page
                    String redirectUrl = String.format("http://localhost:4200/auth/callback?token=%s&name=%s&email=%s&userId=%s&role=%s",
                        jwt, java.net.URLEncoder.encode(name, "UTF-8"), email, user.getId(), user.getRole());
                    
                    response.sendRedirect(redirectUrl);
                })
            );

        return http.build();
    }

}
