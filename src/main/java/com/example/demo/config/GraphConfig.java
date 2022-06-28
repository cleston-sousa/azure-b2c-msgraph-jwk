package com.example.demo.config;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.google.gson.JsonPrimitive;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.logger.LoggerLevel;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.serializer.AdditionalDataManager;

@Configuration
public class GraphConfig {

  @Value("${client-id}")
  public String clientId;

  @Value("${client-secret}")
  public String clientSecret;

  @Value("${tenant-guid}")
  public String tenantGuid;

  @Value("${b2c-extension-app-object-id}")
  public String b2cExtensionAppObjectId;

  @Value("${b2c-extension-app-client-id}")
  public String b2cExtensionAppClientId;

  @Value("${custom-b2c-attributes}")
  public String[] customB2CAttributes;

  @Value("${common-b2c-attributes}")
  public String[] commonB2CAttributes;

  public String attributeExtensionPrefix;

  HashMap<String, String> mapExtensionAttributes;

  public void setAdditionalDataManagerValues(User userGraph, Object source) {
    Map<String, String> map = getMapExtensionAttributes();
    AdditionalDataManager dataManager = userGraph.additionalDataManager();
    map.forEach((attName, extAttName) -> {
      try {
        Object value = PropertyUtils.getProperty(source, attName);
        if (value == null) {
          return;
        } else if (value instanceof Number) {
          dataManager.put(extAttName, new JsonPrimitive((Number) value));
        } else if (value instanceof Boolean) {
          dataManager.put(extAttName, new JsonPrimitive((Boolean) value));
        } else if (value instanceof String) {
          dataManager.put(extAttName, new JsonPrimitive((String) value));
        } else if (value instanceof Character) {
          dataManager.put(extAttName, new JsonPrimitive((Character) value));
        }
      } catch (Exception e) {
      }
    });
  }

  public <T> T getFromAdditionalDataManager(User userGraph, Class<T> classType) {
    try {
      Map<String, String> map = getMapExtensionAttributes();
      AdditionalDataManager dataManager = userGraph.additionalDataManager();
      T destination = classType.newInstance();
      map.forEach((attName, extAttName) -> {
        try {
          Object value = dataManager.get(extAttName);
          if (value == null) {
            return;
          } else if (value instanceof JsonPrimitive) {
            JsonPrimitive valueJson = (JsonPrimitive) value;
            Class<?> propertyType = PropertyUtils.getPropertyType(destination, attName);
            if (propertyType == Integer.class) {
              PropertyUtils.setProperty(destination, attName, valueJson.getAsInt());
            } else if (propertyType == Long.class) {
              PropertyUtils.setProperty(destination, attName, valueJson.getAsLong());
            } else if (propertyType == Float.class) {
              PropertyUtils.setProperty(destination, attName, valueJson.getAsFloat());
            } else if (propertyType == Short.class) {
              PropertyUtils.setProperty(destination, attName, valueJson.getAsShort());
            } else if (propertyType == BigDecimal.class) {
              PropertyUtils.setProperty(destination, attName, valueJson.getAsBigDecimal());
            } else if (propertyType == BigInteger.class) {
              PropertyUtils.setProperty(destination, attName, valueJson.getAsBigInteger());
            } else if (propertyType == String.class) {
              PropertyUtils.setProperty(destination, attName, valueJson.getAsString());
            } else if (propertyType == Boolean.class) {
              PropertyUtils.setProperty(destination, attName, valueJson.getAsBoolean());
            }
          }
        } catch (Exception e) {
        }
      });
      return destination;
    } catch (Exception e) {
      return null;
    }
  }

  public Map<String, String> getMapExtensionAttributes() {
    if (mapExtensionAttributes == null) {
      final HashMap<String, String> result = new HashMap<>();
      Arrays.stream(customB2CAttributes).forEach(attName -> {
        result.put(attName, getExtensionPrefix() + attName);
      });
      mapExtensionAttributes = result;
      return result;
    }
    return mapExtensionAttributes;
  }

  public String getExtensionPrefix() {
    if (attributeExtensionPrefix == null) {
      attributeExtensionPrefix = "extension_" + b2cExtensionAppClientId.replace("-", "") + "_";
    }
    return attributeExtensionPrefix;
  }

  public List<String> getCustomB2CAttributesList() {
    return Arrays.stream(customB2CAttributes)
        .map(item -> getExtensionPrefix() + item).collect(Collectors.toList());
  }

  public String getCustomB2CAttributes() {
    return getCustomB2CAttributesList().stream().map(item -> item).collect(Collectors.joining(","));
  }

  public String getCommonB2CAttributes() {
    return Arrays.stream(commonB2CAttributes).map(item -> item).collect(Collectors.joining(","));
  }

  public String getAllAttributes() {
    return getCommonB2CAttributes() + "," + getCustomB2CAttributes();
  }

  @Bean
  public GraphServiceClient graphClient() {
    ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
        .clientId(clientId)
        .clientSecret(clientSecret)
        .tenantId(tenantGuid)
        .build();

    TokenCredentialAuthProvider tokenCredAuthProvider =
        new TokenCredentialAuthProvider(clientSecretCredential);

    GraphServiceClient graphClient = GraphServiceClient
        .builder()
        .authenticationProvider(tokenCredAuthProvider)
        .buildClient();

    graphClient.getLogger().setLoggingLevel(LoggerLevel.DEBUG);

    return graphClient;
  }

}
