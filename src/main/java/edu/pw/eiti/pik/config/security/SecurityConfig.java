package edu.pw.eiti.pik.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String[] SWAGGER_ANT_PATTERNS = {
            "/v2/api-docs", "/configuration/ui", "/swagger-resources/**", "/configuration/**", "/swagger-ui.html", "/webjars/**",
    };

    private static final String[] PUBLIC_ANT_PATTERNS = {
    		"/",
            "/user/login", "/user/me",
            "/index.html", "/loginPanel.html", "/logoutPanel.html", "/navbar.html", "/myProjects.html",
            "/projectForm.html",
            "/js/loginController.js", "/js/appController.js", "/js/logoutController.js"
    };

    private final UserDetailsService userDetailsService;
    private final String secretKey;

    public SecurityConfig(UserDetailsService userServiceImpl, String secretKey) {
        this.userDetailsService = userServiceImpl;
        this.secretKey = secretKey;
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeRequests().antMatchers("/h2-console/**").permitAll().and()
                .authorizeRequests().antMatchers(PUBLIC_ANT_PATTERNS).permitAll().and()
                .authorizeRequests().antMatchers(SWAGGER_ANT_PATTERNS).permitAll().and()
                .authorizeRequests().anyRequest().authenticated()
                .and()
                .addFilter(new JwtAuthorizationFilter(authenticationManager(), secretKey))
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        httpSecurity
                .csrf().disable();
        httpSecurity
                .headers().frameOptions().disable();
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
    }
}
