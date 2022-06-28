package com.example.demo.domain.response;

import lombok.Data;

@Data
public class GraphIdentityResponse {

  public String signInType;

  public String issuer;

  public String issuerAssignedId;

}
