package com.example.demo.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

// @Component
public class JwtTesterFilter implements Filter {

  @Autowired
  TokenValidationConfig tokenValidationConfig;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRep = (HttpServletRequest) request;
    HttpServletResponse httpRes = (HttpServletResponse) response;
    String token = httpRep.getHeader("x-b2c-token");

    tokenValidationConfig.tokenValidation(token);

    chain.doFilter(request, response);

  }
}
