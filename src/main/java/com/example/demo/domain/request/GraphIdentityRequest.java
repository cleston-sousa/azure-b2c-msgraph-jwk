package com.example.demo.domain.request;

import lombok.Data;

@Data
public class GraphIdentityRequest {

  public String signInType;

  public String issuer;

  public String issuerAssignedId;

}
