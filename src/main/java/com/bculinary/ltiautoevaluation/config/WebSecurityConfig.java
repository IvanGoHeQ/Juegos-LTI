package com.bculinary.ltiautoevaluation.config;

import edu.ksu.lti.launch.service.LtiLoginService;
import edu.ksu.lti.launch.service.SingleToolConsumerService;
import edu.ksu.lti.launch.service.ToolConsumerService;
import edu.ksu.lti.launch.spring.config.LtiConfigurer;
import edu.ksu.lti.launch.spring.config.LtiLaunchCsrfMatcher;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${lti-autoevaluation.instance:consumer}")
    private String instance;

    @Value("${lti-autoevaluation.name:LTI Gradebook}")
    private String name;

    @Value("${lti-autoevaluation.url:someurl}")
    private String url;

    @Value("${lti-autoevaluation.secret:secret}")
    private String secret;

    @Autowired
    private LtiLoginService ltiLoginService;

    @Autowired
    private ToolConsumerService toolConsumerService;

    @Override
    public void configure(WebSecurity webSecurity) throws Exception {
        super.configure(webSecurity);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.setSharedObject(LtiLoginService.class, ltiLoginService);
        LtiConfigurer<HttpSecurity> ltiConfigurer = new LtiConfigurer<>(toolConsumerService, "/launch", false, false, null);
        http.apply(ltiConfigurer);

        // We have to disable CSRF on the LTI launch.
        http.csrf().requireCsrfProtectionMatcher(new LtiLaunchCsrfMatcher("/launch"));

        // Setup spring security to require all requests to be authenticated.
        http.authorizeRequests()
            .antMatchers("/term.html")
            .permitAll()
            .anyRequest().authenticated()
            .and()
            .headers().frameOptions().disable().and().csrf().disable();
    }
    
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Override
	@Bean(name = "authenticationManager")
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
    

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth
			.inMemoryAuthentication()
			.withUser("admin").password("{noop}admin").authorities("USER", "ADMIN");
	}


    @Bean
    public ToolConsumerService toolConsumerService() {
        return new SingleToolConsumerService(instance, name, url, secret);
    }

    /**
     * This is just to disable the creation of the default user.
     */
    @Bean
    public UserDetailsManager userDetailsManager() {
        return new InMemoryUserDetailsManager();
    }

}
