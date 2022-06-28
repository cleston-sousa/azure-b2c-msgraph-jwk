package com.example.demo.domain.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.example.demo.domain.request.GraphIdentityRequest;
import com.example.demo.domain.request.GraphUserRequest;
import com.example.demo.domain.response.GraphUserResponse;
import com.microsoft.graph.models.ObjectIdentity;
import com.microsoft.graph.models.User;

@Mapper(componentModel = "spring")
public interface UserGraphMapper {

  User userRequestToUserGraph(GraphUserRequest userRequest);

  GraphUserResponse userGraphToUserResponse(User userGraph);

  GraphIdentityRequest objectIdentityToGraphIdentityRequest(ObjectIdentity objectIdentity);

  ObjectIdentity graphIdentityRequestToObjectIdentity(GraphIdentityRequest graphIdentityRequest);

}
