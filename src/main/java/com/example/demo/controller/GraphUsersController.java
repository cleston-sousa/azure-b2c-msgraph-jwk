package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.config.GraphConfig;
import com.example.demo.domain.GraphCustomAttributes;
import com.example.demo.domain.mapper.UserGraphMapper;
import com.example.demo.domain.request.GraphUserRequest;
import com.example.demo.domain.response.GraphUserResponse;
import com.microsoft.graph.models.PasswordProfile;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.ExtensionPropertyCollectionPage;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.UserCollectionPage;

@RestController
@RequestMapping("/b2c")
public class GraphUsersController {

  @Autowired
  public GraphServiceClient graphClient;

  @Autowired
  public GraphConfig graphConfig;

  @Autowired
  UserGraphMapper userGraphMapper;

  @PostMapping(path = "/users")
  public ResponseEntity postUser(@RequestBody GraphUserRequest userRequest) {

    // instance of graph model user
    User userGraph = userGraphMapper.userRequestToUserGraph(userRequest);
    graphConfig.setAdditionalDataManagerValues(userGraph, userRequest.getCustomAtt());

    userGraph.accountEnabled = true;
    PasswordProfile passwordProfile = new PasswordProfile();
    passwordProfile.forceChangePasswordNextSignIn = false;
    passwordProfile.password = userRequest.getPassword();
    userGraph.passwordProfile = passwordProfile;

    try {
      // send data to AADB2C
      graphClient.users().buildRequest().post(userGraph);

      // response created
      return ResponseEntity.status(HttpStatus.CREATED).build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }

  @GetMapping(path = "/users")
  public ResponseEntity getUsers() {
    UserCollectionPage usersB2CList = graphClient.users().buildRequest()
        .select(graphConfig.getAllAttributes())
        .get();

    // response AAD B2C list of users
    List<GraphUserResponse> listUserResponse = usersB2CList.getCurrentPage().stream()
        .map(userB2C -> {

          // response AAD B2C user
          GraphUserResponse userResponse = userGraphMapper.userGraphToUserResponse(userB2C);
          GraphCustomAttributes graphCustomAttributes = graphConfig
              .getFromAdditionalDataManager(userB2C, GraphCustomAttributes.class);
          userResponse.setCustomAtt(graphCustomAttributes);

          return userResponse;
        }).collect(Collectors.toList());

    return ResponseEntity.ok(listUserResponse);
  }

  @GetMapping(path = "/users/{objectId}")
  public ResponseEntity getUser(@PathVariable("objectId") String objectId) {

    // get user from AAD B2C
    User userB2C = graphClient.users(objectId).buildRequest()
        .select(graphConfig.getAllAttributes())
        .get();

    // response AAD B2C user
    GraphUserResponse userResponse = userGraphMapper.userGraphToUserResponse(userB2C);
    GraphCustomAttributes graphCustomAttributes = graphConfig
        .getFromAdditionalDataManager(userB2C, GraphCustomAttributes.class);
    userResponse.setCustomAtt(graphCustomAttributes);

    return ResponseEntity.ok(userResponse);
  }

  @PatchMapping(path = "/users/{objectId}")
  public ResponseEntity patchUser(@PathVariable("objectId") String objectId,
      @RequestBody GraphUserRequest userRequest) {
    try {
      // instance of graph model user
      User userGraph = userGraphMapper.userRequestToUserGraph(userRequest);
      graphConfig.setAdditionalDataManagerValues(userGraph, userRequest.getCustomAtt());

      // send data to AADB2C
      User result = graphClient.users(objectId).buildRequest().patch(userGraph);
      // response no content
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping(path = "/users/{objectId}")
  public ResponseEntity deleteUser(@PathVariable("objectId") String objectId) {
    try {
      // send data to AADB2C
      graphClient.users(objectId).buildRequest().delete();
      // response no content
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping(path = "/users/custom-attributes", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity listAttributes() {
    ExtensionPropertyCollectionPage extensionProperties = graphClient
        .applications(graphConfig.b2cExtensionAppObjectId).extensionProperties()
        .buildRequest()
        .get();
    return ResponseEntity.status(HttpStatus.OK).body(extensionProperties);
  }

}
