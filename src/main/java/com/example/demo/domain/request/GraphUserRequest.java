package com.example.demo.domain.request;

import java.util.List;

import com.example.demo.domain.GraphCustomAttributes;

import lombok.Data;

@Data
public class GraphUserRequest {

  public String displayName;

  public String givenName;

  public String surname;

  public String mailNickname;

  public String userPrincipalName;

  public String password;

  public List<String> otherMails;

  public String mail;

  public GraphCustomAttributes customAtt;

}
