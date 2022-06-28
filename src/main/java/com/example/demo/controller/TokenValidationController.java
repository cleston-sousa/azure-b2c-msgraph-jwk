package com.example.demo.controller;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.example.demo.config.TokenValidationConfig;

@RestController
@RequestMapping("/b2c")
public class TokenValidationController {

  @Autowired
  TokenValidationConfig tokenValidationConfig;

  @PostMapping(path = "/token")
  public ResponseEntity postUser(@RequestBody String token) throws Exception {

    try {

      tokenValidationConfig.tokenValidation(token);

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
    }

    return ResponseEntity.status(HttpStatus.OK).build();
  }

}
