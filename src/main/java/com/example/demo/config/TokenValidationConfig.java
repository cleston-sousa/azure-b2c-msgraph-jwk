package com.example.demo.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.auth0.jwk.InvalidPublicKeyException;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

@Configuration
public class TokenValidationConfig {

  @Value("${url-well-known-policy}")
  public String urlWellKnownPolicy;

  @Value("${aadb2c-policyClaim}")
  public String aadb2cPolicyClaim;

  @Value("${issuerClaim}")
  public String issuerClaim;

  @Value("${jwks-urlClaim}")
  public String jwksUrlClaim;

  public Map<String, Claim> tokenValidation(String token) {

    if (token==null)
      throw new RuntimeException("Token invalido");

    try {
      // decode token
      DecodedJWT jwtDecode = JWT.decode(token);

      // get issuer openid url
      String urlWellKnown = jwtDecode.getClaim(issuerClaim).asString() +
          urlWellKnownPolicy +
          jwtDecode.getClaim(aadb2cPolicyClaim).asString();

      RestTemplate restTemplate = new RestTemplate();
      // access issuer openid url
      Map<String, String> resultMap = restTemplate.getForObject(urlWellKnown, Map.class);

      // get jwks url
      String jwksUrl = resultMap.get(jwksUrlClaim);

      // access jwks url
      JwkProvider jwkProvider = new UrlJwkProvider(new URL(jwksUrl));

      // find token key into jwk provider keys
      Jwk jwk = jwkProvider.get(jwtDecode.getKeyId());

      // extract public key
      Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);

      try {
        // create verifier
        JWTVerifier verifier = JWT.require(algorithm).build(); //Reusable verifier instance

        // apply verifier on token
        DecodedJWT jwtVerified = verifier.verify(token);

        return jwtVerified.getClaims();

      } catch (TokenExpiredException e) {
        throw new RuntimeException(e);
      } catch (SignatureVerificationException e) {
        throw new RuntimeException(e);
      }
    } catch (InvalidPublicKeyException e) {
      throw new RuntimeException(e);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    } catch (JwkException e) {
      throw new RuntimeException(e);
    }

  }


}
