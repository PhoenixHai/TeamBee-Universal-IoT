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

package cn.ctaiot.protocol.config;

import cn.universal.core.protocol.ProtocolModuleRuntimeRegistry;
import com.ctg.ag.sdk.biz.AepDeviceCommandClient;
import com.ctg.ag.sdk.biz.AepDeviceCommandLwmProfileClient;
import com.ctg.ag.sdk.biz.AepDeviceManagementClient;
import com.ctg.ag.sdk.biz.AepProductManagementClient;
import com.ctg.ag.sdk.biz.AepPublicProductManagementClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * CT-AIoT 自动配置类
 *
 * <p>当 ctaiot.protocol.enable=true 时，自动扫描包并创建所有CT-AIoT相关的Bean
 *
 * <p>通过 META-INF/spring.factories 实现 Spring Boot 自动配置
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/2
 */
@Configuration
@ConditionalOnProperty(
    name = "ctaiot.protocol.enable",
    havingValue = "true",
    matchIfMissing = false)
@ComponentScan(basePackages = "cn.ctaiot.protocol")
@EnableConfigurationProperties(CTAIoTProperties.class)
@Slf4j
public class CTAIoTAutoConfiguration {

  private final CTAIoTProperties properties;

  @Autowired(required = false)
  private CTAIoTModuleInfo moduleInfo;

  public CTAIoTAutoConfiguration(CTAIoTProperties properties) {
    this.properties = properties;
    log.info(
        "[CT-AIoT自动配置] 模块已启用，配置: server={}, appid={}",
        properties.getServer(),
        properties.getAppid());
  }

  @PostConstruct
  public void registerProtocol() {
    if (moduleInfo != null) {
      ProtocolModuleRuntimeRegistry.registerProtocol(moduleInfo);
      log.info("[CT-AIoT自动配置] 协议模块已注册到运行时注册表");
    }
  }

  @PreDestroy
  public void unregisterProtocol() {
    if (moduleInfo != null) {
      ProtocolModuleRuntimeRegistry.unregisterProtocol(moduleInfo.getCode());
      log.info("[CT-AIoT自动配置] 协议模块已从运行时注册表注销");
    }
  }

  // ======================== AEP客户端Bean ========================

  @Bean
  @Lazy
  public AepDeviceManagementClient aepDeviceManagementClient() {
    return AepDeviceManagementClient.newClient()
        .appKey(properties.getAppkey())
        .appSecret(properties.getAppsecret())
        .build();
  }

  @Bean
  @Lazy
  public AepProductManagementClient aepProductManagementClient() {
    return AepProductManagementClient.newClient()
        .appKey(properties.getAppkey())
        .appSecret(properties.getAppsecret())
        .build();
  }

  @Bean
  @Lazy
  public AepPublicProductManagementClient aepPublicProductManagementClient() {
    return AepPublicProductManagementClient.newClient()
        .appKey(properties.getAppkey())
        .appSecret(properties.getAppsecret())
        .build();
  }

  @Bean
  @Lazy
  public AepDeviceCommandClient aepDeviceCommandClient() {
    return AepDeviceCommandClient.newClient()
        .appKey(properties.getAppkey())
        .appSecret(properties.getAppsecret())
        .build();
  }

  @Bean
  public AepDeviceCommandLwmProfileClient aepDeviceCommandLwmProfileClient() {
    return AepDeviceCommandLwmProfileClient.newClient()
        .appKey(properties.getAppkey())
        .appSecret(properties.getAppsecret())
        .build();
  }

  // ======================== 配置属性Bean ========================
  // 注意：服务类、处理器类、控制器类通过 @ComponentScan 自动扫描创建
  // 这里只需要创建不能通过注解创建的Bean（如第三方库的客户端和配置对象）

  @Bean
  public CTAIoTConfig ctaiotConfig() {
    CTAIoTConfig config = new CTAIoTConfig();
    config.setAppid(properties.getAppid());
    config.setAppKey(properties.getAppkey());
    config.setAppSecret(properties.getAppsecret());
    config.setServer(properties.getServer());
    config.setTenantId(properties.getTenantId());
    config.setToken(properties.getToken());
    config.setEnable(properties.isEnable());
    return config;
  }
}
