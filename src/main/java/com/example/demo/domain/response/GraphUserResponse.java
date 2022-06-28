package com.example.demo.domain.response;

import java.util.List;

import com.example.demo.domain.GraphCustomAttributes;

import lombok.Data;

@Data
public class GraphUserResponse {

  public String id;

  public String displayName;

  public String accountEnabled;

  public String givenName;

  public String surname;

  public String mailNickname;

  public String userPrincipalName;

  public GraphCustomAttributes customAtt;

  public List<GraphIdentityResponse> identities;

  public String mail;

  public List<String> otherMails;
}
