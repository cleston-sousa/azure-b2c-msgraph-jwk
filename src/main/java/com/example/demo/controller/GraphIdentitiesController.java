package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.interfaces.Claim;
import com.example.demo.config.GraphConfig;
import com.example.demo.config.TokenValidationConfig;
import com.example.demo.domain.GraphCustomAttributes;
import com.example.demo.domain.mapper.UserGraphMapper;
import com.example.demo.domain.request.GraphIdentityRequest;
import com.google.gson.JsonPrimitive;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.models.ObjectIdentity;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;

@RestController
@RequestMapping("/b2c")
public class GraphIdentitiesController {

  @Autowired
  public GraphServiceClient graphClient;

  @Autowired
  public GraphConfig graphConfig;

  @Autowired
  public TokenValidationConfig tokenValidationConfig;

  @Autowired
  UserGraphMapper userGraphMapper;


  @PostMapping(path = "/identities/{objectId}")
  public ResponseEntity postIdentity(@PathVariable("objectId") String objectId,
      @RequestBody GraphIdentityRequest identityRequest) {
    try {
      // get user from AAD B2C
      User userB2C = graphClient.users(objectId).buildRequest()
          .select(graphConfig.getAllAttributes())
          .get();

      // instance Graph Object Identity
      ObjectIdentity objectIdentity = userGraphMapper
          .graphIdentityRequestToObjectIdentity(identityRequest);

      // filter identity from list
      List<ObjectIdentity> identitiesList = userB2C.identities;

      // add new Object Identity
      identitiesList.add(objectIdentity);

      // instance of graph model user
      User userGraph = new User();
      userGraph.identities = identitiesList;

      // send data to AADB2C
      User result = graphClient.users(objectId).buildRequest().patch(userGraph);

      return ResponseEntity.noContent().build();
    } catch (GraphServiceException e) {
      return ResponseEntity.status(e.getResponseCode()).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @DeleteMapping(path = "/identities/{objectId}")
  public ResponseEntity deleteIdentity(@PathVariable("objectId") String objectId,
      @RequestBody GraphIdentityRequest identityRequest) {
    try {
      // get user from AADB2C
      User userB2C = graphClient.users(objectId).buildRequest()
          .select(graphConfig.getAllAttributes())
          .get();

      // filter identity from list
      List<ObjectIdentity> filteredIdentitiesList = userB2C.identities.stream()
          .filter(identityB2C -> {
            boolean keepIdentity = true;

            GraphIdentityRequest identityRequestB2C = userGraphMapper
                .objectIdentityToGraphIdentityRequest(identityB2C);

            if (identityRequest.equals(identityRequestB2C)) {
              keepIdentity = false;
            }

            return keepIdentity;
          }).collect(Collectors.toList());

      // instance of graph model user
      User userGraph = new User();
      userGraph.identities = filteredIdentitiesList;

      // send data to AADB2C
      User result = graphClient.users(objectId).buildRequest().patch(userGraph);

      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/stepup")
  public ResponseEntity stepup(@RequestHeader(value = "authorization") String bearerToken) {

    String token = bearerToken != null ? bearerToken.substring(7) : "";

    Map<String, Claim> claimsMap = tokenValidationConfig
        .tokenValidation(token);

    Claim claimSub = claimsMap.get("sub");

    String objectId = claimSub.asString();

    try {

      // get user from AADB2C
      User userB2C = graphClient.users(objectId).buildRequest()
          .select(graphConfig.getAllAttributes())
          .get();

      GraphCustomAttributes graphCustomAttributes = graphConfig
          .getFromAdditionalDataManager(userB2C, GraphCustomAttributes.class);

      if (!StringUtils.isBlank(graphCustomAttributes.getAttLinked())) {
        return ResponseEntity.badRequest().build();
      }

      // instance of graph model user
      User userGraph = new User();
      userGraph.additionalDataManager().put(graphConfig.getExtensionPrefix() + "attLinked",
          new JsonPrimitive("Esse usuario foi linkado"));

      // send data to AADB2C
      User result = graphClient.users(objectId).buildRequest().patch(userGraph);

      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }
  }

}
