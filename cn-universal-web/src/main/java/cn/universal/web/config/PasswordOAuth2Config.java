/*
 *
 * Copyright (c) 2025, iot-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 Aleo 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: Aleo
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.web.config;

import cn.universal.persistence.mapper.IoTUserMapper;
import cn.universal.web.auth.converter.PasswordAuthenticationConverter;
import cn.universal.web.auth.provider.PasswordAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.authentication.AuthenticationConverter;

/**
 * 密码模式 OAuth2 配置 独立配置，不影响原有的验证码模式
 */
@Configuration
public class PasswordOAuth2Config {

  @Bean
  public AuthenticationConverter passwordAuthenticationConverter(
      @Autowired(required = false) CustomClientRegistrationService clientRegistrationService) {
    if (clientRegistrationService == null) {
      return null; // 如果没有数据库配置，返回null
    }
    return new PasswordAuthenticationConverter(clientRegistrationService);
  }

  @Bean
  public PasswordAuthenticationProvider passwordAuthenticationProvider(
      @Autowired(required = false) CustomClientRegistrationService clientRegistrationService,
      @Autowired(required = false) IoTUserMapper iotUserMapper,
      @Autowired(required = false) OAuth2TokenGenerator<?> tokenGenerator,
      @Autowired(required = false) OAuth2AuthorizationService AuthorizationService) {
    if (clientRegistrationService == null
        || iotUserMapper == null
        || tokenGenerator == null
        || AuthorizationService == null) {
      return null; // 如果缺少依赖，返回null
    }
    return new PasswordAuthenticationProvider(
        tokenGenerator, clientRegistrationService,
        iotUserMapper, AuthorizationService);
  }
}
