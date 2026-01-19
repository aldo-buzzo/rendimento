package com.example.rendimento.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configurazione di Spring Security per l'autenticazione e l'autorizzazione.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    /**
     * Configura il filtro di sicurezza HTTP.
     *
     * @param http il builder di configurazione HTTP
     * @return la catena di filtri di sicurezza configurata
     * @throws Exception se si verifica un errore durante la configurazione
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configurazione SecurityFilterChain");
        http
            .csrf(csrf -> {
                csrf.disable(); // Disabilita CSRF per semplificare le richieste API
                log.info("CSRF disabilitato");
            })
            .authorizeHttpRequests(authorize -> {
                authorize
                    // Consenti accesso pubblico alle API di autenticazione, pagine di login/registrazione e risorse statiche
                    .requestMatchers("/api/auth/**", "/login", "/registrazione", "/css/**", "/js/**", "/images/**").permitAll()
                    // Assicurati che login.html e registrazione.html siano accessibili senza autenticazione
                    .requestMatchers("/login.html", "/registrazione.html").permitAll()
                    // Endpoint di test per diagnosticare problemi di autenticazione
                    .requestMatchers("/api/auth/test-admin", "/api/auth/create-test-admin").permitAll()
                    // Tutte le altre richieste richiedono autenticazione
                    .anyRequest().authenticated();
                log.info("Configurazione autorizzazioni completata");
            })
            .formLogin(form -> {
                form
                    .loginPage("/login")
                    .loginProcessingUrl("/api/auth/login")
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .defaultSuccessUrl("/", true)
                    .failureUrl("/login?error=true")
                    .permitAll();
                log.info("Configurazione form login completata: loginPage=/login, loginProcessingUrl=/api/auth/login, defaultSuccessUrl=/");
            })
            .logout(logout -> {
                logout
                    .logoutUrl("/api/auth/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .permitAll();
                log.info("Configurazione logout completata: logoutUrl=/api/auth/logout, logoutSuccessUrl=/login?logout=true");
            });
        
        log.info("SecurityFilterChain configurato con successo");
        return http.build();
    }
    
    /**
     * Configura l'encoder per le password.
     *
     * @return l'encoder per le password
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("Creazione bean PasswordEncoder (BCryptPasswordEncoder)");
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Configura l'autenticazione.
     *
     * @param auth il builder di configurazione dell'autenticazione
     * @throws Exception se si verifica un errore durante la configurazione
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth, @Lazy PasswordEncoder passwordEncoder) throws Exception {
        log.info("Configurazione AuthenticationManagerBuilder");
        auth
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder);
        log.info("AuthenticationManagerBuilder configurato con UserDetailsService e PasswordEncoder");
    }
}
