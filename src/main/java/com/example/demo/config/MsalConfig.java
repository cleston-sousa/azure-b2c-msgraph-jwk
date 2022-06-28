package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;

public class MsalConfig {

  @Value("${msgraph-scope}")
  public String msgraphScope;
  @Value("${msgraph-authority}")
  public String msgraphAuthority;

}
