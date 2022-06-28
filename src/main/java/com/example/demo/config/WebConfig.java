package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@PropertySource("classpath:application.properties")
@PropertySource("file:/test/poc/application.properties")
public class WebConfig extends WebSecurityConfigurerAdapter {

  // @Autowired
  JwtTesterFilter jwtTesterFilter;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
//        .addFilterBefore(jwtTesterFilter, BasicAuthenticationFilter.class)
        .csrf().disable();
  }


}
